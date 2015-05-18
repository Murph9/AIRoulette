/*********************************************
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2012
 */

import java.util.*;
import java.awt.Point;
import java.io.*;
import java.net.*;

public class Agent {

	public static final char[] options = {'f', 'l', 'r', 'c', 'b'}; 
		//lowercase because avoiding conflicts
	
	public static final char[] fieldIcons = {'T', 'a', '*', '~', 'd', 'B', 'g'};
		//T = tree, a = axe, * = wall, d = dynamite, ~ = water, B = boat, g = gold
	
	public static final char[] otherIcons = {'h', '?'}; //defined by us
		//h = home, ? - known space, any more?
	
	//suggested variables (i.e. variables that Bounty keeps as well)
	public boolean hasAxe;
	public boolean haskey;
	public boolean hasGold;
	public boolean inBoat;
	public int numBombs;
	
	//variables we might need that are guesses:
	public int orientation; //or something to know which direction we are facing
	
	public final int MAX_SIZE = 161;
	public char grid[][]; //the map (size to be determined)
	
	public int moveCount; //just really for debugging
	
	Agent() {
		moveCount = 0;
		numBombs = 0;
		
		grid = new char[MAX_SIZE][MAX_SIZE];
		
		for (int i = 0; i < MAX_SIZE; i++) {
			for (int j = 0; j < MAX_SIZE; j++) {
				grid[i][j] = '?'; //set it all to unknown
			}
		}
	}
	
	//our method
	public char get_action(char view[][]) {
		moveCount++;
		System.out.println(moveCount);
		/*comment out this line for slowed play
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		*/
		
		/* Possible Movement Logic?:
		 * 
		 * if got goal go home
		 * try for goal
		 * if item 
		 *   pick it up
		 * else if something like, check for unexplored areas
		 *   follow edge
		 *   fill in holes
		 * else
		 *   try use axe
		 *   try use boat
		 *   try use bombs
		 */
		
		
		Random rand = new Random(); //a random moving ai.
		
		char c = 0;
		while (true) {
			c = options[rand.nextInt(options.length)];
			if (c =='f' && view[1][2] == '~') {
				//its water don't move there
			} else if (c=='f' && view[1][2] == '*') {
			
			} else {
				break;
			}
		}
		
		return c;
	}
	
	//Priority queue node
	class QueueNode<E> implements Comparable<QueueNode<E>> {
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
	
	//will require some kind of convert to action sequence
		//note that it uses the java.awt.Point class which contains 2 ints
	//
	public LinkedList<Point> aStar(Point start, Point dest) {
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
				for (Point p : getNeightbours(curPoint)) {
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
	
	//gets the neighbours
	private LinkedList<Point> getNeightbours(Point in) {
		LinkedList<Point> out = new LinkedList<Point>();
	
		if (isInside(in.x+1, in.y)) out.add(new Point(in.x+1,in.y));
		if (isInside(in.x-1, in.y))	out.add(new Point(in.x-1,in.y));
		
		if (isInside(in.x, in.y+1))	out.add(new Point(in.x,in.y+1));
		if (isInside(in.x, in.y-1))	out.add(new Point(in.x,in.y-1));
		
		return out;
	}
	
	//simple checker
	private boolean isInside(int x, int y) {
		if (x > MAX_SIZE || x < 0) return false;
		if (y > MAX_SIZE || y < 0) return false;
		return true;
	}
	
	//found on the internets
	//http://stackoverflow.com/questions/42519/how-do-you-rotate-a-two-dimensional-array
	/*
	public int[][] rotateMatrixRight(int[][] matrix) {
	    // W and H are already swapped
	    int w = matrix.length;
	    int h = matrix[0].length;
	    int[][] ret = new int[h][w];
	    for (int i = 0; i < h; ++i) {
	        for (int j = 0; j < w; ++j) {
	            ret[i][j] = matrix[w - j - 1][i];
	        }
	    }
	    return ret;
	}
	
	
	public int[][] rotateMatrixLeft(int[][] matrix) {
	    // W and H are already swapped
	    int w = matrix.length;
	    int h = matrix[0].length;   
	    int[][] ret = new int[h][w];
	    for (int i = 0; i < h; ++i) {
	        for (int j = 0; j < w; ++j) {
	            ret[i][j] = matrix[j][h - i - 1];
	        }
	    }
	    return ret;
	}
	 */
	
	
	//////////////////////////////////////////////////////////////////
	//Don't touch below

	void print_view(char view[][]) {
		int i, j;

		System.out.println("\n+-----+");
		for (i = 0; i < 5; i++) {
			System.out.print("|");
			for (j = 0; j < 5; j++) {
				if ((i == 2) && (j == 2)) {
					System.out.print('^'); //player is always at 2,2; f would go to 1,2 
				} else {
					System.out.print(view[i][j]);
				}
			}
			System.out.println("|");
		}
		System.out.println("+-----+");
	}

	public static void main(String[] args) {
		InputStream in = null;
		OutputStream out = null;
		Socket socket = null;
		Agent agent = new Agent();
		char view[][] = new char[5][5];
		char action = 'F';
		int port;
		int ch;
		int i, j;

		if (args.length < 2) {
			System.out.println("Usage: java Agent -p <port>\n");
			System.exit(-1);
		}

		port = Integer.parseInt(args[1]);

		try { // open socket to Game Engine
			socket = new Socket("localhost", port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (IOException e) {
			System.out.println("Could not bind to port: " + port);
			System.exit(-1);
		}

		try { // scan 5-by-5 wintow around current location
			while (true) {
				for (i = 0; i < 5; i++) {
					for (j = 0; j < 5; j++) {
						if (!((i == 2) && (j == 2))) {
							ch = in.read();
							if (ch == -1) {
								System.exit(-1);
							}
							view[i][j] = (char) ch;
						}
					}
				}
				agent.print_view(view); // COMMENT THIS OUT BEFORE SUBMISSION,
										// TODO please
				action = agent.get_action(view);
				out.write(action);
			}
		} catch (IOException e) {
			System.out.println("Lost connection to port: " + port);
			System.exit(-1);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}
}
