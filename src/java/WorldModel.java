import jason.environment.grid.GridWorldModel;

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

	public WorldModel(int w, int h, int nbAgs) {
		super(w, h, nbAgs);
	}
}
