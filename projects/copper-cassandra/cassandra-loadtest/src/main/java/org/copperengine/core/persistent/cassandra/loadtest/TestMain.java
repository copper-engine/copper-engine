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
package org.copperengine.core.persistent.cassandra.loadtest;

import java.util.Arrays;

import org.copperengine.core.persistent.cassandra.CassandraSessionManagerImpl;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class TestMain {

    public static void main(String[] args) {
        int counter = 0;
        CassandraSessionManagerImpl sessionManagerImpl = new CassandraSessionManagerImpl(Arrays.asList("nuc1.scoop-gmbh.de", "nuc2.scoop-gmbh.de"), null, "copper_red");
        sessionManagerImpl.startup();
        try {
            Session session = sessionManagerImpl.getSession();
            PreparedStatement stmt = session.prepare("SELECT ID FROM COP_WORKFLOW_INSTANCE");
            long startTS = System.currentTimeMillis();
            ResultSet rs = session.execute(stmt.bind().setConsistencyLevel(ConsistencyLevel.TWO).setFetchSize(20));
            Row row = null;
            while ((row = rs.one()) != null) {
                System.out.println(row.getString("ID"));
                counter++;
            }
            long et = System.currentTimeMillis() - startTS;
            System.out.println(et);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sessionManagerImpl.shutdown();
        System.out.println(counter);
    }

}
