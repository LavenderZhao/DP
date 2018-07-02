import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import control.ConstraintRewrite2;
import control.JdbcUtils;
import control.PostgreSQLJDBC6;
import dao.BaseDao;
import model.ConstraintStru2;
import model.QueriesStru;
import model.RandomMarkov;
import model.TableStru;

public class MainTest {

	private static String address = "localhost";
	public static String dbName = "cqa";
	public static String port = "5432";
	public static String usrName = "postgres";
	public static String psw = "123";
	public static String sqlPath = "/Users/johnny/workplace/MSCProj/originQuery.sql";
	private static String constraints = "reader(a,b,c,d,e,f),reader'(g,h,c,i,j,k) -: [ false |a=g]";//"borrow(a,b,c), borrow'(e,d,c) -: a=e,b=d";
	private float epsilon = 0.1f;
	private float theta = 0.01f;
	private static Connection c;
	private static PostgreSQLJDBC6 postgreSQLJDBC;
	private static HashMap<String, ArrayList> tableMap;
	private ConstraintRewrite2 constraintRewrite;
	private static JdbcUtils jdbcUtils = new JdbcUtils();

	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub

		postgreSQLJDBC = new PostgreSQLJDBC6();
		MainTest test = new MainTest();
		c = postgreSQLJDBC.connectDB(address, port, dbName, usrName, psw); // init the connection of the database
		tableMap = postgreSQLJDBC.getTableSchema(c); // the schema of the database
		String sql = "SELECT  *\n" +
				"FROM  reader\n" +
				"WHERE rid = '4' and lastname = 'lastname_910_';";
		test.sampleFramework(constraints.trim(),sql,0);
	}

	/********
	 *
	 * @param constraint
	 * @param
	 * @return
	 * @throws SQLException
	 *             find the violation tuples and regarding table, and save them in
	 *             the del_ table
	 */
	public ConstraintStru2 violationCheck(String constraint, int sequence) throws SQLException {

		constraintRewrite = new ConstraintRewrite2();

		/**********
		 * if has two "reader" need to write as "reader","reader'" ex.
		 * reader(firstname,lastname,rid,born,gender,phone),reader'(firstname,lastname,rid,born,gender,phone)
		 * -: [ false |reader.rid = reader'.rid ,reader.firstname = reader'.firtname]
		 * reader(a,b,c,d,e,f), reader'(g,h,c,i,j,k),.... -: [ false |a=g,...]
		 *********/
		// "reader(a,b,c,d,e,f),reader'(g,h,c,i,j,k) -: [ false |a=g]"
		constraintRewrite.parse(constraint);

		/*********
		 * check the constraint format
		 *********/
		if (!constraintRewrite.tbFormatCheck(tableMap)) {
			System.err.println("constraint table schema error");
			return null;
		}

		/*********
		 * constraint rewrite and get violation tuples
		 *********/

		// { ........ }
		// <"att1 att2 att3",ArrayList<tuples>>
		ArrayList<String> consTbLst = new ArrayList<>(); // all the regarding table in a constraint
		for (TableStru tbStru : constraintRewrite.getTableList()) {
			consTbLst.add(tbStru.getTableName());
		}
		// { ........ }
		String[] depSqlArray = constraintRewrite.rewrite(tableMap);
		// String[ sql , attName1,attName2...]
		ArrayList<HashMap> vioTupleMap = constraintRewrite.getVioTuples(depSqlArray, c, tableMap);

		// create deletion table with same structure and store them in the deletion
		// table

		/*
		 * String createDelTable = "CREATE TABLE del_" + depSqlArray[0] + sequence +
		 * " AS SELECT * FROM " + depSqlArray[0] + " WHERE 1=2;"; String createDelSql =
		 * constraintRewrite.createDeletionTableSql(depSqlArray[0],c,
		 * tableMap.get(depSqlArray[0]),vioTuples, sequence);
		 * postgreSQLJDBC.execute(c,createDelTable,false);
		 * postgreSQLJDBC.execute(c,createDelSql,false);
		 */

		ConstraintStru2 constraintStru = new ConstraintStru2(vioTupleMap, depSqlArray);
		// System.out.println(sql);

		return constraintStru;
	}

	public void sampleFramework(String constraint, String sql, int sequence) throws SQLException {
		BaseDao basedao = new BaseDao();
		int count = 0;
		Random random = new Random(System.currentTimeMillis());
		int m = (int) ((1 / (2 * epsilon)) * Math.log(2 / theta));
		ArrayList<String> tableNames = basedao.getTableNames(c);

		ConstraintStru2 constraintStru = violationCheck(constraint, sequence);

		try {
			// Run Row(SQL(theta)) for each constraint

			for (int i = 0; i < m; i++) {
				System.out.println("the " + i + " round!");

				ArrayList<TableStru> tableList = constraintRewrite.getTableList();

				RandomMarkov randomMarkov = new RandomMarkov(constraintStru, random, tableList, tableMap);

				// create delete table for each table
				jdbcUtils.createDeleteTbale(c, tableNames);

				// markov chain provide the tuples which to delete next
				while (randomMarkov.hasNext()) {

					HashMap tuple = randomMarkov.next();
					String tableName = (String) tuple.get("tableName");
					// reader_rid,reader_firstname ...reader'_rid
					jdbcUtils.updateTable(tuple, "del_" + tableName, c);
					// System.out.println(tuple);
				}

				jdbcUtils.CreateDeleteView(c, tableNames);
				QueriesStru stru = jdbcUtils.splitQuery(sql);
				boolean flag = jdbcUtils.queryRewrite(stru, c);

				if (flag) {
					count++;
				}
				System.out.println("count: " + count);
				jdbcUtils.DropDView(c, tableNames);
				jdbcUtils.DropDTable(c, tableNames);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(count + "/" + m);

	}

}
