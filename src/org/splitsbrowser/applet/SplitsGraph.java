/*
 *  Splitsbrowser - SplitsGraph
 *
 *  Original Copyright (c) 2000  Dave Ryder
 *  Version 2 Copyright (c) 2002 Reinhard Balling
*   Version 2.1 Copyright (c) 2002 Reinhard Balling
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
 * Created:    Dave Ryder
 * Version:    $Revision: 1.4 $
 * Changed:    $Date: 2003/09/18 19:29:40 $
 * Changed by: $Author: daveryder $
 */
package org.splitsbrowser.applet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import org.splitsbrowser.model.results.ControlCollection;
import org.splitsbrowser.model.results.Course;
import org.splitsbrowser.model.results.Result;
import org.splitsbrowser.model.results.SelectedResults;
import org.splitsbrowser.model.results.Time;
import org.splitsbrowser.util.ICompare;
import org.splitsbrowser.util.Message;
import org.splitsbrowser.util.Sorter;


/**
 *  A panel which displays a five diagrams of orienteering results<ul>
 *  <li>the difference to the reference time</li>
 *  <li>the absolute time after each control (absolute time = starttime + elapsedTime)</li>
 *  <li>total position after each control</li>
 *  <li>leg position for each leg</li>
 *  <li>percent time behind reference runner</li></ul>
 *  The default reference runner is the winner, but any runner with a valid run can be chosen
 *  as reference runner. It is also possible to choose the "ideal time", that is the sum of best split
 *  times as the reference time.<br><br>
 *
 *  This class is in urgent need of refactoring. It started as a single view (GRAPH_TYPE_COMPARISON) which
 *  has since grown in a rather non-oop way (just look for the various switch statements).<br>
 *  Also the functionality of pop-ups needs to be moved out to a class of its own.<br>
 *
 * @version    $Revision: 1.4 $
 */
public class SplitsGraph extends Panel {
    // Class global variables

    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 8215798085350838233L;

	/**  Enumeration to display cumulatibe cplit  */
    public static final int GRAPH_TYPE_COMPARISON = 0;

    /**  Enumeration to display  'race' graph */
    public static final int GRAPH_TYPE_ABSOLUTE = 1;

    /**  Description of the Field */
    public static final int GRAPH_TYPE_PERCENTBEHIND = 2;

    /**  Description of the Field */
    public static final int GRAPH_TYPE_POS = 3;

    /**  Description of the Field */
    public static final int GRAPH_TYPE_SPLITPOS = 4;

    /**  Description of the Field */
    private static final int GRAPH_TYPE_MAX = 5;

    /**  Collection of results selected by the use to be displayed */
    private static Vector displayedResults = new Vector();

    /**  Collection of colors used to draw the graph lines of size displayedResults */
    private static Vector displayColors = new Vector();

    /**  Description of the Field */
    private Axis xAxis = new Axis(false);

    /**  Description of the Field */
    private Axis yAxis = new Axis(false);

    /**  Color for the first stripe on the graph */
    private Color color1 = new Color(216, 220, 255); // D8DCFF

    /**  Color for the scrond stripe on the graph */
    private Color color2 = new Color(224, 232, 240); // E0E8F0
    private Color gridColor = Color.white;

    /**  Description of the Field */
    private ControlCollection cc;

    /**  Course displayed */
    private Course course = null;

    /**  Description of the Field */
    private Dimension dim;

    /**  Offscreen buffer the graph is rendered into before display */
    private Image offscreenBuffer;

    /**  Description of the Field */
    private LessThanResult lt = new LessThanResult();

    /**  Description of the Field */
    private Result reference;
    private SelectedResults selectedResults;
    private String eventName;

    /**  Description of the Field */
    private String[] controlVisitors = null;

    /**  Description of the Field */
    private int[] xPos = null;

    /**  Description of the Field */
    private boolean computeDimensions = true;

    /**  Description of the Field */
    private final int BOTTOM_MARGIN = 30;

    /**  Description of the Field */
    private final int LEFT_MARGIN = 20;

    /**  Description of the Field */
    private final int RIGHT_MARGIN = 10;

    /**  Description of the Field */
    private final int TOP_MARGIN = 25;

    /**  Description of the Field */
    private boolean showSplits = true;

    /**  Description of the Field */
    private boolean showTimeBehind = false;

    /**  Description of the Field */
    private boolean showTimeLoss = true;

    /**  Description of the Field */
    private boolean showTotalTime = false;

    /**  Description of the Field */
    private boolean thickLine = true;

    /**  Description of the Field */
    private int LEFT_AXIS = 0;

    /**  Description of the Field */
    private int RIGHT_AXIS = 1;

    /**  Description of the Field */
    private int cvX;

    /**  Description of the Field */
    private int cvY;

    /**  Description of the Field */
    private int graphType = GRAPH_TYPE_COMPARISON;

    /**  Description of the Field */
    private int legendSlotHeight;

    /**  Description of the Field */
    private int selectedSplit = -2;

    /**  Constructor for the SplitsGraph object */
    public SplitsGraph() {
        super();
        this.setFont(new Font("SansSerif", 0, 10));

        MyListener myListener = new MyListener();

        addMouseListener(myListener);
        addMouseMotionListener(myListener);

        dim = this.getSize();
    }

    /**
     *  Sets the color of the first contol strip in the splits graph
     *
     * @param  newColor1  The new color1 value
     */
    public void setColor1(java.awt.Color newColor1) {
        if (color1 != newColor1) {
            color1 = newColor1;
            repaint();
        }
    }

    /**
     *  Gets the first color used in the graph stripes
     *
     * @return    The color1 value
     */
    public Color getColor1() {
        return color1;
    }

    /**
     * Sets the color of the second contol strip in the splits graph
     *
     * @param  newColor2  The new color2 value
     */
    public void setColor2(Color newColor2) {
        if (color2 != newColor2) {
            color2 = newColor2;
            repaint();
        }
    }

    /**
     *   Gets the second color used in the graph stripes
     *
     * @return    The color2 value
     */
    public Color getColor2() {
        return color2;
    }

    /**
     *  Sets the controlCollection attribute of the SplitsGraph object
     *
     * @param  newCC  The new controlCollection value
     */
    public void setControlCollection(ControlCollection newCC) {
        cc = newCC;
    }

    /**
     *  Sets the course attribute of the SplitsGraph object
     *
     * @param  newCourse  The new course value
     */
    public void setCourse(Course newCourse) {
        course = newCourse;

        displayedResults.removeAllElements();
        displayColors.removeAllElements();
        reference = selectedResults.getReferenceResult();

        /*
         *  Create an array to hold the X positions
         */
        if (newCourse != null) {
            xPos = new int[course.getNumControls() + 3];

            // Add 2 extra spaces: One for start, one for finish, one for infinity
        }
    }

    /**
      *  Returns if a given result is displayed
      */
    public boolean isDisplayed(int resultIndex) {
        int index = displayedResults.indexOf(new Integer(resultIndex));

        return (index != -1);
    }

    public void setEventName(String newEventName) {
        eventName = newEventName;
    }

    /**
     *  Sets the graphType attribute of the SplitsGraph object
     *
     * @param  newGraphType   The new graphType value
     * @exception  Exception  Description of the Exception
     */
    public void setGraphType(int newGraphType) throws Exception {
        if (newGraphType >= GRAPH_TYPE_MAX) {
            throw new Exception("[SplitsGraph.setGraphType] Invalid graph type. Consult programmer.");
        }

        if (graphType != newGraphType) {
            graphType = newGraphType;
            invalidateDimensions();
        }
    }

    /**
     *  Sets the gridColor attribute of the SplitsGraph object
     *
     * @param  newColor  The new gridColor value
     */
    public void setGridColor(Color newColor) {
        gridColor = newColor;
        repaint();
    }

    /**
     *  Gets the gridColor attribute of the SplitsGraph object
     *
     * @return    The gridColor value
     */
    public Color getGridColor() {
        return gridColor;
    }

    public void setSelectedResults(SelectedResults newSelectedResults) {
        selectedResults = newSelectedResults;
    }

    /**
     *  Sets the showSplits attribute of the SplitsGraph object
     *
     * @param  newShowSplits  The new showSplits value
     */
    public void setShowSplits(boolean newShowSplits) {
        showSplits = newShowSplits;
        invalidateDimensions();
    }

    /**
     *  Gets the showSplits attribute of the SplitsGraph object
     *
     * @return    The showSplits value
     */
    public boolean getShowSplits() {
        return showSplits;
    }

    /**
     *  Sets the showTimeBehind attribute of the SplitsGraph object
     *
     * @param  newShowTimeBehind  The new showTimeBehind value
     */
    public void setShowTimeBehind(boolean newShowTimeBehind) {
        showTimeBehind = newShowTimeBehind;
        invalidateDimensions();
    }

    /**
     *  Gets the showTimeBehind attribute of the SplitsGraph object
     *
     * @return    The showTimeBehind value
     */
    public boolean getShowTimeBehind() {
        return showTimeBehind;
    }

    /**
     *  Sets the showTimeLoss attribute of the SplitsGraph object
     *
     * @param  newshowTimeLoss  The new showTimeLoss value
     */
    public void setShowTimeLoss(boolean newshowTimeLoss) {
        showTimeLoss = newshowTimeLoss;
        invalidateDimensions();
    }

    /**
     *  Gets the showTimeLoss attribute of the SplitsGraph object
     *
     * @return    The showTimeLoss value
     */
    public boolean getShowTimeLoss() {
        return showTimeLoss;
    }

    /**
     *  Sets the showTotalTime attribute of the SplitsGraph object
     *
     * @param  newShowTotalTime  The new showTotalTime value
     */
    public void setShowTotalTime(boolean newShowTotalTime) {
        showTotalTime = newShowTotalTime;
        invalidateDimensions();
    }

    /**
     *  Gets the showTotalTime attribute of the SplitsGraph object
     *
     * @return    The showTotalTime value
     */
    public boolean getShowTotalTime() {
        return showTotalTime;
    }

    /**
     *  Sets the thickLine attribute of the SplitsGraph object
     *
     * @param  newThickLine  The new thickLine value
     */
    public void setThickLine(boolean newThickLine) {
        thickLine = newThickLine;
        repaint();
    }

    /**
     *  Displays a given result index.
     *
     * @param newResult is an index into selectedResults.results
     */
    public void displayResult(int newResult) {
        /*
         *  Insert the result into the vector based on total time
         */
        Color[] colors =
        {
            Color.red, Color.blue, Color.green, Color.black, new Color(0xCC0066),
            new Color(0x000099), new Color(0xFFCC00), new Color(0x996600),
            new Color(0x9900FF), new Color(0xCCCC00), new Color(0xFFFF66),
            new Color(0xCC6699), new Color(0x99FF33), new Color(0x3399FF),
            new Color(0xCC33CC), new Color(0x33FFFF), new Color(0xFF00FF)
        };

        if (!isDisplayed(newResult)) {
            displayedResults.addElement(new Integer(newResult));
            displayColors.addElement(new Color(colors[newResult % colors.length].getRGB()));
            invalidateDimensions();
        }
    }

    /**
     *  Invalidate the dimensions of the graph
     *  <li>Recalculate the physical limits</li>
     *  <li>Recaclulate the x-positions for each control</li>
     *  Repaint the graph
     */
    public void invalidateDimensions() {
        //System.out.println("invalidateDimensions");
        computeDimensions = true;
        repaint();
    }

    /**
     *  Description of the Method
     *
     * @param  g  Description of the Parameter
     */
    public void paint(Graphics g) {
        Dimension newSize = this.getSize();

        reference = selectedResults.getReferenceResult();

        if (course == null) {
            g.setColor(this.getParent().getBackground());
            g.fillRect(0, 0, dim.width, dim.height);

            return;
        }

        if (computeDimensions || (dim.width != newSize.width) ||
                (dim.height != newSize.height)) {
            computeGraphDimensions(g);
            computeXPositions();
        }

        drawOffscreenImage();
        g.drawImage(offscreenBuffer, 0, 0, this);
    }

    /**  Removes all the displayed results */
    public void removeAllResults() {
        displayedResults.removeAllElements();
        displayColors.removeAllElements();
        invalidateDimensions();
    }

    /**
     *  Description of the Method
     *
     * @param  iResult  Description of the Parameter
     */
    public void removeResult(int iResult) {
        /*
         *  Hide the result and repaint the screen
         */
        int index = (int) displayedResults.indexOf(new Integer(iResult));

        if (index != -1) {
            displayedResults.removeElementAt(index);
            displayColors.removeElementAt(index);
            invalidateDimensions();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     *  Description of the Method
     *
     * @param  g  Description of the Parameter
     */
    public void update(Graphics g) {
        paint(g);
    }

    protected int getAxisWidth(Graphics g, int axis) {
        int MAX_TEXT_WIDTH = 300;
        int MIN_TEXT_WIDTH = 20;

        // Computes the areas on the canvas that the text and graph occupy
        FontMetrics fontMetrics = g.getFontMetrics(g.getFont());

        int maxTextWidth = 0;

        if (selectedResults.getNumResults() == 0) {
            return 0;
        }

        for (int i = 0; i < displayedResults.size(); i++) {
            Integer resultIndex = (Integer) displayedResults.elementAt(i);

            Result result = selectedResults.getResult(resultIndex.intValue());
            int w;

            if (axis == LEFT_AXIS) {
                w = fontMetrics.stringWidth(getLeftText(result));
            } else {
                w = fontMetrics.stringWidth(getRightText(result,
                                                         result.getNumControls())) +
                                        20;
            }

            maxTextWidth = Math.max(maxTextWidth, w);
        }

        return (Math.max(MIN_TEXT_WIDTH,
                         (Math.min(maxTextWidth, MAX_TEXT_WIDTH))));
    }

    protected String getLeftText(Result result) {
        if (graphType == GRAPH_TYPE_ABSOLUTE) {
            if ((result.getStartTime() == null) ||
                    !result.getStartTime().isValid()) {
                return Message.get("Graph.NoStartTime") + "  ";
            } else {
                String pfx = (result.isValid() ? " " : "*");

                return (result.getStartTime().toString() + " " + pfx +
                       result.getName() + "  ");
            }
        } else {
            return ("");
        }
    }

    protected String getRightText(Result result, int iSplit) {
        // Mark invalid results with a *
        String pfx = (result.isValid() ? "" : "*");
        StringBuffer s = new StringBuffer(pfx + result.getName());

        if (iSplit > -1) {
            if (showTotalTime) {
                s.append("  " + result.getTime(iSplit).toString());

                int pos = result.getTotalPos(iSplit);

                if (pos > 0) {
                    s.append(" (" + new Integer(pos).toString() + ")");
                } else {
                    s.append(" (*)");
                }
            }

            if (showSplits) {
                s.append("   " + result.getSplitTime(iSplit).toString());

                int pos = result.getSplitPos(iSplit);

                if (result.getSplitTime(iSplit).isValid()) {
                    s.append(" (" + new Integer(pos).toString() + ")");
                } else {
                    s.append(" (*)");
                }
            }

            if (showTimeBehind) {
                Result fastest = result.getCourse().getFastestLegTime(iSplit);
                s.append("  " +
                         result.getSplitTime(iSplit)
                               .subtract(fastest.getSplitTime(iSplit)).toString());

                if (graphType == GRAPH_TYPE_PERCENTBEHIND) {
                    Time timeY = getYTime(result, iSplit);

                    if (timeY.isValid()) {
                        s.append(" (" + (timeY.asSeconds() / 60) + "%)");
                    }
                }
            }

            if (showTimeLoss) {
                s.append("  " + result.getTimeLoss(iSplit).toString());
            }
        }

        return (s.toString());
    }

    protected Time getYFirst(Result result) {
        switch (graphType) {
        case GRAPH_TYPE_COMPARISON:
            return Time.ZEROTIME;

        case GRAPH_TYPE_ABSOLUTE:
            return result.getStartTime();

        case GRAPH_TYPE_PERCENTBEHIND:
            return Time.ZEROTIME;

        case GRAPH_TYPE_POS:
            return Time.NULLTIME;

        case GRAPH_TYPE_SPLITPOS:
            return Time.NULLTIME;
        }

        return Time.ZEROTIME;
    }

    protected Time getYTime(Result result, int split) {
        switch (graphType) {
        case GRAPH_TYPE_COMPARISON:
            return result.getTime(split).subtract(reference.getTime(split));

        case GRAPH_TYPE_ABSOLUTE:
            return result.getAbsoluteTime(split).subtract(reference.getTime(split));

        case GRAPH_TYPE_PERCENTBEHIND:

            Time timeR = result.getSplitTime(split);
            Time timeRef = reference.getSplitTime(split);

            if (timeR.isValid() && timeRef.isValid() &&
                    (timeRef.asSeconds() > 0)) {
                return new Time(((timeR.asSeconds() * 6000) / timeRef.asSeconds()) -
                                6000, true);
            } else {
                return Time.INVALIDTIME;
            }

        case GRAPH_TYPE_POS:
            return new Time(result.getTotalPos(split) * 60,
                            (result.getTotalPos(split) > 0) ? true : false);

        // Y-Axis expects a time
        case GRAPH_TYPE_SPLITPOS:
            return new Time(result.getSplitPos(split) * 60,
                            result.getSplitTime(split).isValid());
        }

        return Time.INVALIDTIME;
    }

    protected void computeGraphDimensions(Graphics g) {
        dim = this.getSize();

        // set the physical graph dimensions
        xAxis.setMinPixel(LEFT_MARGIN + getAxisWidth(g, LEFT_AXIS));
        xAxis.setMaxPixel(getSize().width - RIGHT_MARGIN -
                          getAxisWidth(g, RIGHT_AXIS));
        yAxis.setMinPixel(TOP_MARGIN);
        yAxis.setMaxPixel(getSize().height - BOTTOM_MARGIN);

        /*
         *  Compute the logical (time) limits
         */
        /*
         *  Y axis limits
         */
        int maxTime;
        int minTime;

        if (displayedResults.size() == 0) {
            minTime = 0;
            maxTime = 1;
        } else {
            maxTime = 0;
            minTime = Time.MAXTIME.asSeconds();

            for (int i = 0; i < displayedResults.size(); i++) {
                Result result =
                    selectedResults.getResult(((Integer) displayedResults.elementAt(i)).intValue());
                Time time;

                // Handle first (start) time
                time = getYFirst(result);

                if (time.isValid()) {
                    maxTime = Math.max(maxTime, time.asSeconds());
                    minTime = Math.min(minTime, time.asSeconds());
                }

                // ...and each of the splits
                for (int splitCount = 0; splitCount <= course.getNumControls();
                         splitCount++) {
                    time = getYTime(result, splitCount);

                    if (time.isValid()) {
                        maxTime = Math.max(maxTime, time.asSeconds());
                        minTime = Math.min(minTime, time.asSeconds());
                    }
                }
            }
        }

        if (minTime == Time.MAXTIME.asSeconds()) {
            minTime = maxTime - 1;
        }

        //System.out.println("["+minTime+","+maxTime+"]");
        yAxis.setMaxTime(new Time(maxTime));
        yAxis.setMinTime(new Time(minTime));

        /*
         *  X axis limits
         */
        xAxis.setMinTime(Time.ZEROTIME);
        xAxis.setMaxTime(reference.getTime(reference.getNumControls()));

        computeDimensions = false;
    }

    protected void computeXPositions() {
        /*
         *  Compute the X positions for the controls 0=Start, 1..numControls(), numControls()+1=Finish
         */
        xPos[0] = xAxis.toPixel(0);

        for (int i = 0; i <= course.getNumControls(); i++) {
            //System.out.println("[paint] i="+i+",Time="+reference.getTime(i).asSeconds());
            xPos[i + 1] = xAxis.toPixel(reference.getTime(i).asSeconds());
        }

        xPos[course.getNumControls() + 2] = Integer.MAX_VALUE >> 1;
    }

    protected void paintBackground(Graphics g) {
        /*
         *  First paint the Frame around the drawing area
         */
        Dimension dim = this.getSize();
        g.setColor(this.getBackground());
        g.draw3DRect(1, 1, dim.width - 2, dim.height - 2, false);

        FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
        int fontHeight = fontMetrics.getHeight();
        int halfFontHeight = fontHeight / 2;

        /*
         *  Draw coloured stripes for each control
         */
        boolean useColor1 = true;

        for (int i = 1; i <= (course.getNumControls() + 1); i++) {
            if (useColor1) {
                g.setColor(color1);
            } else {
                g.setColor(color2);
            }

            g.fillRect(xPos[i - 1], yAxis.getMinPixel(), xPos[i] - xPos[i - 1],
                       yAxis.getMaxPixel() - yAxis.getMinPixel());
            useColor1 = !useColor1;
        }

        g.setColor(gridColor);

        /*
         *  Draw a horizontal grid
         */
        int x0 = xAxis.getMinPixel();
        int x1 = xAxis.getMaxPixel();

        for (int i = 0; i < yAxis.getNumLabels(); i++) {
            g.drawLine(x0, yAxis.getLabelPixel(i), x1, yAxis.getLabelPixel(i));
        }

        /*
         *  Draw a box around the graph
         */
        g.setColor(gridColor);
        g.draw3DRect(xAxis.getMinPixel() - 1, yAxis.getMinPixel(),
                     xAxis.getMaxPixel() - xAxis.getMinPixel() + 1,
                     yAxis.getMaxPixel() - yAxis.getMinPixel(), false);

        // Draw the axis labels
        g.setColor(getForeground());

        // Control numbers at the top
        int y = yAxis.getMinPixel() - 5;

        for (int i = 0; i < (course.getNumControls() - 1); i++) {
            String label = new Integer(i + 1).toString();

            if (course.isValidControl(i)) {
                g.drawString(label,
                             xPos[i + 1] -
                             (fontMetrics.stringWidth(label) / 2), y);
            }
        }

        // Annotate the last label
        int i = course.getNumControls() - 1;
        String s =
            new Integer(i + 1).toString() + " " + Message.get("Graph.Finish");

        g.drawString(s, xPos[i + 1], y);

        // Draw "Start" label
        String start = Message.get("Graph.Start");

        g.drawString(start, xPos[0] - (fontMetrics.stringWidth(start) / 2), y);

        // ...and the time at the bottom of the graph
        y = yAxis.getMaxPixel() + fontMetrics.getHeight();

        for (i = 0; i < xAxis.getNumLabels(); i++) {
            g.drawString(xAxis.getLabelString(i), xAxis.getLabelPixel(i), y);
        }

        i = xAxis.getNumLabels() - 1;
        g.drawString(Message.get("Graph.Time"),
                     xAxis.getLabelPixel(i) -
                     (fontMetrics.stringWidth(Message.get("Graph.Time")) / 2),
                     y + fontHeight);
        g.drawString(eventName, xAxis.getLabelPixel(0), y + fontHeight);

        // time on the left axis
        String labelSuffix = "";

        if (graphType == GRAPH_TYPE_PERCENTBEHIND) {
            labelSuffix = " %";
        }

        for (i = 0; i < yAxis.getNumLabels(); i++) {
            s = yAxis.getLabelString(i) + labelSuffix;

            int x = xAxis.getMinPixel() - fontMetrics.stringWidth(s);

            g.drawString(s, x, yAxis.getLabelPixel(i) + halfFontHeight);
        }

        // Paint selected split
        if (selectedSplit >= -1) {
            int w = 1;

            g.setColor(Color.black);

            //gridColor);
            // Draw line at split
            g.fillRect(xPos[selectedSplit + 1] - w, yAxis.getMinPixel(), w,
                       yAxis.getMaxPixel() - yAxis.getMinPixel());
        }
    }

    private void setControlVisitors(boolean isLeftButton) {
        if (selectedSplit >= -1) {
            if (cvY < yAxis.getMinPixel()) {
                controlVisitors =
                    cc.getCoursesForControlNo(course.getControlCode(selectedSplit));
            } else {
                if (graphType == GRAPH_TYPE_ABSOLUTE) {
                    Time actualTime =
                        yAxis.toTime(cvY).add(reference.getTime(selectedSplit));
                    controlVisitors =
                        cc.getTimesNear(actualTime,
                                        course.getControlCode(selectedSplit),
                                        selectedResults.getAgeClasses());
                } else {
                    if (isLeftButton) {
                        controlVisitors = fastestTimesForLegText();
                    } else {
                        controlVisitors =
                            cc.getFastestTimesForLeg(course.getControlCode(selectedSplit -
                                                                           1),
                                                     course.getControlCode(selectedSplit),
                                                     selectedResults.getAgeClasses());
                    }
                }
            }
        } else {
            controlVisitors = new String[] { "" };
        }
    }

    private Result getDisplayedResult(int i) {
        int resultNo = ((Integer) displayedResults.elementAt(i)).intValue();

        return selectedResults.getResult(resultNo);
    }

    private void drawOffscreenImage() {
        Dimension dim = getSize();

        offscreenBuffer = createImage(dim.width, dim.height);

        Graphics g = offscreenBuffer.getGraphics();
        FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
        int fontHeight = fontMetrics.getHeight();

        legendSlotHeight = fontHeight - 4;

        int halfFontHeight = fontHeight / 2;

        /*
         *  Paint the background
         */
        paintBackground(g);

        /*
         *  Draw the results
         */
        reference = selectedResults.getReferenceResult();

        Integer[] sortedByFinishTime = new Integer[displayedResults.size()];
        int[] legend = new int[displayedResults.size()];
        int nToSort = 0;

        for (int i = 0; i < displayedResults.size(); i++) {
            /*
             *  Set the line style
             */
            g.setColor((Color) displayColors.elementAt(i));

            Result result = getDisplayedResult(i);
            Time t;
            Time tOld = getYFirst(result);
            int yOld = yAxis.toPixel(tOld.asSeconds());
            int xOld = xPos[0];

            for (int splitCount = 0;
                     splitCount < (result.getNumControls() + 1);
                     splitCount++) {
                t = getYTime(result, splitCount);

                int y = yAxis.toPixel(t.asSeconds());

                // Only draw the line if both results are valid
                if (t.isValid() && tOld.isValid()) {
                    // Draw three lines to make a thick one
                    g.drawLine(xOld, yOld, xPos[splitCount + 1], y);

                    if (thickLine) {
                        g.drawLine(xOld, yOld + 1, xPos[splitCount + 1], y + 1);
                        g.drawLine(xOld + 1, yOld, xPos[splitCount + 1] + 1, y);
                    }
                }

                yOld = y;
                xOld = xPos[splitCount + 1];
                tOld = t;
            }

            // Add text on the right of the graph
            Time yTime = getYTime(result, course.getNumControls());

            if (!yTime.isValid()) {
                yTime = Time.MAXTIME;
            }

            result.setSortTime(yTime);
            sortedByFinishTime[nToSort++] = new Integer(i + 1);

            // Add Text to left of graph
            if (graphType == GRAPH_TYPE_ABSOLUTE) {
                int y;

                if (getYFirst(result).isValid()) {
                    y = yAxis.toPixel(getYFirst(result).asSeconds());
                    g.setColor(getForeground());
                } else {
                    y = yAxis.getMaxPixel() - fontHeight;
                    g.setColor(Color.red);
                }

                g.drawString(getLeftText(result), LEFT_MARGIN, y);
            }
        }

        Sorter.Sort(sortedByFinishTime, 0, nToSort - 1, lt);

        int maxYLegend = yAxis.getMaxPixel() + legendSlotHeight;
        int maxShownRunners =
            ((maxYLegend - yAxis.getMinPixel()) / legendSlotHeight) - 1;

        if (nToSort > maxShownRunners) {
            nToSort = maxShownRunners;
        }

        int oldY = -1000;

        for (int i = 0; i < nToSort; i++) {
            Result result =
                getDisplayedResult(sortedByFinishTime[i].intValue() - 1);
            int y =
                yAxis.toPixel(result.getSortTime().asSeconds()) +
                halfFontHeight;

            if ((y - oldY) < legendSlotHeight) {
                y = oldY + legendSlotHeight;
            }

            legend[i] = y;

            if (y > maxYLegend) {
                int moveDist = y - maxYLegend;
                int i1 = i;

                while ((i1 >= 0) && (moveDist >= 0)) {
                    legend[i1] -= moveDist;

                    if (i1 > 0) {
                        moveDist =
                            legendSlotHeight - (legend[i1] - legend[i1 - 1]);
                    }

                    i1 -= 1;
                }
            }

            oldY = legend[i];
        }

        int xright = xAxis.getMaxPixel();

        for (int posL = 0; posL < nToSort; posL++) {
            if (legend[posL] != 0) {
                int y = legend[posL];

                g.setColor((Color) displayColors.elementAt(sortedByFinishTime[posL].intValue() -
                                                           1));
                g.drawLine(xright + 4, y - halfFontHeight, xright + 16,
                           y - halfFontHeight);
                g.drawLine(xright + 4, y - halfFontHeight + 1, xright + 16,
                           y - halfFontHeight + 1);
                g.setColor(Color.black);
                g.drawString(getRightText(getDisplayedResult(sortedByFinishTime[posL].intValue() -
                                                             1), selectedSplit),
                             xright + 20, y);
            }
        }
        

        if (controlVisitors != null) {
            showControlVisitors(g);
        }
        
        // TODO Horrible hack to display a message if we can not display a race graph
       if (graphType == GRAPH_TYPE_ABSOLUTE) {
           if (selectedResults.getResult(0) != null) {
               Result r =
                   selectedResults.getResult(0);

               if (!r.getStartTime().isValid()) {
                   g.setColor(Color.red);
                   g.setFont(new Font("SansSerif", Font.PLAIN, 15));
                   int xpos = xAxis.getMinPixel() + 15;
                   g.drawString(Message.get("Graph.NoRaceGraph1"),
                   xpos , 50);
                   g.drawString(Message.get("Graph.NoRaceGraph2"),
                                xpos, 70);
               }
           }
       }

    }

    private String[] fastestTimesForLegText() {
        if (selectedSplit < 0) {
            return (null);
        }
      
        Result[] fastestResults =
            selectedResults.getFastestResults(10, selectedSplit);
        String[] s = new String[fastestResults.length + 1];

        s[0] = "Selected classes";

        for (int i = 0; i < fastestResults.length; i++) {
            Result r = (Result) fastestResults[i];
            if (r !=null) {
            s[i + 1] =
                r.getSplitTime(selectedSplit).toString() + " / " + r.getName();
            }
        }

        return (s);
    }

    private void showControlVisitors(Graphics g) {
        FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
        int fontHeight = fontMetrics.getHeight();

        // Get size of box
        int width = 0;

        for (int i = 0; i < controlVisitors.length; i++) {
            width =
                Math.max(width, fontMetrics.stringWidth(controlVisitors[i]));
        }

        int FRAMEWIDTH = 4;
        int yDraw = cvY - ((controlVisitors.length * fontHeight) / 2) + 6;
        int boxHeight =
            (controlVisitors.length * fontHeight) + FRAMEWIDTH + FRAMEWIDTH;
        int boxWidth = width + FRAMEWIDTH + FRAMEWIDTH;

        if ((yDraw + boxHeight) > getSize().height) {
            yDraw = getSize().height - boxHeight - 2;
        }

        yDraw = Math.max(yDraw, fontHeight + FRAMEWIDTH + 2);

        int xDraw = (cvX + 20) - FRAMEWIDTH;
        xDraw = Math.max(xDraw, 2);

        if ((xDraw + boxWidth) > getSize().width) {
            xDraw = getSize().width - boxWidth - 2;
        }

        //System.out.println(Integer.toString(xDraw) + " "+Integer.toString(yDraw));
        g.setColor(new Color(0xFFFFE0));
        g.fillRect(xDraw, yDraw - fontHeight - FRAMEWIDTH + 2, boxWidth,
                   boxHeight);

        g.setColor(new Color(0xFFF0A0));
        g.fillRect(xDraw, yDraw - fontHeight - FRAMEWIDTH + 2, boxWidth,
                   fontHeight + FRAMEWIDTH);

        g.setColor(Color.black);
        g.drawRect(xDraw, yDraw - fontHeight - FRAMEWIDTH + 2, boxWidth,
                   boxHeight);

        yDraw -= 3;

        for (int i = 0; i < controlVisitors.length; i++) {
            String s = controlVisitors[i];

            if (s.endsWith("*")) {
                g.setColor(Color.blue);
                s = s.substring(0, s.length() - 1);
            } else {
                g.setColor(Color.black);
            }

            g.drawString(s, xDraw + FRAMEWIDTH, yDraw);
            yDraw += fontHeight;

            if (i == 0) {
                yDraw += 3;
            }
        }
    }

    private boolean updateSelectedIndex(MouseEvent e) {
        int newSelectedSplit = -2;

        if (course != null) {
            //System.out.println("MouseMoved");
            int mouseX = e.getX();

            newSelectedSplit = selectedSplit;

            for (int xIndex = 0; xIndex <= (course.getNumControls() + 1);
                     xIndex++) {
                if (mouseX < ((xPos[xIndex] + xPos[xIndex + 1]) / 2)) {
                    // Note that the x positions are offset 1 from the split indices
                    newSelectedSplit = xIndex - 1;

                    break;
                }
            }
        } else {
            newSelectedSplit = -2;
        }

        if (newSelectedSplit != selectedSplit) {
            selectedSplit = newSelectedSplit;

            return true;
        }

        return false;
    }

    private class LessThanResult implements ICompare {
        public boolean lessThan(Object a, Object b) {
            return getDisplayedResult(((Integer) a).intValue() - 1).getSortTime()
                       .lessThan(getDisplayedResult(((Integer) b).intValue() -
                                                    1).getSortTime());
        }
    }

    private class MyListener extends MouseAdapter implements MouseMotionListener {
        public void mouseDragged(MouseEvent e) {
            cvX = e.getX();
            cvY = e.getY();
            updateSelectedIndex(e);

            if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
                // right mouse key
                setControlVisitors(false);
            }

            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
                // left mouse key
                setControlVisitors(true);
            }

            repaint();
        }

        public void mouseMoved(MouseEvent e) {
            if (updateSelectedIndex(e)) {
                repaint();
            }
        }

        public void mousePressed(MouseEvent e) {
            cvX = e.getX();
            cvY = e.getY();

            if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
                // right mouse key
                setControlVisitors(false);
            }

            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
                // left mouse key
                setControlVisitors(true);
            }

            repaint();
        }

        public void mouseReleased(MouseEvent e) {
            controlVisitors = null;
            repaint();
        }
    }
}
