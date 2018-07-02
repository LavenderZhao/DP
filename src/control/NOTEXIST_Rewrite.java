package control;

import java.sql.Connection;

import model.QueriesStru;

public class NOTEXIST_Rewrite implements QueryRewrite {

	public String rewrite(QueriesStru stru, Connection conn) {

		String sql = jdbcUtils.getText(queryPath).trim(); // the original query
		String subsql = ""; // store the all attributes

		if (stru.getWhere().equals("null")) {
			sql += "WHERE \n    (";
		} else {
			String tmp = sql.substring(sql.length() - 1, sql.length());
			System.out.println("tmp:" + tmp);
			if (tmp.equals(";")) {
				sql = sql.substring(0, sql.length() - 1);
			}
			sql += " AND (";
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
		sql += subsql + ") NOT EXISTS (SELECT " + subsql + " FROM ";
		String tmp = stru.getFrom();
		// replace the original "from ..." with _del table
		for (String tablename : stru.getTablelist()) {
			tmp = tmp.replaceAll(tablename, tablename + "_del");
		}
		sql += tmp + ")";
		System.out.println(sql);
		return sql;
	}

}
