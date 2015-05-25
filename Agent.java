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
	
	// NEW VARIABLE
	public static Point[] previousPos;
	public static final int PREVS = 6;
	
	public static final int MAX_SIZE = 165; 
		//max size 80*2 + playerpos 1 + viewport 4 = 165
	
	public static char grid[][]; //the map (size to be determined)
	
	public int moveCount; //just really for debugging
	
	Agent() {
		moveCount = 0;
		numBombs = 0;
		orientation = 0;
		goldPos = null;
		pos = new Point(MAX_SIZE/2, MAX_SIZE/2); //should = 80 if (MAX_SIZE == 161)
		previousPos = new Point[PREVS];
		Help.initArray(previousPos);
		
		//Variables to make the printout cleaner
		minI = MAX_SIZE;
		minJ = MAX_SIZE;
		maxI = 0;
		maxJ = 0;
		
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
		
		fillGrid(view); //fill in grid.
		print(grid);
		char c = computeAction(); //important part
		
		char in = getInfrontChar(); //override the computed action if we are facing a tree with an axe
		if (in == 'T' && hasAxe == true) {
			c = 'c';
		}
		
		//finishing code to save the state to use next time
		switch(c) {
		case 'l': orientation--; break; //think right to up which is 1 to 0
		case 'r': orientation++; break; //think up to right which is 0 to 1
		case 'f':
			// add things to the previous pos array
			Help.arrayShift(Agent.previousPos);
			Agent.previousPos[0] = new Point(Agent.pos.x, Agent.pos.y); //avoid giving the reference
			
			char ni = getInfrontChar();
			if (ni == 'T' || ni == '*') break; //we won't move so don't change the agent pos
			if (ni == '~' && !inBoat) 	break; //see water, but no boat, stop
			if (ni == 'g') hasGold = true; goldPos = null;
			if (ni == 'a') hasAxe = true;
			if (ni == 'b') numBombs++;
			if (ni == 'B') inBoat = true;
			if (ni == ' ' && inBoat) inBoat = false;
			
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
		System.out.println("Chose: " + c + ", Infront '" + getInfrontChar() +"', Move counter: " + moveCount);
		return c;
	}
	
	//the meat of the code
	private char computeAction() {
		
		//if there is no path to where they want to go, try for using boat on path
		//where try using a boat is:
			//TODO if there is water in your path, find the boat that is in that body of water
		//else go to the next thing on the list
	
		//expected list:
		/* hasGold == true aim for 'H' got goal going home
		 * try for goal
		 * get the axe
		 * get bomb
	 * explore (for ?s) 
		 * 	then with in 2 range
		 *
		 * cut Trees (lumberjack mode)
		 * 
		 * see if using bombs help you get to goal
		 * see if using bombs help you to get an item
		 * 
		 * see if using bombs help you to get ?s
		 * 
		 * random or something?
		 */
		LinkedList<Character> defaultList = new LinkedList<Character>(Arrays.asList('.', '~', '*', 'T'));
		if (hasAxe) defaultList.remove(new Character('T')); //because its not a problem anymore
		if (inBoat) defaultList.remove(new Character('~'));
		
		
		if (hasGold) {
			char out = getPath(new char[]{'H'}, defaultList);
			if (out != 0) {
				return out;
			}
		}
		
		if (goldPos != null) {
			char out = getPath(new char[]{'g'}, defaultList);
		if (out != 0) {
				return out;
			}
		}
		
		char out = getPath(new char[]{'a', 'd', '?', 'T'}, defaultList);
		if (out != 0) {
			return out;
	}
		
		
		LinkedList<Point> path = Help.bfs4Char(pos, '?', 2, defaultList);
		if (!path.isEmpty()) {
			Point p = path.get(1);
			System.out.println("Looking for: ?");
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
		}
		
		out = getPath(new char[]{'B'}, defaultList);
		if (out != 0) {
			return out;
		}
		
		System.out.println("so random:?");
		Random r = new Random();
		return options[r.nextInt(3)];
		
		//get paths for bombs:
			//see if you can use bombs to get you to goal
			//see if you can use bombs to get an item
			//see if you can use bombs to get a ?

		
		//make sure this line is unreachable code
//		return 'x';
	}
	
	private char getPath(char searchingFor[], LinkedList<Character> avoid) {
		LinkedList<Character> a = null;
		
		for (int i = 0; i < searchingFor.length; i++) {
			a = new LinkedList<Character>(avoid);
			
			if (inBoat) {
				a.add(new Character(' '));
			}

			//try for path with out water 
			LinkedList<Point> trail = Help.bfs4Char(pos, searchingFor[i], 0, a);
			if (trail.size() > 0) {
				Point p = trail.get(1);
				Point d = trail.get(trail.size() - 1);
				System.out.println("Looking for: "+searchingFor[i] + ", At: "+ d.x + " " + d.y);
				if(getInfrontPoint().equals(p)) {
					return 'f';
				} else {
					return 'l';
				}
			}
			
			//try for path with water
//			a.remove(new Character('~'));
		
//			trail = Help.bfs4Char(pos, searchingFor[i], 0, a);
//			if (trail.size() > 0) {
//				Point p = trail.get(1);
//				if (grid[p.y][p.x] == '~') {
//					//next spot is water, find the boat for this water.
//					LinkedList<Character> av = new LinkedList<Character>(Arrays.asList(' ', '*', '.', 'T'));
//					LinkedList<Point> tempPath = Help.bfs4Char(p, 'B', 0, av);
//					
//					av.remove(new Character(' '));
//					av.add(new Character('~'));
//					p = tempPath.getLast();
//					tempPath = Help.bfs4Char(pos, grid[p.y][p.x], 0, av);
//
//					if (getInfrontPoint().equals(p)) {
//						return 'f';
//					} else {
//						return 'l';
//					}
//				}
//				
//				
//				if (getInfrontPoint().equals(p)) {
//					return 'f';
//				} else {
//					return 'l';
//				}
//			}
		}
		
		return 0; //default return for failure
	}
	
	// Checks if the agent is stuck leaving and entering a boat
	private boolean Stuck() {
		int count = 0;
		for (int i = 0; i < PREVS; i++) {
			// odd positions only
			if (i%2 == 1) {
				if (Agent.pos.x == Agent.previousPos[i].x && Agent.pos.y == Agent.previousPos[i].y) {
					count++;
				}
			}
		}
		if (count == PREVS/2) {
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

	/*
	private boolean gridContains(char in, char stayIn) {
		for (int i = 0; i < MAX_SIZE; i++) {
			for (int j = 0; j < MAX_SIZE; j++) {
				if (grid[i][j] == in) {
					return true;
				}
			}
		}
		return false;
	}*/
	
	void print(char view[][]) {
		
		// Modified view made by Ethan
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
//				agent.print_view(view); //COMMENT THIS OUT BEFORE SUBMISSION,
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
