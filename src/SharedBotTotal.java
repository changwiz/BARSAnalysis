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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;


public class SharedBotTotal {
	private static final int increment = 1;
	private static Connection con;
	public static final String PATH = "D:/SharedBots/";
	public static final int increment_unit = Calendar.HOUR;
	public static final String matlab_unit = "hours";
	public static final int total_count = 70 * 24;
	private static String from = "2013-01-07 00:00:00";
	private static String to = "2013-03-18 00:00:00";
	private static int numberOfIps;
	private static MatlabProxy proxy;

	public static void main(String[] args) throws MatlabConnectionException,
			MatlabInvocationException {
		if (args.length >= 0 && args.length <= 2) {
			if (args.length >= 1) {
				from = args[1];
			}
			if (args.length == 2) {
				to = args[2];
			}
			MatlabProxyFactory factory = new MatlabProxyFactory();
			proxy = factory.getProxy();
			openDB();
			generateReports();
			closeDB();
			proxy.disconnect();
		} else {
			System.out.println("Usage: SharedBotTotal [from] [to]");
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

	private static void generateReports() {

		Statement stmt;
		String sql;
		ResultSet rs;
		try {
			stmt = con.createStatement();
			File f = new File(PATH + "shared_bot_total.txt");
			List<String> unique_ips = new ArrayList<String>();
			List<Integer> number = new ArrayList<Integer>();
			String evalStatement = "";
			proxy.eval("path='" + PATH
					+ "';data=[];");
			if (f.exists()) {
				InputStream fileInput = new FileInputStream(f);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(fileInput, "utf-8"));
				String inputLine;
				while ((inputLine = reader.readLine()) != null) {
					unique_ips.add(inputLine);
					if((inputLine = reader.readLine()) != null){
						int num = Integer.parseInt(inputLine);
						number.add(num);
						evalStatement += " "+num;
					}
				}
				reader.close();
			} else {
				f.getParentFile().mkdir();
				f.createNewFile();
				FileWriter fw = new FileWriter(f.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				String template = "select ip, count(ip) as 'number' from (select distinct ip,botnet_id,family from bots) S group by ip";
				sql = template;
				rs = stmt.executeQuery(sql);
 
				while (rs.next()) {
					unique_ips.add(rs.getString("ip"));
					bw.write(rs.getString("ip") + "\n");
					number.add(rs.getInt("number"));
					bw.write(rs.getInt("number") + "\n");
					evalStatement += " "+rs.getInt("number");

				}
				bw.close();
				rs.close();
			}
			proxy.eval("data=[" + evalStatement + "];");
			
			
			f = new File(PATH + "shared_bot_total_across_family.txt");
			List<String> unique_ips1 = new ArrayList<String>();
			List<Integer> number1 = new ArrayList<Integer>();
			String evalStatement1 = "";
			proxy.eval("path='" + PATH
					+ "';data1=[];");
			if (f.exists()) {
				InputStream fileInput = new FileInputStream(f);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(fileInput, "utf-8"));
				String inputLine;
				while ((inputLine = reader.readLine()) != null) {
					unique_ips1.add(inputLine);
					if((inputLine = reader.readLine()) != null){
						int num = Integer.parseInt(inputLine);
						number1.add(num);
						evalStatement1 += " "+num;
					}
				}
				reader.close();
			} else {
				f.getParentFile().mkdir();
				f.createNewFile();
				FileWriter fw = new FileWriter(f.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				String template = "select ip, count(ip) as 'number' from (select distinct ip,family from bots) S group by ip";
				sql = template;
				rs = stmt.executeQuery(sql);
 
				while (rs.next()) {
					unique_ips1.add(rs.getString("ip"));
					bw.write(rs.getString("ip") + "\n");
					number1.add(rs.getInt("number"));
					bw.write(rs.getInt("number") + "\n");
					evalStatement1 += " "+rs.getInt("number");

				}
				bw.close();
				rs.close();
			}
			proxy.eval("data1=[" + evalStatement1 + "];");
			
			proxy.eval("run shared_bot_total.m;");
			stmt.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
