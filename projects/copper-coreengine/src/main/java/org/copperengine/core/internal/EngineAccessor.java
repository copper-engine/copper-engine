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
package org.copperengine.core.internal;

import org.copperengine.core.ProcessingState;
import org.copperengine.core.Workflow;
import org.copperengine.core.persistent.ErrorData;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.persistent.RegisterCall;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

public class EngineAccessor {

    private static final Method methodRegister;
    private static final Method methodUnregister;

    static {
        try {
            methodRegister = PersistentScottyEngine.class.getDeclaredMethod("register", Workflow.class);
            methodRegister.setAccessible(true);

            methodUnregister = PersistentScottyEngine.class.getDeclaredMethod("unregister", Workflow.class);
            methodUnregister.setAccessible(true);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static void register(PersistentScottyEngine engine, Workflow<?> w) {
        try {
            methodRegister.invoke(engine, w);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void unregister(PersistentScottyEngine engine, Workflow<?> w) {
        try {
            methodUnregister.invoke(engine, w);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
