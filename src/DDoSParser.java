import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DDoSParser extends DefaultHandler {

	private String family;

	public DDoSParser(String family) {
		super();
		this.family = family;
	}

	private List<DDoS> ddosList = new ArrayList<DDoS>();
	private DDoS currentDDos;

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (qName.equalsIgnoreCase("ddos")) {
			currentDDos = new DDoS();
			currentDDos.setFamily(family);
			currentDDos.populate(attributes);
		} else if (qName.equalsIgnoreCase("bgp")) {
			currentDDos.createBgp();
			currentDDos.getBgp().populate(attributes);
		} else if (qName.equalsIgnoreCase("botnet")) {
			currentDDos.setBotnetId(attributes.getValue("id"));
		} else if (qName.equalsIgnoreCase("geoip")) {
			currentDDos.createGeoip();
			currentDDos.getGeoip().populate(attributes);
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (qName.equalsIgnoreCase("ddos")) {
			ddosList.add(currentDDos);
		}

	}

	public List<DDoS> getDDoSList() {
		return ddosList;
	}
}
