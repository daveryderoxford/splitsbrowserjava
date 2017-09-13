/*
 *  Splitsbrowser - SplashPanel
 *
 *  Copyright (C) 2002 Reinhard Balling

 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this library; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
/*
 * Version control info - Do not edit
 * Created:    Reinhard Balling
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/08/25 06:31:56 $
 * Changed by: $Author: daveryder $
 */
 
package org.splitsbrowser.applet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Polygon;

import java.util.StringTokenizer; 

/**
 *  A (simple) splash panel.
 *
 * @version    $Revision: 1.1 $
 */
public class SplashPanel extends Panel {
    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 5845422538675468213L;

	/**  Large font */
    private Font bigFont = new java.awt.Font("Arial", Font.BOLD, 120);

    /**  Small font */
    private Font smallFont = new java.awt.Font("Arial", Font.BOLD, 16);

    /**  Tiny font */
    private Font verySmallFont = new java.awt.Font("Arial", Font.BOLD, 12);
    private String eventName;

    /**  String displayed while loading file */
    private String strLoading = "";

    /**  Constructor for the SplashPanel object */
    public SplashPanel() {
        super();
    }

    /**
     *  Sets the string displayed during loading the event data
     *
     * @param  newStrLoading  The string included in the splash screen during loading of event data
     */
    public void setStrLoading(String newStrLoading) {
        strLoading = newStrLoading;
        this.validate();
    }

    /**
     *  Paint the splash panel
     *
     * @param  g  The graphics context
     */
    public void paint(Graphics g) {
        Dimension dim = getSize();
        g.setColor(this.getBackground());
        g.fillRect(0, 0, dim.width, dim.height);
        g.setFont(bigFont);

        FontMetrics fontMetrics = g.getFontMetrics(bigFont);
        int w = 100;
        int x0 = (dim.width - w) / 2;
        int y0 = (dim.height - w) / 2;

        int[] xpoints = new int[3];
        int[] ypoints = new int[3];

        xpoints[0] = x0;
        ypoints[0] = y0;
        xpoints[1] = x0 + w;
        ypoints[1] = y0;
        xpoints[2] = x0;
        ypoints[2] = y0 + w;

        // draw the orienteering control
        g.setColor(Color.red);
        g.fillRect(x0, y0, w, w);
        g.setColor(Color.white);
        g.fillPolygon(new Polygon(xpoints, ypoints, 3));

        // Draw the chart symbol
        g.setColor(Color.blue);

        for (int i = 0; i < 5; i++) {
            g.drawLine(x0, y0 + 50 + i, x0 + 28, y0 + 41 + i);
            g.drawLine(x0 + 28, y0 + 41 + i, x0 + 47, y0 + 79 + i);
            g.drawLine(x0 + 47, y0 + 79 + i, x0 + 99, y0 + 59 + i);
        }

        for (int i = 1; i < 2; i++) {
            g.drawLine(x0 + i, y0 + 50, x0 + 28 + i, y0 + 41);
            g.drawLine(x0 + 28 + i, y0 + 41, x0 + 47 + i, y0 + 79);
            g.drawLine(x0 + 47 + i, y0 + 79, x0 + 97 + i, y0 + 59);
        }

        String s = About.name();
        int width = fontMetrics.stringWidth(s);
        int height = fontMetrics.getHeight();

        g.setColor(Color.black);
        x0 = (dim.width - width) / 2;
        y0 = (dim.height - height) / 2;
        g.drawString(s, x0 + 4, y0 + 4);
        g.setColor(Color.white);
        g.drawString(s, x0, y0);
        g.setColor(Color.black);

        s = About.about() + "\n \n" + "*\n" + strLoading;

        StringTokenizer st = new StringTokenizer(s, "\n");

        g.setFont(smallFont);

        while (st.hasMoreTokens()) {
            String nt = st.nextToken();

            if (nt.equals("*")) {
                g.setColor(Color.blue);

                continue;
            }

            y0 += (height * 1.1);
            fontMetrics = g.getFontMetrics(g.getFont());
            width = fontMetrics.stringWidth(nt);
            g.drawString(nt, (dim.width - width) / 2, y0);
            height = fontMetrics.getHeight();
            g.setFont(verySmallFont);
            g.setColor(Color.black);
        }

        g.setFont(smallFont);
        fontMetrics = g.getFontMetrics(smallFont);

        if (eventName != null) {
            width = fontMetrics.stringWidth(eventName);
            height = fontMetrics.getHeight();
            x0 = (dim.width - width) / 2;
            g.drawString(eventName, x0, y0 + 30);
        }
    }

    /**
     *  Sets the event name
     *
     * @param  newStrLoading  The string included in the splash screen during loading of event data
     */
    public void seteventName(String newEventName) {
        eventName = newEventName;
        this.validate();
    }

    public void update(Graphics g) {
        paint(g);
    }
}
