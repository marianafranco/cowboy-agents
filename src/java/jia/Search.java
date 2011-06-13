package jia;

import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import arch.WorldModel;
import busca.AEstrela;
import busca.Busca;
import busca.Estado;
import busca.Heuristica;
import busca.Nodo;

/**
 * Search a path to a target location and gives the direction
 * (north, south, west, south, ..).
 * 
 * @author Mariana Ramos Franco, Rafael Barbolo Lopes
 */
public class Search {

    final WorldModel 	  model;
    final Location        from, to;
    int[]                 actionsOrder;    
    int                   nbStates = 0;
    boolean         	  considerFenceAsObstacles;

    static final int[] defaultActions = { 1, 2, 3, 4, 5, 6, 7, 8 }; // initial order of actions

    Logger logger = Logger.getLogger(Search.class.getName());

    Search(WorldModel m, Location from, Location to, int[] actions, boolean considerFenceAsObstacles) {
        this.model = m;
        this.from  = from;
        this.to    = to;
        this.considerFenceAsObstacles = considerFenceAsObstacles;
        if (actions != null) {
            this.actionsOrder = actions;
        } else {
            this.actionsOrder = defaultActions;
        }
    }

    /** used normally to discover the distance from 'from' to 'to' */
    Search(WorldModel m, Location from, Location to) {
        this(m,from,to,null,false);
    }

    public Nodo search() throws Exception { 
        Busca searchAlg = new AEstrela();
        GridState root = new GridState(from, "initial", this);
        root.setIsRoot();
        return searchAlg.busca(root);
    }

    public String firstAction(Nodo solution) {
        Nodo root = solution;
        Estado prev1 = null;
        Estado prev2 = null;
        while (root != null) {
            prev2 = prev1;
            prev1 = root.getEstado();
            root = root.getPai();
        }
        if (prev2 != null) {
            return ((GridState)prev2).op;
        }
        return null;
    }
}


final class GridState implements Estado, Heuristica {

    // State information
    final Location      pos; // current location
    final String        op;
    final Search        ia;
    final int           hashCode;
    boolean             isRoot = false;

    public GridState(Location l, String op, Search ia) {
        this.pos = l;
        this.op  = op;
        this.ia  = ia;
        hashCode = pos.hashCode();
        
        ia.nbStates++;
    }

    public void setIsRoot() {
        isRoot = true;
    }

    public int custo() {
        return 1;
    }

    public boolean ehMeta() {
        return pos.equals(ia.to);
    }

    public String getDescricao() {
        return "Grid search";
    }

    public int h() {
        return pos.distance(ia.to);
    }

    public List<Estado> sucessores() {
        List<Estado> s = new ArrayList<Estado>(4);
        if (ia.nbStates > 50000) {
            ia.logger.info("*** It seems I am in a loop!");
            return s; 
        }

        // 8 directions
        for (int a = 0; a < 8; a++) {
            switch (ia.actionsOrder[a]) {
            case 1: suc(s,new Location(pos.x-1,pos.y),"west"); break;
            case 2: suc(s,new Location(pos.x+1,pos.y),"east"); break;
            case 3: suc(s,new Location(pos.x,pos.y-1),"north"); break;
            case 4: suc(s,new Location(pos.x,pos.y+1),"south"); break;
            case 5: suc(s,new Location(pos.x+1,pos.y-1),"northeast"); break;
            case 6: suc(s,new Location(pos.x+1,pos.y+1),"southeast"); break;
            case 7: suc(s,new Location(pos.x-1,pos.y-1),"northwest"); break;
            case 8: suc(s,new Location(pos.x-1,pos.y+1),"southwest"); break;
            }
        }

        // if it is root state, sort the option by least visited
        if (isRoot) {
            Collections.sort(s, new VisitedComparator(ia.model));
        }
        return s;
    }

    private void suc(List<Estado> s, Location newl, String op) {
        if (ia.model.isFree(newl)) {
        	if (ia.considerFenceAsObstacles && ia.model.hasObject(WorldModel.CLOSED_FENCE, newl.x, newl.y)) {
        		return;
        	}
        	s.add(new GridState(newl,op,ia));
        }
    }

    public boolean equals(Object o) {
        if (o != null && o instanceof GridState) {
            GridState m = (GridState)o;
            return pos.equals(m.pos);
        }
        return false;
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        return "(" + pos + "-" + op + ")"; 
    }
}

class VisitedComparator implements Comparator<Estado> {

    WorldModel model;
    VisitedComparator(WorldModel m) {
        model = m;
    }

    public int compare(Estado o1, Estado o2) {
        int v1 = model.getVisited(((GridState)o1).pos);
        int v2 = model.getVisited(((GridState)o2).pos);
        if (v1 > v2) return 1;
        if (v2 > v1) return -1;
        return 0;
    }
}
