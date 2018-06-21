package control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import dao.BaseDao;

public class Op {

	BaseDao baseDao = new BaseDao();

	/**
	 * Insert data into database
	 * 
	 * @param conn
	 * @param tableName
	 * @param data
	 */
	public void insertdata(Connection conn, String tableName, String data) {

		try {
			// generate insert sql
			int columnNumber = 0;
			String[] columnData = data.split("\\,");
			String sql = "insert into " + tableName + "(";
			ArrayList<String> columnNames = baseDao.getColumnNames(tableName, conn);

			for (String columnName : columnNames) {
				sql += columnName;
				columnNumber++;
			}
			sql += ")" + "values(";
			for (int i = 0; i < columnNumber; i++) {
				sql += "'" + columnData[i] + "',";
			}
			sql = sql.substring(0, sql.length() - 1); // remove the lase ','
			sql += ")";

			// operate sql
			PreparedStatement psql = conn.prepareStatement(sql);
			psql.executeUpdate(); // execute sql
			psql.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Insert successfully！" + "\n");
		}
	}

	/**
	 * update data
	 * 
	 * @param conn
	 */
	public static void updatedata(Connection conn) {
		try {
			PreparedStatement psql;
			psql = conn.prepareStatement("update emp set sal = ? where ename = ?");
			psql.setFloat(1, (float) 5000.0);
			psql.setString(2, "Mark");
			psql.executeUpdate();
			psql.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Modefy successfully！" + "\n");
		}
	}

	/**
	 * delete data
	 * 
	 * @param conn
	 */
	public static void deletedata(Connection conn) {
		try {
			PreparedStatement psql;
			psql = conn.prepareStatement("delete from emp where sal < ?");
			psql.setFloat(1, 3000.00F);
			psql.executeUpdate();
			psql.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Delete successfully！" + "\n");
		}

	}
}

// 1,book1
