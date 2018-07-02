package control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import dao.BaseDao;
import model.QueriesStru;

public class JdbcUtils {

	public QueriesStru stru = new QueriesStru();
	public BaseDao baseDao = new BaseDao();


	// Get the context of file
	public String getText(String path) {
		File file = new File(path);
		if (!file.exists() || file.isDirectory()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		try {
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String temp = null;
			temp = br.readLine();
			while (temp != null) {
				if (temp.length() >= 2) {
					String str1 = temp.substring(0, 1);
					String str2 = temp.substring(0, 2);
					if (str1.equals("#") || str2.equals("--") || str2.equals("/*") || str2.equals("//")) {
						temp = br.readLine();
						continue;
					}
					sb.append(temp + "\r\n");
				}

				temp = br.readLine();
			}
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public QueriesStru splitQuery(String sqlText) {


		String tmp;
		String[] tmp2;
		String[] splitsql;
		String[] split2;
		ArrayList<String> attlist = new ArrayList<>(); // store attribute required in the query
		ArrayList<String> tablelist = new ArrayList<>();
		sqlText = sqlText.toLowerCase(); // get context of the query and turn all the text
		// split the query and get all the attribute we want to select

		splitsql = sqlText.split("from");
		stru.setSelect(splitsql[0].trim());
		split2 = splitsql[0].trim().split("\\s+", 2);
		tmp = split2[1];
		split2 = tmp.split(",");
		for (int i = 0; i < split2.length; i++) {
			if (!split2[i].isEmpty() && !split2[i].equals(" ")) {
				attlist.add(split2[i].trim());

			}
		}
		stru.setAtt(attlist);

		// Get all the table name
		splitsql = splitsql[1].split("where");
		// System.out.println(splitsql[0]);
		stru.setFrom(splitsql[0].trim());
		split2 = splitsql[0].trim().split(",");

		for (int i = 0; i < split2.length; i++) {
			// judge is it a conjunctive query
			if (split2[i].contains("join") && split2[i].contains("on")) {
				tmp2 = split2[i].trim().split("join");
				for (String tablesql : tmp2) {
					String[] tmp3 = tablesql.trim().split(" |\n");
					tablelist.add(tmp3[0].trim());
				}
			} else {
				tablelist.add(split2[i].trim());
			}
		}
		stru.setTablelist(tablelist);
		// set constrains
		// if query does not contain constrains, set where as "null"
		if (splitsql.length != 1) {
			tmp = splitsql[1].trim().substring(splitsql[1].trim().length() - 1, splitsql[1].trim().length());
			// System.out.println("tmp:" + tmp);
			if (tmp.equals(";")) {
				stru.setWhere(splitsql[1].trim().substring(0, splitsql[1].trim().length() - 1));
			} else {
				stru.setWhere(splitsql[1].trim());
			}

		} else {
			stru.setWhere("null");
		}

		return stru;
	}

	/**
	 * Create a new View of table which not include the deleted tuples
	 */
	public void CreateDeleteView(Connection conn, ArrayList<String> tableNames) {

		for (String tableName : tableNames) {
			String sql = "CREATE VIEW NEW_" + tableName + " AS \nSELECT * \nFROM " + tableName + "\nWHERE (";
			ArrayList<String> columnNames = baseDao.getColumnNames(tableName, conn);
			for (String columName : columnNames) {
				sql += columName + ", ";
			}
			sql = sql.substring(0, sql.length() - 2);
			sql += ") NOT IN (SELECT * FROM del_" + tableName + ");";
			// System.out.println(sql);
			baseDao.executeSQL(sql, conn);
		}

	}

	public void DropDView(Connection conn, ArrayList<String> tableNames) {
		String sql = null;
		for (String tableName : tableNames) {
			// System.out.println(tableName);
			sql = "DROP VIEW NEW_" + tableName + ";";
			baseDao.executeSQL(sql, conn);
			// System.out.println(sql);
		}

	}

	public void DropDTable(Connection conn, ArrayList<String> tableNames) {

		for (String tableName : tableNames) {
			if (baseDao.validateTableNameExist("del_" + tableName, conn)) {
				String sql = "DROP TABLE del_" + tableName + ";";
				// System.out.println(sql);
				baseDao.executeSQL(sql, conn);
			}

		}

	}

	/**
	 * Create new delete table for each table
	 * 
	 * @param conn
	 */
	public void createDeleteTbale(Connection conn, ArrayList<String> tableNames) {

		for (String tableName : tableNames) {
			// System.out.println(tableName);
			String judge = tableName.substring(0, 3);
			if (!judge.equals("del")) {
				/*
				 * if (baseDao.validateTableNameExist("del_" + tableName, conn)) { String sql =
				 * "DROP TABLE del_" + tableName + ";"; baseDao.executeSQL(sql, conn); }
				 */
				String sql = "CREATE TABLE del_" + tableName + "(";
				ArrayList<String> columnNames = baseDao.getColumnNames(tableName, conn);
				ArrayList<String> columnTypes = baseDao.getColumnTypes(tableName, conn);
				int i = 0;
				for (String columnName : columnNames) {
					String columnType = columnTypes.get(i++);
					sql += columnName + " " + columnType + ",";
				}
				sql = sql.substring(0, sql.length() - 1);
				sql += ");";
				// System.out.println(sql);
				baseDao.executeSQL(sql, conn);

			}

		}

	}

	/**
	 * Add new tuple to deleted into del_table
	 * 
	 * @param tuple
	 * @param tableName
	 * @param conn
	 */
	public void updateTable(HashMap tuple, String tableName, Connection conn) {

		String data = "";
		ArrayList<String> columnNames = baseDao.getColumnNames(tableName, conn);
		for (String columnName : columnNames) {
			data += " '" + tuple.get(columnName) + "', ";
		}
		data = data.substring(0, data.length() - 2);
		// System.out.println("data:" + data);
		baseDao.insertdata(conn, tableName, data);
	}

	public boolean queryRewrite(QueriesStru stru, Connection conn) {

		Statement stmt = null;
		Boolean bool = false;
		ResultSet rs = null;

		String sql = stru.getSelect() + "\nFROM";
		ArrayList<String> tablelist = stru.getTablelist();
		for (String tableName : tablelist) {
			sql += " NEW_" + tableName + ",";
		}
		sql = sql.substring(0, sql.length() - 1);
		sql += "\nWHERE " + stru.getWhere();
		System.out.println(sql);
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				bool = true;
				System.out.println(rs.getString(1) + "\t" + rs.getString(2));
				// System.out.println(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bool;
	}

}
