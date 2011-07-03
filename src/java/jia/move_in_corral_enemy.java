package jia;

import java.util.Random;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;
import arch.CowboyArch;
import arch.WorldModel;
import arch.WorldModel.Move;

/**
 * Give a random direction to keep the agent inside the corral enemy.
 * Use: jia.move_in_corral_enemy(+X,+Y,-D);
 * Where: X and Y are the agent positions and D the random direction inside the
 * corral enemy.
 * 
 * @author Mariana Ramos Franco
 */
public class move_in_corral_enemy extends DefaultInternalAction {
    
	private static final RandomEnum<WorldModel.Move> r =
        new RandomEnum<WorldModel.Move>(WorldModel.Move.class);

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        int x = (int)((NumberTerm)terms[0]).solve(); 
        int y = (int)((NumberTerm)terms[1]).solve();

    	Move action = r.random();
    	if (isInCorralEnemy(ts, action, x, y)) {
    		return un.unifies(terms[2], new Atom(action.toString()));
    	} else {
    		return un.unifies(terms[2], new Atom("skip"));
    	}
    }

    private static boolean isInCorralEnemy(TransitionSystem ts, Move action, int x, int y) {
    	WorldModel model = ((CowboyArch)ts.getUserAgArch()).getModel();
    	Location newLocation = null;
    	switch (action) {
        	case west     : newLocation = new Location(x-1,y);
        	case east     : newLocation = new Location(x+1,y);
        	case north    : newLocation = new Location(x,y-1);
        	case northeast: newLocation = new Location(x+1,y-1);
        	case northwest: newLocation = new Location(x-1,y-1);
        	case south    : newLocation = new Location(x,y+1);
        	case southeast: newLocation = new Location(x+1,y+1);
        	case southwest: newLocation = new Location(x-1,y+1);
        	case skip	  : newLocation = new Location(x,y);
        }
    	if (model.hasObject(WorldModel.ENEMYCORRAL, newLocation)) {
    		return true;
    	}
    	return false;
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

