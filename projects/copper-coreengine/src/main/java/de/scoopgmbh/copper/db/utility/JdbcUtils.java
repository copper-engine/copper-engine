package de.scoopgmbh.copper.db.utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(JdbcUtils.class);

	/**
	 * Close the given JDBC Connection and ignore any thrown exception.
	 * This is useful for typical finally blocks in manual JDBC code.
	 */
	public static void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
			}
			catch (SQLException ex) {
				logger.debug("Could not close JDBC Connection", ex);
			}
			catch (Throwable ex) {
				logger.debug("Unexpected exception on closing JDBC Connection", ex);
			}
		}
	}

	public static void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			}
			catch (SQLException ex) {
				logger.debug("Could not close JDBC statement", ex);
			}
			catch (Throwable ex) {
				logger.debug("Unexpected exception on closing JDBC statement", ex);
			}
		}
		
	}

}
