import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BARSParser extends DefaultHandler {
	private static int BOT_PARSE = 0;
	private static int CNC_PARSE = 1;
	private static int DDOS_PARSE = 2;

	private static Connection con;
	private static String botParseSuffix = "bots.xml";
	private static String cncParseSuffix = "cnc.xml";
	private static String ddosParseSuffix = "ddos.xml";
	private static String from;
	private static String to;
	private static String createBotTable = "create table bots(botnet_id VARCHAR(255) NOT NULL,ip VARCHAR(255) NOT NULL,family VARCHAR(255) NOT NULL,confidence INT(3) NOT NULL,timestamp DATETIME NOT NULL,addtime DATETIME NOT NULL,PRIMARY KEY(botnet_id,ip,timestamp));";
	private static String createGeoIPTable = "create table geoip(ip VARCHAR(255) NOT NULL,asname VARCHAR(255) NOT NULL,asn INT(11) NOT NULL,longitude DOUBLE(11,8) NOT NULL,latitude DOUBLE(11,8) NOT NULL,city VARCHAR(255) NOT NULL,cc VARCHAR(255) NOT NULL,PRIMARY KEY(ip));";
	private static String createCncTable = "create table cnc(botnet_id VARCHAR(255) NOT NULL,type VARCHAR(255) NOT NULL,family VARCHAR(255) NOT NULL,confidence INT(3) NOT NULL,timestamp DATETIME NOT NULL,last_active DATETIME NOT NULL,ip VARCHAR(255) NOT NULL, port VARCHAR(255) NOT NULL, protocol INT(4),url VARCHAR(255), sha1 VARCHAR(255), md5 VARCHAR(255), PRIMARY KEY(botnet_id,last_active,sha1));";
	private static String createDDoSTable = "create table ddos(id VARCHAR(255) NOT NULL,targetip VARCHAR(255) NOT NULL,botnet_id VARCHAR(255) NOT NULL, family VARCHAR(255),confidence INT(3) NOT NULL,ongoing INT(1),category VACHAR(255), timestamp DATETIME NOT NULL,addtime DATETIME NOT NULL,endtime DATETIME, PRIMARY KEY(id,family,timestamp));";
	private static String insertBot = "INSERT IGNORE INTO bots (botnet_id,ip,family,confidence,timestamp,addtime) "
			+ "values ('{0}', '{1}', '{2}','{3}', '{4}', '{5}');";
	private static String insertGeoIp = "INSERT IGNORE INTO geoip (ip,asname,asn,longitude,latitude,city,cc) "
			+ "values ('{0}', '{1}', '{2}','{3}', '{4}', '{5}', '{6}');";
	private static String insertBotnet = "INSERT IGNORE INTO cnc (botnet_id,type,family,confidence,timestamp,last_active,ip,port,protocol,url,sha1,md5) "
			+ "values ('{0}', '{1}', '{2}','{3}', '{4}', '{5}', '{6}', '{7}', '{8}', '{9}', '{10}', '{11}');";
	private static String insertDDoS = "INSERT IGNORE INTO ddos (id, targetip, botnet_id, family, confidence, ongoing, category, timestamp, addtime, endtime) "
			+ "values ('{0}', '{1}', '{2}','{3}', '{4}', '{5}', '{6}', '{7}', '{8}', '{9}');";
	private static String replaceDDoS = "REPLACE INTO ddos (id, targetip, botnet_id, family, confidence, ongoing, category, timestamp, addtime, endtime) "
			+ "values ('{0}', '{1}', '{2}','{3}', '{4}', '{5}', '{6}', '{7}', '{8}', '{9}');";

	/**
	 * @param args
	 */
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
					.println("Usage: BARSParser dir_path parse_mode [from] [to]\nparse_mode: 0(bots) 1(cnc) 2(ddos)");
		}
	}

	private static void parseDocument(String myDirectoryPath, int mode) {
		File dir = new File(myDirectoryPath);
		if (dir.isDirectory()) {
			for (File child : dir.listFiles()) {
				if (isTargetDir(child)) {
					System.out.println(child.getName());
					for (File mChild : child.listFiles()) {
						if (isTargetFile(mChild, mode)) {
							insert(parseFile(mChild, mode));
						}
					}
				} else if (child.isFile() && isTargetFile(child, mode)) {
					insert(parseFile(child, mode));
				}
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
		if (mode == BOT_PARSE) {
			return file.getName().endsWith(botParseSuffix);
		} else if (mode == CNC_PARSE) {
			return file.getName().endsWith(cncParseSuffix);
		} else if (mode == DDOS_PARSE) {
			return file.getName().endsWith(ddosParseSuffix);
		}
		return false;
	}

	private static List<?> parseFile(File file, int mode) {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser sp = spf.newSAXParser();
			if (mode == BOT_PARSE) {
				BotParser parser = new BotParser();
				sp.parse(file, parser);
				return parser.getBotList();
			} else if (mode == CNC_PARSE) {
				BotnetParser parser = new BotnetParser();
				sp.parse(file, parser);
				return parser.getBotnetList();
			} else if (mode == DDOS_PARSE) {
				String fileName = file.getName();
				DDoSParser parser = new DDoSParser(fileName.substring(0, fileName.length() - ddosParseSuffix.length() - 1));
				sp.parse(file, parser);
				return parser.getDDoSList();
			}
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		return null;
	}

	private static void openDB() {
		closeDB();
		try {

			// Register the JDBC driver for MySQL.
			Class.forName("com.mysql.jdbc.Driver");

			// Define URL of database server for
			// database named mysql on the localhost
			// with the default port number 3306.
			String url = "jdbc:mysql://localhost:3306/bars";

			// Get a connection to the database for a
			// user named root with a blank password.
			// This user is the default administrator
			// having full privileges to do anything.
			con = DriverManager.getConnection(url, "root", "");

			// Display URL and connection information
			System.out.println("URL: " + url);
			System.out.println("Connection: " + con);
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

	private static void insert(List<?> list) {
		int mode = -1;
		if (list == null || list.size() == 0) {
			return;
		} else if (list.get(0) instanceof Bot) {
			mode = BOT_PARSE;
		} else if (list.get(0) instanceof Botnet) {
			mode = CNC_PARSE;
		}
		Statement stmt;
		try {
			stmt = con.createStatement();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String sql = null;
			for (Object object : list) {
				if (mode == BOT_PARSE) {
					Bot bot = (Bot) object;
					sql = MessageFormat.format(insertBot.replace("'", "''"),
							bot.getBotnetId(), bot.getIp(), bot.getFamily(),
							bot.getConfidence(),
							dateFormat.format(bot.getTimeStamp()),
							dateFormat.format(bot.getAddTime()));
					stmt.executeUpdate(sql);
					sql = MessageFormat.format(insertGeoIp.replace("'", "''"),
							bot.getIp(), bot.getBgp().getAsname(), bot.getBgp().getAsn(),
							bot.getGeoip().getLongitude(), bot.getGeoip().getLatitude(),
							bot.getGeoip().getCity().replace("'", "''"),
							bot.getGeoip().getCc());
					stmt.executeUpdate(sql);
				} else if (mode == CNC_PARSE) {
					Botnet botnet = (Botnet) object;
					// botnet_id,type,family,confidence,timestamp,last_active,ip,port,protocol,url,sha1,md5
					if (botnet.getMalwareList() == null
							|| botnet.getMalwareList().size() == 0) {
						sql = MessageFormat.format(insertBotnet.replace("'",
								"''"), botnet.getBotnetId(), botnet.getType(),
								botnet.getFamily(), botnet.getConfidence(),
								dateFormat.format(botnet.getTimeStamp()),
								dateFormat.format(botnet.getLastActive()),
								botnet.getBotnetDetail().getIp(), botnet
										.getBotnetDetail().getPort(), botnet
										.getBotnetDetail().getProtocol(),
								botnet.getBotnetDetail().getUrl(), null, null);
						stmt.executeUpdate(sql);
					} else {
						for (int i = 0; i < botnet.getMalwareList().size(); ++i) {
							sql = MessageFormat.format(insertBotnet.replace(
									"'", "''"), botnet.getBotnetId(), botnet
									.getType(), botnet.getFamily(), botnet
									.getConfidence(), dateFormat.format(botnet
									.getTimeStamp()), dateFormat.format(botnet
									.getLastActive()), botnet.getBotnetDetail()
									.getIp(), botnet.getBotnetDetail()
									.getPort(), botnet.getBotnetDetail()
									.getProtocol(), botnet.getBotnetDetail()
									.getUrl(), botnet.getMalwareList().get(i)
									.getSha1(), botnet.getMalwareList().get(i)
									.getMd5());
							stmt.executeUpdate(sql);
						}
					}

				} else if(mode == DDOS_PARSE){
					DDoS ddos = (DDoS) object;
					String templateSql;
					if(ddos.getOngoing() == 0 && ddos.getEndTime() != null) {
						templateSql = replaceDDoS;
					} else {
						templateSql = insertDDoS;
					}
					sql = MessageFormat.format(templateSql.replace("'", "''"),
							ddos.getId(), ddos.getTargetIp(),ddos.getBotnetId(), 
							ddos.getFamily(), ddos.getConfidence(),
							ddos.getOngoing(),ddos.getCategory(),
							dateFormat.format(ddos.getTimeStamp()),
							dateFormat.format(ddos.getAddTime()),
							dateFormat.format(ddos.getEndTime()));
					stmt.executeUpdate(sql);
					sql = MessageFormat.format(insertGeoIp.replace("'", "''"),
							ddos.getTargetIp(), ddos.getBgp().getAsname(), ddos.getBgp().getAsn(),
							ddos.getGeoip().getLongitude(), ddos.getGeoip().getLatitude(),
							ddos.getGeoip().getCity().replace("'", "''"),
							ddos.getGeoip().getCc());
					stmt.executeUpdate(sql);
				}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
