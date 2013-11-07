import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

public class SharedBotFamilyOverTime5 {
	private static final int increment = 7; // day
	private static Connection con;
	public static final String PATH = "D:/SharedBots/";
	public static final int increment_unit = Calendar.DATE;
	public static final String matlab_unit = "hours";
	public static final int total_count = 70;
	private static String from = "2013-01-07 00:00:00";
	private static String to = "2013-03-18 00:00:00";
	private static MatlabProxy proxy;
	private static String family;
	private static String[] families = { "optima", "pandora" };

	public static void main(String[] args) throws MatlabConnectionException,
			MatlabInvocationException {
		if (args.length >= 1 && args.length <= 3) {
			family = args[0];
			if (args.length >= 2) {
				from = args[1];
			}
			if (args.length == 3) {
				to = args[2];
			}
			MatlabProxyFactory factory = new MatlabProxyFactory();
			proxy = factory.getProxy();
			openDB();
			if (family.equalsIgnoreCase("all")) {
				for (String name : families) {
					generateReports(name);
				}
			} else {
				generateReports(family);
			}
			closeDB();
			proxy.disconnect();
		} else {
			System.out.println("Usage: SharedBotAnalysis family [from] [to]");
		}
	}

	private static void openDB() {
		closeDB();
		try {

			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/bars";
			con = DriverManager.getConnection(url, "root", "");
			System.out.println("URL: " + url);
		} catch (Exception e) {
			e.printStackTrace();
		}// end catch
	}

	private static void closeDB() {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void generateReports(String family) {

		Statement stmt;
		String sql;
		ResultSet rs;
		try {
			stmt = con.createStatement();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Calendar c = Calendar.getInstance();
			proxy.eval("path='" + PATH + "';data=[];family='" + family
					+ "';");
			for (int count = 0; count <= total_count / increment; count++) {
				try {
					c.setTime(dateFormat.parse(from));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				c.add(increment_unit, increment * count);
				Date end = c.getTime();
				c.add(increment_unit, -increment);
				Date start = c.getTime();
				String template = "select ip, count(ip) as 'number' from (select distinct ip,botnet_id,family from bots where family='{0}' and timestamp<='{1}' and timestamp>'{2}') S group by ip ";
				sql = MessageFormat.format(template.replace("'", "''"), family,
						dateFormat.format(end), dateFormat.format(start));
				rs = stmt.executeQuery(sql);
				int shared2 = 0, shared3=0,shared4=0,shared5=0;
				while (rs.next()) {
					int num = rs.getInt("number");
					if (num == 2) {
						shared2++;
					} else if (num ==3) {
						shared3++;
					} else if(num ==4){
						shared4++;
					}else if(num>=5){
						shared5++;
					}
				}
				proxy.eval("data=[data;[" + shared2 + " " + shared3 + " "+ shared4 + " " + shared5+"]];");
				rs.close();
			}
			proxy.eval("run shared_bot_family_time_5.m;");

			stmt.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
