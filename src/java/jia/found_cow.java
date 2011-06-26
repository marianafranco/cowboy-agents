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
 * Verify if a cow was seen in the last 10 steps.
 * @author Mariana Ramos Franco
 */
public class found_cow extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
    	 return ((CowboyArch)ts.getUserAgArch()).hasfoundCow();
    }
}