package ass3;

import java.util.*;

public class ExploreNode implements Comparable<ExploreNode> {

   private Agent a;
   private int rowStart;
   private int colStart;
   private int heuristic;
   
   public ExploreNode (Agent ag, int r, int c) {
      a = ag;
      rowStart = r;
      colStart = c;
      calcHeuristic();
   }
   
   private void calcHeuristic () {
      /*
      int h = a.updateHeuristic();
      int dist = Math.abs(rowStart - a.getRowPos()) + Math.abs(colStart - a.getColPos());
      h = h + (dist*10);
      */
      int h = a.getMoveHistory().length();
      this.heuristic = h;
   }
   
   public int getHeuristic () {
      return heuristic;
   }
   
   public Agent getA() {
      return a;
   }
   

   @Override
   public int compareTo(ExploreNode arg0) {
      return ((this.getHeuristic() - arg0.getHeuristic())*-1);
   }
}
