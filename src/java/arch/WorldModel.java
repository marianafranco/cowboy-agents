package arch;
import jason.environment.grid.Area;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

/**
 * Class used to model the scenario.
 * 
 * @author Mariana Ramos Franco
 */
public class WorldModel extends GridWorldModel {

	// an agent is 0000010 and an obstacle is 000100 (defined on the GridWorldModel)
	public static final int COW = 16;
	public static final int ENEMY  = 32;
    public static final int CORRAL = 64;
    public static final int TARGET = 128; // one agent target location
    public static final int ENEMY_CORRAL = 256;
    public static final int OPEN_FENCE = 512;
    public static final int CLOSED_FENCE = 1024;
    public static final int SWITCH = 2048;

    Area corral;

    int[][] visited; // count the visited locations
    int minVisited = 0; // min value for near least visited

	public WorldModel(int w, int h, int nbAgs) {
		super(w, h, nbAgs);
		visited = new int[getWidth()][getHeight()];
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                visited[i][j] = 0;
            }
        }
	}

	public void setCorral(int corralx0, int corralx1, int corraly0, int corraly1) {
		Location topLeft = new Location(corralx0, corraly0);
		Location bottomRight = new Location(corralx1, corraly1);
	    for (int y = topLeft.y; y <= bottomRight.y; y++)
            for (int x = topLeft.x; x <= bottomRight.x; x++) {
                add(CORRAL, x, y);
            }
	    corral = new Area(topLeft, bottomRight);
    }

	public Area getCorral() {
        return corral;
    }

	@Override 
    public boolean isFree(int x, int y) {
        return super.isFree(x,y) && !hasObject(COW, x, y) && !hasObject(ENEMY, x, y);
    }

	@Override 
    public boolean isFree(Location l) {
		return isFree(l.x, l.y);
	}

	public void incVisited(Location l) {
    	incVisited(l.x,l.y);
    }

    public void incVisited(int x, int y) {
    	visited[x][y] += 2;
        for (int r = 1; r <= 8; r++) {
            for (int c = x - r; c <= x + r; c++){
            	for (int l = y - r; l <= y + r; l++) {
            		if (inGrid(c,l)) {
                    	visited[c][l] += 1;
                    }
            	}
            }
    	}
    }

    public int getVisited(Location l) {
        return visited[l.x][l.y];
    }

    /** returns the near location of x,y that was least visited */
    public Location getNearLeastVisited(int agx, int agy) {
        Location agloc = new Location(agx,agy);        
        while (true) {
            int x = agx;
            int y = agy;
            int w = 1; 
            int dx = 0;
            int dy = 0;
            int stage = 1;
            Location better = null;
            
            while (w < getWidth()) {
                switch (stage) {
                    case 1: if (dx < w) {
                                dx++;
                                break;
                            } else {
                                stage = 2; 
                            }
                    case 2: if (dy < w) {
                                dy++;
                                break;
                            } else {
                                stage = 3;
                            }
                    case 3: if (dx > 0) {
                                dx--;
                                break;
                            } else {
                                stage = 4;
                            }
                    case 4: if (dy > 0) {
                                dy--;
                                break;
                            } else {
                                stage = 1;
                                x--;
                                y--;
                                w += 2;
                            }
                }
                
                Location l = new Location(x+dx,y+dy);
                if (isFree(l) && !l.equals(agloc)) {
                    if (visited[l.x][l.y] < minVisited) { // a place better then minVisited! go there
                        return l;
                    } if (visited[l.x][l.y] == minVisited) { // a place in the minVisited level
                        if (better == null) {
                            better = l;
                        } else if (l.distance(agloc) < better.distance(agloc)) {
                            better = l;
                        } else if (l.distance(agloc) == better.distance(agloc) && random.nextBoolean()) { // to chose ramdomly equal options
                            better = l;
                        }
                    }
                }
            } // end while

            if (better != null) {
                return better;
            }
            minVisited++;
        }
    }
}
