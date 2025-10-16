package org.copperengine.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.jupiter.api.Test;

public class StackEntryTest {

    private static final int JUMP_NO = 5;

    private static final class Dummy implements Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public boolean equals(Object obj) {
            return true;
        }
    }

    @Test
    public void testSerializationAllEmpty() throws Exception {
        StackEntry se = new StackEntry(JUMP_NO);
        se.locals = new Object[] {};
        se.stack = new Object[] {};
        byte[] bytes = toBytes(se);
        assertEquals(68,  bytes.length);

        StackEntry se2 = toStackEntry(bytes);
        assertNotNull(se2);
        assertEquals(se2.jumpNo, JUMP_NO);
        assertNull(se2.locals);
        assertNull(se2.stack);
    }

    @Test
    public void testSerializationWithData() throws Exception {
        StackEntry se = new StackEntry(JUMP_NO);
        se.locals = new Object[] { "localA", "localB", "localC", "localD", "localE", "localF", 4711, new Dummy() };
        se.stack = new Object[] { "stackA", "stackB", 42, new Dummy()};
        byte[] bytes = toBytes(se);

        assertEquals(292,  bytes.length);

        StackEntry se2 = toStackEntry(bytes);
        assertNotNull(se2);
        assertEquals(se2.jumpNo, JUMP_NO);
        assertArrayEquals(se.locals, se2.locals);
        assertArrayEquals(se.stack, se2.stack);
    }

    private byte[] toBytes(StackEntry se) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(se);
        oos.flush();
        oos.close();
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    private StackEntry toStackEntry(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream oos = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (StackEntry) oos.readObject();
        }
    }    

}
