import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xml.sax.Attributes;

public class Bot {
	private BGP bgp;
	private String botnetId;
	private GeoIp geoip;

	private Date timeStamp;
	private String ip;
	public void setIp(String ip) {
		this.ip = ip;
	}

	private String family;
	private int confidence;
	private Date addTime;

	public void populate(Attributes attributes) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		try {
			this.timeStamp = dateFormat.parse(attributes.getValue("timestamp"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.ip = attributes.getValue("ip");
		this.family = attributes.getValue("family");
		this.confidence = Integer.parseInt(attributes.getValue("confidence"));
		try {
			this.addTime = dateFormat.parse(attributes.getValue("addtime"));
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

	public Date getAddTime() {
		return addTime;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public String getIp() {
		return ip;
	}

	public String getBotnetId() {
		return botnetId;
	}

	public void setBotnetId(String botnetId) {
		this.botnetId = botnetId;
	}

	public BGP getBgp() {
		return bgp;
	}

	public void createBgp() {
		this.bgp = new BGP();
	}

	public GeoIp getGeoip() {
		return geoip != null?geoip:new GeoIp();
	}

	public void createGeoip() {
		this.geoip = new GeoIp();
	}

	class BGP {
		private String asname;
		private int asn;

		public void populate(Attributes attributes) {
			this.asname = attributes.getValue("asname");
			this.asn = Integer.parseInt(attributes.getValue("asn"));
		}

		public String getAsname() {
			return asname;
		}

		public int getAsn() {
			return asn;
		}
	}

	class GeoIp {
		private double longitude;
		private double latitude;
		private String city = "";
		private String cc = "";

		public void populate(Attributes attributes) {
			this.longitude = Double.parseDouble(attributes
					.getValue("longitude"));
			this.latitude = Double.parseDouble(attributes.getValue("latitude"));
			this.city = attributes.getValue("city");
			this.cc = attributes.getValue("cc");
		}

		public double getLongitude() {
			return longitude;
		}

		public double getLatitude() {
			return latitude;
		}

		public String getCity() {
			return city;
		}

		public String getCc() {
			return cc;
		}
	}
}
