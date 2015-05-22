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
	
	public static final int MAX_SIZE = 61; //TODO change back to 161, 
		//suggesting change to 80*2 + 1 + 4 = 165 (because of view port)
	
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
//		/*comment out this line for slowed play
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
		if (in == 'T' && hasAxe == true) c = 'c';
		
		//finishing code to save the state to use next time
		switch(c) {
		case 'l': orientation--; break; //think right to up which is 1 to 0
		case 'r': orientation++; break; //think up to right which is 0 to 1
		case 'f':
			// NEW STUFF
			Help.arrayShift(Agent.previousPos);
			Agent.previousPos[0] = new Point(Agent.pos.x, Agent.pos.y);
			//
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
		
		//TODO make it cleaner, there is a lot of ifs here.
		
		
		LinkedList<Character> defaultList = new LinkedList<Character>(Arrays.asList('.', '~', '*', 'T'));
		if (hasAxe) defaultList.remove(new Character('T')); //because its not a problem anymore
		if (inBoat) defaultList.remove(new Character('~'));
		
		if (hasGold) { //then go home
			//get move then return
			
			LinkedList<Point> trail = Help.bfs4Char('H', 0, defaultList);
			if (!trail.isEmpty()) {
				Point p = trail.get(1);
				System.out.println("Have gold, going home, " + pos.x + "|" + pos.y +", " + trail);
				
				if(getInfrontPoint().equals(p)) {
					return 'f';
				} else {
					return 'l';
				}
			}
		}
		
		//try for goal
		if (goldPos != null) { //we have seen it

			LinkedList<Point> trail = Help.bfs4Char('g', 0, defaultList);
			if (trail.isEmpty()) {
				//we can't reach the goal without breaking '*'s now...
				defaultList.remove(new Character('*'));
				
				trail = Help.bfs4Char('g', 2, defaultList);
				
				if (trail.size() == 1) { //only pos and dest
					System.out.println("Going against knowledge and using bomb, " + pos.x + "|" + pos.y);
					if (getInfrontChar() == '*') {
						return 'b'; //use a bomb
					} else {
						return 'l'; //or rotate till we get there
					}
				}
				
				Point p = trail.get(1);
				System.out.println("Know gold exists but can't get there, " + pos.x + "|" + pos.y +", " + p);
				
				if(getInfrontPoint().equals(p)) {
					return 'f';
				} else {
					return 'l';
				}
			}
			
			Point p = trail.get(1);
			System.out.println("Know gold exists, " + pos.x + "|" + pos.y +", " + p);
			
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
		}
		
		if (hasAxe && gridContains('T')) {
			LinkedList<Point> trail = Help.bfs4Char('T', 0, defaultList);
			Point p = trail.get(1);
			System.out.println("Have axe on route to T, " + pos.x + "|" + pos.y +", " + p);
			
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
		}
		
		//if can see an item try to go to it
		//else if can explore some reachable '?'
		//else
			//try axe
			//try boat
			//try bombs
		
		if (gridContains('a')) {
			LinkedList<Point> trail = Help.bfs4Char('a', 0, defaultList);
			Point p = trail.get(1);
			System.out.println("Getting axe, " + pos.x + "|" + pos.y +", " + p);
			
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
		}
		
		if (gridContains('d')) {
			LinkedList<Point> trail = Help.bfs4Char('d', 0, defaultList);
			Point p = trail.get(1);
			System.out.println("Getting dynamite," + pos.x + "|" + pos.y +", " + p);
			
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
		}
		
		if (gridContains('B')) {
			LinkedList<Point> trail = Help.bfs4Char('B', 0, defaultList);
			Point p = trail.get(1);
			System.out.println("Finding boat," + pos.x + "|" + pos.y +", " + p);
			
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
		}
		
		if (inBoat) {
			defaultList.remove(new Character('~'));
			LinkedList<Point> trail = Help.bfs4Char('?', 0, defaultList);
			if (Stuck()) {
				defaultList.add(new Character(' '));
				trail = Help.bfs4Char('?', 0, defaultList);
				defaultList.remove(new Character(' '));
			}
			Point p = trail.get(1);
			System.out.println("Using boat looking for ?s," + pos.x + "|" + pos.y +", " + p);
			
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
		}
		
		//default explore: 
		
		//TODO something about trying to uncover all ?s even if you can't just reach them (2 blocks away)
		LinkedList<Point> trail = Help.bfs4Char('?', 0, defaultList);
		if (trail.isEmpty()) {
			//i.e. the result didn't find a solution
			trail = Help.bfs4Char('?', 2, defaultList);
			Point p = trail.get(1);
			System.out.println("Looking for far reaching ?s," + pos.x + "|" + pos.y +", " + p);
			
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
			
		} else {
			Point p = trail.get(1);
			System.out.println("Looking for ?s," + pos.x + "|" + pos.y +", " + p);
			
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
		}
		
		//make sure this line is unreachable code
//		return 'x';
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
	
	void print(char view[][]) {
		
		// Modified view made by Ethan
		System.out.print("+");
		for (int i = 0; i < maxJ - minJ; i++) {
			System.out.print("-");
		}
		System.out.println("+");
		for (int i = minI; i <= maxI; i++) {
			System.out.print("|");
			for (int j = minJ; j < maxJ; j++) {
				System.out.print(view[i][j]);
			}
			System.out.println("|");
		}
		System.out.print("+");
		for (int i = 0; i < maxJ - minJ; i++) {
			System.out.print("-");
		}
		System.out.println("+");
		//
		
		/*System.out.print("+");
		for (int i = 0; i < view.length; i++) {
			System.out.print("-");
		}
		System.out.println("+");
		for (int i = 0; i < view.length; i++) {
			System.out.print("|");
			for (int j = 0; j < view[0].length; j++) {
				System.out.print(view[i][j]);
			}
			System.out.println("|");
		}
		System.out.print("+");
		for (int i = 0; i < view.length; i++) {
			System.out.print("-");
		}
		System.out.println("+");*/
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
