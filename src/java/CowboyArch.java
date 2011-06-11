import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.JasonException;
import jason.asSyntax.Literal;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;
import jmoise.OrgAgent;


public class CowboyArch extends OrgAgent {
	private LocalWorldModel model = null;
	private String simId = null;
    private int myId  = -1;

    protected Logger logger = Logger.getLogger(CowboyArch.class.getName());

    @Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts)
    		throws JasonException {
        super.initAg(agClass, bbPars, asSrc, stts);
    }

    @Override
    public void stopAg() {
    	super.stopAg();
    }

    /**
     * this version of perceive is used in local simulator. 
     * it get the perception and updates the world model. 
     * only relevant percepts are leaved in the list of perception for the agent.
     */
    @Override
    public List<Literal> perceive() {
    	List<Literal> per = super.perceive();
    	try {
            if (per != null) {
                Iterator<Literal> ip = per.iterator();
                while (ip.hasNext()) {
                	Literal p = ip.next();
                    String  ps = p.toString();
                    if (ps.startsWith("simulation")) {
                    	simulationPerceived(p);
                    	ip.remove();
                    }
                }
            }
    	} catch (Exception e) {
            logger.log(Level.SEVERE, "Error in perceive!", e);
    	}
    	return per;
    }

    /**
     * Perceived "simulation(id,opponent,steps,corralx0,corralx1,
     * corraly0,corraly1,gsizex,gsizey)".
     * @param ps
     * 			the simulation perceived.
     */
    private void simulationPerceived(Literal p){
    	simId = p.getTerm(0).toString();
    	String gsizex = p.getTerm(7).toString();
    	String gsizey = p.getTerm(8).toString();
    	int w = Integer.parseInt(gsizex);
    	int h = Integer.parseInt(gsizey);
    	model = new LocalWorldModel(w, h, 10);
    	logger.info("Created LocalWorldModel");
    }
}
