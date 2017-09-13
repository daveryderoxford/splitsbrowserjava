/*
 *  Splitsbrowser - Axis class.
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
 * Created:    Dave Ryder
 * Version:    $Revision: 1.3 $
 * Changed:    $Date: 2003/09/18 19:28:50 $
 * Changed by: $Author: daveryder $
 */
 
package org.splitsbrowser.applet;

/**
 *  A utility class to manage an axis Is responsible for computing axis limits
 *  and gird intervals and mapping between user and pixel spaces
 *
 * @version    $Revision: 1.3 $
 */

import org.splitsbrowser.model.results.Time;

public class Axis {
    /**  Description of the Field */
    private static int SEC_PER_MIN = 60;

    /**  Target intervals for 'ince' ticks */
    private static final int[] intervals = { 1, 2, 5 };

    /**  Maximum graphics pixel value for axis
     *   - stored as double internally to avoid type converstions */
    private double maxPixel = 0;

    /**  Maximum time to plot on axis  */
    private double maxTime = Time.ZEROTIME.asSeconds();

    /**  Minimum graphics pixel value for axis   */
    private double minPixel = 0;

    /**  Maximum time to plot on axis */
    private double minTime = Time.ZEROTIME.asSeconds();

    // grid interval in seconds

    /**  Calculated number of pixels per second */
    private transient double pixelsPerSec;

    /**  Description of the Field */
    private transient int firstLabel;

    /**  Interval between grid ticks (seconds) */
    private transient int gridInterval;

    /**  Shoud the maximum and minimum time limits be rounded to the nearest minute */
    private boolean roundTimes;

    /**  Maximum time requested.
     *  This is stored as well as the computed mininum to avoid rounding problems  */
    private int targetMaxTime;

    /**  Minumum time requested.
     *  This is stored as well as the computed mininum to avoid rounding problems  */
    private int targetMinTime;

    /**
     *  Constructs a new axis
     *
     * @param  newRoundTimes  Boolean to indicate if time should be rounded to the
     *      nearest minute
     */
    public Axis(boolean newRoundTimes) {
        roundTimes = newRoundTimes;
    }

    /**
     *  Gets the labelPixel attribute of the Axis object
     *
     * @param  i  Description of the Parameter
     * @return    The labelPixel value
     */
    public int getLabelPixel(int i) {
        return toPixel(firstLabel + (i * gridInterval));
    }

    /**
     *  Gets the labelString attribute of the Axis object
     *  This returns 
     *
     * @param  i  Index of 
     * @return    The labelString value
     */
    public String getLabelString(int i) {
        // Get label string in minutes
        return new Integer((firstLabel + (i * gridInterval)) / SEC_PER_MIN).toString();
    }

    /**
     *  Sets the maximum pixel corodinate for the axis
     *
     * @param  newMaxPixel  Maximum pixel value
     */
    public void setMaxPixel(int newMaxPixel) {
        maxPixel = newMaxPixel;
        computeMapping();
    }

    /**
     *  Returns the maximum pixel value
     *
     * @return    The maxPixel value
     */
    public int getMaxPixel() {
        return (int) maxPixel;
    }

    /**
     *  Sets the maxTime attribute of the Axis object
     *
     * @param  newMaxTime  The new maxTime value
     */
    public void setMaxTime(Time newMaxTime) {
        targetMaxTime = newMaxTime.asSeconds();

        computeMapping();
    }

    /**
     *  Gets the maxTime attribute of the Axis object
     *
     * @return    The maxTime value
     */
    public Time getMaxTime() {
        return new Time((int) maxTime);
    }

    /**
     *  Sets the minPixel attribute of the Axis object
     *
     * @param  newMinPixel  The new minPixel value
     */
    public void setMinPixel(int newMinPixel) {
        minPixel = newMinPixel;
        computeMapping();
    }

    /**
     *  Gets the minPixel attribute of the Axis object
     *
     * @return    The minPixel value
     */
    public int getMinPixel() {
        return (int) minPixel;
    }

    /**
     *  Sets the minTime attribute of the Axis object
     *
     * @param  newMinTime  The new minTime value
     */
    public void setMinTime(Time newMinTime) {
        targetMinTime = newMinTime.asSeconds();

        computeMapping();
    }

    /**
     *  Gets the minTime attribute of the Axis object
     *
     * @return    The minTime value
     */
    public Time getMinTime() {
        return new Time((int) minTime);
    }

    /**
     *  Gets the numLabels attribute of the Axis object
     *
     * @return    The numLabels value
     */
    public int getNumLabels() {
        return (((int) maxTime - firstLabel) / gridInterval) + 1;
    }

    /**
     *  Sets the roundTimes attribute of the Axis object
     *
     * @param  newRoundPixel  The new roundTimes value
     */
    public void setRoundTimes(boolean newRoundPixel) {
        roundTimes = newRoundPixel;
        computeMapping();
    }

    /**
     *  Gets the roundTimes attribute of the Axis object
     *
     * @return    The roundTimes value
     */
    public boolean getRoundTimes() {
        return roundTimes;
    }

    /**
     *  Returns the graphics pixel for a given time
     *
     * @param  time  Time requested to be converted
     * @return       Computed graphics pixel value
     */
    public int toPixel(Time time) {
        return toPixel(time.asSeconds());
    }

    /**
     *  Returns the graphics pixel for a given a time in seconds
     *
     * @param  seconds  Time in seconds requested to be converted
     * @return          Computed graphics pixel value
     */
    public int toPixel(int seconds) {
        return (int) (((seconds - minTime) * pixelsPerSec) + minPixel);
    }

    /**
     *  Returns the time for a given pixel value
     *
     * @param  pixel  Graphics pixel value to be converted
     * @return        Computed time
     */
    public Time toTime(int pixel) {
        int intTime = (int) (((pixel - minPixel) / pixelsPerSec) + minTime);

        return (new Time(intTime));
    }

    private void computeMapping() {
        // Round limits to nearest minute if required 
        if (roundTimes) {
            maxTime = SEC_PER_MIN * ((targetMaxTime / SEC_PER_MIN) + 1);
            minTime = SEC_PER_MIN * (targetMinTime / SEC_PER_MIN);
        } else {
            maxTime = targetMaxTime;
            minTime = targetMinTime;
        }

        // Ensure that the max time is always one to ensure one tick
        if (maxTime < SEC_PER_MIN) {
            maxTime = SEC_PER_MIN;
        }

        if (minTime == maxTime) {
            maxTime += SEC_PER_MIN;
        }

        double timeRange = (maxTime - minTime);

        pixelsPerSec = (maxPixel - minPixel) / timeRange;
        gridInterval = gridInt(timeRange);

        int num = (((int) minTime + gridInterval) - 1) / gridInterval;

        firstLabel = (num + 0) * gridInterval;
    }

    /**
     *  Computes the grid interval to be a 'nice' range
     *
     * @param  range  range of the axis (seconds)
     */
    private int gridInt(double range) {
        int r = (int) (range / SEC_PER_MIN) / 6;
        int p = 1;
        int w;
        int intervalPosn = 0;

        do {
            for (intervalPosn = 0; intervalPosn < intervals.length;
                     intervalPosn++) {
                w = intervals[intervalPosn] * p;

                if (w >= r) {
                    return w * SEC_PER_MIN;
                }
            }

            p = p * 10;
        } while (true);
    }
}
