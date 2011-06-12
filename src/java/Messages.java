import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class used to:
 * - create the XML messages sent to the server.
 * - parse the XML messages received from the server.
 * 
 * @author Mariana Ramos Franco
 */
public class Messages {

	/** Action's types */
	private static final String[] actions = {"skip", "north", "northeast",
		"east", "southeast", "south", "southwest", "west", "northwest"};

	/** Simulation attributes */
	private static final String[] simulationAttrs = {"corralx0", "corralx1",
		"corraly0", "corraly1", "gsizex", "gsizey", "id",
		"opponent", "steps"};

	/** Request action perceptions */
	private static final String[] requestActionPerceptions = {"deadline",
		"id", "posx", "posy", "score", "step"};

	/**
	 * Creates an authentication request message.
	 * @param username
	 * 			the user name.
	 * @param password
	 * 			the user password.
	 * @return the authentication request message.
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static String createAuthRequestMsg(String username, String password)
			throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document document = docBuilder.newDocument();
		document.setXmlStandalone(true);
		Element auth = document.createElement("authentication");
		auth.setAttribute("username", username);
		auth.setAttribute("password", password);
		String xmlString = createXML("auth-request", auth, document);
		return xmlString;
	}

	/**
	 * Creates an action message.
	 * @param id
	 * 			the message id.
	 * @param type
	 * 			the action's type.
	 * @return the action message.
	 * @throws Exception
	 */
	public static String createActionMsg(String id, String type)
			throws Exception {
		Set<String> actionsSet = new HashSet<String>(Arrays.asList(actions));
		if (!actionsSet.contains(type)) {
			throw new Exception("Invalid action type: " + type);
		}
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document document = docBuilder.newDocument();
		document.setXmlStandalone(true);
		Element action = document.createElement("action");
		action.setAttribute("id", id);
		action.setAttribute("type", type);
		String xmlString = createXML("action", action, document);
		return xmlString;
	}

	/**
	 * Creates the XML message.
	 * @param type
	 * 			the message type.
	 * @param content
	 * 			the message content.
	 * @param document
	 * 			the document object.
	 * @return the XML message.
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	private static String createXML(String type, Element content, Document document)
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

	/**
	 * Parses the XML message received from the server.
	 * @param xml
	 * 			the XML message received from the server.
	 * @return the message type.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static String parseMsg(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xml));
		Document doc = docBuilder.parse(inStream);
		Node message = doc.getFirstChild();
		NamedNodeMap attributes = message.getAttributes();
		Node typeNode = attributes.getNamedItem("type");
		String type = typeNode.getTextContent();
		return type;
	}

	/**
	 * Parses the authentication response message and returns the
	 * authentication result: ok or fail.
	 * @param xml
	 * 			the authentication response message.
	 * @return ok or fail.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static String parseAuthResponse(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xml));
		Document doc = docBuilder.parse(inStream);
		NodeList authNode = doc.getElementsByTagName("authentication");
		NamedNodeMap attributes = authNode.item(0).getAttributes();
		Node resultNode = attributes.getNamedItem("result");
		String result = resultNode.getTextContent();
		return result;
	}

	/**
	 * Parses the simulation message received from the server.
	 * @param xml
	 * 			the simulation message.
	 * @return a HashMap where the keys are simulation attributes.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static HashMap<String, String> parseSimStart(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		HashMap<String, String> simValues = new HashMap<String, String>(); 
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xml));
		Document doc = docBuilder.parse(inStream);
		NodeList simNode = doc.getElementsByTagName("simulation");
		NamedNodeMap attributes = simNode.item(0).getAttributes();
		for (String attribute : simulationAttrs) {
			Node attrNode = attributes.getNamedItem(attribute);
			String attrValue = attrNode.getTextContent();
			simValues.put(attribute, attrValue);
		}
		return simValues;
	}

	/**
	 * Parses the request-action message received from the server and returns
	 * the perception values.
	 * @param xml
	 * 			the request-action message.
	 * @return a HashMap where the keys are perception attributes.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static HashMap<String, String> parseRequestActionPerception(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		HashMap<String, String> perceptionValues = new HashMap<String, String>();
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xml));
		Document doc = docBuilder.parse(inStream);
		NodeList perceptionNode = doc.getElementsByTagName("perception");
		NamedNodeMap attributes = perceptionNode.item(0).getAttributes();
		for (String attribute : requestActionPerceptions) {
			Node attrNode = attributes.getNamedItem(attribute);
			String attrValue = attrNode.getTextContent();
			perceptionValues.put(attribute, attrValue);
		}
		return perceptionValues;
	}

	/**
	 * Parse the cells send on the request-action message.
	 * @param xml
	 * 			the request-action message.
	 * @return a List with all the cell's values.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static List<String> parseRequestActionCells(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		List<String> cells = new ArrayList<String>();
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xml));
		Document doc = docBuilder.parse(inStream);
		NodeList cellsNode = doc.getElementsByTagName("cell");
		for (int i = 0; i < cellsNode.getLength(); i++) {
			Node cell = cellsNode.item(i);
			NamedNodeMap attributes = cell.getAttributes();
			String x = attributes.getNamedItem("x").getTextContent();
			String y = attributes.getNamedItem("y").getTextContent();
			NodeList cellChilds = cell.getChildNodes();
			for (int j = 0; j < cellChilds.getLength(); j++) {
				if (cellChilds.item(j).getNodeType() == Element.ELEMENT_NODE) {
					Element cellContent = (Element)cellChilds.item(j);
					String content = cellContent.getNodeName();
					String contentAttr = "null";
					if (content.equals("agent") || content.equals("corral")) {
						contentAttr = cellContent.getAttributes().getNamedItem("type").getTextContent();
					} else if (content.equals("cow")) {
						contentAttr = cellContent.getAttributes().getNamedItem("ID").getTextContent();
					} else if (content.equals("fence")) {
						contentAttr = cellContent.getAttributes().getNamedItem("open").getTextContent();
					}
					cells.add("cell(" + x + "," + y + "," + content + "," + contentAttr + ")");
				}
			}
		}
		return cells;
	}

	/**
	 * Main method used to test the createMsg methods.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			System.out.println(Messages.createAuthRequestMsg("leader", "123"));
			System.out.println(Messages.createActionMsg("10", "north"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
