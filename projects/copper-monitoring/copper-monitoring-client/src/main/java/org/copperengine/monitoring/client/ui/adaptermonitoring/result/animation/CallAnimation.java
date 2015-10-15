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
package org.copperengine.monitoring.client.ui.adaptermonitoring.result.animation;

import javafx.scene.paint.Color;

public class CallAnimation extends EventAnimationBase {

    public static final Color ADAPTER_CALL_COLOR = Color.CORAL;
    public int count = 1;

    public CallAnimation(AnimationPartParameter animationPartBaseParameter) {
        super(ADAPTER_CALL_COLOR, animationPartBaseParameter);
    }

    @Override
    public String getDisplayText() {
        if (count > 1) {
            return id + " " + count + "x";
        }
        return id;
    }

}
