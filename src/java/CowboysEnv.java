import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;

/**
 * Defines possible actions to be performed by the agents. Also update the
 * agent percept with the informations received from the server.
 * 
 * @author Mariana Ramos Franco
 */
public class CowboysEnv extends Environment {
	
	private Logger logger = Logger.getLogger("cowboy-agents.mas2j."
			+ CowboysEnv.class.getName());

	/** the configuration properties */
	private Properties props;

	/** HashMap which keeps the ServerConnection objects created for
	 * each agent to connect to the server. */
	private HashMap<String, ServerConnection> connections;

	/** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
    	super.init(args);
    	props = new Properties();
		try {
			props.load(new FileInputStream("config.properties"));
		} catch (Exception e) {
			final String msg = "Impossible to load the config.properties file."
				+ e.getLocalizedMessage();
			logger.log(Level.SEVERE, msg, e);
		}
		connections = new HashMap<String, ServerConnection>();
    }

    /**
     * The agent "agName" executes "action".
     * @param agName
     * 			the agents name.
     * @param action
     * 			the action to be executed.
     * @return boolean indicating if the action was correctly executed or not.
     */
    @Override
    public boolean executeAction(String agName, Structure action) {
    	// connects to the server
    	if (action.getFunctor().equals("connectToServer")) {
    		try {
    			connectToServer(agName);
    		} catch (Exception e) {
    			final String msg = "Impossible to connect to the server: "
    				+ e.getLocalizedMessage();
    			logger.log(Level.SEVERE, msg, e);
    			return false;
    		}
    		return true;
    	// authentication
    	} else if (action.getFunctor().equals("authentication")) {
    		//String role = action.getTerm(0).toString();
    		try {
				authentication(agName);
			} catch (Exception e) {
				final String msg = "Error when requesting authentication: "
					+ e.getLocalizedMessage();
				logger.log(Level.SEVERE, msg, e);
				return false;
			}
			return true;
    	} else {
			logger.info("executing: "+action+", but not implemented!");
			return false;
		}
    }


    /**
     * Connects to the server.
     * @param agName
     * 			the agents name.
     * @throws IOException 
     * @throws UnknownHostException 
     */
    private void connectToServer(String agName)
    		throws UnknownHostException, IOException {
    	final String host = props.getProperty("server.host");
		final int port = Integer.parseInt(props.getProperty("server.port"));
		ServerConnection server = new ServerConnection();
		server.connect(host, port);
		connections.put(agName, server);
		logger.info("[" + agName + "] Connected to the server.");
    }

    /**
     * Disconnects from the server.
     * @param agName
     * 			the agents name.
     * @throws IOException 
     */
    private void disconnectFromServer(String agName) throws IOException {
    	ServerConnection server = connections.get(agName);
		server.close();
		logger.info("[" + agName + "] Disconnected from the server.");
    }

    /**
     * Try to authenticate the agent to the server.
     * @param agName
     * 			the agents name.
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    private void authentication(String agName)
    		throws ParserConfigurationException, TransformerException, IOException {
		final String username = props.getProperty(agName + ".username");
		final String password = props.getProperty(agName + ".password");
		Messages msg = new Messages();
		String authRequest = msg.createAuthRequestMsg(username, password);
		ServerConnection server = connections.get(agName);
		logger.info("[" + agName + "] Requesting authentication.");
		server.sendMsg(authRequest);
		serverMonitor(agName, server);
    }

    /**
     * Create a thread to monitor new messages received from the server. 
     * @param agName
     * 			the agents name.
     * @param server
     * 			the connection to the server.
     */
    private void serverMonitor(String agName,
    		ServerConnection server) {
    	MonitorThread monitor = new MonitorThread(agName, server);
    	monitor.start();
    }

    /**
     * Monitor thread responsible for parse the messages
     * received from the server and updates the agent percept. 
     */
    class MonitorThread extends Thread {
    	private String agName;
    	private ServerConnection server;

    	MonitorThread (String agName, ServerConnection server) {
    		this.agName = agName;
    		this.server = server;
    		logger.info("[" + agName + "] Monitor thread created.");
    	}

    	public void run() {
	        while(true){
	        	try {
					Thread.sleep(1000); // wait 1 second
					String msg = server.readMsg();
					String type = Messages.parseMsg(msg);
					updateAgPercept(agName, type, msg);
					if (type.equals("bye")){
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					final String msg = "Error when receiving a message from the server: "
						+ e.getLocalizedMessage();
					logger.log(Level.SEVERE, msg, e);
				}
	        }
	    }
	}

    /**
     * Updates the agent's percept.
     * @param agName
     * 			the agents name.
     * @param type
     * 			the message type received from the server.
     * @param msgReceived
     * 			theh message received from the server.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private void updateAgPercept(String agName, String type, String msgReceived)
    		throws ParserConfigurationException, SAXException, IOException {
    	if (type.equals("auth-response")) {
    		updateAuthPercept(agName, msgReceived);
    	} else if (type.equals("sim-start")) {
    		updateSimStartPercept(agName, msgReceived);
    	} else if (type.equals("sim-end")) {
    		
    	} else if (type.equals("bye")) {
    		disconnectFromServer(agName);
    	} else if (type.equals("request-action")) {
    		
    	}
    }

    /**
     * Adds the 'auth(result)' percept to the agent.
     * @param agName
     * 			the agents name.
     * @param msgReceived
     * 			the received authentication result message.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private void updateAuthPercept(String agName, String msgReceived)
    		throws ParserConfigurationException, SAXException, IOException{
    	String result = Messages.parseAuthResponse(msgReceived);
    	addPercept(agName, Literal.parseLiteral("auth(" + result + ")"));
    	logger.info("[" + agName + "] Auth " + result);
    }

    /**
     * Adds the simulation(attr, value) percept to the agent.
     * @param agName
     * 			the agents name.
     * @param msgReceived
     * 			the received simulation message.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private void updateSimStartPercept(String agName, String msgReceived)
    		throws ParserConfigurationException, SAXException, IOException {
    	HashMap<String, String> simValues = Messages.parseSimStart(msgReceived);
    	String id = simValues.get("id");
    	String opponent = simValues.get("opponent");
    	String steps = simValues.get("steps");
    	String corralx0 = simValues.get("corralx0");
    	String corralx1 = simValues.get("corralx1");
    	String corraly0 = simValues.get("corraly0");
    	String corraly1 = simValues.get("corraly1");
    	String gsizex = simValues.get("gsizex");
    	String gsizey = simValues.get("gsizey");
    	// simulation(id,opponent,steps,corralx0,corralx1,corraly0,corraly1,gsizex,gsizey)
    	addPercept(agName, Literal.parseLiteral(
				"simulation(" + id + "," + opponent + "," + steps
				+ "," + corralx0 + "," + corralx1 + "," + corraly0
				+ "," + corraly1 + "," + gsizex + "," + gsizey
				+ ")"));
    	logger.info("[" + agName + "] Received a sim-start message");
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}
