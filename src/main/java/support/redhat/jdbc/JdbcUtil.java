package support.redhat.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class JdbcUtil {
	private static final Logger log = Logger.getLogger(JdbcUtil.class.getCanonicalName());

	/**
	 * Obtain a connection, execute update, close the connection
	 */
	public static int executeUpdate(DataSource dataSource, String sql, boolean suppressException) throws SQLException {
		Connection connection = dataSource.getConnection();
		try {
			return executeUpdate(connection, sql, suppressException);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				log.log(Level.WARNING, "Connection.close() failed", e);
			}
		}
	}

	/**
	 * Execute update without closing the connection
	 */
	public static int executeUpdate(Connection connection, String sql, boolean suppressException) throws SQLException {
		Statement statement = connection.createStatement();
		try {
			return statement.executeUpdate(sql);
		} catch (SQLException e) {
			handleException(suppressException, e);
			return 0; // when exception suppressed
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				log.log(Level.WARNING, "Statement.close() failed", e);
			}
		}
	}

	/**
	 * Obtain a connection, execute query, convert to list of Object rows, close the connection
	 */
	public static List<List<Object>> executeQuery(DataSource datasource, String sql) throws SQLException {
		Connection connection = datasource.getConnection();
		try {
			return executeQuery(connection, sql);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				log.log(Level.WARNING, "Connection.close() failed", e);
			}
		}
	}

	/**
	 * Execute query and convert to list of Object rows without closing the connection
	 */
	public static List<List<Object>> executeQuery(Connection connection, String sql) throws SQLException {
		Statement statement = connection.createStatement();
		try {
			ResultSet rs = statement.executeQuery(sql);
			try {
				List<List<Object>> results = new ArrayList<List<Object>>();
				if (rs.next()) {
					int numColumns = rs.getMetaData().getColumnCount();
					while (true) {
						List<Object> row = new ArrayList<Object>();
						for (int i = 1; i <= numColumns; i++) {
							row.add(rs.getObject(i));
						}
						results.add(row);
						if (!rs.next()) {
							break;
						}
					}
				}
				return results;
			} finally {
				try {
					rs.close();
				} catch (SQLException e) {
					log.log(Level.WARNING, "ResultSet.close() failed", e);
				}
			}
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				log.log(Level.WARNING, "Statement.close() failed", e);
			}
		}
	}

	private static void handleException(boolean suppressException, SQLException e) throws SQLException {
		if (suppressException) {
			log.log(Level.FINER, e.getMessage(), e);
		} else {
			throw e;
		}
	}
}
