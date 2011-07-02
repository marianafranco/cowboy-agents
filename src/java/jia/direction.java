package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.Random;
import java.util.logging.Level;

import arch.CowboyArch;
import arch.LocalWorldModel;
import busca.Nodo;
import arch.WorldModel;

/**
 * 
 * Gives the direction (up, down, left, right) towards some location.
 * Uses A* for this task.
 * Use: jia.direction(+Xo,+Yo,+Xd,+Yd,-Dir);
 * Where: (Xo,Yo) is the origin and (Xd, Yd) is the destiny. D is the direction deserved.
 * @author jomi
 */
public class direction extends DefaultInternalAction {

    WorldModel.Move[] actionsOrder = new WorldModel.Move[WorldModel.nbActions];
    Random     random = new Random();

    private static final RandomEnum<WorldModel.Move> r =
        new RandomEnum<WorldModel.Move>(WorldModel.Move.class);

    public direction() {
        for (int i=0; i<WorldModel.nbActions; i++) {
            actionsOrder[i] = Search.defaultActions[i];
        }
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            String sAction = "skip";

            Nodo solution = findPath(ts, terms);

            if (solution != null) {
                WorldModel.Move m = Search.firstAction(solution);
                if (m != null)
                    sAction = m.toString();
            } else {
                ts.getLogger().info("No route from "+ terms[0]+","+terms[1] +" to "+ terms[2]+","+terms[3]+"!"); //+ ((CowboyArch)ts.getUserAgArch()).getModel());
                // TODO: changed to return a random move
                sAction = r.random().toString();
            }
            return un.unifies(terms[4], new Atom(sAction));
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "direction error: " + e, e);
        }
        return false;
    }

    protected Nodo findPath(TransitionSystem ts, Term[] terms) throws Exception {
        CowboyArch arch = (CowboyArch)ts.getUserAgArch();
        LocalWorldModel model = arch.getModel();

        Nodo solution = null;

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
            int i1 = random.nextInt(WorldModel.nbActions);
            int i2 = 0; // more radical change, the selected action will be the first, previously: random.nextInt(WorldModel.nbActions);
            WorldModel.Move temp = actionsOrder[i2];
            actionsOrder[i2] = actionsOrder[i1];
            actionsOrder[i1] = temp;

            boolean fencesAsObs = terms.length > 5  && terms[5].toString().equals("fences");
            // if an agent is inside the corral, corral must be a path, no do not consider corra as obstacle here
            Search astar    = new Search(model, from, to, actionsOrder, true, false, true, false, false, fencesAsObs, arch);
            solution = astar.search();

            if (solution == null && !fencesAsObs) {
                // Test impossible path
                Search s = new Search(model, from, to, arch); // search without agent/cows as obstacles
                int fixtimes = 0;
                while (s.search() == null && arch.isRunning() && fixtimes < 100) { // the number should be great enough to set all corral as obstacles 
                    fixtimes++; // to avoid being in this loop forever 
                    // if search is null, it is impossible in the scenario to goto n, set it as obstacle  
                    ts.getLogger().info("[direction] No possible path to "+to+" setting as obstacle.");
                    arch.obstaclePerceived(to.x, to.y);
                    //model.add(WorldModel.OBSTACLE, to);
                    to = model.nearFree(to, null);
                    s = new Search(model, from, to, arch);
                }

                // run A* again
                astar    = new Search(model, from, to, actionsOrder, true, false, true, false, false, false, arch);
                solution = astar.search();
            }
        }        
        return solution;
    }
    
    private static class RandomEnum<E extends Enum> {

        private static final Random RND = new Random();
        private final E[] values;

        public RandomEnum(Class<E> token) {
            values = token.getEnumConstants();
        }

        public E random() {
            return values[RND.nextInt(values.length)];
        }
    }
}