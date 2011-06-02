import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jason.asSyntax.Structure;
import jason.environment.Environment;

/**
 * Defines possible actions to be performed by the agents.
 * @author Mariana Ramos Franco
 */
public class CowboysEnv extends Environment {
	
	private Logger logger = Logger.getLogger("cowboy-agents.mas2j."
			+ CowboysEnv.class.getName());
	
	private Properties props;
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
    	}// connects to the server
    	if (action.getFunctor().equals("disconnectFromServer")) {
    		try {
        		disconnectFromServer(agName);
    		} catch (Exception e) {
    			final String msg = "Error when disconnecting from the server: "
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

    private void authentication(String agName)
    		throws ParserConfigurationException, TransformerException, IOException {
		final String username = props.getProperty(agName + ".username");
		final String password = props.getProperty(agName + ".password");
		Messages msg = new Messages();
		String authRequest = msg.createAuthRequestMsg(username, password);
		ServerConnection server = connections.get(agName);
		server.sendMsg(authRequest);
		serverMonitor(agName, server);
		logger.info("[" + agName + "] Requesting authentication.");
    }

    private void serverMonitor(String agName, final ServerConnection server) {
    	new Thread() {
    		public void run() {
    	        while(true){
    	        	try {
						Thread.sleep(1000); // wait 1 second
						logger.info(server.readMsg());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    	        }
    	    }
    	};
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}
