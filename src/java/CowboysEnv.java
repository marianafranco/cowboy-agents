import java.util.logging.Level;
import java.util.logging.Logger;

import jason.asSyntax.Structure;
import jason.environment.Environment;

/**
 * Defines possible actions to be performed by the agents.
 * @author Mariana Ramos Franco
 */
public class CowboysEnv extends Environment {
	
	private Logger logger = Logger.getLogger("cowboy-agents.mas2j."
			+ CowboysEnv.class.getName());

	/** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
    	super.init(args);
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
    	// connects to the server.
    	if (action.getFunctor().equals("connectToServer")) {
    		try {
				ServerConnection.connect();
			} catch (Exception e) {
				final String msg = "Impossible to connect to the server: "
					+ e.getLocalizedMessage();
				logger.log(Level.SEVERE, msg, e);
				return false;
			}
    		return true;
    	// disconnects from the server.
    	} else if (action.getFunctor().equals("disconnectFromServer")){
    		try {
				ServerConnection.close();
			} catch (Exception e) {
				final String msg = "Error when disconnecting from the server: "
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

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}
