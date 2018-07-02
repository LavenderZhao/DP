package control;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.QueriesStru;

//create view of original query once before the loop and drop after the loop
//create and drop view of delete in each loop
public class VIEW_Rewrite implements QueryRewrite {

	public void createOquery(Connection conn) {

		String sql1 = "CREATE VIEW ORIGINAL_QUERY AS \n";
		sql1 += jdbcUtils.getText(queryPath); // the original query
		System.out.println(sql1);
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql1);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createDQuery(QueriesStru stru, Connection conn) {
		Statement stmt = null;
		String sql2 = "CREATE VIEW DEL_QUERY AS \n";
		sql2 += jdbcUtils.getText(queryPath);
		// replace the original "from ..." with _del table
		for (String tablename : stru.getTablelist()) {
			sql2 = sql2.replaceAll(tablename, tablename + "_del");
		}
		try {
			ResultSet rs = stmt.executeQuery(sql2);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sql2);
	}

	public String rewrite(QueriesStru stru, Connection conn) {

		String sql = "SELECT *\nFROM ORIGINAL_QUERY \nWHERE ";
		String subsql = "";

		if (stru.getWhere().equals("null")) {
			sql += "WHERE \n    (";
		} else {
			sql += stru.getWhere() + " AND (";
		}

		if (stru.getAtt().get(0).equals("*")) {
			for (String tablename : stru.getTablelist()) {
				for (String attname : baseDao.getColumnNames(tablename, conn)) {
					subsql += attname + ",";
				}
			}
		} else {
			for (String attname : stru.getAtt()) {
				subsql += attname + ", ";
			}
		}
		subsql = subsql.substring(0, subsql.length() - 1);
		sql += subsql + ") NOT EXISTS (SELECT * FROM DEL_QUERY)";
		System.out.println(sql);
		return sql;
	}

	public void dropView(Connection conn, String viewname) {

		String sql = "DROP VIEW " + viewname;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
