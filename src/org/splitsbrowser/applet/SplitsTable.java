/*
 *  SplitsBrowser - SplitsTable class.
 *
 *  Copyright (C) 2000  Dave Ryder
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
 * Created:    Dave Ryder 2 November 2002
 * Version:    $Revision: 1.3 $
 * Changed:    $Date: 2003/09/18 19:29:40 $
 * Changed by: $Author: daveryder $
 */
 
package org.splitsbrowser.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.TextArea;

import org.splitsbrowser.model.results.AgeClass;
import org.splitsbrowser.model.results.Course;
import org.splitsbrowser.model.results.Result;
import org.splitsbrowser.util.Message;

/**
 *  A scrolling panel which displays a simple text vie wof split results
 *
 * @version  $Revision: 1.3 $
 */
public class SplitsTable extends TextArea {
    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 8395587937894068075L;

	/**  Description of the Field */
    private static int FIELD_WIDTH = 7;

    /**  Description of the Field */
    private static int NAME_WIDTH = 20;

    /**  Description of the Field */
    protected String CONTROL_GAP = "  ";

    /**  Description of the Field */
    protected String NAME_PADDING = "                   ";

    /**  Description of the Field */
    protected String POS_PADDING = "     ";

    /**  Description of the Field */
    protected String TITLE_PADDING = POS_PADDING + NAME_PADDING + "        ";

    /**  Description of the Field */
    protected int CONTROL_REQ = 5;

    /**  Description of the Field */
    protected int CONTROL_COLS = CONTROL_REQ + CONTROL_GAP.length();

    /**  Description of the Field */
    protected int TITLE_COLS = TITLE_PADDING.length();

    /**  Description of the Field */
    BorderLayout borderLayout1 = new BorderLayout();

    /**  Description of the Field */
    private AgeClass ageClass;

    /**  Description of the Field */
    private Course course;

    /**  Description of the Field */
    private boolean ageClassInvalid = true;

    /**  Description of the Field */
    private boolean showSplitPos = false;

    /**  Description of the Field */
    private boolean showSplits = true;

    /**  Description of the Field */
    private boolean showTimeBehind = false;

    /**  Description of the Field */
    private boolean showTotalPos = false;

    /**  Description of the Field */
    private boolean showTotalTime = true;

    /**  Constructor for the SplitsTable object */
    public SplitsTable() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *  Sets the ageClass attribute of the SplitsTable object
     *
     * @param  newCourse    The new ageClass value
     * @param  newAgeClass  The new ageClass value
     */
    public void setAgeClass(Course newCourse, AgeClass newAgeClass) {
        if ((newCourse == null) || (newAgeClass == null)) {
            System.out.println("[setAgeClass] null");
            setText(Message.get("Table.SelectOneClass"));
        }

        // else System.out.println("[setAgeClass] "+newCourse.getName()+"/"+newAgeClass.getName());
        course = newCourse;
        ageClass = newAgeClass;

        // delay setting the text until it is first shown
        ageClassInvalid = true;
        repaint();
    }

    /**
     *  Sets the showSplitPos attribute of the SplitsTable object
     *
     * @param  newShowSplitPos  The new showSplitPos value
     */
    public void setShowSplitPos(boolean newShowSplitPos) {
        showSplitPos = newShowSplitPos;
        ageClassInvalid = true;
        repaint();
    }

    /**
     *  Gets the showSplitPos attribute of the SplitsTable object
     *
     * @return    The showSplitPos value
     */
    public boolean getShowSplitPos() {
        return showSplitPos;
    }

    /**
     *  Sets the showSplits attribute of the SplitsTable object
     *
     * @param  newShowSplits  The new showSplits value
     */
    public void setShowSplits(boolean newShowSplits) {
        showSplits = newShowSplits;
        ageClassInvalid = true;
        repaint();
    }

    /**
     *  Gets the showSplits attribute of the SplitsTable object
     *
     * @return    The showSplits value
     */
    public boolean getShowSplits() {
        return showSplits;
    }

    /**
     *  Sets the showTimeBehind attribute of the SplitsTable object
     *
     * @param  newShowTimeBehind  The new showTimeBehind value
     */
    public void setShowTimeBehind(boolean newShowTimeBehind) {
        showTimeBehind = newShowTimeBehind;
        ageClassInvalid = true;
        repaint();
    }

    /**
     *  Gets the showTimeBehind attribute of the SplitsTable object
     *
     * @return    The showTimeBehind value
     */
    public boolean getShowTimeBehind() {
        return showTimeBehind;
    }

    /**
     *  Sets the showTotalPos attribute of the SplitsTable object
     *
     * @param  newShowTotalPos  The new showTotalPos value
     */
    public void setShowTotalPos(boolean newShowTotalPos) {
        showTotalPos = newShowTotalPos;
        ageClassInvalid = true;
        repaint();
    }

    /**
     *  Gets the showTotalPos attribute of the SplitsTable object
     *
     * @return    The showTotalPos value
     */
    public boolean getShowTotalPos() {
        return showTotalPos;
    }

    /**
     *  Sets the showTotalTime attribute of the SplitsTable object
     *
     * @param  newShowTotalTime  The new showTotalTime value
     */
    public void setShowTotalTime(boolean newShowTotalTime) {
        showTotalTime = newShowTotalTime;
        ageClassInvalid = true;
        repaint();
    }

    /**
     *  Gets the showTotalTime attribute of the SplitsTable object
     *
     * @return    The showTotalTime value
     */
    public boolean getShowTotalTime() {
        return showTotalTime;
    }

    /**
     *  Description of the Method
     *
     * @param  g  Description of the Parameter
     */
    public void paint(Graphics g) {
        if (ageClassInvalid) {
            if (ageClass != null) {
                setCols();
                setRows();
                setAgeClassText();
            }

            ageClassInvalid = false;
        }

        super.paint(g);
    }

    public void update(Graphics g) {
        paint(g);
    }

    protected void setAgeClassText() {
        boolean showPos = showSplitPos | showTotalPos;
        int pW = 3;

        // Pad width for positions
        int nR = ageClass.getNumResults();

        if (nR >= 10) {
            pW++;
        }

        if (nR > 100) {
            pW++;
        }

        if (!showPos) {
            pW = 0;
        }

        StringBuffer s = new StringBuffer(getColumns() * getRows());

        s.append("  " + ageClass.getName() + "  " + course.getDistance() + " " +
                 course.getClimb() + "m " + course.getNumControls() + "\n");

        String z1 = "          ";
        String z2 = "          ";

        for (int iSplit = 0; iSplit < course.getNumControls(); iSplit++) {
            z1 = z1 + leftPad(Integer.toString(iSplit + 1), FIELD_WIDTH + pW);

            if (course.getControlCode(iSplit) != "") {
                z2 += leftPad("(" + course.getControlCode(iSplit) + ")",
                              FIELD_WIDTH + pW);
            }
        }

        s.append(rightPad("", 30 - pW) + z1 + " " +
                 leftPad(Message.get("Graph.Finish"), FIELD_WIDTH + pW) + "\n");

        if (!z2.equals("")) {
            s.append(rightPad("", 32 - pW) + z2 + "\n");
        }

        s.append("\n");

        for (int res = 0; res < ageClass.getNumResults(); res++) {
            // Top line has name and split times
            Result result = ageClass.getResult(res);
            String name = result.getName(NAME_WIDTH);
            String pos = leftPad(Integer.toString(res + 1), 3);

            if (!result.isValid()) {
                pos = "   ";
            }

            s.append(pos + "  " + name);

            if (result.getStartTime().isValid()) {
                s.append(leftPad("[" + result.getStartTime().toString() + "]",
                                 10));
            } else {
                s.append("  [------]");
            }

            s.append(leftPad(result.getFinishTime().toString(), 7));

            // Write the total times
            boolean invalidTimeFnd = false;

            for (int iSplit = 0; iSplit <= course.getNumControls(); iSplit++) {
                invalidTimeFnd =
                    invalidTimeFnd ||
                    (!result.getTime(iSplit).isValid() && !result.isValid());
                s.append(leftPad(result.getTime(iSplit).toString(), FIELD_WIDTH));

                String str = "";

                if (showPos) {
                    if (result.getTime(iSplit).isValid() && !invalidTimeFnd) {
                        str = "(" +
                              Integer.toString(result.getTotalPos(iSplit)) +
                              ")";
                    }
                }

                s.append(rightPad(str, pW));
            }

            s.append("\n");

            // If splits required write the split times
            if (showSplits) {
                s.append("     " + rightPad(result.getClub(), NAME_WIDTH + 6) +
                         "           ");

                for (int iSplit = 0; iSplit <= course.getNumControls();
                         iSplit++) {
                    s.append(leftPad(result.getSplitTime(iSplit).toString(),
                                     FIELD_WIDTH));

                    String str = "";

                    if (showPos) {
                        if (result.getSplitTime(iSplit).isValid()) {
                            str = "(" +
                                  Integer.toString(result.getSplitPos(iSplit)) +
                                  ")";
                        }
                    }

                    s.append(rightPad(str, pW));
                }

                s.append("\n");
            }

            s.append("\n");
        }

        setText(s.toString());
    }

    protected void setCols() {
        int numCols = (course.getNumControls() * CONTROL_COLS) + TITLE_COLS;

        this.setColumns(numCols);
    }

    protected int rowsPerResult() {
        int rowsPerResult = 2;

        if (showSplits) {
            rowsPerResult++;
        }

        return (rowsPerResult);
    }

    private void setRows() {
        int rows = ageClass.getNumResults() * rowsPerResult();

        this.setRows(rows);
    }

    private void jbInit() throws Exception {
        this.setSize(new Dimension(403, 291));

        // A fixed width font must be used in this control
        this.setFont(new Font("Courier", 0, 11));
        this.setBackground(Color.white);
        this.setEditable(false);
    }

    private String leftPad(String value, int len) {
        String s = value;

        while (len > s.length()) {
            s = " " + s;
        }

        return (s);
    }

    private String rightPad(String value, int len) {
        String s = value;

        while (s.length() < len) {
            s = s + " ";
        }

        return (s);
    }
}
