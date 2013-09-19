import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BotParser extends DefaultHandler {

	private List<Bot> botList = new ArrayList<Bot>();
	private Bot currentBot;

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (qName.equalsIgnoreCase("bot")) {
			currentBot = new Bot();
			currentBot.populate(attributes);
		} else if (qName.equalsIgnoreCase("bgp")) {
			currentBot.createBgp();
			currentBot.getBgp().populate(attributes);
		} else if (qName.equalsIgnoreCase("botnet")) {
			currentBot.setBotnetId(attributes.getValue("id"));
		} else if (qName.equalsIgnoreCase("geoip")) {
			currentBot.createGeoip();
			currentBot.getGeoip().populate(attributes);
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if (qName.equalsIgnoreCase("bot")) {
			botList.add(currentBot);
		}

	}

	public List<Bot> getBotList() {
		return botList;
	}
}
