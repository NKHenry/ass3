/*********************************************
 *  Agent.java 
 *  Nathaniel Henry z3419400 Submission
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2017
 *  
 *  Program Description 
 *  
 *  The primary search algorithm used is djikstra's, I implemented
 *  two versions of this; 1 searches for a specific point e.g.(80,80), the
 *  other searches for any space that contains a particular value e.g. 'a'.
 *  I attempted to modify my search into an A* but found that adding non 0 heuristics
 *  were 
 *  
 *  My overarching strategy is to search the map as much as possible and gather
 *  the most amount of information possible before attempting to reach the gold. 
 *  When picking a goal to search for the agent looks at the objects it has seen
 *  and will attempt to reach the most valuable goal. If a search fails the goal 
 *  will be adjusted and the search is run again.
 *  
 *  The Agent class holds similar information to that of the supplied Raft.java
 *  class. The purpose of this is so that i can keep track of the game state and
 *  play the game and plan ahead within my own Agent class with identical behavior 
 *  to that of the game engine. I added some boolean flags that indicate whether 
 *  certain objects have been seen, allowing it to pick goals when searching. 
 *  
 *  The only other additional class used is the Node class. This is used
 *  when executing Djikstra's. The Node holds an Agent, cost and current
 *  position. By keeping an Agent within each node we can easily find what
 *  moves legal from that particular state, track move history, and also keep 
 *  track of any resources that we have used on the path to that state. The cost
 *  simply represents the number of different points on the map we have reached,
 *  not the number of moves.
 *  
 *  
*/
package ass3;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.Point;


public class Agent {

  final static int EAST   = 0;
  final static int NORTH  = 1;
  final static int WEST   = 2;
  final static int SOUTH  = 3;
  final static int START = 80;
  
  private int rowPos;
  private int colPos;
  private int dir;
  
  private char map[][];
  
  private int heuristic;
  private String moveHistory;
  private String todo;
  
  private boolean searching;
  private boolean found_treasure;
  private boolean found_axe;
  private boolean found_key;
  private int dynamites_seen;
  
  private boolean have_axe; //+500
  private boolean have_key; //+100
  private boolean have_treasure; //+1000
  private boolean have_raft; //+100
  private boolean on_raft; 
  private int num_dynamites; // +50
  
   private boolean off_map      = false;

   private boolean game_won     = false;
   private boolean game_lost    = false;
  
  public Agent ()
  {
    map = new char[160][160];
    int i;
    int j;
    // intiialise the map to be unknown
    for (i=0; i < 160; i++) {
      for (j=0; j < 160; j++) {
        map[i][j] = 'z';
      }
    }
    rowPos = START;
    colPos = START;
    dir = NORTH;
  
    searching = true;
    found_treasure = false;
    found_axe = false;
    found_key = false;
    dynamites_seen = 0;
    
    heuristic = 0;
    moveHistory = "";
    todo = "";
    
    have_axe     = false;
    have_key     = false;
    have_treasure= false;
    have_raft    = false;
    on_raft      = false;
    num_dynamites    = 0;
    
    off_map   = false;
    game_won  = false;
    game_lost = false;
  }
  
  /**
   * This function chooses the next move our agent will take. 
   * @param view - the current view of our agent that was sent from the game engine
   * @return character representing the next desired move
   */
  public char get_action( char view[][] ) {

    updateMap(view);  
    
    if (searching && on_raft) {
       this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'z', false, true);
       if (this.todo == "") {
          searching = false;
       }
    }
    
    if (have_treasure && todo.length() == 0) {
       this.todo = djikstra(new Point(this.rowPos, this.colPos), new Point(START,START), false, false);
       if (this.todo == "" && num_dynamites > 0) {
          this.todo = djikstra(new Point(this.rowPos, this.colPos), new Point(START,START), true, false);
       }
    }
    if (this.todo.length() == 0 && found_key && !have_key) {
       this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'k', false, false);
       if (this.todo == "" && num_dynamites > 0) {
          this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'k', true, false);
        }
    }
    if (this.todo.length() == 0 && dynamites_seen > num_dynamites) {
       this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'd', false, false);
       if (this.todo == "" && num_dynamites > 0) {
          this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'd', true, false);
        }
    }
    if (this.todo.length() == 0 && found_axe && !have_axe) {
       this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'a', false, false);
       if (this.todo == "" && num_dynamites > 0) {
          this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'a', true, false);
       }
    }
    if (this.todo.length() == 0 && found_treasure && !have_treasure) {
       this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), '$', false, false);
       if (this.todo == "" && num_dynamites > 0) {
          this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), '$', true, false);
       }
    }
    if (todo.length() == 0) {
       this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'z', false, false);
       if (this.todo == "" && num_dynamites > 0) {
          this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'z', true, false);
       }
    } 
    if (todo.length() == 0 && !have_raft && have_axe) {
       this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'T', false, false);
       if (this.todo == "" && num_dynamites > 0) {
          this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'T', true, false);
       }
    }
    
    //sometimes in the initial stages we may return a goal position that is actually illegal
    //once we receive more information. This makes sure we don't kill ourselves baesd on a
    //previous uninformed search
    while (this.todo.length() != 0) {
        char c = todo.charAt(0);
        todo = todo.substring(1);
        Agent test = this.cloneAgent();
        boolean legal = test.apply(c);
        if (legal && !test.game_lost) {
           apply(c);
           return c;
        }
        if (searching && on_raft) {
           this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'z', false, true);
           if (this.todo == "") {
              searching = false;
           }
        }
        
        if (have_treasure && todo.length() == 0) {
           this.todo = djikstra(new Point(this.rowPos, this.colPos), new Point(START,START), false, false);
           if (this.todo == "" && num_dynamites > 0) {
              this.todo = djikstra(new Point(this.rowPos, this.colPos), new Point(START,START), true, false);
           }
        }
        if (this.todo.length() == 0 && found_key && !have_key) {
           this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'k', false, false);
           if (this.todo == "" && num_dynamites > 0) {
              this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'k', true, false);
            }
        }
        if (this.todo.length() == 0 && dynamites_seen > num_dynamites) {
           this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'd', false, false);
           if (this.todo == "" && num_dynamites > 0) {
              this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'd', true, false);
            }
        }
        if (this.todo.length() == 0 && found_axe && !have_axe) {
           this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'a', false, false);
           if (this.todo == "" && num_dynamites > 0) {
              this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'a', true, false);
           }
        }
        if (this.todo.length() == 0 && found_treasure && !have_treasure) {
           this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), '$', false, false);
           if (this.todo == "" && num_dynamites > 0) {
              this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), '$', true, false);
           }
        }
        if (todo.length() == 0) {
           this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'z', false, false);
           if (this.todo == "" && num_dynamites > 0) {
              this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'z', true, false);
           }
        } 
        if (todo.length() == 0 && !have_raft && have_axe) {
           this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'T', false, false);
           if (this.todo == "" && num_dynamites > 0) {
              this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'T', true, false);
           }
        }     
     
    }
     
     return 'f';
  }
  
  /**
   * @return int - the current row position of agent
   */
  public int getRowPos() {
     return rowPos;
  }
  
  /**
   * 
   * @return int - the current column position of agent
   */
  public int getColPos () {
     return colPos;
  }
  
  /**
   * 
   * @return String - the moveHistory of this agent (for use during searching)
   */
  public String getMoveHistory() {
     return moveHistory;
  }


  /**
   * Implementation of Djikstra in which we know the exact position of our goal
   * this is used for returning to the starting position
   * If the search is unsuccessful we return an empty string
   * @param start - Point representing the starting position of search
   * @param goal - Point that is the goal position
   * @param allow_d - whether we are allowed to use dynamite on this search
   * @param water_search - whether we are allowed to leave the water
   * @return String representing the moves needed to reach the goal
   */
  String djikstra(Point start, Point goal, boolean allow_d, boolean water_search) {
     HashMap<Point, Point> from = new HashMap<Point,Point>();
     HashMap<Point, Integer> dist = new HashMap<Point, Integer>();
     HashSet<Point> visited = new HashSet<Point>();
     PriorityQueue<Node> q = new PriorityQueue<Node>();
     
     int moves[][] = {{-1,0}, {1,0}, {0,-1}, {0,1}};
     int j;
     int i = 0;
     int maxPops = 20000; //maximum number of iterations before we cancel the search
     
     Node first = new Node(start, 0, 0, this.cloneAgent());
     Node curr = first;
     dist.put(start, 0);
     q.add(first);
     
     while (!q.isEmpty() && i < maxPops) {
        curr = q.poll();
        
        if (curr.getPos().equals(goal)) {
           break;
        }
        if (visited.contains(curr.getPos())) {
           continue;
        }
        visited.add(curr.getPos());
        
        //check and add surrounding squares to the queue
        for (j=0; j < 4; j++) {
           int x = (int) curr.getPos().getX() + moves[j][0];
           int y = (int) curr.getPos().getY() + moves[j][1];
           if (curr.getA().canMove(x,y, allow_d, water_search)) {
              Point p = new Point(x,y);
              Agent a = curr.getA().cloneAgent();
              int dRow = x - a.getRowPos();
              int dCol = y - a.getColPos();
              String turn = "";
              if (dRow == -1) {
                 turn = a.moveSquare(NORTH);
              }
              else if (dRow == 1) {
                 turn = a.moveSquare(SOUTH);
              }
              else if (dCol == -1) {
                 turn = a.moveSquare(WEST);
              }
              else if (dCol == 1) {
                 turn = a.moveSquare(EAST);
              }
              boolean broken = false;
              for (char c : turn.toCharArray()) {
                 boolean legal = a.apply(c);
                 if (!legal) {
                    broken = true;
                 }
              }
              if (broken) {
                 continue;
              }
              a.moveHistory += turn;
                          
              Node toAdd = new Node(p, curr.getCost()+1, 0, a);
              if (!(from.containsKey(p) && dist.get(p) < toAdd.getCost())) {
                 from.put(p, curr.getPos());
                 dist.put(p, toAdd.getCost());
                 q.add(toAdd);
              }
           }
        }
        if (q.isEmpty()) {
           i = maxPops;
           break;
        }
        i ++;
     }
     
     if (i == maxPops) { 
        return "";
     }
     if (!curr.getPos().equals(goal)) {
        return "";
     }
     return curr.getA().moveHistory;
  }
  
  /**
   * Implementation of Djikstra in which don't know the grid position of our goal
   * this is used for returning to the starting position
   * If the search is unsuccessful we return an empty string
   * @param start - Point representing the starting position of search
   * @param goal - char that represents the goal square that we want to reach
   * @param allow_d - whether we are allowed to use dynamite on this search
   * @param water_search - whether we are allowed to leave the water
   * @return String representing the moves needed to reach the goal
   */
  String naiveDjikstra(Point start, char goal, boolean allow_d, boolean water_search) {
     HashMap<Point, Point> from = new HashMap<Point,Point>();
     HashMap<Point, Integer> dist = new HashMap<Point, Integer>();
     HashSet<Point> visited = new HashSet<Point>();
     PriorityQueue<Node> q = new PriorityQueue<Node>();
     
     int moves[][] = {{-1,0}, {1,0}, {0,-1}, {0,1}};
     int j;
     int maxPops = 50000;
     int i =0;
     
     Agent n = this.cloneAgent();
     Node first = new Node(start, 0, 0, n);
     Node curr = first;
     dist.put(start, 0);
     q.add(first);
     
     while (!q.isEmpty() && i < maxPops) {
        curr = q.poll();
        
        if (map[(int) curr.getPos().getX()][(int) curr.getPos().getY()] == goal) {
           if (goal == '$') {
              String homePath = curr.getA().djikstra(curr.getPos(), new Point (START,START), true, false);
              if (!(homePath == "")) {
                 curr.getA().moveHistory += homePath;
                 break;
              } else {
                 continue;
              }
           }
           else {
              break;
           }
        }

        visited.add(curr.getPos());
        
        for (j=0; j < 4; j++) {
           int x = (int) curr.getPos().getX() + moves[j][0];
           int y = (int) curr.getPos().getY() + moves[j][1];
           if (curr.getA().canMove(x,y, allow_d, water_search) || (map[x][y] == goal && goal == 'z')) {
              Point p = new Point(x,y);
              Agent a = curr.getA().cloneAgent();
              int dRow = x - a.getRowPos();
              int dCol = y - a.getColPos();
              String turn = "";
              if (dRow == -1) {
                 turn = a.moveSquare(NORTH);
              }
              else if (dRow == 1) {
                 turn = a.moveSquare(SOUTH);
              }
              else if (dCol == -1) {
                 turn = a.moveSquare(WEST);
              }
              else if (dCol == 1) {
                 turn = a.moveSquare(EAST);
              }
              boolean broken = false;
              for (char c : turn.toCharArray()) {
                 boolean legal = a.apply(c);
                 if (!legal) {
                    broken = true;
                 }
              }
              if (broken) {
                 continue;
              }
              a.moveHistory += turn;
             
              
              Node toAdd = new Node(p, curr.getCost()+1, 0, a);
              if (!(from.containsKey(p) && dist.get(p) < toAdd.getCost())) {
                 from.put(p, curr.getPos());
                 dist.put(p, toAdd.getCost());
                 q.add(toAdd);
              }
           }
        }
        if (q.isEmpty()) {
           i = maxPops;
           break;
        }
        i ++;
     }
     
     if (i == maxPops) {
        return "";
     }
     if (!(map[(int) curr.getPos().getX()][(int) curr.getPos().getY()] == goal)) {
        return "";
     }
     return curr.getA().moveHistory;
  }
  
  /**
   * 
   * @param direction - represents NORTH, SOUTH, EAST, WEST direction we want to move to
   * @return String representing the moves needed to move in desired direction
   */
   String moveSquare(int direction) {
      String ret = "";
      int sRow = this.rowPos;
      int sCol = this.colPos;
      switch (direction) {
         case NORTH: sRow --; break;
         case SOUTH: sRow ++; break;
         case EAST:  sCol ++; break;
         case WEST:  sCol --; break;
      }
      int dirn = this.dir;
      while (dirn != direction) {
         dirn = (dirn + 1) % 4;
         ret += "l";
      }
      char square = map[sRow][sCol];
      if (square == '-') {
         ret += 'u';
      }
      else if (square == 'T') {
         ret += 'c';
      }
      else if (square == '*') {
         ret += 'b';
      }
      ret += "f";
      return ret;
   }
  
   /**
    * Checks whether we can move into a certain square
    * @param row - the desired row position
    * @param col - the desired column position 
    * @param dynamites - boolean representing whether we can use dynamite
    * @param water_search - boolean representing whether we are in water search phase
    * @return boolean - true if we can move, false otherwise
    */
   boolean canMove(int row, int col, boolean dynamites, boolean water_search) {
       char space = map[row][col];
       if (water_search && space != '~') {
          return false;
       }
       if (space == '.') {
          return false;
       }
       switch (space) {
          case ' ': case 'a': case 'd': case 'k': case '$':
          return true;
       }
       if (space == 'T' && have_axe) {
          return true;
       }
       else if ((space == '~' && have_raft) || on_raft) {
          return true;
       }
       else if (space == '-' && have_key) {
          return true;
       }
       else if (space == '*' && (num_dynamites > 0) && dynamites) {
          return true;
       }
       return false;
   }
    
   /**
    * Returns a clone of itself
    * @return Agent - clone of the agent that called this function
    */
   Agent cloneAgent () {
       Agent a = new Agent();
       
       a.rowPos = this.rowPos;
       a.colPos = this.colPos;
       a.dir    = this.dir;
       
       int i;
       int j;
       
       for (i=0 ; i < 160; i++) {
          for (j=0; j< 160; j++) {
             a.map[i][j] = this.map[i][j];
          }
       }
       
       
       a.heuristic = this.heuristic;
       a.moveHistory = this.moveHistory;
       
       a.searching = this.searching;
       a.found_treasure = this.found_treasure;
       a.found_axe = this.found_axe;
       a.found_key = this.found_key;
       a.dynamites_seen = this.dynamites_seen;
       
       a.have_axe = this.have_axe; //+500
       a.have_key = this.have_key; //+100
       a.have_treasure = this.have_treasure; //+1000
       a.have_raft = this.have_raft; //+100
       a.on_raft = this.on_raft; 
       a.num_dynamites = this.num_dynamites; // +50
       
       return a;
    }
    
   /**
    * This didn't end up getting used as A* was never implemented
    * @return heuristic value of this state
    */
   int updateHeuristic() {
       int sum = 0;      
       if (have_key) {
          sum += 20;
       }
       if (have_treasure) {
          sum += 100;
       }
       /*
       if (have_raft) {
          sum += 20;
       }
       */
       sum += (num_dynamites * 20); // +50
       heuristic = sum;
       return sum;
    }
    
   /**
    * I basically copied this from Raft.java and modified it slightly
    * so that it would fit with my additional parameters. Use this function
    * to increment our game state during searching and as we interact with 
    * the game server
    * @param action - char action to be applied
    * @return - boolean as to whether action was successful
    */
   private boolean apply( char action )
     {
        int d_row, d_col;
        int new_row, new_col;
        char ch;

        if(( action == 'L' )||( action == 'l' )) {
           dir = ( dir + 1 ) % 4;
           return( true );
        }
        else if(( action == 'R' )||( action == 'r' )) {
           dir = ( dir + 3 ) % 4;
           return( true );
        }
        else {
           d_row = 0; d_col = 0;
           switch( dir ) {
            case NORTH: d_row = -1; break;
            case SOUTH: d_row =  1; break;
            case EAST:  d_col =  1; break;
            case WEST:  d_col = -1; break;
           }
           new_row = rowPos + d_row;
           new_col = colPos + d_col;
           if(  (new_row < 0)||(map[new_row][new_col] == '.')
              ||(new_col < 0)||(new_col >= map[new_row].length)) {
              if(( action == 'F' )||( action == 'f' )) {
                 if( !off_map ) {
                    map[rowPos][colPos] = '~';
                    off_map = true;
                 }
                 rowPos = new_row;
                 colPos = new_col;
                 game_lost = true;
                 return( true );
              }
              else {
                 return( false );
              }
           }

           ch = map[new_row][new_col];

           switch( action ) {
           case 'F': case 'f':
              switch( ch ) { // can't move into an obstacle
              case '*': case 'T': case '-':
                 return( false );
              }
              if( !off_map ) map[rowPos][colPos] = ' ';

              switch( ch ) {
               case '~':
                  if( on_raft ) {
                      if( !off_map ) map[rowPos][colPos] = '~';
                  }
        else if( have_raft ) {
            on_raft = true;
            if( !off_map ) map[rowPos][colPos] = ' ';
        }
                  else {
                      game_lost = true;
                  }
                  break;
               case ' ': case 'a': case 'k': case '$': case 'd':
        if( on_raft && !off_map ) {
            map[rowPos][colPos] = '~';
            on_raft = false;
            have_raft = false;
        }
                  break;
              }
              rowPos = new_row;
              colPos = new_col;

              switch( ch ) {
               case 'a': have_axe      = true; break;
               case 'k': have_key      = true; break;
               case '$': have_treasure = true; break;
               case 'd': num_dynamites ++; break;
              }
              if( have_treasure &&( rowPos == START )&&( colPos == START )) {
                 game_won = true;
              }
              if( !off_map ) map[rowPos][colPos] = ' ';
              off_map = false;
              return( true );

           case 'C': case 'c': // chop
              if(( ch == 'T' )&& have_axe ) {
                 map[new_row][new_col] = ' ';
            have_raft = true;
                 return( true );
              }
              break;

           case 'U': case 'u': // unlock
              if(( ch == '-' )&& have_key ) {
                 map[new_row][new_col] = ' ';
                 return( true );
              }
              break;

           case 'B': case 'b': // blast
              if( num_dynamites > 0 ) {
                 switch( ch ) {
                 case '*': case 'T': case '-':
                    map[new_row][new_col] = ' ';
                    num_dynamites --;
                    dynamites_seen --;
                    return( true );
                 }
              }
              break;
           }
        }
        return( false );
     }
  
   /**
    * Updates our map to include new information from the game server
    * @param view - the new view information from game server
    */
   void updateMap (char view[][]) 
   {
     int i; 
     int j;
     for (i=0; i < 5; i ++) {
       for(j=0; j < 5; j ++) {
         char c = view[i][j];
         switch (c) {
           case '$': this.found_treasure = true; break;
           case 'a': this.found_axe      = true; break;
           case 'k': this.found_key      = true; break;
         }
         if (c == 'd') {
            char square = ' ';
            switch (dir) {
            case NORTH: square = this.map[rowPos-2 + i][colPos-2+j]; break;
            case EAST:  square = this.map[rowPos-2 + j][colPos+2-i]; break;
            case SOUTH: square = this.map[rowPos+2 - i][colPos+2-j]; break;
            case WEST:  square = this.map[rowPos+2 - j][colPos-2+i]; break;
            }
            if (square != 'd') {
               this.dynamites_seen ++;
            }
         }         
          switch (dir) {
           case NORTH: this.map[rowPos-2 + i][colPos-2+j] = c; break;
           case EAST:  this.map[rowPos-2 + j][colPos+2-i] = c; break;
           case SOUTH: this.map[rowPos+2 - i][colPos+2-j] = c; break;
           case WEST:  this.map[rowPos+2 - j][colPos-2+i] = c; break;         
         }
       }
     }
   }
   
   /**
    * prints our current map view to console - only used during debugging
    */
   void printMap () {
    int i,j;

      System.out.println("\n+-----+");
      for( i=60; i < 120; i++ ) {
         System.out.print("|");
         for( j=60; j < 120; j++ ) {
            if(( i == rowPos )&&( j == colPos )) {
               //System.out.print('^');
               switch (dir) {
               case NORTH: System.out.print('^'); break;
               case EAST:  System.out.print('>'); break;
               case SOUTH: System.out.print('v'); break;
               case WEST:  System.out.print('<'); break;
               }
            }
            else {
               //if (map[i][j] != 'z') {
                  System.out.print( map[i][j] );
             //  }
            }
         }
         System.out.println("|");
      }
      System.out.println("+-----+");
   }
  
   /**
    * prints the current view to console - only used during debugging
    * @param view - our Agents current view
    */
   void print_view( char view[][] )
   {
      int i,j;

      System.out.println("\n+-----+");
      for( i=0; i < 5; i++ ) {
         System.out.print("|");
         for( j=0; j < 5; j++ ) {
            if(( i == 2 )&&( j == 2 )) {
               System.out.print('^');
            }
            else {
               System.out.print( view[i][j] );
            }
         }
         System.out.println("|");
      }
      System.out.println("+-----+");
   }

   /**
    * receive info from game engine then choose next action
    * @param args
    */
   public static void main( String[] args )
   {
      InputStream in  = null;
      OutputStream out= null;
      Socket socket   = null;
      Agent  agent    = new Agent();
      char   view[][] = new char[5][5];
      char   action   = 'F';
      int port;
      int ch;
      int i,j;

      if( args.length < 2 ) {
         System.out.println("Usage: java Agent -p <port>\n");
         System.exit(-1);
      }

      port = Integer.parseInt( args[1] );

      try { // open socket to Game Engine
         socket = new Socket( "localhost", port );
         in  = socket.getInputStream();
         out = socket.getOutputStream();
      }
      catch( IOException e ) {
         System.out.println("Could not bind to port: "+port);
         System.exit(-1);
      }

      try { // scan 5-by-5 wintow around current location
         while( true ) {
            for( i=0; i < 5; i++ ) {
               for( j=0; j < 5; j++ ) {
                  if( !(( i == 2 )&&( j == 2 ))) {
                     ch = in.read();
                     if( ch == -1 ) {
                        System.exit(-1);
                     }
                     view[i][j] = (char) ch;
                  }
               }
            }

            action = agent.get_action( view );
            out.write( action );
         }
      }
      catch( IOException e ) {
         System.out.println("Lost connection to port: "+ port );
         System.exit(-1);
      }
      finally {
         try {
            socket.close();
         }
         catch( IOException e ) {}
      }
   }
}
