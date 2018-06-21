package dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

public class BaseDao {

	public String dbName = "cqa";
	public String port = "5432";
	public String usrName = "postgres";
	public String psw = "";
	public String sqlPath = "/Users/qq/Documents/GitHub/DPMT/sql/example.sql";

	private static final String SQL = "SELECT * FROM ";

	/**
	 * connect to database
	 * 
	 * @return
	 */
	public Connection connectDB() {
		Connection c = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:" + port + "/" + dbName, usrName, psw);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");
		return c;
	}

	/**
	 * Close the database connection
	 * 
	 * @param conn
	 */
	public void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
				System.exit(0);
			}
		}
	}

	/**
	 * get all the table names in the database
	 */
	public ArrayList<String> getTableNames(Connection conn) {
		ArrayList<String> tableNames = new ArrayList<>();
		ResultSet rs = null;
		try {
			// Get the metadata of the database
			DatabaseMetaData db = conn.getMetaData();
			// Get all table names from metadata
			rs = db.getTables(null, null, null, new String[] { "TABLE" });
			while (rs.next()) {
				tableNames.add(rs.getString(3));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("getTableNames failure: " + e.getMessage());
			System.exit(0);
		}
		return tableNames;
	}

	/**
	 * get the name of column
	 * 
	 * @param tableName
	 * 
	 * @return
	 */
	public ArrayList<String> getColumnNames(String tableName, Connection conn) {
		ArrayList<String> columnNames = new ArrayList<>();

		PreparedStatement pStemt = null;
		String tableSql = SQL + tableName;
		try {
			pStemt = conn.prepareStatement(tableSql);

			ResultSetMetaData rsmd = pStemt.getMetaData();
			// number of columns
			int size = rsmd.getColumnCount();
			for (int i = 0; i < size; i++) {
				columnNames.add(rsmd.getColumnName(i + 1));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("getColumnNames failure: " + e.getMessage());
			System.exit(0);
		}
		return columnNames;
	}

	/**
	 * get type of column
	 * 
	 * @param tableName
	 * @return
	 */
	public ArrayList<String> getColumnTypes(String tableName, Connection conn) {
		ArrayList<String> columnTypes = new ArrayList<>();

		PreparedStatement pStemt = null;
		String tableSql = SQL + tableName;
		try {
			pStemt = conn.prepareStatement(tableSql);
			// Get the metadata of the database
			ResultSetMetaData rsmd = pStemt.getMetaData();
			// number of clumn in table
			int size = rsmd.getColumnCount();
			for (int i = 0; i < size; i++) {

				columnTypes.add(rsmd.getColumnTypeName(i + 1));

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("getColumnTypes failure: " + e.getMessage());
			System.exit(0);
		}
		return columnTypes;
	}

	/**
	 * get comments of column
	 * 
	 * @param tableName
	 * @return
	 */
	public ArrayList<String> getColumnComments(String tableName) {

		Connection conn = connectDB();
		PreparedStatement pStemt = null;
		String tableSql = SQL + tableName;
		ArrayList<String> columnComments = new ArrayList<>();
		ResultSet rs = null;
		try {
			pStemt = conn.prepareStatement(tableSql);
			rs = pStemt.executeQuery("show full columns from " + tableName);
			while (rs.next()) {
				columnComments.add(rs.getString("Comment"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
					closeConnection(conn);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("getColumnComments close ResultSet and connection failure: " + e.getMessage());
					System.exit(0);
				}
			}
		}
		return columnComments;
	}

	/**
	 * Get primary key
	 * 
	 * @throws SQLException
	 */
	public ArrayList<String> getPKey(String tableName, Connection conn) throws SQLException {
		ArrayList<String> Pkey = new ArrayList<>();
		// Get the metadata of the database
		DatabaseMetaData db = conn.getMetaData();
		ResultSet pkRSet = db.getPrimaryKeys(null, null, tableName);

		// get all the primary key of the table
		while (pkRSet.next()) {
			Pkey.add(pkRSet.getString("COLUMN_NAME"));
		}
		return Pkey;
	}
	/*
	 * public void main(String[] args) { Connection conn = connectDB();
	 * ArrayList<String> tableNames = getTableNames(conn);
	 * System.out.println("tableNames:" + tableNames); for (String tableName :
	 * tableNames) { System.out.println("ColumnNames:" + getColumnNames(tableName,
	 * conn)); System.out.println("ColumnTypes:" + getColumnTypes(tableName, conn));
	 * System.out.println("ColumnComments:" + getColumnComments(tableName)); } }
	 */
}
