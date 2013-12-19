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
package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;

public class ProcessorPoolInfo implements Serializable {
    private static final long serialVersionUID = -7695520323442864482L;

    private String id;
    private ProcessorPoolTyp processorPoolTyp;

    // priorty only
    private int lowerThreshold;
    private int upperThreshold;
    private int upperThresholdReachedWaitMSec;
    private int emptyQueueWaitMSec;
    private int dequeueBulkSize;

    // all
    private int numberOfThreads;
    private int threadPriority;
    private int memoryQueueSize;

    public ProcessorPoolInfo(String poolId, ProcessorPoolTyp processorPoolTyp, int lowerThreshold, int upperThreshold,
            int upperThresholdReachedWaitMSec, int emptyQueueWaitMSec, int dequeueBulkSize, int numberOfThreads, int threadPriority,
            int memoryQueueSize) {
        super();
        this.id = poolId;
        this.processorPoolTyp = processorPoolTyp;
        this.lowerThreshold = lowerThreshold;
        this.upperThreshold = upperThreshold;
        this.upperThresholdReachedWaitMSec = upperThresholdReachedWaitMSec;
        this.emptyQueueWaitMSec = emptyQueueWaitMSec;
        this.dequeueBulkSize = dequeueBulkSize;
        this.numberOfThreads = numberOfThreads;
        this.threadPriority = threadPriority;
        this.memoryQueueSize = memoryQueueSize;
    }

    public ProcessorPoolInfo(String poolId, ProcessorPoolTyp processorPoolTyp) {
        super();
        this.id = poolId;
        this.processorPoolTyp = processorPoolTyp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ProcessorPoolTyp getProcessorPoolTyp() {
        return processorPoolTyp;
    }

    public void setProcessorPoolTyp(ProcessorPoolTyp processorPoolTyp) {
        this.processorPoolTyp = processorPoolTyp;
    }

    public int getLowerThreshold() {
        return lowerThreshold;
    }

    public void setLowerThreshold(int lowerThreshold) {
        this.lowerThreshold = lowerThreshold;
    }

    public int getUpperThreshold() {
        return upperThreshold;
    }

    public void setUpperThreshold(int upperThreshold) {
        this.upperThreshold = upperThreshold;
    }

    public int getUpperThresholdReachedWaitMSec() {
        return upperThresholdReachedWaitMSec;
    }

    public void setUpperThresholdReachedWaitMSec(int upperThresholdReachedWaitMSec) {
        this.upperThresholdReachedWaitMSec = upperThresholdReachedWaitMSec;
    }

    public int getEmptyQueueWaitMSec() {
        return emptyQueueWaitMSec;
    }

    public void setEmptyQueueWaitMSec(int emptyQueueWaitMSec) {
        this.emptyQueueWaitMSec = emptyQueueWaitMSec;
    }

    public int getDequeueBulkSize() {
        return dequeueBulkSize;
    }

    public void setDequeueBulkSize(int dequeueBulkSize) {
        this.dequeueBulkSize = dequeueBulkSize;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public int getThreadPriority() {
        return threadPriority;
    }

    public void setThreadPriority(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    public int getMemoryQueueSize() {
        return memoryQueueSize;
    }

    public void setMemoryQueueSize(int memoryQueueSize) {
        this.memoryQueueSize = memoryQueueSize;
    }

    @Override
    public String toString() {
        return "ProcessorPoolInfo [id=" + id + ", processorPoolTyp=" + processorPoolTyp + ", lowerThreshold=" + lowerThreshold + ", upperThreshold="
                + upperThreshold + ", upperThresholdReachedWaitMSec=" + upperThresholdReachedWaitMSec + ", emptyQueueWaitMSec=" + emptyQueueWaitMSec
                + ", dequeueBulkSize=" + dequeueBulkSize + ", numberOfThreads=" + numberOfThreads + ", threadPriority=" + threadPriority + ", memoryQueueSize="
                + memoryQueueSize + "]";
    }

    public static enum ProcessorPoolTyp {
        TRANSIENT, PERSISTENT
    }

}
