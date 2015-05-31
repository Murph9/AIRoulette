import java.awt.Point;
import java.util.*;

//class for helpful methods
public class Help {

	
	//int count of bombs (walls count as 1, ground nothing) 
	//heuristic is 0
	
	//will require some kind of convert to action sequence
		//note that it uses the java.awt.Point class which contains 2 ints
//	public static LinkedList<Point> aStar(Point start, Point dest, int maxWeight, LinkedList<Character> avoid) {
//		PriorityQueue<QueueNode<Point>> pq = new PriorityQueue<QueueNode<Point>>();
//		
//		HashSet<QueueNode<Point>> seen = new HashSet<QueueNode<Point>>();
//		
//		QueueNode<Point> popped = null;
//		Point curPoint = null;
//		
//		pq.add(new QueueNode<Point>(null, start, 0+0));
//		while (!pq.isEmpty()) {
//			popped = pq.poll();
//			curPoint = popped.e;
//			
//			if (!seen.contains(popped)) {
//				seen.add(popped);
//			}
//			
//			if (curPoint.equals(dest)) { //we are done here
//				break;
//			} else {
//				
//				for (Point p : getNeighbours(curPoint)) {
//					if (seen.contains(p)) {
//						continue;
//					}
//					
//					pq.add(new QueueNode<Point>(popped, p, 0+0));
//				}
//			}
//		}
//
//		LinkedList<Point> trail = new LinkedList<Point>();
//		QueueNode<Point> cur = popped;
//		while(cur != null) {
//			trail.addFirst(cur.e);
//			cur = cur.parent;	
//		}
//		
//		return trail;
//	}

	
	public static LinkedList<Point> bfs4CharThroughWall(Point start, char in, int maxWeight, LinkedList<Character> avoid) {
		if (avoid == null) {
			avoid = new LinkedList<Character>(); //just so its searchable but useless (so you can give it null)
		}

		LinkedList<Point> Q = new LinkedList<Point>();
		HashMap<Point, Point> parentMap = new HashMap<Point, Point>();
		
		Point v = null;
		boolean foundSolution = false;
		
		Q.add(start); //enqueue
		while (!Q.isEmpty()) {
			v = Q.poll(); //dequeue
			
			//process v
			char array[] = {in};
			if (isCharWithinRange(v, array, 0)) {
				foundSolution = true;
				break;
			}
			
			for (Point p : getNeighbours(v)) {
				if (avoid.contains(Agent.grid[p.y][p.x])) {
					continue;
				}

				LinkedList<Character> temp = getPath(parentMap, start, p);
				int occurrences = Collections.frequency(temp, '*');
				if (occurrences > maxWeight) {
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
	
	
	private static LinkedList<Character> getPath(HashMap<Point, Point> map, Point start, Point v) {
		LinkedList<Character> trail = new LinkedList<Character>();
		trail.add(Agent.grid[v.y][v.x]);

		while(!v.equals(start) && map.containsKey(v)) {
			trail.add(Agent.grid[map.get(v).y][map.get(v).x]);
			v = map.get(v);
		}
		
		return trail;
	}
	
//	private static int getWeightofPath(HashMap<Point, Point> map, Point start, Point currentP) {
//		int weight = 0;
//
//		while(currentP != null && !currentP.equals(start)) {
//			if (Agent.grid[currentP.y][currentP.x] == '*') {
//				weight++;
//			}
//			currentP = map.get(currentP);
//		}
//		if (weight > 1) System.exit(0);
//		return weight;
//	}
	
	
	//special case of bfs that just looks for the closest given char, and returns the first move Point
	/**
	 * @param in char to search to
	 * @param offset distance to char that is acceptable
	 * @param avoid list of chars that act as walls in the search
	 * @return
	 */
	public static LinkedList<Point> bfs4Chars(Point start, char in[], int offset, LinkedList<Character> avoid) {
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
	
	private static boolean isCharWithinRange(Point p, char c[], int range) {
		for (int k = 0; k < c.length; k++) {
			if (range < 1 && Agent.grid[p.y][p.x] == c[k]) { //so its the thing im searching for
				return true;
			}
		}
		
		for (int i = -range; i < range+1; i++) {
			for (int j = -range; j < range+1; j++) {
				for (int k = 0; k < c.length; k++) {
					if (c[k] == Agent.grid[p.y+i][p.x+j]) {
						return true;
					}					
				}
			}
		}
		
		return false;
	}
	

	// looks for a point
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
			
			if (v.x == end.x && v.y == end.y) {
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
//	static class QueueNode<E> implements Comparable<QueueNode<E>> {
//		QueueNode<E> parent;
//		E e;
//		double weight; //although its probably an int
//		
//		QueueNode (QueueNode<E> parent, E e, double weight) {
//			this.parent = parent;
//			this.e = e;
//			this.weight = weight;
//		}
//		
//		@Override
//		public int compareTo(QueueNode<E> arg0) {
//			if (this.weight <= arg0.weight) {
//				return -1;
//			}
//			return 1;
//		}
//	}
	
	// A star search.  MaxWeight is the number of bombs the agent has
	public static LinkedList<Point> aStarSearch(Point from, char to, int maxWeight, LinkedList<Character> avoid) {
        Queue<State> openSet = new PriorityQueue<State>(1,
        		new Comparator<State>() {

				@Override
				public int compare(State cost1, State cost2) {
					return cost1.getGVal() - cost2.getGVal();
				}
        });
        
        ArrayList<State> closedSet = new ArrayList<State>();
        LinkedList<Point> trail = new LinkedList<Point>();
        HashMap<Point, Point> parentMap = new HashMap<Point, Point>();
        
        State first = new State(from, 0);
        openSet.add(first);

        while (!openSet.isEmpty()) {
        	State current = new State(new Point(0, 0), 0);
        	current.setNode(openSet.peek().getNode());
        	current.setGVal(openSet.peek().getGVal());
        	openSet.remove();
        	
        	closedSet.add(current);
        	
        	char array[] = {to};
        	if (isCharWithinRange(current.getNode(), array, 0)) {
        		trail.add(current.getNode());
        		while(!current.getNode().equals(from)) {
        			trail.addFirst(parentMap.get(current.getNode()));
        			current.setNode(parentMap.get(current.getNode()));
        		}
        		break;
        	}
        	
        	for (Point p : getNeighbours(current.getNode())) {
				if (avoid.contains(Agent.grid[p.y][p.x])) {
					continue;
				}
				
				State temp = new State(p, current.getGVal());
				if (Agent.grid[p.y][p.x] == '*') {
					temp.setGVal(current.getGVal() + 100);
				}
				if (Agent.grid[p.y][p.x] == ' ' || Agent.grid[p.y][p.x] == '~') {
					temp.setGVal(current.getGVal() + 1);
				}
            	
            	if (checkClosedSet(closedSet, temp.getNode()) == false 
            			&& checkOpenSet(openSet, temp.getNode(), temp.getGVal()) == false) {
            		if (!parentMap.containsKey(p)) {
            			openSet.add(temp);
    					parentMap.put(p, current.getNode());
    				}
            	}
			}
        }
        
        int bombsNeeded = 0;
        
        
        for (int j = 0; j < trail.size(); j++) {
        	if (Agent.grid[trail.get(j).y][trail.get(j).x] == '*') {
        		bombsNeeded++;
        	}
        	if (bombsNeeded > maxWeight) {
        		trail.clear();
        	}
        }
            
        return trail;
    }
    
    /**
     * Checks the open set for a node, and updates it with a better value if found
     * @param set the open set
     * @param name name of the city
     * @param gVal g value of the path
     * @return true if found, false otherwise
     */
    private static boolean checkOpenSet (Queue<State> set, Point name, int gVal) {
    	ArrayList<State> list = new ArrayList<State>();
    	State temp = new State(new Point(0, 0), 0);
    	int i = 0;
    	
    	while (!set.isEmpty()) {
    		list.add(set.remove());
    	}
    	set.addAll(list);
    	
    	for(i = 0; i < list.size(); i++) {
    		temp = list.get(i);
    		
    		if(temp.getNode().x == name.x && temp.getNode().y == name.y && temp.getGVal() > gVal) {
    			set.remove(temp);
    			temp.setGVal(gVal);
    			set.add(temp);
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Determines if a State is in the closed set
     * @param set the closed set
     * @param name the name of the state
     * @return true if found false otherwise
     */
    private static boolean checkClosedSet (ArrayList<State> set, Point name) {
    	int i = 0;
    	
    	for (i = 0; i < set.size(); i++) {
    		if (set.get(i).getNode().x == name.x && set.get(i).getNode().y == name.y) {
    			return true;
    		}
    	}
    	return false;
    }
}
