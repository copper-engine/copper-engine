/**
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
package org.copperengine.ext.persistent;

import java.util.Arrays;

import javax.sql.DataSource;

import org.copperengine.core.util.PojoDependencyInjector;

public class RdbmsEngineFactoryTestUsage {

    public void testUsage() {
        RdbmsEngineFactory<PojoDependencyInjector> engineFactory = new RdbmsEngineFactory<PojoDependencyInjector>(Arrays.asList("package.of.copper.workflow.classes")) {
            @Override
            protected PojoDependencyInjector createDependencyInjector() {
                return new PojoDependencyInjector();
            }

            @Override
            protected DataSource createDataSource() {
                return null;
            }
        };
        engineFactory.getEngine().startup();

    }

}
