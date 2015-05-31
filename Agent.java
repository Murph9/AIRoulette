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

/*
 * Group 55 - Ethan Morgan (z3459802, emor680) and Jake Murphy (z3461173, jhmu917)
 * Briefly describe how your program works, including any algorithms and data structures employed, and explain any design decisions you made along the way. 
 * 
 * Our program works in stages.  Using a Breadth First Search, it looks for a path to the following things, in order of priority:
 * 	Home (only if it has the gold)
 * 	gold (only if it knows where it is)
 * 	axe
 * 	bomb
 * 	Unknown area (denoted by ?)
 * Within its attempt to find a path, it first tries to find a path over land.  If unable, it attempts to find one using a boat, to go over water.
 * If it is still unable to find a clear path, it attempts to find one through walls, using a 0 heuristic AStar search algorithm to find the path
 * that goes through a few walls possible.
 * For the AStar algorithm, we had to create a State class, to hold the map points as well as the weight of the path taken so far.
 * A major change in out design was to change the code from attempting to find a new path using the BFS every time it moved, to finding a single path,
 * staying on it until it had finished its move and only then re-searching.  This change greatly increased the speed of our code, reducing the runtime.
 * 
 */

//	<Patrician> what does your robot do, sam ?
//	<bovril> it collects data about the surrounding environment, then discards it and drives into walls
//	- Bash.org quote #240849


public class Agent {

	public static final char[] options = {'f', 'l', 'r', 'c', 'b'};
		//f = forward, l = rotate left, r = rotate right, c = chop, b = bomb
		//lowercase because avoiding conflicts
	
	public static final char[] fieldIcons = {'T', 'a', '*', '~', 'd', 'B', 'g', '.'};
		//T = tree, a = axe, * = wall, d = dynamite, ~ = water, B = boat, g = gold, . = outside
	
	public static final char[] otherIcons = {'h', '?'}; //defined by us
		//h = home, ? - unknown, any more?
	
	//suggested variables (i.e. variables that Bounty keeps as well)
	public static boolean hasAxe;
	public static boolean haskey;
	public static boolean inBoat;
	public static int numBombs;
	public static int minI, minJ, maxI, maxJ;
	
	public boolean hasGold;
	public Point goldPos;
	
	//variables we might need that are guesses:
	public int orientation; //or something to know which direction we are facing
		//0 = up, 1 = right, 2 = down, 3 = left  (so clockwise)
		//starts on 0, so the inital direction is up
	
	public static Point pos; //current position of the agent
	public int moveCount; //just really for debugging
	public LinkedList<Point> path;
	
	public static final int MAX_SIZE = 165; 
		//max size 80*2 + playerpos 1 + viewport 4 = 165
	
	public static char grid[][]; //the map (size to be determined)
	
	Agent() {
		moveCount = 0;
		numBombs = 0;
		orientation = 0;
		goldPos = null;
		pos = new Point(MAX_SIZE/2, MAX_SIZE/2); //should = 80 if (MAX_SIZE == 161)
		
		//Variables to make the printout cleaner
		minI = MAX_SIZE;
		minJ = MAX_SIZE;
		maxI = 0;
		maxJ = 0;
		
		path = new LinkedList<Point>();
		
		grid = new char[MAX_SIZE][MAX_SIZE];
		for (int i = 0; i < MAX_SIZE; i++) {
			for (int j = 0; j < MAX_SIZE; j++) {
				grid[i][j] = '?'; //set it all to unknown
			}
		}
		
		grid[pos.y][pos.x] = 'H'; //home character :)
	}
	
	//our method
	public char get_action(char view[][]) {
		/*comment out this line for slowed play
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		*/

		//move the incoming view so we can interperet it
			//by doing this we are making all our variables compared to global position
		switch(orientation) {
//		case 0: //is doing nothing with its life..; break;
		case 1: 
			view = Help.rotateMatrixRight(view); break;
		case 2: 
			view = Help.rotateMatrixLeft(view); 
			view = Help.rotateMatrixLeft(view); break;
		case 3: 
			view = Help.rotateMatrixLeft(view); break;
		}
		
		fillGrid(view); //fill in grid
		//print(grid);
		
		char c;
		if (path.size() > 0) {
			Point p = path.getFirst();
			if (getInfrontPoint().equals(p)) {
				c = 'f';
				path.removeFirst();
			} else {
				c = 'l';
			}
		} else {
			getPath(); //important part
			if (path.size() > 0) {
				Point p = path.getFirst();
				if (getInfrontPoint().equals(p)) {
					c = 'f';
					path.removeFirst();
				} else {
					c = 'l';
				}
			} else {
				c = tryUsingBombs();
			}
			
			path.clear();
		}

		char in = getInfrontChar();
			//override the computed action if we are facing a tree with an axe
		if (in == 'T' && hasAxe == true) {
			c = 'c';
		}
		
		//finishing code to save the state to use next time
		switch(c) {
		case 'l': orientation--; break; //think right to up which is 1 to 0
		case 'r': orientation++; break; //think up to right which is 0 to 1
		case 'f':
			if (in == 'T' || in == '*') break; //we won't move so don't change the agent pos
			if (in == '~' && !inBoat) 	break; //see water, but no boat, stop
			if (in == 'g') hasGold = true; goldPos = null;
			if (in == 'a') hasAxe = true;
			if (in == 'd') numBombs++;
			if (in == 'B') inBoat = true;
			if (in == ' ' && inBoat) inBoat = false;
			
			switch(orientation) { //use your brain as left and down are the positive directions (its a grid)
			case 0: pos.y--; break;
			case 1: pos.x++; break;
			case 2: pos.y++; break;
			case 3: pos.x--; break;
			}
			break;
		case 'b':
			numBombs--; break;
		}
		
		if (orientation < 0) orientation = 3;
		if (orientation > 3) orientation = 0;
		
		moveCount++;
//		System.out.println("Chose: " + c + ", Infront '" + getInfrontChar() +"', Move counter: " + moveCount);
		
		if (numBombs < 0) {
//			System.out.println("Now is not the time to use that. (bomb)");
			System.exit(1); //then we should quit as this is a fail.
		}
		
		return c;
	}
	
	//the meat of the code
	private void getPath() {
		//Method list:
		/* hasGold == true aim for 'H' got goal going home
		 * try for goal
		 * get the axe
		 * get bomb
		 * explore (for ?s) 
		 * 	then with in 2 range
		 *
		 * see if using bombs help you get to goal (no need for home here, always guarenteed to get home)
		 * see if using bombs help you to get an item
		 * see if using bombs help you to get ?s
		 * 
		 * random
		 */
		LinkedList<Character> defaultList = new LinkedList<Character>(Arrays.asList('.', '~', '*', 'T', '?'));
		if (hasAxe) defaultList.remove(new Character('T')); //because its not a problem anymore
		if (inBoat) defaultList.remove(new Character('~'));
		boolean out;
		
		//go home if you have gold
		if (hasGold) {
			out = setPath(new char[]{'H'}, defaultList, 0);
			if (out) {
				return;
			}
		}
		
		//try to go to gold
		if (goldPos != null) {
			out = setPath(new char[]{'g'}, defaultList, 0);
			if (out) {
				return;
			}
		}
		defaultList.remove(new Character('?'));
		
		//look for items, then explore ?s you can reach
		out = setPath(new char[]{'a', 'd', '?'}, defaultList, 0);
//		out = setPath(new char[]{'?'}, defaultList);
		if (out) {
			return;
		}
		
		out = setPath(new char[]{'?'}, defaultList, 2);
//		out = setPath(new char[]{'?'}, defaultList);
		if (out) {
			return;
		}

		//look for far reaching ?s
		char array[] = {'?'};
		path = Help.bfs4Chars(pos, array, 2, defaultList);
		if (!path.isEmpty()) {
			return;
		}
		
	}
	
	private char tryUsingBombs() {
		LinkedList<Character> defaultList = new LinkedList<Character>(Arrays.asList('.', '~', 'T', '?'));
		if (hasAxe) defaultList.remove(new Character('T')); //because its not a problem anymore
		if (inBoat) defaultList.remove(new Character('~'));
		
		//look to use bombs
		char temp = getPathThroughWall(new char[]{'g', 'a', 'd', '?'}, defaultList);
		if (temp != 0) {
			return temp;
		}
		
		return '0';
	}

	
	
	private boolean setPath(char searchingFor[], LinkedList<Character> avoid, int offset) {
		LinkedList<Character> a = null;
		int waterIndex = 0;
		
		a = new LinkedList<Character>(avoid);
		
		//try for path with out water
		LinkedList<Point> trail = Help.bfs4Chars(pos, searchingFor, offset, a);
		waterIndex = getWaterIndex(trail);
		if (trail.size() > 0) {
			Point p = trail.get(1);
			
			// For when you are in a boat next to a shoreline
			if (inBoat && waterIndex != 0) {
				if (grid[p.y][p.x] == new Character(' ') || grid[p.y][p.x] == new Character('T')) {
					if (checkBodyConnect(pos, trail.get(waterIndex))) {
						// if the water you are on is connected to a body of water further in your trail, go there via water
						LinkedList<Character> av = new LinkedList<Character>(Arrays.asList('.', ' ', '*', 'T'));
						trail = Help.bfs4Point(pos, trail.get(waterIndex), 0, av);
					}
				}
			}
			trail.removeFirst();
			path = trail;
			return true;
		}
		
		//try for path with water
		if (!gridContains('B')) {
			return false;
		}
		a.remove(new Character('~'));
	
		trail = Help.bfs4Chars(pos, searchingFor, offset, a);
		if (trail.size() > 0) {
			
			Point p;
			waterIndex = getWaterIndex(trail);
			if (waterIndex != 0) {
				p = trail.get(waterIndex);
				LinkedList<Character> av = new LinkedList<Character>(Arrays.asList(' ', '*', '.', 'T'));
				LinkedList<Point> tempPath;
				Point boatSpot = findBoat(p);
				if (boatSpot != null) {
					tempPath = Help.bfs4Point(p, boatSpot, 0, av);
				} else {
					char array[] = {'B'};
					tempPath = Help.bfs4Chars(p, array, offset, av);
				}
				if (tempPath.size() < 1) {
					return false;
				}
				av.remove(new Character(' '));
				av.add(new Character('~'));
				p = tempPath.getLast();
				tempPath = Help.bfs4Point(pos, p, 0, av);
				if (tempPath.size() < 1) {
					return false;
				}
				
				tempPath.removeFirst();
				path = tempPath;
				return true;
			}
			p = trail.get(1);
			trail.removeFirst();
			path = trail;
			return true;
		}
		
		return false;
	}
	
	
	//looks through walls
	private char getPathThroughWall(char searchingFor[], LinkedList<Character> avoid) {
		
		LinkedList<Character> a = null;
		int waterIndex = 0;
		
		for (int i = 0; i < searchingFor.length; i++) {
			a = new LinkedList<Character>(avoid);

			//try for path with out water
			LinkedList<Point> trail = Help.aStarSearch(pos, searchingFor[i], numBombs, a);
			//LinkedList<Point> trail = Help.bfs4CharThroughWall(pos, searchingFor[i], numBombs, a);
			waterIndex = getWaterIndex(trail);
			if (trail.size() > 0) {
				Point p = trail.get(1);
				
				if (inBoat && waterIndex != 0) {
					if (grid[p.y][p.x] == new Character(' ') || grid[p.y][p.x] == new Character('T')) {
						if (checkBodyConnect(pos, trail.get(waterIndex))) {
							// if the water you are on is connected to a body of water further in your trail, go there via water
							LinkedList<Character> av = new LinkedList<Character>(Arrays.asList('.', ' ', '*', 'T'));
							trail = Help.bfs4Point(pos, trail.get(waterIndex), 0, av);
							p = trail.get(1);
						}
					}
				}
				
				if(getInfrontPoint().equals(p)) {
					if (grid[p.y][p.x] == '*'){
						return 'b';
					}
					return 'f';
				} else {
					return 'l';
				}
			}
			
			//try for path with water
			if (!gridContains('B')) {
				continue;
			}
			a.remove(new Character('~'));
		
			trail = Help.aStarSearch(pos, searchingFor[i], numBombs, a);
  			if (trail.size() > 0) {
  				
  				Point p;
  				waterIndex = getWaterIndex(trail);
				if (waterIndex != 0) {
					p = trail.get(waterIndex);
					//next spot is water, find the boat for this water.
					LinkedList<Character> av = new LinkedList<Character>(Arrays.asList(' ', '*', '.', 'T'));
					LinkedList<Point> tempPath;
					Point boatSpot = findBoat(p);
					if (boatSpot != null) {
						tempPath = Help.bfs4Point(p, boatSpot, 0, av);
					} else {
						char array[] = {'B'};
						tempPath = Help.bfs4Chars(p, array, 0, av);
					}
					if (tempPath.size() < 1) {
						continue;
					}
					av.remove(new Character(' '));
					av.add(new Character('~'));
					p = tempPath.getLast();
					tempPath = Help.bfs4Point(pos, p, 0, av);
					if (tempPath.size() < 1) {
						continue;
					}
					p = tempPath.get(1);
					
					if (getInfrontPoint().equals(p)) {
						if (grid[p.y][p.x] == '*'){
							return 'b';
						}
						return 'f';
					} else {
						return 'l';
					}
				}
				p = trail.get(1);
				
				if (getInfrontPoint().equals(p)) {
					return 'f';
				} else {
					return 'l';
				}
			}
		}
		
		
		return 0;
	}
	
	// Gets the index of the first water tile in the trail
	private int getWaterIndex(LinkedList<Point> trail) {
		for (int i = 1; i < trail.size(); i++) {
			Point p = trail.get(i);
			if (grid[p.y][p.x] == '~') {
				return i;
			}	
		}
		return 0;
	}
	
	// Finds a boat for a water tile
	private Point findBoat(Point waterTile) {
		LinkedList<Character> avoid = new LinkedList<Character>(Arrays.asList('.', ' ', '*', 'T'));
		char array[] = {'B'};
		LinkedList<Point> tempPath = Help.bfs4Chars(waterTile, array, 0, avoid);
		Point p = null;
		if (tempPath.size() > 0) {
			p = tempPath.getLast();
		}
		return p;
		
	}
	
	// Should check if two waters tiles are on the same body of water
	private boolean checkBodyConnect(Point a, Point b) {
		LinkedList<Character> avoid = new LinkedList<Character>(Arrays.asList('.', ' ', '*', 'T'));
		LinkedList<Point> tempPath = Help.bfs4Point(a, b, 0, avoid);
		if (tempPath.size() > 0) {
			return true;
		}
		return false;
	}
	
	//so we don't kill ourselves by moving forward
	private char getInfrontChar() {
		switch(orientation) { //use your brain as left and up are the positive direction
		case 0: return grid[pos.y-1][pos.x]; //middle is [apos.y][apos.x]
		case 1: return grid[pos.y][pos.x+1];
		case 2: return grid[pos.y+1][pos.x];
		case 3: return grid[pos.y][pos.x-1];
		}
		return '?';
	}
	
	private Point getInfrontPoint() {
		switch(orientation) { //use your brain as left and up are the positive direction
		case 0: return new Point(pos.x, pos.y-1);
		case 1: return new Point(pos.x+1, pos.y);
		case 2: return new Point(pos.x, pos.y+1);
		case 3: return new Point(pos.x-1, pos.y);
		}
		return null;
	}
	
	//fills in our world grid.
	private void fillGrid(char[][] view) {
		int x = pos.x;
		int y = pos.y;
		
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				grid[y+i-2][x+j-2] = view[i][j];
				
				// Stuff to help with printing out
				minI = ((y+i-2) < minI) ? y+i-2 : minI;
				minJ = ((x+j-2) < minJ) ? x+j-2 : minJ;
				maxI = ((y+i-2) > maxI) ? y+i-2 : maxI;
				maxJ = ((x+j-2) > maxJ) ? x+j-2 : maxJ;
				//
				
				if (grid[y+i-2][x+j-2] == 'g') {
					goldPos = new Point(x+j-2, y+i-2);
				}
			}
		}
		switch(orientation) {
		case 0: grid[y][x] = '^'; break;
		case 1: grid[y][x] = '>'; break;
		case 2: grid[y][x] = 'v'; break;
		case 3: grid[y][x] = '<'; break;
		}
		grid[MAX_SIZE/2][MAX_SIZE/2] = 'H'; //for home (its always here)
	}

	private boolean gridContains(char in) {
		for (int i = 0; i < MAX_SIZE; i++) {
			for (int j = 0; j < MAX_SIZE; j++) {
				if (grid[i][j] == in) {
					return true;
				}
			}
		}
		return false;
	}
	
	// Prints out the map the agent has of the world
	void print(char view[][]) {
		
		System.out.print("+");
		for (int i = 0; i <= maxJ - minJ; i++) {
			System.out.print("-");
		}
		System.out.println("+");
		for (int i = minI; i <= maxI; i++) {
			System.out.print("|");
			for (int j = minJ; j <= maxJ; j++) {
				System.out.print(view[i][j]);
			}
			System.out.println("|");
		}
		System.out.print("+");
		for (int i = 0; i <= maxJ - minJ; i++) {
			System.out.print("-");
		}
		System.out.println("+");
	}

	//please don't touch below
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
		long time = System.currentTimeMillis();

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
				action = agent.get_action(view);
				out.write(action);
			}
		} catch (IOException e) {
			System.out.println(System.currentTimeMillis() - time);
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
