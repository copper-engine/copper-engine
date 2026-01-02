package test;

import org.copperengine.core.Interrupt;
import org.copperengine.core.Workflow;

public class OneSavepoint extends Workflow<Void> {

    @Override
    public void main() throws Interrupt {
        System.out.println("main start");
        int i1 = 1;
        final int i2 = 2;
        savepoint(); // <1>
        System.out.println("jumpNo=0");
        System.out.println("main end");
    }
}
