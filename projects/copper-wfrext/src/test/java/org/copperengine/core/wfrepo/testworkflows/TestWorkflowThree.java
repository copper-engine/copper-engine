package org.copperengine.core.wfrepo.testworkflows;

import org.copperengine.core.Interrupt;

public class TestWorkflowThree extends TestWorkflowTwo {

    private static final long serialVersionUID = 1L;

    static class MyInnerClass {
        public void printFoo() {
            System.out.println("foo");
        }
    }

    @Override
    public void main() throws Interrupt {
        new MyInnerClass().printFoo();
    }

}
