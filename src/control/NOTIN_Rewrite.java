package control;

import java.sql.Connection;

import model.QueriesStru;

/**
 * 
 * @author Sukei
 *
 */
public class NOTIN_Rewrite implements QueryRewrite {

	public String rewrite(QueriesStru stru, Connection conn) {

		String sql = post.getText(queryPath); // the original query
		String subsql = ""; // store the all attributes

		// if the original query does not contain constrains
		if (stru.getWhere().equals("null")) {
			sql += "WHERE \n    (";
		} else {
			sql += " AND (";
		}

		for (String attname : stru.getAtt()) {
			subsql += attname + ",";
		}
		subsql = subsql.substring(0, subsql.length() - 1);
		sql += subsql + ") \n    NOT IN \n    (SELECT " + subsql + " FROM ";
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
