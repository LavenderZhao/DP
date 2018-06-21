package control;

import java.util.ArrayList;

import dao.BaseDao;
import model.QueriesStru;

public class SplitQueries {

	public String queryPath = "/Users/qq/Documents/GitHub/DPMT/sql/query.sql"; // path of query
	public PostgreSQLJDBC post = new PostgreSQLJDBC();
	public QueriesStru stru = new QueriesStru();
	public BaseDao baseDao = new BaseDao();

	public QueriesStru splitQuery() {

		String sqlText; // store context of query
		String tmp;
		String[] tmp2;
		String[] splitsql;
		String[] split2;
		ArrayList<String> attlist = new ArrayList<>(); // store attribute required in the query
		ArrayList<String> tablelist = new ArrayList<>();
		sqlText = post.getText(queryPath).toLowerCase(); // get context of the query and turn all the text

		//// split the query and get all the attribute we want to select
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
		System.out.println(splitsql[0]);
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
			stru.setWhere(splitsql[1].trim());
		} else {
			stru.setWhere("null");
		}

		return stru;
	}

}
