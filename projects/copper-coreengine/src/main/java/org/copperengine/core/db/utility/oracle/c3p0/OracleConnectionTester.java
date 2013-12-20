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
package org.copperengine.core.db.utility.oracle.c3p0;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.mchange.v2.c3p0.ConnectionTester;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class OracleConnectionTester implements ConnectionTester {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("RV_RETURN_VALUE_IGNORED")
    public int activeCheckConnection(Connection con) {
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM DUAL");
            try {
                stmt.executeQuery();
            } finally {
                stmt.close();
            }
            return CONNECTION_IS_OKAY;
        } catch (Exception e) {
            return CONNECTION_IS_INVALID;
        }
    }

    public int statusOnException(Connection con, Throwable t) {
        return activeCheckConnection(con);
    }

}
