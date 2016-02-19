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
package org.copperengine.core.test.tranzient.simple;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.test.MockAdapter;
import org.copperengine.core.test.tranzient.simple.ScottyTest.TestData;
import org.copperengine.core.util.AsyncResponseReceiver;

public class ScottyTestTransientWorkflow extends Workflow<AsyncResponseReceiver<TestData>> {

    private static final long serialVersionUID = -9191480374225984819L;

    private static String setOnly = "";

    private MockAdapter mockAdapter;

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    @Override
    public void main() throws Interrupt {
        TestData usedTestData = new TestData();

        // LOAD* instruction for increment most fields in usedTestData
        final byte theByte = new byte[] { (byte) (usedTestData.theByte + 1) }[0];
        short theShort = new short[] { (short) (usedTestData.theShort + 1) }[0];
        int theInt = new int[] { usedTestData.theInt + 1 }[0];
        long theLong = new long[] { usedTestData.theLong + 1 }[0];
        float theFloat = new float[] { usedTestData.theFloat + 1 }[0];
        double theDouble = new double[] { usedTestData.theDouble + 1 }[0];
        boolean theBoolen = new boolean[] { !usedTestData.theBoolean }[0];
        char theChar = new char[] { (char) (usedTestData.theChar + 1) }[0];

        TestData[] testDatas = usedTestData.testDatas;
        TestData[][] testDatass = usedTestData.testDatass;
        long arrayLength = testDatas.length; // ARRAYLENGTH
        TestData testData = testDatas[0]; // AALOAD
        testData = testDatass[0][0];
        testData = usedTestData.testData;

        // More convert instruction and other stuff
        long l = 100000000L;
        float f = l; // L2F
        double d = (double) l + f; // L2D;F2D;DADD
        f = (float) 100000000.1234D; // LDC
        f = f + (float) d + l; // D2F;L2F;FADD
        d = (double) 100000000.1234D; // LDC
        int i = (int) l; // L2I
        i = (int) d + i; // D2I, IADD
        i = (int) f + i; // F2I, IADD
        l = (long) d + l; // D2L; LADD
        l = (long) f + l; // F2L; LADD
        if (f < l) // FCMPG
            i = 0;
        if (f < 0.1D) // DCMPG
            i = 0;

        long x = 1;
        x >>>= 3; // LUSHR

        synchronized (this) { // MONITORENTER
            i = 0; // FRAME FULL ignored?;MONITOREXIS;

        }

        setOnly = "set"; // PUTSTATIC
        // BadInterface.z(); // INVOKESTATIC on Java8 Interface (IllegalArgumentException: INVOKESPECIAL/STATIC on
        // interfaces require ASM 5)

        String nullData = null; // special handling in ScottymethodAdapter; FRAME CHOP 1 ignored?
        try {
            usedTestData = execute(nullData, theByte, theShort, theInt, theLong, theFloat, theDouble, theBoolen, theChar, testData, testDatas, testDatass);
        } catch (Throwable e) {
            e.printStackTrace();
            getData().setResponse(null); // INVOKEVIRTUAL;INVOKEINTERFACE
        }

        int increments = 2; // FRAMESAME ignored?
        usedTestData.theByte = (byte) (usedTestData.theByte - increments);
        usedTestData.theShort = (short) (usedTestData.theShort - increments);
        usedTestData.theInt = usedTestData.theInt - increments;
        usedTestData.theLong = usedTestData.theLong - increments;
        usedTestData.theFloat = usedTestData.theFloat - increments;
        usedTestData.theDouble = usedTestData.theDouble - increments;
        usedTestData.theBoolean = !usedTestData.theBoolean;
        usedTestData.theChar = (char) (usedTestData.theChar - increments);
        getData().setResponse(usedTestData);
    }

    private TestData execute(String nullData, byte theByte, short theShort, int theInt, long theLong, float theFloat, double theDouble, boolean theBoolean, char theChar, TestData testData, TestData[] testDatas, TestData[][] testDatass) throws Interrupt {
        final String cid = getEngine().createUUID();
        mockAdapter.foo("foo", cid);
        wait(WaitMode.ALL, 1000, cid);
        Response<String> response = getAndRemoveResponse(cid);
        if (response == null)
            throw new AssertionError();
        if (!response.getCorrelationId().equals(cid))
            throw new AssertionError();
        if (getAndRemoveResponse(cid) != null)
            throw new AssertionError();
        if (!response.getResponse().equals("foo"))
            throw new AssertionError();
        TestData returnedTestData = new TestData();
        returnedTestData.theByte = (byte) (theByte + 1);
        returnedTestData.theShort = (short) (theShort + 1);
        returnedTestData.theInt = theInt + 1;
        returnedTestData.theLong = theLong + 1;
        returnedTestData.theFloat = theFloat + 1;
        returnedTestData.theDouble = theDouble + 1;
        returnedTestData.theBoolean = theBoolean;
        returnedTestData.theChar = (char) (theChar + 1);
        returnedTestData.testData = testData;
        returnedTestData.testDatas = testDatas;
        returnedTestData.testDatass = testDatass;
        return returnedTestData;
    }
}
