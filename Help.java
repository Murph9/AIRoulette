import java.awt.Point;
import java.util.*;

//class for helpful methods
public class Help {

	
	//will require some kind of convert to action sequence
		//note that it uses the java.awt.Point class which contains 2 ints
	public static LinkedList<Point> aStar(Point start, Point dest) {
		PriorityQueue<QueueNode<Point>> pq = new PriorityQueue<QueueNode<Point>>();
		
		HashSet<QueueNode<Point>> seen = new HashSet<QueueNode<Point>>();
		
		QueueNode<Point> popped = null;
		Point curPoint = null;
		
		pq.add(new QueueNode<Point>(null, start, 0+0));
		while (!pq.isEmpty()) {
			popped = pq.poll();
			curPoint = popped.e;
			
			if (!seen.contains(popped)) {
				seen.add(popped);
			}
			
			if (curPoint.equals(dest)) { //we are done here
				break;
			} else {
				for (Point p : getNeighbours(curPoint)) {
					if (!seen.contains(p)) {
						pq.add(new QueueNode<Point>(popped, p, 0));
					}
				}
			}
		}

		LinkedList<Point> trail = new LinkedList<Point>();
		QueueNode<Point> cur = popped;
		while(cur != null) {
			trail.addFirst(cur.e);
			cur = cur.parent;	
		}
		
		return trail;
	}

	
	//special case of bfs that just looks for the closest given char, and returns the first move Point
	/**
	 * @param in char to search to
	 * @param avoid list of chars that act as walls in the search
	 * @return
	 */
	/**
	 * @param in char to search to
	 * @param offset distance to char that is acceptable
	 * @param avoid list of chars that act as walls in the search
	 * @return
	 */
	public static LinkedList<Point> bfs4Char(Point start, char in, int offset, LinkedList<Character> avoid) {
		if (avoid == null) {
			avoid = new LinkedList<Character>(); //just so its search able but useless
		}

		LinkedList<Point> Q = new LinkedList<Point>();
		HashMap<Point, Point> parentMap = new HashMap<Point, Point>();
		
		Point v = null;
		boolean foundSolution = false;
		
		Q.add(start); //enqueue
		while (!Q.isEmpty()) {
			v = Q.poll(); //dequeue
			
			//process v
			if (isCharWithinRange(v, in, offset)) {
				foundSolution = true;
				break;
			}
						
			for (Point p : getNeighbours(v)) {
				if (avoid.contains(Agent.grid[p.y][p.x])) {
					continue;
				}
				
				if (!parentMap.containsKey(p)) {
					Q.add(p);
					parentMap.put(p, v);
				}
			}
			//avoid.remove(new Character(' '));//so it doesn't never get off land
		}

		if (!foundSolution) { //then we may have a problem
			return new LinkedList<Point>();
		}
		
		
		LinkedList<Point> trail = new LinkedList<Point>();
		trail.add(v);

		while(!v.equals(start)) {
			trail.addFirst(parentMap.get(v));
			v = parentMap.get(v);
		}
		
		return trail;
	}
	
	private static boolean isCharWithinRange(Point p, char c, int range) {
		if (range < 1 && Agent.grid[p.y][p.x] == c) { //so its the thing im searching for
			return true;
		}
		
		for (int i = -range; i < range+1; i++) {
			for (int j = -range; j < range+1; j++) {
				if (c == Agent.grid[p.y+i][p.x+j]) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	//just in case we need it (don't think this version works, look at 4char)
	public static LinkedList<Point> bfs(Point start, Point dest) {
		LinkedList<Point> q = new LinkedList<Point>();
		LinkedList<Point> neig = new LinkedList<Point>();
		HashMap<Point, Point> parentMap = new HashMap<Point, Point>();
		
		Point cur = null;
		
		q.add(start);
		while (!q.isEmpty()) {
			cur = q.poll();
			
			neig = getNeighbours(cur);
			if (neig.contains(dest)) {
				parentMap.put(dest, cur);
				break;
			}
			
			for (Point p : neig) {
				if (Agent.grid[p.y][p.x] == '*') {
					continue;
				}
				
				if (!parentMap.containsKey(p)) {
					parentMap.put(p, cur);
					q.add(p);
				}
			}
		}
		
		LinkedList<Point> trail = new LinkedList<Point>();
		trail.add(dest);
		cur = dest;
		
		while (cur != start) {
			trail.addFirst(parentMap.get(cur));
			cur = parentMap.get(cur);
		}
		
		return trail;
	}
	
	
	//gets the neighbours
	public static LinkedList<Point> getNeighbours(Point in) {
		LinkedList<Point> out = new LinkedList<Point>();
	
		if (isInside(in.x+1, in.y)) out.add(new Point(in.x+1,in.y));
		if (isInside(in.x-1, in.y))	out.add(new Point(in.x-1,in.y));
		
		if (isInside(in.x, in.y+1))	out.add(new Point(in.x,in.y+1));
		if (isInside(in.x, in.y-1))	out.add(new Point(in.x,in.y-1));
		
		return out;
	}
	
	//simple grid checker
	public static boolean isInside(int x, int y) {
		if (x >= Agent.MAX_SIZE || x < 0) return false;
		if (y >= Agent.MAX_SIZE || y < 0) return false;
		return true;
	}


	
	//found on the internets
	//http://stackoverflow.com/questions/42519/how-do-you-rotate-a-two-dimensional-array
	public static char[][] rotateMatrixRight(char[][] matrix) {
	    int w = matrix.length; // W and H are already swapped
	    int h = matrix[0].length;
	    char[][] ret = new char[h][w];
	    for (int i = 0; i < h; ++i) {
	        for (int j = 0; j < w; ++j) {
	            ret[i][j] = matrix[w - j - 1][i];
	        }
	    }
	    return ret;
	}
	
	public static char[][] rotateMatrixLeft(char[][] matrix) {
	    int w = matrix.length; // W and H are already swapped
	    int h = matrix[0].length;   
	    char[][] ret = new char[h][w];
	    for (int i = 0; i < h; ++i) {
	        for (int j = 0; j < w; ++j) {
	            ret[i][j] = matrix[j][h - i - 1];
	        }
	    }
	    return ret;
	}


	//Priority queue node
	static class QueueNode<E> implements Comparable<QueueNode<E>> {
		QueueNode<E> parent;
		E e;
		double weight; //although its probably an int
		QueueNode (QueueNode<E> parent, E e, double weight) {
			this.parent = parent;
			this.e = e;
			this.weight = weight;
		}
		
		@Override
		public int compareTo(QueueNode<E> arg0) {
			if (this.weight <= arg0.weight) {
				return -1;
			}
			return 1;
		}
	}
	
	//Initialises array
	public static void initArray(Point[] array) {
		Point emptyPoint = new Point(-1, -1);
		for (int i = 0; i < array.length; i++) {
			array[i] = emptyPoint;
		}
	}
	
	// Shifts all elements of an array 1 to the left
	public static void arrayShift(Point[] array) {
		for (int i = array.length - 1; i > 0; i--) {
			array[i] = array[i-1];
		}
		
	}
	
	// TEMPORARY
	public static LinkedList<Point> bfs4Point(Point start, Point end, int offset, LinkedList<Character> avoid) {
		if (avoid == null) {
			avoid = new LinkedList<Character>(); //just so its search able but useless
		}

		LinkedList<Point> Q = new LinkedList<Point>();
		HashMap<Point, Point> parentMap = new HashMap<Point, Point>();
		
		Point v = null;
		boolean foundSolution = false;
		
		Q.add(start); //enqueue
		while (!Q.isEmpty()) {
			v = Q.poll(); //dequeue
			
			//process v
			for (Point p : getNeighbours(v)) {
				if (p.x == end.x && p.y == end.y) {
					foundSolution = true;
				}
			}
			
			if (foundSolution) {
				break;
			}
						
			for (Point p : getNeighbours(v)) {
				if (avoid.contains(Agent.grid[p.y][p.x])) {
					continue;
				}
				
				if (!parentMap.containsKey(p)) {
					Q.add(p);
					parentMap.put(p, v);
				}
			}
		}

		if (!foundSolution) { //then we may have a problem
			return new LinkedList<Point>();
		}
		
		
		LinkedList<Point> trail = new LinkedList<Point>();
		trail.add(v);

		while(!v.equals(start)) {
			trail.addFirst(parentMap.get(v));
			v = parentMap.get(v);
		}
		
		return trail;
	}
}
