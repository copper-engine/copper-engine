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

import javafx.scene.Node;

public abstract class AnimationPartBase {
    public final long startTime;
    public long endTime;
    public final String id;

    public final double startx;
    public final double starty;
    public final double endx;
    public final double endy;

    public AnimationPartBase(AnimationPartParameter parameterObject) {
        super();
        this.startTime = parameterObject.startTime;
        this.endTime = parameterObject.endTime;
        this.id = parameterObject.id;
        this.startx = parameterObject.startx;
        this.starty = parameterObject.starty;
        this.endx = parameterObject.endx;
        this.endy = parameterObject.endy;
    }

    public abstract Node createVisualRepresentation();
}