/*********************************************
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2012
 */

import java.util.*;
import java.io.*;
import java.net.*;

public class Agent {

	public static final char[] options = {'f', 'l', 'r', 'c', 'b'}; 
		//lowercase because avoiding conflicts
	
	public static final char[] fieldIcons = {'T', 'a', '*', '~', 'd', 'B', 'g'};
		//T = tree, a = axe, * = wall, d = dynamite, ~ = water, B = boat, g = gold
	
	public static final char[] otherIcons = {'h'}; //defined by us
		//h = home, can't think of any more
	
	//suggested variables (i.e. variables that Bounty keeps as well)
	public boolean hasAxe;
	public boolean haskey;
	public boolean hasGold;
	public boolean inBoat;
	public int numBombs;
	
	//variables we might need that are guesses:
	public int orientation; //or something to know which direction we are facing
	public Cell[] grid; //the map (size to be determined)
	
	public int moveCount; //just really for debugging
	
	Agent() {
		moveCount = 0;
		numBombs = 0;
	}
	
	//our method
	public char get_action(char view[][]) {
		moveCount++;
		System.out.println(moveCount);
		
		/*//comment out this line for slowed play
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		*/
		
		Random rand = new Random(); //a random moving ai.
		
		char c = 0;
		while (true) {
			c = options[rand.nextInt(options.length)];
			if (c =='f' && view[1][2] == '~') {
				//its water don't move there
			} else {
				break;
			}
		}
		
		
		return c;
	}
	
	
	class Cell {		
		char type;
		//maybe put the search weight code here?
	}
	
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
