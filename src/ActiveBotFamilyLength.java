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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

public class ActiveBotFamilyLength {
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
			File f = new File(PATH + family + "/" + family + "_length.txt");
			HashMap<String, List<Date>> bots = new HashMap<String, List<Date>>();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			if (f.exists()) {
				InputStream fileInput = new FileInputStream(f);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(fileInput, "utf-8"));
				String inputLine;
				while ((inputLine = reader.readLine()) != null) {
					String ip = inputLine;
					List<Date> dates = bots.get(ip);
					if(dates == null){
						dates = new ArrayList<Date>();
						bots.put(ip, dates);
					}
					if((inputLine = reader.readLine()) != null){
						dates.add(dateFormat.parse(inputLine));
					}
				}
				reader.close();
			} else {
				f.getParentFile().mkdir();
				f.createNewFile();
				FileWriter fw = new FileWriter(f.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				String template = "select distinct ip, timestamp from bots where family='{0}'";
				sql = MessageFormat.format(template.replace("'", "''"), family);
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					String ip = rs.getString("ip");
					bw.write(ip + "\n");
					String date = rs.getString("timestamp");
					bw.write(date+ "\n");
					List<Date> dates = bots.get(ip);
					if(dates == null){
						dates = new ArrayList<Date>();
						bots.put(ip, dates);
					}
					dates.add(dateFormat.parse(date));
					}
				bw.close();
				rs.close();
				}
				
			proxy.eval("path='" + PATH
					+ "';data=[];family='"
					+ family + "';");
			Calendar c = Calendar.getInstance();
			for(String ip: bots.keySet()){
				List<Date> time = bots.get(ip);
				if(time.size() ==1){
					proxy.eval("data(end+1)=1;");
				} else {
					Date lastdate = time.get(0);
					Date newdate;
					long count = 1;
					long diff = getDateDiff(time.get(0),time.get(time.size()-1),TimeUnit.DAYS);
					if(diff>1){
						count = diff;
					}
					proxy.equals("data(end+1)="+count+";");
	
				}
			}
			//proxy.eval("run bot_active_family.m;");
			stmt.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
}
