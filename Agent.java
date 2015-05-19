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

	public static final char[] options = {'f', 'l', 'r'};//, 'c', 'b'};
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
	
	public boolean hasGold;
	public Point goldPos;
	
	//variables we might need that are guesses:
	public int orientation; //or something to know which direction we are facing
		//0 = up, 1 = right, 2 = down, 3 = left  (so clockwise)
		//starts on 0, so the inital direction is up
	public static Point pos; //current position of the agent
	
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
			char ni = getInfrontChar();
			if (ni == 'T' || ni == '*' || ni == '~') break; //we won't move so don't change the agent pos
			if (ni == 'g') hasGold = true;
			if (ni == 'a') hasAxe = true;
			if (ni == 'b') numBombs++;
			
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
		

		if (hasGold) { //then go home
			//get move then return
			
			LinkedList<Point> trail = Help.bfs4Char('H');
			Point p = trail.get(1);
			System.out.println("Have gold, going home, " + pos.x + "|" + pos.y +", " + trail);
			
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
		}
		
		//try for goal
		if (goldPos != null) { //we have seen it
			//TODO needs check if can reach goal
			LinkedList<Point> trail = Help.bfs4Char('g');
			Point p = trail.get(1);
			System.out.println("Know gold exists, " + pos.x + "|" + pos.y +", " + p);
			
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
		}
		
		if (hasAxe && gridContains('T')) { //TODO gets lost on looking for 'T'
			LinkedList<Point> trail = Help.bfs4Char('T');
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
			LinkedList<Point> trail = Help.bfs4Char('a');
			Point p = trail.get(1);
			System.out.println("Getting axe, " + pos.x + "|" + pos.y +", " + p);
			
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
		}
		
		if (gridContains('d')) {
			LinkedList<Point> trail = Help.bfs4Char('d');
			Point p = trail.get(1);
			System.out.println("Getting dynamite," + pos.x + "|" + pos.y +", " + p);
			
			if(getInfrontPoint().equals(p)) {
				return 'f';
			} else {
				return 'l';
			}
		}
		
		
		//default explore: 
		
		//TODO something about trying to uncover all ?s even if you can't reach them
		LinkedList<Point> trail = Help.bfs4Char('?');
		Point p = trail.get(1);
		System.out.println("Default looking for ?s," + pos.x + "|" + pos.y +", " + p);
		
		if(getInfrontPoint().equals(p)) {
			return 'f';
		} else {
			return 'l';
		}
		
		
		//TODO: if we can't find a clean path to a ? look for places within 2 of a ?
		
		//unreachable code here
//		return 'x';
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
		System.out.print("+");
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
