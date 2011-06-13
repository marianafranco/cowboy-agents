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

import env.CowboysEnv;

import arch.Fence;
import arch.WorldModel;
import arch.CowboyArch;
import busca.Nodo;

/** 
 * 
 * Wait
 *  
 * @author jomi
 */
public class sleep_some_time extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        Thread.sleep(500); // sleep 500 milliseconds
        return true;
    }
}

