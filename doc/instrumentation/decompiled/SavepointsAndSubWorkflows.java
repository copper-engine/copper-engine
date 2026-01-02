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
public class SavepointsAndSubWorkflows extends Workflow<Void> {
    public void main() throws Interrupt {
        if (this.__stack.size() == this.__stackPosition) {
            int i1 = 1;
            int i2 = 2;
            System.out.println("main start");
            this.savepoint();
            this.__stack.push(new StackEntry(new Object[0], 0, new Object[]{this, i1, i2}));
            ++this.__stackPosition;
            throw new Interrupt();
        } else {
            int var10000 = ((StackEntry)this.__stack.get(this.__stackPosition)).jumpNo;
            if (var10000 == 0) {
                Object[] var22 = ((StackEntry)this.__stack.get(this.__stackPosition)).locals;
                this = (SavepointsAndSubWorkflows)var22[0];
                int var10 = (Integer)var22[1];
                int var14 = (Integer)var22[2];
                ++this.__stackPosition;
                this.__stack.pop();
                --this.__stackPosition;
                System.out.println("jumpNo=0");
                this.savepoint();
                this.__stack.push(new StackEntry(new Object[0], 1, new Object[]{this, var10, var14}));
                ++this.__stackPosition;
                throw new Interrupt();
            } else if (var10000 == 1) {
                Object[] var21 = ((StackEntry)this.__stack.get(this.__stackPosition)).locals;
                this = (SavepointsAndSubWorkflows)var21[0];
                int var9 = (Integer)var21[1];
                int var13 = (Integer)var21[2];
                ++this.__stackPosition;
                this.__stack.pop();
                --this.__stackPosition;
                System.out.println("jumpNo=1");
                this.savepoint();
                this.__stack.push(new StackEntry(new Object[0], 2, new Object[]{this, var9, var13}));
                ++this.__stackPosition;
                throw new Interrupt();
            } else {
                SavepointsAndSubWorkflows var18;
                int var23; // <1>
                long var26; // <2>
                if (var10000 == 2) {
                    Object[] var16 = ((StackEntry)this.__stack.get(this.__stackPosition)).locals;
                    this = (SavepointsAndSubWorkflows)var16[0];
                    int i1 = (Integer)var16[1];
                    int i2 = (Integer)var16[2];
                    ++this.__stackPosition;
                    this.__stack.pop();
                    --this.__stackPosition;
                    System.out.println("jumpNo=2");
                    this.__stack.push(new StackEntry(new Object[]{this, i1, 666L}, 3, new Object[]{this, i1, i2})); // <3>
                    var16 = ((StackEntry)this.__stack.get(this.__stackPosition)).stack; // <4>
                    var18 = (SavepointsAndSubWorkflows)var16[0]; // <4>
                    var23 = (Integer)var16[1]; // <4>
                    var26 = (Long)var16[2]; // <4>
                    ++this.__stackPosition; // <5>
                } else { // <6>
                    if (var10000 != 3) {
                        throw new RuntimeException("No such label");
                    }

                    Object[] var19 = ((StackEntry)this.__stack.get(this.__stackPosition)).locals;
                    this = (SavepointsAndSubWorkflows)var19[0];
                    int var8 = (Integer)var19[1];
                    int var12 = (Integer)var19[2];
                    var19 = ((StackEntry)this.__stack.get(this.__stackPosition)).stack; // <7>
                    var18 = (SavepointsAndSubWorkflows)var19[0]; // <7>
                    var23 = (Integer)var19[1]; // <7>
                    var26 = (Long)var19[2]; // <7>
                    ++this.__stackPosition; // <8>
                }

                try {
                    var18.subWorkflow(var23, var26); // <9>
                } catch (Interrupt var3) { // <10>
                    throw var3; // <10>
                } catch (Throwable var4) { // <10>
                    this.__stack.pop(); // <10>
                    --this.__stackPosition; // <10>
                    throw var4; // <10>
                } // <10>

                this.__stack.pop();
                --this.__stackPosition;
                System.out.println("main end");
            }
        }
    }

    public void subWorkflow(int i1, long l) throws Interrupt {
        if (this.__stack.size() == this.__stackPosition) {
            System.out.println("subWorklow start");
            this.savepoint();
            this.__stack.push(new StackEntry(new Object[0], 0, new Object[]{this, i1, l, null}));
            ++this.__stackPosition;
            throw new Interrupt();
        } else {
            int var10000 = ((StackEntry)this.__stack.get(this.__stackPosition)).jumpNo;
            if (var10000 == 0) {
                Object[] var23 = ((StackEntry)this.__stack.get(this.__stackPosition)).locals;
                this = (SavepointsAndSubWorkflows)var23[0];
                i1 = (Integer)var23[1];
                l = (Long)var23[2];
                ++this.__stackPosition;
                this.__stack.pop();
                --this.__stackPosition;
                System.out.println("jumpNo=0");
                this.savepoint();
                this.__stack.push(new StackEntry(new Object[0], 1, new Object[]{this, i1, l, null}));
                ++this.__stackPosition;
                throw new Interrupt();
            } else if (var10000 == 1) {
                Object[] var22 = ((StackEntry)this.__stack.get(this.__stackPosition)).locals;
                this = (SavepointsAndSubWorkflows)var22[0];
                i1 = (Integer)var22[1];
                l = (Long)var22[2];
                ++this.__stackPosition;
                this.__stack.pop();
                --this.__stackPosition;
                System.out.println("jumpNo=1");
                this.savepoint();
                this.__stack.push(new StackEntry(new Object[0], 2, new Object[]{this, i1, l, null}));
                ++this.__stackPosition;
                throw new Interrupt();
            } else {
                SavepointsAndSubWorkflows var19;
                long[] var24;
                if (var10000 == 2) {
                    Object[] var17 = ((StackEntry)this.__stack.get(this.__stackPosition)).locals;
                    this = (SavepointsAndSubWorkflows)var17[0];
                    i1 = (Integer)var17[1];
                    l = (Long)var17[2];
                    ++this.__stackPosition;
                    this.__stack.pop();
                    --this.__stackPosition;
                    System.out.println("jumpNo=2");
                    this.__stack.push(new StackEntry(new Object[]{this, new long[]{777L, 888L, 999L}}, 3, new Object[]{this, i1, l, null}));
                    var17 = ((StackEntry)this.__stack.get(this.__stackPosition)).stack;
                    var19 = (SavepointsAndSubWorkflows)var17[0];
                    var24 = (long[])var17[1];
                    ++this.__stackPosition;
                } else {
                    if (var10000 != 3) {
                        throw new RuntimeException("No such label");
                    }

                    Object[] var20 = ((StackEntry)this.__stack.get(this.__stackPosition)).locals;
                    this = (SavepointsAndSubWorkflows)var20[0];
                    i1 = (Integer)var20[1];
                    l = (Long)var20[2];
                    var20 = ((StackEntry)this.__stack.get(this.__stackPosition)).stack;
                    var19 = (SavepointsAndSubWorkflows)var20[0];
                    var24 = (long[])var20[1];
                    ++this.__stackPosition;
                }

                try {
                    var19.subWorkflow2(var24);
                } catch (Interrupt var4) {
                    throw var4;
                } catch (Throwable var5) {
                    this.__stack.pop();
                    --this.__stackPosition;
                    throw var5;
                }

                this.__stack.pop();
                --this.__stackPosition;
                System.out.println("subWorklow end");
            }
        }
    }

    public void subWorkflow2(long[] l) throws Interrupt {
        if (this.__stack.size() == this.__stackPosition) {
            System.out.println("subWorklow2 start");
            this.savepoint();
            this.__stack.push(new StackEntry(new Object[0], 0, new Object[]{this, l}));
            ++this.__stackPosition;
            throw new Interrupt();
        } else if (((StackEntry)this.__stack.get(this.__stackPosition)).jumpNo == 0) {
            Object[] var10000 = ((StackEntry)this.__stack.get(this.__stackPosition)).locals;
            this = (SavepointsAndSubWorkflows)var10000[0];
            l = (long[])var10000[1];
            ++this.__stackPosition;
            this.__stack.pop();
            --this.__stackPosition;
            System.out.println("jumpNo=0");
            System.out.println("subWorklow2 end");
        } else {
            throw new RuntimeException("No such label");
        }
    }
}
