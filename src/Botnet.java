import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;

public class Botnet {
	private BotnetDetail detail;
	private List<Bot> botList = new ArrayList<Bot>();
	private List<Malware> malwareList = new ArrayList<Malware>();

	private String botnetId;
	private Date timeStamp;
	private Date lastActive;
	private String type;
	private String family;
	private int confidence;

	public void populate(Attributes attributes) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		try {
			this.timeStamp = dateFormat.parse(attributes.getValue("timestamp"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.type = attributes.getValue("type");
		this.botnetId = attributes.getValue("id");
		this.family = attributes.getValue("family");
		this.confidence = Integer.parseInt(attributes.getValue("confidence"));
		try {
			this.lastActive = dateFormat.parse(attributes
					.getValue("last_active"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public String getFamily() {
		return family;
	}

	public int getConfidence() {
		return confidence;
	}

	public Date getLastActive() {
		return lastActive;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public String getType() {
		return type;
	}

	public String getBotnetId() {
		return botnetId;
	}

	public Malware addMalware(Attributes attributes) {
		Malware malware = new Malware();
		malware.populate(attributes);
		this.malwareList.add(malware);
		return malware;
	}

	public List<Malware> getMalwareList() {
		return malwareList;
	}

	public Bot addBot(Attributes attributes) {
		Bot bot = new Bot();
		bot.setIp(attributes.getValue("ip"));
		this.botList.add(bot);
		return bot;
	}

	public List<Bot> getBotList() {
		return botList;
	}

	public void createBotnetDetail(Attributes attributes) {
		BotnetDetail detail = new BotnetDetail();
		detail.populate(attributes);
		this.detail = detail;
	}

	public BotnetDetail getBotnetDetail() {
		return detail;
	}

	class Malware {
		private String sha1;
		private String md5;

		public void populate(Attributes attributes) {
			this.sha1 = attributes.getValue("sha1");
			this.md5 = attributes.getValue("md5");
		}

		public String getSha1() {
			return sha1;
		}

		public String getMd5() {
			return md5;
		}
	}

	class BotnetDetail {
		private int port;
		private int protocol = -1;
		private String ip;
		private String url;

		public void populate(Attributes attributes) {
			this.port = Integer.parseInt(attributes.getValue("port"));
			if(attributes.getValue("protocol") != null){
				this.protocol = Integer.parseInt(attributes.getValue("protocol"));
			}
			this.ip = attributes.getValue("ip");
			this.url = attributes.getValue("url");
		}

		public int getPort() {
			return port;
		}

		public int getProtocol() {
			return protocol;
		}

		public String getIp() {
			return ip;
		}

		public String getUrl() {
			return url;
		}
	}
}
