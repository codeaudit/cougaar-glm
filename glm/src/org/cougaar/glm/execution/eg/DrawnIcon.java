/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.glm.execution.eg;

import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import javax.swing.Icon;

public class DrawnIcon implements Icon {
    static RenderingHints antialiasingHint =
        new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    private static final Color ENABLED_COLOR = Color.black;
    private static final Color SHADOW_COLOR = Color.white;
    private static final Color DISABLED_COLOR = Color.gray;
    
    public static Icon getPlayIcon(boolean enabled) {
        GeneralPath playShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 4);
        playShape.moveTo(1f, 1f);
        playShape.lineTo(11f, 5f);
        playShape.lineTo(1f, 9f);
        playShape.closePath();
        Color fillColor;
        GeneralPath shadowShape = null;
        if (enabled) {
            fillColor = ENABLED_COLOR;
        } else {
            shadowShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
            shadowShape.moveTo(12f, 6f);
            shadowShape.lineTo(2f, 10f);
            fillColor = DISABLED_COLOR;
        }
        return new DrawnIcon(playShape, fillColor, shadowShape, SHADOW_COLOR);
    }

    public static Icon getStopIcon(boolean enabled) {
        GeneralPath stopShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 5);
        stopShape.moveTo(1f, 1f);
        stopShape.lineTo(9f, 1f);
        stopShape.lineTo(9f, 9f);
        stopShape.lineTo(1f, 9f);
        stopShape.closePath();
        Color fillColor;
        GeneralPath shadowShape = null;
        if (enabled) {
            fillColor = ENABLED_COLOR;
        } else {
            shadowShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
            shadowShape.moveTo(2f, 10f);
            shadowShape.lineTo(10f, 10f);
            shadowShape.lineTo(10f, 2f);
            fillColor = DISABLED_COLOR;
        }
        return new DrawnIcon(stopShape, fillColor, shadowShape, SHADOW_COLOR);
    }

    public static Icon getPauseIcon(boolean enabled) {
        GeneralPath pauseShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 8);
        pauseShape.moveTo(1f, 1f);
        pauseShape.lineTo(4f, 1f);
        pauseShape.lineTo(4f, 9f);
        pauseShape.lineTo(1f, 9f);
        pauseShape.closePath();
        pauseShape.moveTo(7f, 1f);
        pauseShape.lineTo(10f, 1f);
        pauseShape.lineTo(10f, 9f);
        pauseShape.lineTo(7f, 9f);
        pauseShape.closePath();
        Color fillColor;
        GeneralPath shadowShape = null;
        if (enabled) {
            fillColor = ENABLED_COLOR;
        } else {
            shadowShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 6);
            shadowShape.moveTo(5f, 2f);
            shadowShape.lineTo(5f, 10f);
            shadowShape.lineTo(2f, 10f);
            shadowShape.moveTo(11f, 2f);
            shadowShape.lineTo(11f, 10f);
            shadowShape.lineTo(8f, 10f);
            fillColor = DISABLED_COLOR;
        }
        return new DrawnIcon(pauseShape, fillColor, shadowShape, SHADOW_COLOR);
    }

    private Shape fillShape, shadowShape;
    private Color fillColor, shadowColor;

    public DrawnIcon(Shape fillShape, Color fillColor, Shape shadowShape, Color shadowColor) {
        this.fillShape = fillShape;
        this.fillColor = fillColor;
        this.shadowShape = shadowShape;
        this.shadowColor = shadowColor;
    }

    public int getIconHeight() {
        return 12;
    }

    public int getIconWidth() {
        return 12;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = ((Graphics2D) g);
        Color base = g.getColor();
        g2d.translate(x, y);
        g2d.addRenderingHints(antialiasingHint);
        if (shadowShape != null) {
            g2d.setColor(shadowColor);
            g2d.draw(shadowShape);
        }
        g2d.setColor(fillColor);
        g2d.fill(fillShape);
        g2d.translate(-x, -y);
        g.setColor(base);
    }
}
