import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

public class ActiveBotFamily {
	private static final int increment = 1;
	private static Connection con;
	// private static String botParseSuffix = "bots.xml";
	// private static String cncParseSuffix = "cnc.xml";
	// private static String ddosParseSuffix = "ddos.xml";
	public static final String PATH = "D:/ActiveBots/";
	public static final int increment_unit = Calendar.DATE;
	public static final String matlab_unit = "hours";
	public static final int total_count = 70;
	private static String from = "2013-01-07 00:00:00";
	private static String to = "2013-03-18 00:00:00";
	private static String family;
	private static MatlabProxy proxy;
	// private static String[] families=
	// {"aldibot","armageddon","asprox","blackenergy","colddeath","conficker","darkcomet","darkshell","ddoser","dirtjumper","gumblar","illusion","myloader","nitol","optima","pandora","redgirl","storm","tdss","torpig","waledac","yzf","zeus"};
	private static String[] families = {"illusion", "optima", "pandora" };

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
			System.out.println("Usage: ActiveBotAnalysis family [from] [to]");
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
			File f = new File(PATH + family + "/" + family + ".txt");
			List<String> botnet_ids = new ArrayList<String>();
			if (f.exists()) {
				InputStream fileInput = new FileInputStream(f);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(fileInput, "utf-8"));
				String inputLine;
				while ((inputLine = reader.readLine()) != null) {
					botnet_ids.add(inputLine);
				}
				reader.close();
			} else {
				f.getParentFile().mkdir();
				f.createNewFile();
				FileWriter fw = new FileWriter(f.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				String template = "select distinct botnet_id from cnc where family='{0}'";
				sql = MessageFormat.format(template.replace("'", "''"), family);
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					botnet_ids.add(rs.getString("botnet_id"));
					bw.write(rs.getString("botnet_id") + "\n");
				}
				bw.close();
				rs.close();
			}
			if(family.equalsIgnoreCase("conficker")){
				botnet_ids.add("sinkhole");
			}
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Calendar c = Calendar.getInstance();
			proxy.eval("matlab_unit='" + matlab_unit + "';path='" + PATH
					+ "';data=[];botfamily='"
					+ family + "';");
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
				String template = "select count(distinct ip) as 'number' from bots where family='{0}' and timestamp<='{1}' and timestamp>'{2}'";
				sql = MessageFormat.format(template.replace("'", "''"), family,
						dateFormat.format(end), dateFormat.format(start));
				rs = stmt.executeQuery(sql);
				if (rs.next()) {
					proxy.eval("data(end+1)=" + rs.getInt("number") + ";");
				} else {
					proxy.eval("data(end+1)=0;");
				}
				rs.close();
			}
			proxy.eval("run bot_active_family.m;");
			stmt.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
