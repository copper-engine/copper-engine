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
package org.copperengine.core.test;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.copperengine.core.common.IdFactory;
import org.copperengine.core.common.JdkRandomUUIDFactory;
import org.copperengine.core.db.utility.RetryingTransaction;
import org.copperengine.core.monitoring.NullRuntimeStatisticsCollector;
import org.copperengine.core.monitoring.StmtStatistic;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class TestDataCreator {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final IdFactory idFactory = new JdkRandomUUIDFactory();
        final ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass("oracle.jdbc.OracleDriver");
        dataSource.setJdbcUrl("jdbc:oracle:thin:COPPER2/COPPER2@localhost:1521:orcl11g");
        dataSource.setMinPoolSize(1);
        dataSource.setMaxPoolSize(1);

        PrintStream ps = System.out; // new PrintStream(new File("C:\\perf-test-results.log"));

        // test(dataSource, idFactory, createByteArray(16), createByteArray(256), ps);
        // test(dataSource, idFactory, createByteArray(16), createByteArray(256), ps);
        // test(dataSource, idFactory, createByteArray(16), createByteArray(256), ps);
        //
        // test(dataSource, idFactory, createByteArray(256), createByteArray(256), ps);
        // test(dataSource, idFactory, createByteArray(256), createByteArray(256), ps);
        // test(dataSource, idFactory, createByteArray(256), createByteArray(256), ps);
        //
        // test(dataSource, idFactory, createByteArray(1024), createByteArray(256), ps);
        // test(dataSource, idFactory, createByteArray(1024), createByteArray(256), ps);
        // test(dataSource, idFactory, createByteArray(1024), createByteArray(256), ps);
        //
        // test(dataSource, idFactory, createByteArray(2000), createByteArray(256), ps);
        // test(dataSource, idFactory, createByteArray(2000), createByteArray(256), ps);
        // test(dataSource, idFactory, createByteArray(2000), createByteArray(256), ps);

        test(dataSource, idFactory, createByteArray(2000), createByteArray(256), createString(16), createString(16), ps);
        test(dataSource, idFactory, createByteArray(2000), createByteArray(256), createString(16), createString(16), ps);
        test(dataSource, idFactory, createByteArray(2000), createByteArray(256), createString(16), createString(16), ps);

        test(dataSource, idFactory, createByteArray(2000), createByteArray(256), createString(256), createString(256), ps);
        test(dataSource, idFactory, createByteArray(2000), createByteArray(256), createString(256), createString(256), ps);
        test(dataSource, idFactory, createByteArray(2000), createByteArray(256), createString(256), createString(256), ps);

        test(dataSource, idFactory, createByteArray(2000), createByteArray(256), createString(1024), createString(256), ps);
        test(dataSource, idFactory, createByteArray(2000), createByteArray(256), createString(1024), createString(256), ps);
        test(dataSource, idFactory, createByteArray(2000), createByteArray(256), createString(1024), createString(256), ps);

        test(dataSource, idFactory, createByteArray(2000), createByteArray(256), createString(2048), createString(256), ps);
        test(dataSource, idFactory, createByteArray(2000), createByteArray(256), createString(2048), createString(256), ps);
        test(dataSource, idFactory, createByteArray(2000), createByteArray(256), createString(2048), createString(256), ps);

        ps.close();
    }

    private static byte[] createByteArray(int size) {
        final byte[] data = new byte[size];
        for (int i = 0; i < data.length; i++) {
            data[i] = 64;
        }
        return data;
    }

    private static String createString(int size) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(i % 10);
        }
        // System.out.println(sb.toString().length());
        return sb.toString();
    }

    private static void test(final DataSource dataSource, final IdFactory idFactory, final byte[] data, final byte[] response, final String data_s, final String response_s, final PrintStream ps) throws InterruptedException, Exception {
        Thread.sleep(30000);
        final StmtStatistic a = new StmtStatistic("INSERT INTO BUSI", new NullRuntimeStatisticsCollector());
        final StmtStatistic b = new StmtStatistic("INSERT INTO WAIT", new NullRuntimeStatisticsCollector());
        final StmtStatistic c = new StmtStatistic("INSERT INTO RESP", new NullRuntimeStatisticsCollector());
        for (int i = 0; i < 5; i++) {
            final List<String> ids = new ArrayList<String>(100000);
            final List<String> cids = new ArrayList<String>(100000);
            new RetryingTransaction<Void>(dataSource) {
                @Override
                protected Void execute() throws Exception {
                    PreparedStatement stmtBP = getConnection().prepareStatement("INSERT INTO COP_WORKFLOW_INSTANCE (ID,STATE,PRIORITY,LAST_MOD_TS,PPOOL_ID,DATA_S) VALUES (?,2,5,SYSTIMESTAMP,'P#DEFAULT',?)");
                    for (int k = 0; k < 100; k++) {
                        for (int i = 0; i < 200; i++) {
                            String id = idFactory.createId();
                            ids.add(id);
                            stmtBP.setString(1, id);
                            // stmtBP.setBytes(2, data);
                            stmtBP.setString(2, data_s);
                            stmtBP.addBatch();
                        }
                        a.start();
                        stmtBP.executeBatch();
                        getConnection().commit();
                        a.stop(200);
                        stmtBP.clearBatch();
                    }

                    PreparedStatement stmtWAIT = getConnection().prepareStatement("INSERT INTO COP_WAIT (CORRELATION_ID, WORKFLOW_INSTANCE_ID, CS_WAITMODE, COUNT) VALUES (?,?,0,1)");
                    for (int k = 0; k < 100; k++) {
                        for (int i = 0; i < 200; i++) {
                            String bp_id = ids.remove(ids.size() - 1);
                            String c_id = idFactory.createId();
                            cids.add(c_id);
                            stmtWAIT.setString(1, c_id);
                            stmtWAIT.setString(2, bp_id);
                            stmtWAIT.addBatch();
                        }
                        b.start();
                        stmtWAIT.executeBatch();
                        getConnection().commit();
                        b.stop(200);
                        stmtWAIT.clearBatch();
                    }

                    PreparedStatement stmtRES = getConnection().prepareStatement("INSERT INTO COP_RESPONSE (CORRELATION_ID, RESPONSE_TS, RESPONSE_S) VALUES (?,SYSTIMESTAMP,?)");
                    for (int k = 0; k < 25; k++) {
                        for (int i = 0; i < 200; i++) {
                            String c_id = cids.remove(cids.size() - 1);
                            stmtRES.setString(1, c_id);
                            // stmtRES.setBytes(2, response);
                            stmtRES.setString(2, response_s);
                            stmtRES.addBatch();
                        }
                        c.start();
                        stmtRES.executeBatch();
                        getConnection().commit();
                        c.stop(200);
                        stmtRES.clearBatch();
                    }

                    return null;
                }
            }.run();
        }
        ps.println("Proliant, String, data.length=" + data_s.length() + ", response.length=" + response_s.length());
        ps.println();
    }

}
