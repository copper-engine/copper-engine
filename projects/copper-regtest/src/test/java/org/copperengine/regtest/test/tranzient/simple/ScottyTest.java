/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.regtest.test.tranzient.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.util.Arrays;

import org.copperengine.core.EngineState;
import org.copperengine.regtest.test.tranzient.TransientEngineTestContext;
import org.copperengine.core.util.BlockingResponseReceiver;
import org.junit.jupiter.api.Test;

/**
 * This class test the instrumentation of the core engine, called Scotty.
 */
public class ScottyTest {

    public static class TestData implements Serializable {

        private static final long serialVersionUID = 1L;

        public byte theByte = 101;
        public short theShort = 102;
        public int theInt = 103;
        public long theLong = 104L;
        public float theFloat = 105.1516F;
        public double theDouble = 106.1516;
        public boolean theBoolean = true;
        public char theChar = '\u20AC';

        public TestData testData;
        public TestData[] testDatas;
        public TestData[][] testDatass;

        public TestData() {
            testDatas = new TestData[] { new TestData(1) };
            testData = testDatas[0];
            testDatass = new TestData[][] { new TestData[] { new TestData(2) } };
        }

        public TestData(int theInt) {
            this.theInt = theInt;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((testData == null) ? 0 : testData.hashCode());
            result = prime * result + Arrays.hashCode(testDatas);
            result = prime * result + Arrays.hashCode(testDatass);
            result = prime * result + (theBoolean ? 1231 : 1237);
            result = prime * result + theByte;
            result = prime * result + theChar;
            long temp;
            temp = Double.doubleToLongBits(theDouble);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + Float.floatToIntBits(theFloat);
            result = prime * result + theInt;
            result = prime * result + (int) (theLong ^ (theLong >>> 32));
            result = prime * result + theShort;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestData other = (TestData) obj;
            if (testData == null) {
                if (other.testData != null)
                    return false;
            } else if (!testData.equals(other.testData))
                return false;
            if (!Arrays.equals(testDatas, other.testDatas))
                return false;
            if (!Arrays.deepEquals(testDatass, other.testDatass))
                return false;
            if (theBoolean != other.theBoolean)
                return false;
            if (theByte != other.theByte)
                return false;
            if (theChar != other.theChar)
                return false;
            if (Double.doubleToLongBits(theDouble) != Double.doubleToLongBits(other.theDouble))
                return false;
            if (Float.floatToIntBits(theFloat) != Float.floatToIntBits(other.theFloat))
                return false;
            if (theInt != other.theInt)
                return false;
            if (theLong != other.theLong)
                return false;
            if (theShort != other.theShort)
                return false;
            return true;
        }
    }

    @Test
    public void testWorkflow() throws Exception {
        try (TransientEngineTestContext ctx = new TransientEngineTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            BlockingResponseReceiver<TestData> brr = new BlockingResponseReceiver<TestData>();
            ctx.getEngine().run("org.copperengine.regtest.test.tranzient.simple.ScottyTestTransientWorkflow", brr);
            brr.wait4response(5000L);
            assertEquals(new TestData(), brr.getResponse());
        }
    }

}
