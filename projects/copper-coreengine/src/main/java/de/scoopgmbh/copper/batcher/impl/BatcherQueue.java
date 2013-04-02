/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.batcher.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutorBase;


class BatcherQueue {

	enum State {
		STARTED, STOPPING, STOPPED
	};

	static class BatchInfo {

		@SuppressWarnings({ "rawtypes" })
		static final Comparator<BatchCommand> comparator = new Comparator<BatchCommand>() {
			public int compare(BatchCommand o1, BatchCommand o2) {
				if (o1.targetTime() < o2.targetTime())
					return -1;
				if (o1.targetTime() == o2.targetTime())
					return 0;
				return 1;
			}
		};

		long minTargetTime = Long.MAX_VALUE;
		int preferredSize;
		int maximumSize;
		BatchCommandArray batch;
		Condition signaller;

		BatchInfo(BatchExecutorBase<?,?> executor) {
			this.preferredSize = executor.preferredBatchSize();
			this.maximumSize = executor.maximumBatchSize();
			if (maximumSize < preferredSize)
				throw new IllegalArgumentException(
						"Preferred batch size must not exceed maximum batch size");
			this.batch = new BatchCommandArray(executor.prioritize(), initialArraySize());
		}

		int initialArraySize() {
			return Math.min(preferredSize * 2, maximumSize);
		}

		public List<BatchCommand<?,?>> removeCommands(boolean stopped) {
			int batchSize = batch.size();
			if (batchSize <= maximumSize) {
				BatchCommandArray commands = this.batch;
				minTargetTime = Long.MAX_VALUE;
				batch = new BatchCommandArray(commands.sorted, initialArraySize());
				signaller = null;
				return commands;
			}
			BatchCommand<?,?>[] commands = new BatchCommand<?,?>[maximumSize];
			batch.removeElementsFromStart(commands);
			minTargetTime = 0;
			if (!stopped && batch.size() < preferredSize) {
				minTargetTime = Long.MAX_VALUE;
				if (batch.size() > 0)
					minTargetTime = batch.get(0).targetTime();
			}
			signaller = null;
			return Arrays.asList(commands);
		}

		long waitDelay() {
			long ret = minTargetTime - System.currentTimeMillis();
			return (ret < 0) ? 0 : ret;
		}

		/**
		 * @return the new target time, if changed, else -1
		 */
		Long add(BatchCommand<?, ?> cmd) {
			batch.add(cmd);
			if (this.preferredSize == batch.size() && minTargetTime > 0)
				return (minTargetTime = 0);
			if (cmd.targetTime() < minTargetTime)
				return (minTargetTime = cmd.targetTime());
			return null;
		}

	}

	Map<BatchExecutorBase<?,?>, BatchInfo> batchMap;
	LinkedList<Condition> freeConditions;
	LinkedList<Condition> unusedConditions;
	ArrayList<BatchInfo> batches;
	ReentrantLock lock;
	int numThreads;
	State state;

	public BatcherQueue() {
		this.numThreads = 0;
		lock = new ReentrantLock(false);
		batches = new ArrayList<BatchInfo>();
		batchMap = new HashMap<BatchExecutorBase<?,?>, BatchInfo>();
		freeConditions = new LinkedList<Condition>();
		unusedConditions = new LinkedList<Condition>();
		state = State.STARTED;
	}

	public void submitBatchCommand(BatchCommand<?, ?> cmd) {
		lock.lock();
		try {
			BatchInfo batchInfo = batchMap.get(cmd.executor());
			if (batchInfo == null) {
				batchInfo = new BatchInfo(cmd.executor());
				batchMap.put(cmd.executor(), batchInfo);
				batches.add(batchInfo);
			} else {
				if (batchInfo.batch.size() > batchInfo.maximumSize) {
					//for we do not use fair locks, try to let consumers fetch their bulk first before flooding
					//the queue with commands
					lock.unlock();
					Thread.yield();
					lock.lock();
				}
			}
			Long targetTime = batchInfo.add(cmd);
			enqueueBatch(batchInfo, targetTime);
		} finally {
			lock.unlock();
		}
	}

	void enqueueBatch(BatchInfo batchInfo, Long targetTime) {
		if (targetTime != null && targetTime.longValue() == Long.MAX_VALUE)
			targetTime = null;
		int queuePosition = -1;
		if (targetTime != null) {
			queuePosition = reinsert(batchInfo, targetTime);
		}
		if (batchInfo.signaller == null) {
			if (!freeConditions.isEmpty()) {
				batchInfo.signaller = freeConditions.remove();
				batchInfo.signaller.signal();
			} else if (targetTime != null) {
				if (queuePosition < numThreads) {
					BatchInfo removedBatch = batches.get(numThreads);
					batchInfo.signaller = removedBatch.signaller;
					removedBatch.signaller = null;
					batchInfo.signaller.signal();
				}
			}
		} else {
			if (targetTime != null) {
				batchInfo.signaller.signal();
			}
		}
	}

	int reinsert(BatchInfo batchInfo, long targetTime) {
		int queuePosition = findQueuePosition(batchInfo);
		for (; queuePosition > 0; --queuePosition) {
			BatchInfo currentBatch = batches.get(queuePosition - 1);
			if (currentBatch.minTargetTime <= targetTime)
				break;
			batches.set(queuePosition - 1, batchInfo);
			batches.set(queuePosition, currentBatch);
		}
		return queuePosition;
	}

	int findQueuePosition(BatchInfo batchInfo) {
		int i = batches.size();
		while (batches.get(--i) != batchInfo)
			;
		return i;
	}

	int findQueuePosition(Condition condition) {
		int i = batches.size() - 1;
		for (; i > -1 && batches.get(i).signaller != condition; --i)
			;
		return i;
	}

	void stop() throws InterruptedException {
		while (true) {
			lock.lock();
			try {
				state = State.STOPPING;
				boolean stopped = true;
				for (BatchInfo batch : batches) {
					if (batch.batch.size() > 0)
						stopped = false;
				}
				if (stopped) {
					state = State.STOPPED;
					signalAll();
					return;
				}
				signalAll();
			} finally {
				lock.unlock();
			}
			Thread.sleep(100);
		}
	}

	private void signalAll() {
		for (BatchInfo batch : batches) {
			batch.minTargetTime = 0;
			if (batch.signaller != null) {
				batch.signaller.signal();
			}
		}
		for (Condition cond : freeConditions) {
			cond.signal();
		}
	}
	

	public List<BatchCommand<?,?>> poll() throws InterruptedException {
		lock.lockInterruptibly();
		try {
			outerLoop: while (true) {
				++numThreads;
				Condition myCondition = null;
				if (!unusedConditions.isEmpty())
					myCondition = unusedConditions.pop();
				else {
					myCondition = lock.newCondition();
				}
				if (batches.size() >= numThreads) {
					batches.get(numThreads - 1).signaller = myCondition;
				} else {
					freeConditions.add(myCondition);
				}
				waitLoop: while (true) {
					int queuePosition = findQueuePosition(myCondition);
					if (queuePosition == -1) {
						if (state == State.STOPPED) {
							unusedConditions.add(myCondition);
							return null;
						}
						myCondition.await();
						continue waitLoop;
					}
					BatchInfo myBatch = batches.get(queuePosition);
					long waitDelay = myBatch.waitDelay();
					if (state != State.STOPPED && waitDelay > 0) {
						myCondition.await(waitDelay, TimeUnit.MILLISECONDS);
					} else {
						--numThreads;
						List<BatchCommand<?,?>> commands = myBatch
								.removeCommands(state == State.STOPPING);
						batches.remove(queuePosition);
						batches.add(myBatch);
						enqueueBatch(myBatch, myBatch.minTargetTime);
						unusedConditions.add(myCondition);
						if (commands.size() > 0) {
							return commands;
						}
						if (state == State.STOPPED)
							return null;
						continue outerLoop;
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

}
