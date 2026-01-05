//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package test;

import org.copperengine.core.Interrupt;
import org.copperengine.core.StackEntry;
import org.copperengine.core.Workflow;
import org.copperengine.core.instrument.Transformed;

@Transformed
public class OneSavepoint extends Workflow<Void> {
    public void main() throws Interrupt {
        if (this.__stack.size() == this.__stackPosition) { // <1>
            System.out.println("main start");
            int i1 = 1;
            int i2 = 2;
            this.savepoint();
            this.__stack.push(new StackEntry(new Object[0], 0, new Object[]{this, i1, i2})); // <2>
            ++this.__stackPosition; // <3>
            throw new Interrupt(); // <4>
        } else if (((StackEntry)this.__stack.get(this.__stackPosition)).jumpNo == 0) { // <5>
            Object[] var10000 = ((StackEntry)this.__stack.get(this.__stackPosition)).locals; // <6>
            this = (OneSavepoint)var10000[0]; // <7>
            int i1 = (Integer)var10000[1]; // <8>
            int i2 = (Integer)var10000[2]; // <9>
            ++this.__stackPosition;
            this.__stack.pop(); // <10>
            --this.__stackPosition;
            System.out.println("jumpNo=0");
            System.out.println("main end");
        } else {
            throw new RuntimeException("No such label");
        }
    }
}
