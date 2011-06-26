package arch;

import static jason.asSyntax.ASSyntax.createLiteral;
import static jason.asSyntax.ASSyntax.createNumber;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import env.cow.ClusterModelFactory;
import env.cow.CowModelFactory;
import env.cow.IClusterModel;
import env.cow.ICowModel;

import jason.JasonException;
import jason.RevisionFailedException;
import jason.asSemantics.Message;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;
import jmoise.OrgAgent;

/**
 * 
 * @author Mariana Ramos Franco, Rafael Barbolo Lopes
 */
public class CowboyArch extends OrgAgent {
	private LocalWorldModel model = null;
	private String simId = null;
    private int myId  = -1;
    private String opponent = null;
    private int steps = -1;
    private int numOfCowboys = -1;

    private ICowModel		cModel = null;
	private IClusterModel	clModel = null;

	public static Atom aOBSTACLE    = new Atom("obstacle");
	public static Atom aENEMY       = new Atom("enemy");
	public static Atom aENEMYCORRAL = new Atom("enemycorral");
	public static Atom aALLY        = new Atom("ally");
	public static Atom aEMPTY       = new Atom("empty");
	public static Atom aSWITCH      = new Atom("switch");
    public static Atom aFENCE       = new Atom("fence");
    public static Atom aOPEN        = new Atom("open");
    public static Atom aCLOSED      = new Atom("closed");

    /** stores the location of some cows and the step they were seen */
    private Map<Integer,Integer> lastPerceivedCows = new HashMap<Integer, Integer>();
    private int simStep  = 0;
    
    // sabotage
    private static List<Fence> fences = new ArrayList<Fence>();
    private static boolean cheat_passed = false, cheat_in_position = false, helper_in_position = false;

    protected Logger logger = Logger.getLogger(CowboyArch.class.getName());

    /** the configuration properties */
	private Properties props;

    @Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts)
    		throws JasonException {
        super.initAg(agClass, bbPars, asSrc, stts);

        props = new Properties();
		try {
			props.load(new FileInputStream("config.properties"));
		} catch (Exception e) {
			final String msg = "Impossible to load the config.properties file."
				+ e.getLocalizedMessage();
			logger.log(Level.SEVERE, msg, e);
		}
		numOfCowboys = Integer.parseInt(props.getProperty("numberOf.comboys"));

		/* Load fences positions */
		String[] fences_pos = props.getProperty("fences.open_positions").split(" ");
		for(int i=0; i < fences_pos.length; i++) {
			String[] params = fences_pos[i].split(",");
			Fence fence = new Fence( params[0], Integer.parseInt(params[1]), Integer.parseInt(params[2]) );
			fences.add(fence);
		}
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
                    } else if (ps.startsWith("cell") && model != null) {
                    	cellPerceived(p);
                    	ip.remove();
                    } else if (ps.startsWith("perception") && model != null) {
                    	perceptionPerceived(p);
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
     * @throws RevisionFailedException 
     */
    private void simulationPerceived(Literal p) throws RevisionFailedException{
    	simId = p.getTerm(0).toString();
    	opponent = p.getTerm(1).toString();
    	steps = Integer.parseInt(p.getTerm(2).toString());
    	String gsizex = p.getTerm(7).toString();
    	String gsizey = p.getTerm(8).toString();
    	int w = Integer.parseInt(gsizex);
    	int h = Integer.parseInt(gsizey);
    	model = new LocalWorldModel(w, h,  WorldModel.agsByTeam, getTS().getAg().getBB());
    	int corralx0 = Integer.parseInt( p.getTerm(3).toString());
		int corralx1 = Integer.parseInt( p.getTerm(4).toString());
		int corraly0 = Integer.parseInt( p.getTerm(5).toString());
		int corraly1 = Integer.parseInt( p.getTerm(6).toString());
		model.setCorral(corralx0, corralx1, corraly0, corraly1);
		
		cModel = CowModelFactory.getModel(""+getMyId());
        cModel.setSize(w,h);
        clModel = ClusterModelFactory.getModel(""+getMyId());
        clModel.setStepcl(simStep);
        new Thread((Runnable) clModel).start();

		// add believe "sim_start(simId)"
		getTS().getAg().addBel(Literal.parseLiteral("sim_start(" + simId + ")"));
    }

    /**
     * Perceived "cell(x,y,content,contentAttr)".
     * @param p
     * 			the cell perceived.
     * @throws RevisionFailedException 
     */
    private void cellPerceived(Literal p) throws RevisionFailedException {
    	// absolute positions
    	int x =  Integer.parseInt(p.getTerm(0).toString().replace("(", "").replace(")", ""));
    	int y =  Integer.parseInt(p.getTerm(1).toString().replace("(", "").replace(")", ""));
    	String content = p.getTerm(2).toString();
    	String contentAttr = p.getTerm(3).toString();
    	// update the model with the enemy location and share them with other agents
    	if (content.equals("agent")) {
    		if (contentAttr.equals("ally")) {
    			// do nothing.
    			// the agents location is sent by the locationPerceived method
    		} else if (contentAttr.equals("enemy")) {
    			if (! model.hasObject(WorldModel.ENEMY, x, y)) {
                    model.add(WorldModel.ENEMY, x, y);
        		}
    			/* Don't send the enemy's location to other agents.
        		Message m = new Message("tell", null, null, p);
        		try {
    				broadcast(m);
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    			*/
    		}
    	// update the model with obstacle and share them with other agents
    	} else if (content.equals("obstacle")) {
    		obstaclePerceived(x, y, p);
		// update the model with the cow location and share this with other agents
    	} else if (content.equals("cow")) {
    		Location c = new Location(x, y);
    		if (!model.getCorral().contains(c)) { // cows in the corral are not perceived
    			if (! model.hasObject(WorldModel.COW, x, y)) {
                    model.add(WorldModel.COW, x, y);
                    // add believe "cow(x,y,cowId)"
                    /*
                    getTS().getAg().addBel(
                    		Literal.parseLiteral("cow(" + x +"," + y + "," + contentAttr + ")"));
					*/
                    int cowId = Integer.parseInt(contentAttr);
                    lastPerceivedCows.put(cowId, simStep);
                    cModel.insertCow(cowId,x,y, simStep);
        		}
    			/* Don't send the cow's location to other agents.
        		Message m = new Message("tell", null, null, p);
        		try {
    				broadcast(m);
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    			*/
    		}
    	// update the model with the corral enemy location and share this with other agents
    	} else if (content.equals("corral") && contentAttr.equals("enemy")) {
			if (! model.hasObject(WorldModel.ENEMYCORRAL, x, y)) {
                model.add(WorldModel.ENEMYCORRAL, x, y);
    		}
    		Message m = new Message("tell", null, null, p);
    		try {
				broadcast(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		// update the model with the switch location and share this with other agents
    	} else if (content.equals("switch")) {
    		if (! model.hasObject(WorldModel.SWITCH, x, y)) {
                model.add(WorldModel.SWITCH, x, y);
    		}
    		Message m = new Message("tell", null, null, p);
    		try {
				broadcast(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		// update the model with the fence location and share this with other agents
    	} else if (content.equals("fence")) {
    		int fence = WorldModel.CLOSED_FENCE;
    		if (contentAttr.equals("true")){
    			fence = WorldModel.OPEN_FENCE;
    		}
    		if (!model.hasObject(fence, x, y)) {
    			if (content.equals("true") && !model.hasObject(WorldModel.CLOSED_FENCE, x, y)) {
    				model.remove(WorldModel.CLOSED_FENCE, x, y);
    			} else if (content.equals("false") && !model.hasObject(WorldModel.OPEN_FENCE, x, y)) {
    				model.remove(WorldModel.OPEN_FENCE, x, y);
    			}
                model.add(fence, x, y);
                Message m = new Message("tell", null, null, p);
                try {
    				broadcast(m);
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
            }
    	// update the model with the empty location and share this with other agents
    	} else if (content.equals("empty")) {
    		if (!model.isFree(x, y)) {
    			model.add(WorldModel.CLEAN, x, y);
    		}
    		/* Don't send the empty information to other agents.
    		Message m = new Message("tell", null, null, p);
    		try {
				broadcast(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
    	}
    }

    /** update the model with obstacle and share them with the team mates */
    public void obstaclePerceived(int x, int y, Literal p) {
        if (! model.hasObject(WorldModel.OBSTACLE, x, y)) {
            model.add(WorldModel.OBSTACLE, x, y);

            if(clModel == null) 
    			clModel = ClusterModelFactory.getModel(""+getMyId());
    		clModel.insertTree(x, y);

            Message m = new Message("tell", null, null, p);
            try {
                broadcast(m);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }       
    }

    /** update the model with obstacle and share them with the team mates */
	public void obstaclePerceived(int x, int y) {
		Literal p = createCellPerception(x, y,aOBSTACLE);
		obstaclePerceived(x, y, p);
	}

    /**
     * Perceived "perception(id,posx,posy,score,step,deadline)".
     * @param p
     * 			the perception perceived.
     */
    private void perceptionPerceived(Literal p) {
    	int x =  Integer.parseInt(p.getTerm(1).toString());
    	int y =  Integer.parseInt(p.getTerm(2).toString());
    	locationPerceived(x, y);
    	simStep = simStep + 1;

    	// updates cluster model
    	clModel.setStepcl(simStep);
    	new Thread((Runnable) clModel).start();
    }

    /** update the model with the agent location and share this information with team mates */
	private void locationPerceived(final int x, final int y) {
		Location  oldLoc = model.getAgPos(getMyId());
        if (oldLoc != null && !oldLoc.equals(new Location(x,y))) {
        	model.remove(WorldModel.AGENT, oldLoc);
        }
		if (oldLoc == null || !oldLoc.equals(new Location(x,y))) {
			try {
				model.setAgPos(getMyId(), x, y);
				model.incVisited(x, y);
				Message m = new Message("tell", null, null, "my_status("+x+","+y+")");
				broadcast(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** change broadcast to send messages to only my team mates */
    @Override
    public void broadcast(Message m) throws Exception {
    	String agName = getAgName();
    	if (agName.equals("leader")) {
    		// send message to cowboys
            for (int i=1; i <= numOfCowboys ; i++) {
            	String oname = "cowboy"+i;
                if (!getAgName().equals(oname)) {
                    Message msg = new Message(m);
                    msg.setReceiver(oname);
                    sendMsg(msg);
                }
            }
    	} else if (agName.startsWith("cowboy")) {
    		// send message to the leader
        	String oname = "leader";
        	if (!getAgName().equals(oname)) {
                Message msg = new Message(m);
                msg.setReceiver(oname);
                sendMsg(msg);
            }
    	}
    }

    @Override
    public void checkMail() {
        try {
            super.checkMail();
            // remove messages related to obstacles/cows/fences/corral/agent_position
            // and update the model
            Iterator<Message> im = getTS().getC().getMailBox().iterator();
            while (im.hasNext()) {
            	Message m  = im.next();
                String  ms = m.getPropCont().toString();
                // received "cell(x,y,content,contentAttr)"
                if (ms.startsWith("cell") && model != null) {
                	Literal p = (Literal)m.getPropCont();
                    int x = (int)((NumberTerm)p.getTerm(0)).solve();
                    int y = (int)((NumberTerm)p.getTerm(1)).solve();
                    String content = p.getTerm(2).toString();
                    String contentAttr = p.getTerm(3).toString();
                    // obstacle
                    if (content.equals("obstacle")) {
                    	if (model.inGrid(x,y) && !model.hasObject(WorldModel.OBSTACLE, x, y)) {
                            model.add(WorldModel.OBSTACLE, x, y);
                        }
                        im.remove();
                    // cow
                    } else if (content.equals("cow")) {
                    	if (model.inGrid(x,y) && !model.hasObject(WorldModel.COW, x, y)) {
                            model.add(WorldModel.COW, x, y);
                            // add believe "cow(x,y,cowId)"
                            /*
                            getTS().getAg().addBel(
                            		Literal.parseLiteral("cow(" + x +"," + y + "," + contentAttr + ")"));
							*/
                            int cowId = Integer.parseInt(contentAttr);
                            lastPerceivedCows.put(cowId, simStep);
                            cModel.insertCow(cowId,x,y, simStep);
                        }
                        im.remove();
                    // enemy
                    } else if (content.equals("agent") && contentAttr.equals("enemy")) {
                    	if (model.inGrid(x,y) && !model.hasObject(WorldModel.ENEMY, x, y)) {
                            model.add(WorldModel.ENEMY, x, y);
                        }
                        im.remove();
                    // fence
                    } else if (content.equals("fence")) {
                    	int fence = WorldModel.CLOSED_FENCE;
                		if (contentAttr.equals("true")){
                			fence = WorldModel.OPEN_FENCE;
                		}
                		if (model.inGrid(x,y) && !model.hasObject(fence, x, y)) {
                			if (content.equals("true") && !model.hasObject(WorldModel.CLOSED_FENCE, x, y)) {
                				model.remove(WorldModel.CLOSED_FENCE, x, y);
                			} else if (content.equals("false") && !model.hasObject(WorldModel.OPEN_FENCE, x, y)) {
                				model.remove(WorldModel.OPEN_FENCE, x, y);
                			}
                            model.add(fence, x, y);
                        }
                        im.remove();
                    // corral
                    } else if (content.equals("corral") && contentAttr.equals("enemy")) {
                    	if (model.inGrid(x,y) && !model.hasObject(WorldModel.ENEMYCORRAL, x, y)) {
                            model.add(WorldModel.ENEMYCORRAL, x, y);
                        }
                        im.remove();
                    // switch
                    } else if (content.equals("switch")) {
                    	if (model.inGrid(x,y) && !model.hasObject(WorldModel.SWITCH, x, y)) {
                            model.add(WorldModel.SWITCH, x, y);
                        }
                        im.remove();
                    // empty
                    } else if (content.equals("empty")) {
                    	if (model.inGrid(x,y) && !model.isFree(x, y)) {
                			model.add(WorldModel.CLEAN, x, y);
                		}
                        im.remove();
                    }
                } else if (ms.startsWith("my_status") && model != null) {
                	// update others location
                    Literal p = Literal.parseLiteral(m.getPropCont().toString());
                    int x = (int)((NumberTerm)p.getTerm(0)).solve();
                    int y = (int)((NumberTerm)p.getTerm(1)).solve();
                    if (model.inGrid(x,y)) {
                        try {
                            int agid = getAgId(m.getSender());
                            model.setAgPos(agid, x, y);
                            model.incVisited(x, y);

                            // update ally_pos(sender,x,y)
                            Literal tAlly = createLiteral("ally_pos", new Atom(m.getSender()),
                            		createNumber(x), createNumber(y));
    						getTS().getAg().addBel( tAlly );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    im.remove(); 
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking email!",e);
        }
    }

    public static int getAgId(String agName) {
		if (agName.equals("leader")) {
			return 0;
		} else if (agName.equals("cheat")) {
			return 1;
		} else if (agName.equals("cheat_helper")) {
			return 2;
		} else {
			return (Integer.parseInt(agName.substring(agName.length()-1)) + 2);
		}
	}

    public boolean hasfoundCow() {
    	for (Entry<Integer, Integer> entry : lastPerceivedCows.entrySet()) {
    		if (entry.getValue() > simStep - 10) {
    			return true;
    		}
    	}
    	return false;
    }

	public static Literal createCellPerception(int x, int y, Term obj) {
        return createLiteral("cell", createNumber(x), createNumber(y), obj); 
    }

    public int getMyId() {
        if (myId < 0) {
            myId = getAgId(getAgName());
        }
        return myId;
    }

	void setSimId(String id) {
        simId = id;
    }

    public String getSimId() {
        return simId;
    }

	public LocalWorldModel getModel() {
        return model;
    }

	public List<Fence> getFences() {
		return fences;
	}

	public void setFences(List<Fence> fences) {
		this.fences = fences;
	}

	public static boolean isCheat_passed() {
		return cheat_passed;
	}

	public static void setCheat_passed(boolean cheat_passed) {
		CowboyArch.cheat_passed = cheat_passed;
	}

	public static boolean isHelper_in_position() {
		return helper_in_position;
	}

	public static void setHelper_in_position(boolean helper_in_position) {
		CowboyArch.helper_in_position = helper_in_position;
	}

	public static boolean isCheat_in_position() {
		return cheat_in_position;
	}

	public static void setCheat_in_position(boolean cheat_in_position) {
		CowboyArch.cheat_in_position = cheat_in_position;
	}

}
