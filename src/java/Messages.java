import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class Messages {

	private Document document;
	private static final String[] actions = {"skip", "north", "northeast",
		"east", "southeast", "south", "southwest", "west", "northwest"};
	private static final Set<String> actionsSet = new HashSet<String>(Arrays.asList(actions));

	public String createAuthRequestMsg(String username, String password)
			throws ParserConfigurationException, TransformerException {
		initDoc();
		Element auth = document.createElement("authentication");
		auth.setAttribute("username", username);
		auth.setAttribute("password", password);
		String xmlString = createXML("auth-request", auth);
		return xmlString;
	}

	public String createActionMsg(String id, String type)
			throws Exception {
		if (!actionsSet.contains(type)) {
			throw new Exception("Invalid action type: " + type);
		}
		initDoc();
		Element action = document.createElement("action");
		action.setAttribute("id", id);
		action.setAttribute("type", type);
		String xmlString = createXML("action", action);
		return xmlString;
	}

	public String parseMsg(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputSource inStream = new InputSource();
        inStream.setCharacterStream(new StringReader(xml));
        document = docBuilder.parse(inStream);
        Node message = document.getFirstChild();
        NamedNodeMap attributes = message.getAttributes();
        Node typeNode = attributes.getNamedItem("type");
        String type = typeNode.getTextContent();
        return type;
	}

	private String createXML(String type, Element content)
			throws ParserConfigurationException, TransformerException{
		Element rootElement = document.createElement("message");
		rootElement.setAttribute("type", type);
		document.appendChild(rootElement);
		if (null != content) {
			rootElement.appendChild(content);
		}
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(document);
		StringWriter sw = new StringWriter();
		StreamResult result =  new StreamResult(sw);
		transformer.transform(source, result);
		return sw.toString();
	}

	private void initDoc() throws ParserConfigurationException{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		document = docBuilder.newDocument();
		document.setXmlStandalone(true);
	}

	public static void main(String args[]) {
		try {
			Messages msg = new Messages();
			System.out.println(msg.createAuthRequestMsg("leader", "123"));
			System.out.println(msg.createActionMsg("10", "north"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
