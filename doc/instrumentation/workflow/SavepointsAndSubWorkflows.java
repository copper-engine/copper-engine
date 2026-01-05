package test;

import org.copperengine.core.Interrupt;
import org.copperengine.core.Workflow;

public class SavepointsAndSubWorkflows extends Workflow<Void> {

    @Override
    public void main() throws Interrupt {
        int i1 = 1;
        final int i2 = 2;
        System.out.println("main start");
        savepoint();
        System.out.println("jumpNo=0");
        savepoint();
        System.out.println("jumpNo=1");
        savepoint();
        System.out.println("jumpNo=2");
        subWorkflow(i1, 666L); // <1>
        System.out.println("main end");
    }

    public void subWorkflow(int i1, long l) throws Interrupt {
        System.out.println("subWorklow start");
        savepoint();
        System.out.println("jumpNo=0");
        savepoint();
        System.out.println("jumpNo=1");
        savepoint();
        System.out.println("jumpNo=2");
        subWorkflow2(new long[] { 777L, 888L, 999L }); // <2>
        System.out.println("subWorklow end");
    }

    public void subWorkflow2(long[] l) throws Interrupt {
        System.out.println("subWorklow2 start");
        savepoint();
        System.out.println("jumpNo=0");
        System.out.println("subWorklow2 end");
    }
}
