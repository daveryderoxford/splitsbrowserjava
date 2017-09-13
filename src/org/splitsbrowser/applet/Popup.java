/*
 *  Splitsbrowser Popup.java.
 *
 *  Copyright (C) 2003 Dave Ryder
 *
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
 * Created:    Dave Ryder - Nov 3, 2003
 * Version:    $Revision$
 * Changed:    $Date$
 * Changed by: $Author$
 */

package org.splitsbrowser.applet;

import java.awt.*;
import java.util.Vector;

/**
 */
public class Popup {
    
    private Graphics _graphics;
    private String _title;
    private Vector _text = new Vector(10);
    private Vector _colors = new Vector(10);
    private Image _offscreenImage = null;
    
    
    private void invalidate(){
        _offscreenImage = null;
    }
    public Popup(Graphics g) {
        _graphics = g;
    }
    
    public void clear() {
        _text.clear();
        _colors.clear();
    }
    

    public void paint(Point p, Rectangle limits){
        drawPopup(p, limits) ; 
    }
    
    public void setTitle(String s) {
        _title = s;
        invalidate();
    }
        
    private void drawPopup(Point p, Rectangle limits) {
        FontMetrics fontMetrics = _graphics.getFontMetrics(_graphics.getFont());
        int fontHeight = fontMetrics.getHeight();

        // Get size of box
        int width = 0;

        for (int i = 0; i < _text.size(); i++) {
            width =
                Math.max(width, fontMetrics.stringWidth((String)_text.elementAt(i)));
        }

        int FRAMEWIDTH = 4;
        int yDraw = p.y - ((_text.size()* fontHeight) / 2) + 6;
        int boxHeight =
            (_text.size() * fontHeight) + FRAMEWIDTH *2;
            
        int boxWidth = width + FRAMEWIDTH *2;        

        // Render popup into it
        if ((yDraw + boxHeight) > limits.height) {
            yDraw = limits.height - boxHeight - 2;
        }

        yDraw = Math.max(yDraw, fontHeight + FRAMEWIDTH + 2);

        int xDraw = (p.x + 20) - FRAMEWIDTH;
        xDraw = Math.max(xDraw, 2);

        if ((xDraw + boxWidth) > limits.width) {
            xDraw = limits.width - boxWidth - 2;
        }

        _graphics.setColor(new Color(0xFFFFE0));
        _graphics.fillRect(xDraw, yDraw - fontHeight - FRAMEWIDTH + 2, boxWidth,
                   boxHeight);

        _graphics.setColor(new Color(0xFFF0A0));
        _graphics.fillRect(xDraw, yDraw - fontHeight - FRAMEWIDTH + 2, boxWidth,
                   fontHeight + FRAMEWIDTH);

        _graphics.setColor(Color.black);
        _graphics.drawRect(xDraw, yDraw - fontHeight - FRAMEWIDTH + 2, boxWidth,
                   boxHeight);

        yDraw -= 3;

        for (int i = 0; i <_text.size(); i++) {
            String s = (String) _text.elementAt(i);
            
            Color c = (Color) _colors.elementAt(i);
            _graphics.setColor(c);

            _graphics.drawString(s, xDraw + FRAMEWIDTH, yDraw);
            yDraw += fontHeight;

            if (i == 0) {
                yDraw += 3;
            }
        }
    }

}
