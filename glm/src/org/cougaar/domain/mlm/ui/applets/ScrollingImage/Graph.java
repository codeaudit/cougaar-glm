/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.applets.ScrollingImage;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
  *  ALPINE -- Specialization of JDScrollBox to support sprites.
 **/

public class Graph extends JDScrollBox implements Runnable
{
    class Node {
        double x;
        double y;

        double dx;
        double dy;

        boolean fixed;

        String lbl;
    }
    class Edge {
         int from;
         int to;

         double len;
    }

    int nnodes;
    Node nodes[] = new Node[100];

    int nedges;
    Edge edges[] = new Edge[200];

    Thread relaxer;
    boolean stress;
    boolean random;


    public void start() {
        relaxer = new Thread(this);
        relaxer.start();
    }
    public void stop() {
        relaxer.stop();
    }


   public void run() {
        System.out.println("Graph.run() entered");
        Thread me = Thread.currentThread();
	      while (relaxer == me) {
	         relax();
	         if (random && (Math.random() < 0.03)) {
		           Node n = nodes[(int)(Math.random() * nnodes)];
		           if (!n.fixed) {
		               n.x += 20*Math.random() - 10;
		               n.y += 20*Math.random() - 10;
                   System.out.println("(n.x,n.y)=>(" + n.x + "," + n.y + ")");
		           }
	         }
	         try {
		          Thread.sleep(5000);
	         } catch (InterruptedException e) {
	           	break;
	         }
	      }
    }


    int findNode(String lbl) {
      	for (int i = 0 ; i < nnodes ; i++) {
	          if (nodes[i].lbl.equals(lbl)) {
		            return i;
	           }
        }
  	    return addNode(lbl);
    }

    int addNode(String lbl) {
      	Node n = new Node();
	      n.x = 10 + (int)((double)(this.bigPicWidth-10)*Math.random());
	      n.y = 10 + (int)((double)(this.bigPicHeight-10)*Math.random());
        System.out.println("Assigned Node Coordinates: " + n.x + "," + n.y);
	      n.lbl = lbl;
	      nodes[nnodes] = n;
	      return nnodes++;
    }

    void addEdge(String from, String to, int len) {
	      Edge e = new Edge();
	      e.from = findNode(from);
	      e.to = findNode(to);
	      e.len = len;
	      edges[nedges++] = e;
    }

   public Graph( Image bigPicInImg, Image indexPicImgIn, int scaleIn,
             Color zoomRectColorIn, Color zoomBorderColorIn, int xRectDispIn,
             int yRectDispIn, boolean centerZoomIn)
   {
       super(bigPicInImg, indexPicImgIn, scaleIn, zoomRectColorIn, zoomBorderColorIn
             ,xRectDispIn ,yRectDispIn ,centerZoomIn);
   }

   public void load(String edges, String CenterNodeLabel )
   {
      int default_edge_len = bigPicHeight / 8;


	    for (StringTokenizer t = new StringTokenizer(edges, ",") ; t.hasMoreTokens() ; ) {
	        String str = t.nextToken();
	        int i = str.indexOf('-');
	        if (i > 0) {
		        int len = default_edge_len;
		        int j = str.indexOf('/');
		        if (j > 0) {
		           len = Integer.valueOf(str.substring(j+1)).intValue();
		           str = str.substring(0, j);
		        }
	        	addEdge(str.substring(0,i), str.substring(i+1), len);
	        }
	     }

	     if (CenterNodeLabel != null){
	          Node n = nodes[findNode(CenterNodeLabel)];
	          n.x = bigPicWidth / 2;
	          n.y = bigPicHeight / 2;
	          n.fixed = true;
       }
   }



    synchronized void relax() {
	     for (int i = 0 ; i < nedges ; i++) {
	         Edge e = edges[i];
	         double vx = nodes[e.to].x - nodes[e.from].x;
	         double vy = nodes[e.to].y - nodes[e.from].y;
	         double len = Math.sqrt(vx * vx + vy * vy);
           len = (len == 0) ? .0001 : len;
	         double f = (edges[i].len - len) / (len * 3);
	         double dx = f * vx;
	         double dy = f * vy;

	         nodes[e.to].dx += dx;
	         nodes[e.to].dy += dy;
	         nodes[e.from].dx += -dx;
	         nodes[e.from].dy += -dy;
       }

	     for (int i = 0 ; i < nnodes ; i++) {
	         Node n1 = nodes[i];
	         double dx = 0;
	         double dy = 0;

	         for (int j = 0 ; j < nnodes ; j++) {
		            if (i == j) {
		             continue;
                }
               Node n2 = nodes[j];
		           double vx = n1.x - n2.x;
		           double vy = n1.y - n2.y;
		           double len = vx * vx + vy * vy;
               if (len == 0) {
		              dx += Math.random();
		              dy += Math.random();
		           } else if (len < 100*100) {
		              dx += vx / len;
		              dy += vy / len;
		           }
	         }
	         double dlen = dx * dx + dy * dy;
	         if (dlen > 0) {
		          dlen = Math.sqrt(dlen) / 2;
		          n1.dx += dx / dlen;
		          n1.dy += dy / dlen;
	         }
	     }


	     for (int i = 0 ; i < nnodes ; i++) {
	         Node n = nodes[i];
	         if (!n.fixed) {
		           n.x += Math.max(-5, Math.min(5, n.dx));
               n.y += Math.max(-5, Math.min(5, n.dy));
            }
            if (n.x < 0) {
                n.x = 0;
            } else if (n.x > this.bigPicWidth) {
                n.x = this.bigPicWidth;
            }
            if (n.y < 0) {
                n.y = 0;
            } else if (n.y > this.bigPicHeight) {
                n.y = this.bigPicHeight;
            }
	         n.dx /= 2;
	         n.dy /= 2;
	    }
      //Dimension d = getSize();
      //this.repaint(0,0,d.width,d.height);
      repaint();
    }


    Node pick;
    boolean pickfixed;
    Image offscreen;
    Dimension offscreensize;
    Graphics offgraphics;

    final Color fixedColor = Color.red;
    final Color selectColor = Color.pink;
    final Color edgeColor = Color.black;
    final Color nodeColor = new Color(250, 220, 100);
    final Color stressColor = Color.darkGray;
    final Color arcColor1 = Color.black;
    final Color arcColor2 = Color.pink;
    final Color arcColor3 = Color.red;

    public void paintNode(Graphics g, Node n, FontMetrics fm) {

	       g.setColor((n == pick) ? selectColor : (n.fixed ? fixedColor : nodeColor));
	       int w = fm.stringWidth(n.lbl) + 10;
	       int h = fm.getHeight() + 4;

         //evt.x += ( rectPos.x * (-1 * indexToBigMultiple));
         //evt.y += ( rectPos.y * (-1 * indexToBigMultiple));
         int dx = rectPos.x * indexToBigMultiple * -1;
         int dy = rectPos.y * indexToBigMultiple * -1;
         int x = (int)(n.x ) -dx;
         int y = (int)(n.y ) -dy;

         if( x < 0 ) x = 0;
         if( y < 0 ) y = 0;
         if( x > peekWindowWidth ) x = peekWindowWidth;
         if( y > peekWindowHeight ) y = peekWindowHeight;

         // Clip nodes not displayable...
         //if( ((x >= 0) && (y >= 0)) && ((x <= peekWindowWidth) && ( y <= peekWindowHeight)) )
         //{

            System.out.println("Paint Node @ xy: " + x + ","
                                                 + y + "....nxy:"
                                                 + n.x
                                                 + ","
                                                 + n.y
                                                 + "....dxy: "
                                                 + dx
                                                 + ","
                                                 +dy);


	          g.fillRect(x - w/2, y - h / 2, w, h);
	          g.setColor(Color.black);
	          g.drawRect(x - w/2, y - h / 2, w-1, h-1);
	          g.drawString(n.lbl, x - (w-10)/2, (y - (h-4)/2) + fm.getAscent());
        //}
    }

    public synchronized void update(Graphics g) {

	        Dimension d = getSize();
	        if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
	            offscreen = createImage(d.width, d.height);
	            offscreensize = d;
	            offgraphics = offscreen.getGraphics();
	            offgraphics.setFont(getFont());
	        }

	        offgraphics.setColor(getBackground());
	        offgraphics.fillRect(0, 0, d.width, d.height);

          super.update(offgraphics);

          //offgraphics.clipRect(rectPos.x, rectPos.y, rectWidth, rectHeight );

	        for (int i = 0 ; i < nedges ; i++) {
	           Edge e = edges[i];
             int x1 = (int)nodes[e.from].x;
	           int y1 = (int)nodes[e.from].y;
	           int x2 = (int)nodes[e.to].x;
	           int y2 = (int)nodes[e.to].y;
	           int len = (int)Math.abs(Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)) - e.len);
	           offgraphics.setColor((len < 10) ? arcColor1 : (len < 20 ? arcColor2 : arcColor3)) ;


         int dx = rectPos.x * indexToBigMultiple * -1;
         int dy = rectPos.y * indexToBigMultiple * -1;
         int x11 = (int)(x1 ) -dx;
         int y11 = (int)(y1 ) -dy;
         int x21 = (int)(x2) -dx;
         int y21 = (int)(y2 ) -dy;


         if( x11 < 0 ) x11 = 0;
         if( y11 < 0 ) y11 = 0;
         if( x11 > peekWindowWidth ) x11 = peekWindowWidth;
         if( y11 > peekWindowHeight ) y11 = peekWindowHeight;
         if( x21 < 0 ) x21 = 0;
         if( y21 < 0 ) y21 = 0;
         if( x21 > peekWindowWidth ) x21 = peekWindowWidth;
         if( y21 > peekWindowHeight ) y21 = peekWindowHeight;


 	           offgraphics.drawLine(x11, y11, x21, y21);
             // System.out.print("DrawLine="+x1 + "," + y1 + "," +x2+ "," + y2 );


	           if (stress) {
		            String lbl = String.valueOf(len);
		            offgraphics.setColor(stressColor);
		            offgraphics.drawString(lbl, x1 + (x2-x1)/2, y1 + (y2-y1)/2);
		            offgraphics.setColor(edgeColor);
	           }
          }

	        FontMetrics fm = offgraphics.getFontMetrics();
	        for (int i = 0 ; i < nnodes ; i++) {
	            paintNode(offgraphics, nodes[i], fm);
	        }
	        g.drawImage(offscreen, 0, 0, null);
    }

}


