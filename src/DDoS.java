import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xml.sax.Attributes;

public class DDoS {
	private BGP bgp;
	private String botnetId;
	private GeoIp geoip;

	private String id;
	private Date timeStamp;
	private String targetIp;
	private int ongoing;
	private String family;
	private int confidence;
	private String category;
	private Date addTime;
	private Date endTime;

	public void populate(Attributes attributes) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			this.timeStamp = dateFormat.parse(attributes.getValue("timestamp"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.targetIp = attributes.getValue("targetip");
		this.ongoing = Integer.parseInt(attributes.getValue("ongoing"));
		this.confidence = Integer.parseInt(attributes.getValue("confidence"));
		this.category = attributes.getValue("category");
		try {
			this.addTime = dateFormat.parse(attributes.getValue("addtime"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.id = attributes.getValue("id");
		try {
			this.endTime = dateFormat.parse(attributes.getValue("endtime"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public String getId() {
		return id;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public String getTargetIp() {
		return targetIp;
	}

	public int getOngoing() {
		return ongoing;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getFamily() {
		return family;
	}

	public int getConfidence() {
		return confidence;
	}

	public String getCategory() {
		return category;
	}

	public Date getAddTime() {
		return addTime;
	}

	public Date getEndTime() {
		return endTime;
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
		return geoip != null ? geoip : new GeoIp();
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
			this.longitude = Double.parseDouble(attributes.getValue("longitude"));
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
