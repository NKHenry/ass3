package ass3;

import java.util.*;
import java.awt.Point;

public class Node implements Comparable<Node> {
   private Point pos;
   private int cost;
   private int heuristic;
   private Agent a;
   
   public Node(Point p, int cst, int h, Agent ag) {
      pos = p;
      cost = cst;
      heuristic = h;
      a = ag;
   }

   public Agent getA() {
      return this.a;
   }
   
   public Point getPos() {
      return pos;
   }
   
   public int getCost() {
      return cost;
   }

   public int getHeuristic() {
      return heuristic;
   }

   @Override
   public int compareTo(Node arg0) {
      return (this.getCost() + this.getHeuristic()) - (arg0.getCost() + arg0.getHeuristic()); 
   }
}
