/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.persistent.spring;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TestSpringTxnStuff {


	private static void doTransactional(DataSource ds, String sql) {
		Connection con = DataSourceUtils.getConnection(ds);
		try {
			Statement stmt = null;
			try {
				stmt = con.createStatement();
				stmt.execute(sql);
			}
			catch(SQLException e) {
				throw new RuntimeException(e);
			}
			finally {
				JdbcUtils.closeStatement(stmt);
			}
		}
		finally {
			DataSourceUtils.releaseConnection(con, ds);
		}
	}
	
	private static void doTransactional2(DataSource ds, String sql) {
		Connection con = DataSourceUtils.getConnection(ds);
		try {
			Statement stmt = null;
			try {
				stmt = con.createStatement();
				stmt.execute(sql);
			}
			catch(SQLException e) {
				throw new RuntimeException(e);
			}
			finally {
				JdbcUtils.closeStatement(stmt);
			}
		}
		finally {
			DataSourceUtils.releaseConnection(con, ds);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("context.xml", TestSpringTxnStuff.class);
		PlatformTransactionManager txnManager =ctx.getBean(PlatformTransactionManager.class);
		DataSource dataSource = ctx.getBean(DataSource.class);
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(dataSource);

		TransactionStatus status = txnManager.getTransaction(new DefaultTransactionDefinition());
		try {
			long id = 1;
			jdbcTemplate.execute("DELETE FROM COP_AUDIT_TRAIL_EVENT");
			System.out.println("wait #1");
			Thread.sleep(10000);
			doTransactional(dataSource, "INSERT INTO COP_AUDIT_TRAIL_EVENT (SEQ_ID, OCCURRENCE, CONVERSATION_ID, LOGLEVEL, CONTEXT, LONG_MESSAGE) VALUES ("+(id++)+", SYSTIMESTAMP, '123', 1, 'CTX', 'Hello World!')");
			doTransactional(dataSource, "INSERT INTO COP_AUDIT_TRAIL_EVENT (SEQ_ID, OCCURRENCE, CONVERSATION_ID, LOGLEVEL, CONTEXT, LONG_MESSAGE) VALUES ("+(id++)+", SYSTIMESTAMP, '123', 1, 'CTX', 'Hello World!')");
			System.out.println("wait #2");
			Thread.sleep(10000);
		}
		catch(Exception e) {
			txnManager.rollback(status);
			throw e;
		}
		txnManager.commit(status);
		System.out.println("wait #3");
		Thread.sleep(10000);
	}

}
