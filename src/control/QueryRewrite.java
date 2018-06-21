package control;

import java.sql.Connection;

import dao.BaseDao;
import model.QueriesStru;

public interface QueryRewrite {
	public String queryPath = "/Users/qq/Documents/GitHub/DPMT/sql/query.sql"; // path of query
	public PostgreSQLJDBC post = new PostgreSQLJDBC();

	public BaseDao baseDao = new BaseDao();

	public String rewrite(QueriesStru stru, Connection conn);
}
