package control;

import java.sql.Connection;

import dao.BaseDao;
import model.QueriesStru;

public interface QueryRewrite {
	public String queryPath = "/Users/johnny/workplace/MSCProj/originQuery.sql"; // path of query
	public JdbcUtils jdbcUtils = new JdbcUtils();

	public BaseDao baseDao = new BaseDao();

	// return query sql
	public String rewrite(QueriesStru stru, Connection conn);
}
