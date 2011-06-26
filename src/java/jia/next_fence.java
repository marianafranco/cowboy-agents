package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.Random;
import java.util.logging.Level;

import arch.Fence;
import arch.LocalWorldModel;
import arch.CowboyArch;
import busca.Nodo;

/** 
 * 
 * Moves the agent to the enemy's fence
 *  
 * @author Mariana Ramos Franco, Rafael Barbolo Lopes
 */
public class next_fence extends DefaultInternalAction {
	
	public static int last_x=0, last_y=0, steps=0;
	public static long last_time_pos_changed = System.currentTimeMillis();

	int[]      actionsOrder = { 1, 2, 3, 4, 5, 6, 7, 8 }; // initial order of actions
    Random     random = new Random();
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            String sAction = "skip";
                        
            CowboyArch agent = ((CowboyArch)ts.getUserAgArch());
            
            if (agent.getFences().size() != 0) {
            	Fence next_fence = agent.getFences().get(0);
            	
            	LocalWorldModel model = agent.getModel();
            	
            	// current position
            	int iagx = (int)((NumberTerm)terms[0]).solve();
                int iagy = (int)((NumberTerm)terms[1]).solve();
                
                // desired position
                int itox = next_fence.cheatX();
                int itoy = next_fence.cheatY();
                
                if (itox == iagx && itoy == iagy)
                	CowboyArch.setCheat_in_position(true);
                else
                	CowboyArch.setCheat_in_position(false);
                
                // check if helper is holding the fence open
                if (CowboyArch.isHelper_in_position()) {
                	if (agent.getFences().size() > 1)
                		next_fence = agent.getFences().get(1);
                	else
                		next_fence = agent.getFences().remove(0);
                	itox = next_fence.cheatX();
                    itoy = next_fence.cheatY();
                    if (CowboyArch.isCheat_in_position()) {
                    	steps = 0;
                    }
                }
                
             // workaround to not get stucked in obstacles
                if (!CowboyArch.isCheat_in_position() && System.currentTimeMillis() - last_time_pos_changed > 10000) {
                	double random = Math.random();
                	if (random < 0.25) {
                		itox = iagx + 1;
                		itoy = iagy;
                	}
                	else if (random < 0.5) {
                		itox = iagx - 1;
                		itoy = iagy;
                	}
                	else if (random < 0.75) {
                		itox = iagx;
                		itoy = iagy + 1;
                	}
                	else {
                		itox = iagx;
                		itoy = iagy - 1;
                	}
                }
                
                if (last_x != iagx || last_y != iagy) {
                	last_time_pos_changed = System.currentTimeMillis();
                	steps += 1;
                }
                else {
                	double random = Math.random();
                	if (random < 0.5)
                		itox = iagx;
                	else
                		itoy = iagy;
                }
                
                last_x = iagx;
                last_y = iagy;
                
                if (steps > 10)
                	CowboyArch.setCheat_passed(false); // reset
                else if (steps > 5)
                	CowboyArch.setCheat_passed(true);
               
                
                //ts.getLogger().info("Current position: " + iagx + "," + iagy);
                //ts.getLogger().info("Moving to: " + itox + "," + itoy);
                
                if (model.inGrid(itox,itoy)) {
                    // destination should be a free place
                    while (!model.isFreeOfObstacle(itox,itoy) && itox > 0) itox--;
                    while (!model.isFreeOfObstacle(itox,itoy) && itox < model.getWidth()) itox++;

                    Location from = new Location(iagx, iagy);
                    Location to   = new Location(itox, itoy);
                    
                    // randomly change the place of two actions in actionsOrder
                    int i1 = random.nextInt(4);
                    int i2 = random.nextInt(4);
                    int temp = actionsOrder[i2];
                    actionsOrder[i2] = actionsOrder[i1];
                    actionsOrder[i1] = temp;
                    
                    SearchOld astar    = new SearchOld(model, from, to, actionsOrder, true);
                    Nodo   solution = astar.search();
                    if (solution != null) {
                        String ac = astar.firstAction(solution);
                        if (ac != null) {
                            sAction = ac;
                        }
                    } else {
                        //ts.getLogger().info("No route from "+from+" to "+to+"!"+"\n"); //+model);
                    }
                }
                
                //ts.getLogger().info("Action: " + sAction);
                
                un.unifies(terms[2], new NumberTermImpl(itox));
                un.unifies(terms[3], new NumberTermImpl(itoy));
                un.unifies(terms[4], new Atom(sAction));
                
            } else {
            	Thread.sleep(5000); // wait 5 seconds because there's nothing more to do
            }
            
            return true;
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "next fence error: "+e, e);         
        }
        return false;
    }
}

