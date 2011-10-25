/*
 * Copyright 2002-2011 SCOOP Software GmbH
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

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import de.scoopgmbh.copper.batcher.BatchCommand;

class BatchCommandArray extends AbstractList<BatchCommand<?,?>> implements List<BatchCommand<?,?>> {
	
	BatchCommand<?,?>[] a;
	int                 size;
	int                 offset;
	boolean             sorted;
	long                upperSortBoundValue = Long.MIN_VALUE;
	int                 upperSortBound      = 0;
	
	public BatchCommandArray(boolean sorted, int capacity) {
		a = new BatchCommand<?,?>[capacity];
		size=0;
		offset=0;
		this.sorted = sorted;
	}
	
	public BatchCommand<?,?>[] removeElementsFromStart(BatchCommand<?,?>[] destination) { 
		if (destination.length > size)
			throw new ArrayIndexOutOfBoundsException("Array size exceeded");
		if (sorted && upperSortBound <= destination.length) {
			sortSmooth(upperSortBound,size-1);
			upperSortBound = size-1;
			upperSortBoundValue = a[offset+size-1].targetTime();
		}
		for (int i = 0; i < destination.length; ++i)
			destination[i] =  a[offset+i];
		offset += destination.length;
		size -= destination.length;
		if (sorted) {
			upperSortBound -= destination.length;
		}
		return destination;
	}
	
	//Smooth sort from wikipedia
	//Leonardo numbers
	static final int LP[] = { 1, 1, 3, 5, 9, 15, 25, 41, 67, 109,
	      177, 287, 465, 753, 1219, 1973, 3193, 5167, 8361, 13529, 21891,
	      35421, 57313, 92735, 150049, 242785, 392835, 635621, 1028457,
	      1664079, 2692537, 4356617, 7049155, 11405773, 18454929, 29860703,
	      48315633, 78176337, 126491971, 204668309, 331160281, 535828591,
	      866988873 
	  };
	 
	public void sortSmooth(int lo, int hi) {
		
	    int head = lo; // the offset of the first element of the prefix into m
	 
	    // These variables need a little explaining. If our string of heaps
	    // is of length 38, then the heaps will be of size 25+9+3+1, which are
	    // Leonardo numbers 6, 4, 2, 1. 
	    // Turning this int a binary number, we get b01010110 = x56. We represent
	    // this number as a pair of numbers by right-shifting all the zeros and 
	    // storing the mantissa and exponent as "p" and "pshift".
	    // This is handy, because the exponent is the index into L[] giving the
	    // size of the rightmost heap, and because we can instantly find out if
	    // the rightmost two heaps are consecutive leonardo numbers by checking
	    // (p&3)==3
	 
	    int p = 1; // the bitmap of the current standard concatenation >> pshift
	    int pshift = 1;
	 
	    while (head < hi) {
	      if ((p & 3) == 3) {
	        // Add 1 by merging the first two blocks into a larger one.
	        // The next Leonardo num is one bigger.
	        sift(pshift, head);
	        p >>>= 2;
	        pshift += 2;
	      } else {
	        // adding a new block of length 1
	        if (LP[pshift - 1] >= hi - head) {
	          // this block is its final size.
	          trinkle(p, pshift, head, false);
	        } else {
	          // this block will get merged. Just make it trusty.
	          sift(pshift, head);
	        }
	 
	        if (pshift == 1) {
	          // LP[1] is being used, so we add use LP[0]
	          p <<= 1;
	          pshift--;
	        } else {
	          // shift out to position 1, add LP[1]
	          p <<= (pshift - 1);
	          pshift = 1;
	        }
	      }
	      p |= 1;
	      head++;
	    }
	 
	    trinkle(p, pshift, head, false);
	 
	    while (pshift != 1 || p != 1) {
	      if (pshift <= 1) {
	        // block of length 1. No fiddling needed
	        int trail = Integer.numberOfTrailingZeros(p & ~1);
	        p >>>= trail;
	        pshift += trail;
	      } else {
	        p <<= 2;
	        p ^= 7;
	        pshift -= 2;
	 
	        // ok. This block gets broken into three bits. The rightmost
	        // bit is a block of length 1. The left hand part is split into
	        // two, a block of length LP[pshift+1] and one of LP[pshift].
	        // Both these two are appropriately heapified, but the root
	        // nodes are not nessesarily in order. We therefore semitrinkle
	        // both
	        // of them
	 
	        trinkle(p >>> 1, pshift + 1, head - LP[pshift] - 1, true);
	        trinkle(p, pshift, head - 1, true);
	      }
	 
	      head--;
	    }
	  }
	 
	  private void sift(int pshift,
	      int head) {
	    // we do not use Floyd's improvements to the heapsort sift, because we
	    // are not doing what heapsort does - always moving nodes from near
	    // the bottom of the tree to the root.
	 
	    BatchCommand<?,?> val = a[head];
	 
	    while (pshift > 1) {
	      int rt = head - 1;
	      int lf = head - 1 - LP[pshift - 2];
	 
	      if (val.targetTime() >= a[lf].targetTime() && val.targetTime() >= a[rt].targetTime())
	        break;
	      if (a[lf].targetTime() >= a[rt].targetTime()) {
	        a[head] = a[lf];
	        head = lf;
	        pshift -= 1;
	      } else {
	        a[head] = a[rt];
	        head = rt;
	        pshift -= 2;
	      }
	    }
	 
	    a[head] = val;
	  }
	 
	  private void trinkle(int p,
	      int pshift, int head, boolean isTrusty) {
	 
	    BatchCommand<?,?> val = a[head];
	 
	    while (p != 1) {
	      int stepson = head - LP[pshift];
	 
	      if (a[stepson].targetTime() <= val.targetTime())
	        break; // current node is greater than head. Sift.
	 
	      // no need to check this if we know the current node is trusty,
	      // because we just checked the head (which is val, in the first
	      // iteration)
	      if (!isTrusty && pshift > 1) {
	        int rt = head - 1;
	        int lf = head - 1 - LP[pshift - 2];
	        if (a[rt].targetTime() >= a[stepson].targetTime()
	            || a[lf].targetTime() >= a[stepson].targetTime())
	          break;
	      }
	 
	      a[head] = a[stepson];
	 
	      head = stepson;
	      int trail = Integer.numberOfTrailingZeros(p & ~1);
	      p >>>= trail;
	      pshift += trail;
	      isTrusty = false;
	    }
	 
	    if (!isTrusty) {
	      a[head] = val;
	      sift(pshift, head);
	    }
	  }

	
	public boolean add(BatchCommand<?,?> elem) {
		ensureCapacity(size+1);
		a[size+offset] = elem;
		++size;
		if (sorted && upperSortBound > 0 && elem.targetTime() < upperSortBoundValue) {
			int idx = bsearch(elem.targetTime(),offset,offset+upperSortBound);
			upperSortBoundValue = a[idx].targetTime();
			upperSortBound      = idx-offset;
		}
		return true;
	}

	int bsearch(long targetTime, int fromInclusive, int toInclusive) {
		while (fromInclusive < toInclusive-1) {
			int middle = fromInclusive + (toInclusive-fromInclusive)/2;
			if (a[middle].targetTime() >= targetTime)
				toInclusive = middle;
			else
				fromInclusive = middle;
		}
		return fromInclusive;
	}

	void ensureCapacity(int minCapacity) {
		if (a.length < minCapacity) {
		    int newCapacity = (a.length * 3)/2 + 1;
	    	if (newCapacity < minCapacity)
			    newCapacity = minCapacity;
		    	if (offset == 0)
		            a = Arrays.copyOf(a, newCapacity);
		    	else {
		    		BatchCommand<?,?>[] newArray = (BatchCommand<?,?>[])Array.newInstance(a.getClass().getComponentType(), newCapacity);
		    		System.arraycopy(a, offset, newArray, 0, size);
		    		a = newArray;
		    		offset = 0;
	    	}
			return;
		}
		if (size+offset == a.length) {
			System.arraycopy(a,offset,a,0,size);
			for (int i = size; i < a.length; ++i)
				a[i] = null;
			offset = 0;
		}
	}

	@Override
	public BatchCommand<?,?> get(int index) {
		return a[index+offset];
	}

	@Override
	public int size() {
		return size;
	}

	@Override
    public void clear() {
		for (int i = 0; i < a.length; ++i)
			a[i] = null;
		offset = 0;
		size = 0;
	}

}
