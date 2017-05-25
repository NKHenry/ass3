package ass3;

import java.util.*;

public class HomeNode implements Comparable<HomeNode> {
   private Agent a;
   private int rowStart;
   private int colStart;
   private int rowGoal;
   private int colGoal;
   private int heuristic;
   
   public HomeNode (Agent ag, int rs, int cs, int rg, int cg) {
      a = ag;
      rowStart = rs;
      colStart = cs;
      rowGoal = rg;
      colGoal = cg;
      calcHeuristic();
   }
   
   private void calcHeuristic () {
      int h = a.updateHeuristic();
      int goalD = Math.abs(rowGoal - a.getRowPos()) + Math.abs(colGoal - a.getColPos());
      //int startD = Math.abs(rowStart - a.getRowPos()) + Math.abs(colStart - a.getColPos());
      if (goalD == 0) {
         h = h + 1000;
      }
      else {
         h = h + (1000/goalD);
      }
      //h = h + 1000 * (startD/1000);
      this.heuristic = h;
   }
   
   public int getHeuristic () {
      return heuristic;
   }
   
   public Agent getA() {
      return a;
   }
   
   /*
   @Override
   public int compare(ExploreNode arg0, ExploreNode arg1) {
      return (arg0.getHeuristic() - arg1.getHeuristic());
   }
   */

   @Override
   public int compareTo(HomeNode arg0) {
      return ((this.getHeuristic() - arg0.getHeuristic())*-1);
   }
}
