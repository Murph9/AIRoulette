import java.awt.Point;
import java.util.*;


public class State {
	
	/**
    Constructs an empty state.
    */
	public State(Point newNode, int gCost) {
		Point node = newNode;
		int g = gCost;
	}
	
	/**
	 * Gets the node that the state refers to
	 * @return the node
	 */
	public Point getNode() {
		return node;
	}
	
	/**
	 * Gets the g value
	 * @return the g value
	 */
	public int getGVal() {
		return g;
	}
	
	/**
	 * Sets the node that the state refers to
	 */
	public void setNode(Point newVal) {
		node = newVal;
	}
	
	/**
	 * Sets the g value
	 */
	public void setGVal(int newVal) {
		g = newVal;
	}
	
	private Point node;
	private int g;
}