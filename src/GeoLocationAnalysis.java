import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class GeoLocationAnalysis {
	private static int count=0;
	private static final int incremental=120;
	private static final String botFamily="blackenergy";
	private static BotnetParser parser;
	private static Connection con;
	//private static String botParseSuffix = "bots.xml";
	private static String cncParseSuffix = "cnc.xml";
	//private static String ddosParseSuffix = "ddos.xml";
	private static String from;
	private static String to;

	public static void main(String[] args) {
		if (args.length >= 2 && args.length <= 4) {
			if (args.length >= 3) {
				from = args[2];
			}
			if (args.length == 4) {
				to = args[3];
			}
			openDB();
			parseDocument(args[0], Integer.parseInt(args[1]));
		} else {
			System.out
					.println("Usage: GeoLocationAnalysis dir_path parse_mode [from] [to]\nparse_mode: 0(bots) 1(cnc) 2(ddos)");
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

	private static void parseDocument(String myDirectoryPath, int mode) {
		File dir = new File(myDirectoryPath);
		if (dir.isDirectory()) {
			int count=0;
			for (File child : dir.listFiles()) {
				if (isTargetDir(child) && count%120 ==0) {
					System.out.println(child.getName());
					for (File mChild : child.listFiles()) {
						if (isTargetFile(mChild, mode)) {
							generateReports(parseFile(mChild, mode), parser.getCreateTime());
						}
					}
				} else if (child.isFile() && isTargetFile(child, mode)) {
					generateReports(parseFile(child, mode), parser.getCreateTime());
				}
				count++;
			}
		}
	}

	private static boolean isTargetDir(File dir) {
		int dirNum = Integer.parseInt(dir.getName());
		if (dir.isDirectory()) {
			if (from != null && dirNum < Integer.parseInt(from)) {
				return false;
			}
			if (to != null && dirNum > Integer.parseInt(to)) {
				return false;
			}
			return true;
		}
		return false;
	}

	private static boolean isTargetFile(File file, int mode) {
		return file.getName().endsWith(cncParseSuffix) && file.getName().startsWith(botFamily);
	}

	private static List<?> parseFile(File file, int mode) {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser sp = spf.newSAXParser();
			parser = new BotnetParser();
			sp.parse(file, parser);
			return parser.getBotnetList();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		return null;
	}

	private static void generateReports(List<?> list, Date createTime) {
		if (list == null || list.size() == 0) {
			return;
		}

		Statement stmt;
		try {
			stmt = con.createStatement();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String sql = null;
			Calendar c = Calendar.getInstance();
			c.setTime(createTime);
			c.add(Calendar.DATE, -1);
			
			Calendar d = Calendar.getInstance();
			d.setTime(createTime);
			d.add(Calendar.DATE, 5);
			
			Calendar e = Calendar.getInstance();
			e.setTime(createTime);
			e.add(Calendar.DATE, 4);
			//String template = "select longitude, latitude from geoip inner join (select distinct ip from bots where timestamp<='{0}' and timestamp>'{1}' and family='{2}') T on geoip.ip=T.ip into outfile 'D:/result/{3}.csv' fields terminated by ' ';";
			String template = "select S.ip,S.longitude,S.latitude from (select geoip.ip, longitude, latitude from geoip inner join (select distinct ip from bots where timestamp<='{0}' and timestamp>'{1}' and family='{2}' and botnet_id='5C982DAB-E9C4-38B3-942E-AB46A559E814') T on geoip.ip=T.ip) S where S.ip not in (select geoip.ip from geoip inner join (select distinct ip from bots where timestamp<='{3}' and timestamp>'{4}' and family='{5}' and botnet_id='5C982DAB-E9C4-38B3-942E-AB46A559E814') T on geoip.ip=T.ip) into outfile 'D:/result/{6}.csv' fields terminated by ' ';";
			//sql = MessageFormat.format(template.replace("'", "''"), dateFormat.format(createTime), dateFormat.format(c.getTime()),botFamily, botFamily+ ++count);
			sql = MessageFormat.format(template.replace("'", "''"), dateFormat.format(createTime), dateFormat.format(c.getTime()),botFamily,dateFormat.format(d.getTime()), dateFormat.format(e.getTime()),botFamily, "another_more_test"+ ++count);
			stmt.executeQuery(sql);
			/*for (Object object : list) {
				Botnet botnet = (Botnet) object;
				if(!botnet.getFamily().equalsIgnoreCase("blackenergy")){
					continue;
				}
				// botnet_id,type,family,confidence,timestamp,last_active,ip,port,protocol,url,sha1,md5
				String id = botnet.getBotnetId();
				Calendar c = Calendar.getInstance();
				c.setTime(createTime);
				c.add(Calendar.DATE, -1);
				String template = "select longitude, latitude from geoip inner join (select distinct ip from bots where botnet_id='{0}' and timestamp<='{1}' and timestamp>'{2}' and family='{3}') T on geoip.ip=T.ip into outfile 'D://result//{4}.csv' fields terminated by ' ';";
				sql = MessageFormat.format(template.replace("'", "''"), id,dateFormat.format(createTime), dateFormat.format(c.getTime()),"blackenergy", id);
				stmt.executeQuery(sql);
			}*/
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
