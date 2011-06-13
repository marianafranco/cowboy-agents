package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.Random;
import java.util.logging.Level;

import arch.WorldModel;
import arch.CowboyArch;
import busca.Nodo;

/** 
 * Gives the direction (north, south, west, south, ..) towards some location.
 * 
 * @author Mariana Ramos Franco, Rafael Barbolo Lopes
 */
@SuppressWarnings("serial")
public class direction extends DefaultInternalAction {

    int[]      actionsOrder = { 1, 2, 3, 4, 5, 6, 7, 8 }; // initial order of actions
    Random     random = new Random();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            String sAction = "skip";
            CowboyArch comboy = (CowboyArch)ts.getUserAgArch();
            WorldModel model = ((CowboyArch)ts.getUserAgArch()).getModel();

            int iagx = (int)((NumberTerm)terms[0]).solve();
            int iagy = (int)((NumberTerm)terms[1]).solve();
            int itox = (int)((NumberTerm)terms[2]).solve();
            int itoy = (int)((NumberTerm)terms[3]).solve();

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

                Search astar    = new Search(model, from, to, actionsOrder, true); // fence as obstacle
                Nodo   solution = astar.search();
                if (solution != null) {
                    String ac = astar.firstAction(solution);
                    if (ac != null) {
                        sAction = ac;
                    }
                } else {
                	// search without fence as obstacle
                	astar = new Search(model, from, to, actionsOrder, false);
                	solution = astar.search();
                	if (solution == null) {
                		ts.getLogger().info("No route from "+from+" to "+to+"!"+"\n"+model);
                		comboy.obstaclePerceived(to.x, to.y,
                				Literal.parseLiteral("cell(" + to.x + "," + to.y + ",obstacle,null)"));
                	} else {
                		// need to open the fence
                		ts.getLogger().info("Need fence open on "+to+"!"+"\n"+model);
                		ts.getAg().addBel(Literal.parseLiteral("need_fence(" + to.x + "," + to.y +",open)"));
                	}
                }
            }
            return un.unifies(terms[4], new Atom(sAction));
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "direction error: "+e, e);         
        }
        return false;
    }
}

