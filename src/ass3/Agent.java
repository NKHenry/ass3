/*********************************************
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2017
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
  
  public char get_action( char view[][] ) {

     // REPLACE THIS CODE WITH AI TO CHOOSE ACTION
    updateMap(view);
    printMap(); 
    int ch = 1;     
    
    if (searching && on_raft) {
       this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'z', false, true);
       System.out.println("water search worked");
       if (this.todo == "") {
          System.out.println("finished water search");
          searching = false;
       }
    }
    if (have_treasure && todo.length() == 0) {
       //searching = false;
       this.todo = djikstra(new Point(this.rowPos, this.colPos), new Point(START,START), false, false);
       if (this.todo == "") {
          System.out.println("have dynamites " + num_dynamites);
          this.todo = djikstra(new Point(this.rowPos, this.colPos), new Point(START,START), true, false);
       }
    }
    if (this.todo.length() == 0 && dynamites_seen > num_dynamites) {
       this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'd', false, false);

    }
    if (this.todo.length() == 0 && found_treasure && !have_treasure) {
       this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), '$', false, false);

    }
    if (this.todo.length() == 0 && found_axe && !have_axe) {
       this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'a', false, false);
       if (this.todo == "") {
         this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'a', true, false);
       }
    }
    
    if (todo.length() == 0) {

        this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'z', false, false);
        if (this.todo == "") {
           this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'z', true, false);
        }

    }
    if (todo.length() == 0 && !have_raft && have_axe) {
       this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'T', false, false);
    }
    
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
              System.out.println("finished water search");
              searching = false;
           }
        }
        if (have_treasure && todo.length() == 0) {
           //searching = false;
           this.todo = djikstra(new Point(this.rowPos, this.colPos), new Point(START,START), false, false);
           if (this.todo == "") {
              System.out.println("have dynamites " + num_dynamites);
              this.todo = djikstra(new Point(this.rowPos, this.colPos), new Point(START,START), true, false);
           }
        }
        if (this.todo.length() == 0 && dynamites_seen > num_dynamites) {
           this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'd', false, false);

        }
        if (this.todo.length() == 0 && found_treasure && !have_treasure) {
           this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), '$', false, false);

        }
        if (this.todo.length() == 0 && found_axe && !have_axe) {
           this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'a', false, false);
           if (this.todo == "") {
             this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'a', true, false);
           }
        }
        
        if (todo.length() == 0) {
            this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'z', false, false);
            if (this.todo == "") {
               this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'z', true, false);
            }
        }
        if (todo.length() == 0 && !have_raft && have_axe) {
           this.todo = naiveDjikstra(new Point(this.rowPos, this.colPos), 'T', false, false);
        }      
     
    }

     
     System.out.print("Enter Action(s): ");
     
     try {
        while ( ch != -1 ) {
           // read character from keyboard
           ch  = System.in.read();

           switch( ch ) { // if character is a valid action, return it
           case 'F': case 'L': case 'R': case 'C': case 'U': case 'B':
           case 'f': case 'l': case 'r': case 'c': case 'u': case 'b':
              apply ((char) ch);
              printMap();
              return((char) ch );
           }
        }
     }
     catch (IOException e) {
        System.out.println ("IO error:" + e );
     }
     
     
     return (char) ch;
  }
  
  public int getRowPos() {
     return rowPos;
  }
  
  public int getColPos () {
     return colPos;
  }
  
  public String getMoveHistory() {
     return moveHistory;
  }


  String djikstra(Point start, Point goal, boolean allow_d, boolean water_search) {
     HashMap<Point, Point> from = new HashMap<Point,Point>();
     HashMap<Point, Integer> dist = new HashMap<Point, Integer>();
     HashSet<Point> visited = new HashSet<Point>();
     PriorityQueue<Node> q = new PriorityQueue<Node>();
     
     int moves[][] = {{-1,0}, {1,0}, {0,-1}, {0,1}};
     int j;
     int i = 0;
     int maxPops = 5000;
     
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
                 //System.out.println(NORTH);
                 turn = a.moveSquare(NORTH);
              }
              else if (dRow == 1) {
                 //System.out.println(SOUTH);
                 turn = a.moveSquare(SOUTH);
              }
              else if (dCol == -1) {
                 //System.out.println(WEST);
                 turn = a.moveSquare(WEST);
              }
              else if (dCol == 1) {
                 //System.out.println(EAST);
                 turn = a.moveSquare(EAST);
              }
              for (char c : turn.toCharArray()) {
                 a.apply(c);
              }
              a.moveHistory += turn;
                          
              Node toAdd = new Node(p, curr.getCost()+1, 0, a);
              if (!(from.containsKey(p) && dist.get(p) < toAdd.getCost())) {
                 from.put(p, curr.getPos());
                 dist.put(p, toAdd.getCost());
                 q.add(toAdd);
              }
              //System.out.println("added point " + p.toString());
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
     System.out.println(curr.getA().moveHistory);
     System.out.println("dynamites remaining = " + num_dynamites);
     return curr.getA().moveHistory;
  }
  
  String naiveDjikstra(Point start, char goal, boolean allow_d, boolean water_search) {
     HashMap<Point, Point> from = new HashMap<Point,Point>();
     HashMap<Point, Integer> dist = new HashMap<Point, Integer>();
     HashSet<Point> visited = new HashSet<Point>();
     PriorityQueue<Node> q = new PriorityQueue<Node>();
     
     int moves[][] = {{-1,0}, {1,0}, {0,-1}, {0,1}};
     int j;
     int maxPops = 5000;
     int i =0;
     
     Agent n = this.cloneAgent();
     Node first = new Node(start, 0, 0, n);
     Node curr = first;
     dist.put(start, 0);
     q.add(first);
     
     while (!q.isEmpty() && i < maxPops) {
        curr = q.poll();
        
        if (map[(int) curr.getPos().getX()][(int) curr.getPos().getY()] == goal) {
           break;
        }
        if (visited.contains(curr.getPos())) {
           continue;
        }
        visited.add(curr.getPos());
        
        for (j=0; j < 4; j++) {
           int x = (int) curr.getPos().getX() + moves[j][0];
           int y = (int) curr.getPos().getY() + moves[j][1];
           if (curr.getA().canMove(x,y, allow_d, water_search) || map[x][y] == goal) {
              Point p = new Point(x,y);
              Agent a = curr.getA().cloneAgent();
              int dRow = x - a.getRowPos();
              int dCol = y - a.getColPos();
              String turn = "";
              if (dRow == -1) {
                 //System.out.println(NORTH);
                 turn = a.moveSquare(NORTH);
              }
              else if (dRow == 1) {
                // System.out.println(SOUTH);
                 turn = a.moveSquare(SOUTH);
              }
              else if (dCol == -1) {
                 //System.out.println(WEST);
                 turn = a.moveSquare(WEST);
              }
              else if (dCol == 1) {
                 //System.out.println(EAST);
                 turn = a.moveSquare(EAST);
              }
              for (char c : turn.toCharArray()) {
                 a.apply(c);
              }
              a.moveHistory += turn;
             
              
              Node toAdd = new Node(p, curr.getCost()+1, 0, a);
              if (!(from.containsKey(p) && dist.get(p) < toAdd.getCost())) {
                 from.put(p, curr.getPos());
                 dist.put(p, toAdd.getCost());
                 q.add(toAdd);
              }
              //System.out.println("added point " + p.toString());
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
        System.out.println("search failed couldn't find " + goal);
        return "";
     }
     System.out.println("end pos is " + curr.getPos().toString());
     System.out.println(curr.getA().moveHistory);
     return curr.getA().moveHistory;
  }
  
  String explore(char goal) {
      int initRow = this.rowPos;
      int initCol = this.colPos;
      char actions[] = "frlcb".toCharArray();
      int maxPops = 5000;
      int i = 0;

      PriorityQueue<ExploreNode> q = new PriorityQueue<ExploreNode>();
      Agent first = this.cloneAgent();
      first.moveHistory = "";
      ExploreNode e = new ExploreNode(first, initRow, initCol);
      q.add(e);
      
      //HashSet<String> visited = new HashSet<String>();
      
      while (!q.isEmpty() && i < maxPops) {
         e = q.poll();
         int eRow = e.getA().getRowPos();
         int eCol = e.getA().getColPos();
         if (map[eRow][eCol] == goal) {
            break;
         }
         if (map[eRow][eCol] == 'z' && goal != 'z') {
            continue;
         }
         
         //visited.add(Integer.toString(eRow) + ":" + Integer.toString(eCol));
         
         for (char c : actions) {
            Agent current = e.getA().cloneAgent();
            boolean legal = current.apply(c);
            //String curPos = Integer.toString(current.rowPos) + ":" + Integer.toString(current.colPos);
            if (legal && !current.game_lost) {
               //if (!(c == 'f' && visited.contains(curPos))) {
                  current.moveHistory += c;
                  ExploreNode toAdd = new ExploreNode(current, initRow, initCol);
                  q.add(toAdd);
               //}
            }
         }
         i ++;
      }
      
      if (i == maxPops) {
         System.out.println("hit maxPops");
         return "";
      }
      
      
      return e.getA().moveHistory;
  }
  
  String naiveExplore(int rowGoal , int colGoal) {
     int initRow = this.rowPos;
     int initCol = this.colPos;
     char actions[] = "frlcb".toCharArray();
     int maxPops = 5000;
     int i = 0;

     PriorityQueue<ExploreNode> q = new PriorityQueue<ExploreNode>();
     Agent first = this.cloneAgent();
     first.moveHistory = "";
     ExploreNode e = new ExploreNode(first, initRow, initCol);
     q.add(e);
     
     //HashSet<String> visited = new HashSet<String>();
     
     while (!q.isEmpty() && i < maxPops) {
        e = q.poll();
        int eRow = e.getA().getRowPos();
        int eCol = e.getA().getColPos();
        if (eRow == rowGoal && eCol == colGoal) {
           break;
        }
        if (map[eRow][eCol] == 'z') {
           continue;
        }
        //visited.add(Integer.toString(eRow) + ":" + Integer.toString(eCol));
        
        for (char c : actions) {
           Agent current = e.getA().cloneAgent();
           boolean legal = current.apply(c);
           //String curPos = Integer.toString(current.rowPos) + ":" + Integer.toString(current.colPos);
           if (legal && !current.game_lost) {
              //if (!(c == 'f' && visited.contains(curPos))) {
                 current.moveHistory += c;
                 ExploreNode toAdd = new ExploreNode(current, initRow, initCol);
                 q.add(toAdd);
              //}
           }
        }
        i ++;
     }
     
     if (i == maxPops) {
        System.out.println("hit maxPops");
        return "";
     }
     
     
     return e.getA().moveHistory;
 }

  
  
  
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
       else if (space == '*' && num_dynamites > 0 && dynamites) {
          return true;
       }
       return false;
   }
    
    Agent cloneAgent () {
       Agent a = new Agent();
       
       a.rowPos = this.rowPos;
       a.colPos = this.colPos;
       a.dir    = this.dir;
       
       int i;
       int j;
       //a.map = this.map.clone();
       
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
    
    int updateHeuristic() {
       int sum = 0;      
       if (found_treasure) {
          sum += 100;
       }
       if (found_axe) {
          sum += 50;
       }
       if (found_key) {
          sum += 50;
       }
       sum += (dynamites_seen * 10);
       if (have_axe) {
          sum += 250;
       }
       if (have_key) {
          sum += 100;
       }
       if (have_treasure) {
          sum += 1000;
       }
       if (have_raft) {
          sum += 100;
       }
       if (game_lost) {
          sum -= 10000000;
       }
       if (game_won) {
          sum += 100000000;
       }
       sum += (num_dynamites * 100); // +50
       sum -= (moveHistory.length()*10);
       heuristic = sum;
       return sum;
    }
    
   
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

  
  
  char inFront () {
     char ch = ' ';
     switch (dir) {
     case NORTH: ch = map[rowPos-1][colPos]; break;
     case EAST:  ch = map[rowPos][colPos+1]; break;
     case SOUTH: ch = map[rowPos+1][colPos]; break; 
     case WEST:  ch = map[rowPos][colPos-1]; break;
     }
     return ch;
  }
  
  
   void updateMap (char view[][]) 
   {
     System.out.println("updating map");
     int i; 
     int j;
     for (i=0; i < 5; i ++) {
       for(j=0; j < 5; j ++) {
         char c = view[i][j];
         switch (c) {
           case '$': this.found_treasure = true; break;
           case 'a': this.found_axe      = true; break;
           case 'd': this.dynamites_seen     ++; break;
           case 'k': this.found_key      = true; break;
         }
         
          switch (dir) {
           case NORTH: this.map[rowPos-2 + i][colPos-2+j] = view[i][j]; break;
           case EAST:  this.map[rowPos-2 + j][colPos+2-i] = view[i][j]; break;
           case SOUTH: this.map[rowPos+2 - i][colPos+2-j] = view[i][j]; break;
           case WEST:  this.map[rowPos+2 - j][colPos-2+i] = view[i][j]; break;         
         }
       }
     }
   }
   
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
           // agent.print_view( view ); // COMMENT THIS OUT BEFORE SUBMISSION
            //agent.printMap();
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
