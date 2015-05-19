import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

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
	public static LinkedList<Point> bfs4Char(char in) {
		LinkedList<Point> Q = new LinkedList<Point>();
		HashMap<Point, Point> parentMap = new HashMap<Point, Point>();
		
		Point v = null;
		
		Q.add(Agent.pos); //enqueue
		while (!Q.isEmpty()) {
			v = Q.poll(); //dequeue
			
			//process v
			if (Agent.grid[v.y][v.x] == in) {
				break;
			}
						
			for (Point p : getNeighbours(v)) {
				if (Agent.grid[p.y][p.x] == '~' || Agent.grid[p.y][p.x] == '*') continue; 
					//can't transverse accross this yet
					//TODO check for '?'
				if (Agent.grid[p.y][p.x] == 'T' && !Agent.hasAxe) {
					continue;
				}
				
				if (!parentMap.containsKey(p)) {
					Q.add(p);
					parentMap.put(p, v);
				}
			}

		}
		LinkedList<Point> trail = new LinkedList<Point>();
		trail.add(v);

		while(!v.equals(Agent.pos)) {
			trail.addFirst(parentMap.get(v));
			v = parentMap.get(v);
		}
		
		return trail;
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
	


}
