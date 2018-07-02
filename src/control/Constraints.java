package control;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import dao.BaseDao;

public class Constraints {

	String constraints;

	public Constraints(String constraints) {

		this.constraints = constraints;
	}

	ArrayList<String> type = new ArrayList<String>(Arrays.asList("TGD", "EGD", "CD"));

	public void selectConTable() throws SQLException {

		BaseDao basedao = new BaseDao();

		// connect database
		Connection conn = basedao.connectDB();

		basedao.closeConnection(conn);

		Statement stmt = null;
		String sql = "select  name  from table  group  by name  having count(*)=1";
		ResultSet rs = stmt.executeQuery(sql);
	}
}
