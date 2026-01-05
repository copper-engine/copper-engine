package test;

import org.copperengine.core.Interrupt;
import org.copperengine.core.Workflow;

public class OneSavepointWithTryCatchFinally extends Workflow<Void> {

    @Override
    public void main() throws Interrupt {
        System.out.println("main start");
        try {
            int i1 = 1;
            final int i2 = 2;
            savepoint(); // <1>
            System.out.println("jumpNo=0");
        } catch (Exception e) {
            System.out.println("Exception");
            throw new RuntimeException(e);
        } finally {
            System.out.println("Finally");
        }
        System.out.println("main end");
    }
}
