package arch;

/**
 * 
 * @author Mariana Ramos Franco, Rafael Barbolo Lopes
 */
public class Fence {
	
	private String access_from;
	private int x, y;
	
	public Fence(String access_from, int x, int y) {
		super();
		this.x = x;
		this.y = y;
		this.access_from = access_from;
	}
	
	public int cheatX() {
		if (this.access_from.equals("left")) {
			return this.x - 1;
		}
		if (this.access_from.equals("right")) {
			return this.x + 1;
		}
		return this.x;
	}
	
	public int cheatY() {
		if (this.access_from.equals("top")) {
			return this.y - 1;
		}
		if (this.access_from.equals("bottom")) {
			return this.y + 1;
		}
		return this.y;
	}
	
	public int cheatHelperX() {
		if (this.access_from.equals("left")) {
			return this.x + 1;
		}
		if (this.access_from.equals("right")) {
			return this.x - 1;
		}
		return this.x;
	}
	
	public int cheatHelperY() {
		if (this.access_from.equals("top")) {
			return this.y + 1;
		}
		if (this.access_from.equals("bottom")) {
			return this.y - 1;
		}
		return this.y;
	}
	
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public String getAccess_from() {
		return access_from;
	}
	public void setAccess_from(String access_from) {
		this.access_from = access_from;
	}

}
