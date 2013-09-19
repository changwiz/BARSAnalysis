import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BotnetParser extends DefaultHandler {
	private List<Botnet> botnetList = new ArrayList<Botnet>();
	private Botnet currentBotnet;

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (qName.equalsIgnoreCase("botnet")) {
			currentBotnet = new Botnet();
			currentBotnet.populate(attributes);
		} else if (qName.equalsIgnoreCase("bot")) {
			currentBotnet.addBot(attributes);
		} else if (currentBotnet != null && qName.equalsIgnoreCase(currentBotnet.getType())) {
			currentBotnet.createBotnetDetail(attributes);
		} else if (qName.equalsIgnoreCase("malware")) {
			currentBotnet.addMalware(attributes);
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if (qName.equalsIgnoreCase("botnet")) {
			botnetList.add(currentBotnet);
		}

	}

	public List<Botnet> getBotnetList() {
		return botnetList;
	}
}
