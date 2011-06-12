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
        return super.isFree(x,y) && !hasObject(COW, x, y) && !hasObject(ENEMY, x, y)
        	&& !hasObject(CORRAL, x, y) && !hasObject(ENEMY_CORRAL, x, y);
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
}
