/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package org.copperengine.core.persistent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.wfrepo.FileBasedWorkflowRepository;
import org.junit.Test;

public class StandardJavaSerializerCompatibilityTest {

    String readFile(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder(2048);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String x = br.readLine();
        while (x != null) {
            sb.append(x);
            x = br.readLine();
        }
        String rv = sb.toString();
        if ("null".equals(rv))
            return null;
        return rv;

    }

    @Test
    public void test() throws Exception {
        ZipInputStream zipIS = new ZipInputStream(getClass().getResourceAsStream("copper_2x_objects.zip"));

        StandardJavaSerializer standardJavaSerializer = new StandardJavaSerializer();

        FileBasedWorkflowRepository wfRepo = new FileBasedWorkflowRepository();
        wfRepo.setSourceDirs(Arrays.asList(new String[] { "src/workflow/java" }));
        wfRepo.setTargetDir("build/compiled_workflow");
        wfRepo.start();
        try {
            ZipEntry zipEntry;
            while ((zipEntry = zipIS.getNextEntry()) != null) {
                if (zipEntry.getName().indexOf("workflow_") != -1 && zipEntry.getName().endsWith(".state")) {
                    // System.out.println("Checking " + zipEntry.getName());
                    SerializedWorkflow sw = new SerializedWorkflow();
                    sw.setObjectState(readFile(zipIS));
                    // sw.setData(readFile(new File(directory, f.getName().replace(".state", ".data"))));
                    Workflow<?> wf = standardJavaSerializer.deserializeWorkflow(sw, wfRepo);
                    org.junit.Assert.assertNotNull(wf);
                }
                else if (zipEntry.getName().indexOf("response_") != -1) {
                    // System.out.println("Checking " + zipEntry.getName());
                    Response<?> r = standardJavaSerializer.deserializeResponse(readFile(zipIS));
                    org.junit.Assert.assertNotNull(r);
                }
            }
        } finally {
            wfRepo.shutdown();
            zipIS.close();
        }
    }
}
