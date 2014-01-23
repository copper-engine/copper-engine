/*
 * Copyright 2002-2014 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.copperengine.monitoring.client.form;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ShowFormsStrategyTest {
    @Test
    public void testBack() throws Exception {
        ShowFormsStrategy howFormsStrategy = new ShowFormsStrategy<Node>(new Pane()){
            @Override
            protected void showImpl(Form<?> form) {
                //do nothing
            }

            @Override
            protected void closeImpl(Form<?> form) {
              //do nothing
            }
        };

        Form<?> form1 = Mockito.mock(Form.class);
        Form<?> form2 = Mockito.mock(Form.class);
        Form<?> form3 = Mockito.mock(Form.class);
        Form<?> form4 = Mockito.mock(Form.class);

        howFormsStrategy.show(form1);
        howFormsStrategy.show(form2);
        howFormsStrategy.show(form3);
        howFormsStrategy.show(form4);

        howFormsStrategy.back();
        Assert.assertEquals(form3,howFormsStrategy.getCurrentForm());
        howFormsStrategy.back();
        Assert.assertEquals(form2,howFormsStrategy.getCurrentForm());
        howFormsStrategy.back();
        Assert.assertEquals(form1,howFormsStrategy.getCurrentForm());
        howFormsStrategy.back();
        Assert.assertEquals(form1,howFormsStrategy.getCurrentForm());
    }

    @Test
    public void testBack_limit() throws Exception {
        ShowFormsStrategy howFormsStrategy = new ShowFormsStrategy<Node>(new Pane()){
            @Override
            protected void showImpl(Form<?> form) {
                //do nothing
            }

            @Override
            protected void closeImpl(Form<?> form) {
                //do nothing
            }
        };

        for (int i=0;i<ShowFormsStrategy.HISTORY_LIMIT+5;i++ ){
            howFormsStrategy.show( Mockito.mock(Form.class));
        }
        final Form previous = Mockito.mock(Form.class);
        howFormsStrategy.show(previous);
        howFormsStrategy.show(Mockito.mock(Form.class));
        Assert.assertEquals(ShowFormsStrategy.HISTORY_LIMIT,howFormsStrategy.previous.size());
        howFormsStrategy.back();
        Assert.assertEquals(previous,howFormsStrategy.getCurrentForm());
    }


    @Test
    public void testForward() throws Exception {
        ShowFormsStrategy howFormsStrategy = new ShowFormsStrategy<Node>(new Pane()){
            @Override
            protected void showImpl(Form<?> form) {
                //do nothing
            }

            @Override
            protected void closeImpl(Form<?> form) {
                //do nothing
            }
        };

        Form<?> form1 = Mockito.mock(Form.class);
        Form<?> form2 = Mockito.mock(Form.class);
        Form<?> form3 = Mockito.mock(Form.class);
        Form<?> form4 = Mockito.mock(Form.class);

        howFormsStrategy.show(form1);
        howFormsStrategy.show(form2);
        howFormsStrategy.show(form3);
        howFormsStrategy.show(form4);

        howFormsStrategy.back();
        Assert.assertEquals(form3, howFormsStrategy.getCurrentForm());
        howFormsStrategy.back();
        Assert.assertEquals(form2, howFormsStrategy.getCurrentForm());
        howFormsStrategy.forward();
        Assert.assertEquals(form3,howFormsStrategy.getCurrentForm());
        howFormsStrategy.forward();
        Assert.assertEquals(form4,howFormsStrategy.getCurrentForm());
    }
}
