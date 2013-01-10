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
package de.scoopgmbh.copper.instrument;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.objectweb.asm.Type;

public class BuildStackInfoAdapterTest extends TestCase {


	public void testDup() {
		StackInfo f = new StackInfo();
		f.pushStack(Type.INT_TYPE);
		f.dupStack();
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		try {
			f.popStack();
			Assert.fail("Expected empty stack exception");
		} catch (Exception ex) {
		}
		f = new StackInfo();
		f.pushStack(Type.DOUBLE_TYPE);
		try {
			f.dupStack();
			Assert.fail("Expected exception: wrong computational type.");
		} catch (Exception ex) {
		}
	}

	public void testDupX1() {
		StackInfo f = new StackInfo();
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.FLOAT_TYPE);
		f.dupX1Stack();
		Assert.assertSame(Type.FLOAT_TYPE, f.popStack());
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		Assert.assertSame(Type.FLOAT_TYPE, f.popStack());
		try {
			f.popStack();
			Assert.fail("Expected empty stack exception");
		} catch (Exception ex) {
		}
		f = new StackInfo();
		f.pushStack(Type.DOUBLE_TYPE);
		f.pushStack(Type.INT_TYPE);
		try {
			f.dupX1Stack();
			Assert.fail("Expected exception: wrong computational type.");
		} catch (Exception ex) {
		}
		f = new StackInfo();
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.DOUBLE_TYPE);
		try {
			f.dupX1Stack();
			Assert.fail("Expected exception: wrong computational type.");
		} catch (Exception ex) {
		}
	}

	public void testDupX2() {
		StackInfo f = new StackInfo();
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.FLOAT_TYPE);
		f.pushStack(Type.BYTE_TYPE);
		f.dupX2Stack();
		Assert.assertSame(Type.BYTE_TYPE, f.popStack());
		Assert.assertSame(Type.FLOAT_TYPE, f.popStack());
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		Assert.assertSame(Type.BYTE_TYPE, f.popStack());
		try {
			f.popStack();
			Assert.fail("Expected empty stack exception");
		} catch (Exception ex) {
		}
		f = new StackInfo();
		f.pushStack(Type.DOUBLE_TYPE);
		f.pushStack(Type.INT_TYPE);
		f.dupX2Stack();
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		Assert.assertSame(Type.DOUBLE_TYPE, f.pop2Stack());
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		try {
			f.popStack();
			Assert.fail("Expected empty stack exception");
		} catch (Exception ex) {
		}
		f = new StackInfo();
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.DOUBLE_TYPE);
		try {
			f.dupX2Stack();
			Assert.fail("Expected exception: wrong computational type.");
		} catch (Exception ex) {
		}
		f = new StackInfo();
		f.pushStack(Type.DOUBLE_TYPE);
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.INT_TYPE);
		try {
			f.dupX2Stack();
			Assert.fail("Expected exception: wrong computational type.");
		} catch (Exception ex) {
		}
	}

	public void testDup2() {
		StackInfo f = new StackInfo();
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.FLOAT_TYPE);
		f.dup2Stack();
		Assert.assertSame(Type.FLOAT_TYPE, f.popStack());
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		Assert.assertSame(Type.FLOAT_TYPE, f.popStack());
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		try {
			f.popStack();
			Assert.fail("Expected empty stack exception");
		} catch (Exception ex) {
		}
		f = new StackInfo();
		f.pushStack(Type.DOUBLE_TYPE);
		f.dup2Stack();
		Assert.assertSame(Type.DOUBLE_TYPE, f.pop2Stack());
		Assert.assertSame(Type.DOUBLE_TYPE, f.pop2Stack());
		try {
			f.popStack();
			Assert.fail("Expected empty stack exception");
		} catch (Exception ex) {
		}
		f = new StackInfo();
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.DOUBLE_TYPE);
		f.pushStack(Type.INT_TYPE);
		try {
			f.dup2Stack();
			Assert.fail("Expected exception: wrong computational type.");
		} catch (Exception ex) {
		}
	}


	public void testDup2X1() {
		StackInfo f = new StackInfo();
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.FLOAT_TYPE);
		f.pushStack(Type.CHAR_TYPE);
		f.dup2X1Stack();
		Assert.assertSame(Type.CHAR_TYPE, f.popStack());
		Assert.assertSame(Type.FLOAT_TYPE, f.popStack());
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		Assert.assertSame(Type.CHAR_TYPE, f.popStack());
		Assert.assertSame(Type.FLOAT_TYPE, f.popStack());
		try {
			f.popStack();
			Assert.fail("Expected empty stack exception");
		} catch (Exception ex) {
		}
		f = new StackInfo();
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.DOUBLE_TYPE);
		f.dup2X1Stack();
		Assert.assertSame(Type.DOUBLE_TYPE, f.pop2Stack());
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		Assert.assertSame(Type.DOUBLE_TYPE, f.pop2Stack());
		try {
			f.popStack();
			Assert.fail("Expected empty stack exception");
		} catch (Exception ex) {
		}
		f = new StackInfo();
		f.pushStack(Type.DOUBLE_TYPE);
		f.pushStack(Type.DOUBLE_TYPE);
		try {
			f.dup2X1Stack();
			Assert.fail("Expected exception: wrong computational type.");
		} catch (Exception ex) {
		}
		f = new StackInfo();
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.DOUBLE_TYPE);
		f.pushStack(Type.INT_TYPE);
		try {
			f.dup2X1Stack();
			Assert.fail("Expected exception: wrong computational type.");
		} catch (Exception ex) {
		}
	}

	public void testDup2X2() {
		StackInfo f = new StackInfo();
		//FORM1
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.FLOAT_TYPE);
		f.pushStack(Type.CHAR_TYPE);
		f.pushStack(Type.BYTE_TYPE);
		f.dup2X2Stack();
		Assert.assertSame(Type.BYTE_TYPE, f.popStack());
		Assert.assertSame(Type.CHAR_TYPE, f.popStack());
		Assert.assertSame(Type.FLOAT_TYPE, f.popStack());
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		Assert.assertSame(Type.BYTE_TYPE, f.popStack());
		Assert.assertSame(Type.CHAR_TYPE, f.popStack());
		try {
			f.popStack();
			Assert.fail("Expected empty stack exception");
		} catch (Exception ex) {
		}
		//FORM2
		f = new StackInfo();
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.FLOAT_TYPE);
		f.pushStack(Type.DOUBLE_TYPE);
		f.dup2X2Stack();
		Assert.assertSame(Type.DOUBLE_TYPE, f.pop2Stack());
		Assert.assertSame(Type.FLOAT_TYPE, f.popStack());
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		Assert.assertSame(Type.DOUBLE_TYPE, f.pop2Stack());
		try {
			f.popStack();
			Assert.fail("Expected empty stack exception");
		} catch (Exception ex) {
		}
		//FORM3
		f = new StackInfo();
		f.pushStack(Type.DOUBLE_TYPE);
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.FLOAT_TYPE);
		f.dup2X2Stack();
		Assert.assertSame(Type.FLOAT_TYPE, f.popStack());
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		Assert.assertSame(Type.DOUBLE_TYPE, f.pop2Stack());
		Assert.assertSame(Type.FLOAT_TYPE, f.popStack());
		Assert.assertSame(Type.INT_TYPE, f.popStack());
		f = new StackInfo();
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.DOUBLE_TYPE);
		f.pushStack(Type.FLOAT_TYPE);
		try {
			f.dup2X2Stack();
			Assert.fail("Expected exception: wrong computational type.");
		} catch (Exception ex) {
		}
		f = new StackInfo();
		f.pushStack(Type.DOUBLE_TYPE);
		f.pushStack(Type.INT_TYPE);
		f.pushStack(Type.DOUBLE_TYPE);
		try {
			f.dup2X2Stack();
			Assert.fail("Expected exception: wrong computational type.");
		} catch (Exception ex) {
		}
	}

}
