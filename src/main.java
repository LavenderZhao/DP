import java.sql.Connection;

import control.SplitQueries;
import control.VIEW_Rewrite;
import dao.BaseDao;
import model.QueriesStru;

public class main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// NOTIN_Rewrite rewrite = new NOTIN_Rewrite();
		VIEW_Rewrite rewrite = new VIEW_Rewrite();
		SplitQueries splitQueries = new SplitQueries();
		QueriesStru stru = new QueriesStru();
		BaseDao dao = new BaseDao();
		Connection conn = dao.connectDB();
		stru = splitQueries.splitQuery();
		rewrite.rewrite(stru, conn);

	}

}
