package org.splitsbrowser.model.results;
/*
 *  Splitsbrowser - Result.
 *
 *  Copyright (C) 2002  Dave Ryder
 *  Copyright (C) 2002  Reinhard Balling
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
 * Created:    Reinhard Balling/Dave Ryder
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:30:55 $
 * Changed by: $Author: daveryder $
 */

/**
 * <b>Result</b> holds the result for one runner. In particular:
 * <ul><li>startnumber</li><li>Name</li><li>Club</li><li>{@link AgeClass}</li><li>Starttime</li>
 * <li>Elapsed times after each leg</li><li>Splittimes for each leg</li><li>Position after each leg</li>
 * <li>Position for the leg</li><li>Flag whether result is valid</li></ul>
 * Each {@link Time Time} can be valid or invalid so that partly valid results (e.g. disqualification after control x) can
 * also be compared.
 *
 * @author     Reinhard Balling
 * @version    1.0
 */

import org.splitsbrowser.model.SplitsbrowserException;
import org.splitsbrowser.util.Message;

public class Result {
    /**  AgeClass for this runner */
    private AgeClass ageClass;

    /**  Course for this runner */
    private Course course;

    /**  Club */
    private String club;

    /**  Name of runner (Firstname/familyname in the order given in the results) */
    private String name;

    /**  Helper field for sorting results */
    private Time sortTime;

    /**  Starttime or null if not available */
    private Time startTime;

    /**  Position after each control */
    private int[] position = null;

    /**  Position for individual legs */
    private int[] splitPosition = null;

    /**  Splittimes */
    private Time[] splitTimes = null;

    /** time loss */
    private Time[] timeLoss = null;

    /**  Elapsed times, i.e. time from start until control i (first control has number 0) */
    private Time[] totalTimes;

    /**  Flag whether the result is valid */
    private boolean isValid = false;
    private int numSplits;

    /**  Startnumber: Used to X-ref to starttime when using SI format */
    private int startNumber;
    
    private String status;

    /**
     * Create an anonymous result (for the reference runner in calcOptimumTimes)
     *
     * @param  numControls  Number of controls
     * @param  newCourse           Course
     */
    public Result(int numControls, Course newCourse) {
        // One extra split for Finish control
        numSplits = numControls + 1;
        totalTimes = new Time[numSplits];
        position = new int[numSplits];
        splitPosition = new int[numSplits];
        course = newCourse;
    }

    /**
     *  Constructor for the Result object
     *
     * @param  newName         Name of runner
     * @param  newClub         Club
     * @param  newCourse       {@link Course Course}
     * @param  newAgeClass     {@link AgeClass AgeClass}
     * @param  newTimes        Array of elapsed times (must have numberOfControls+1 elements)
     * @param  newStartTime    {@link Time starttime}
     * @param  newStartNumber  startnumber
     * @param  newIsValid      Flag whether the result is valid (true/false)
     */
    public Result(String newName, String newClub, Course newCourse,
                  AgeClass newAgeClass, Time[] newTimes, Time newStartTime,
                  int newStartNumber, boolean newIsValid)
           throws SplitsbrowserException
    {
        if (newTimes.length != (newCourse.getNumControls() + 1)) {
            throw new SplitsbrowserException("Error in Results constructor: Number of controls not OK for " +
                                newName);
        }

        name = newName;
        club = newClub;
        startNumber = newStartNumber;
        course = newCourse;
        ageClass = newAgeClass;
        totalTimes = newTimes;
        startTime = newStartTime;
        isValid = newIsValid;

        numSplits = newCourse.getNumControls() + 1;

        position = new int[numSplits];
        splitPosition = new int[numSplits];
    }

    /**
     *  Constructor for the Result object
     *
     * @param  newName         Name of runner
     * @param  newClub         Club
     * @param  newCourse       {@link Course Course}
     * @param  newAgeClass     {@link AgeClass AgeClass}
     * @param  newTimes        Array of elapsed times (must have numberOfControls+1 elements)
     * @param  newStartTime    {@link Time starttime}
     * @param  newIsValid      Flag whether the result is valid (true/false)
     */
    public Result(String newName, String newClub, Course newCourse,
                  AgeClass newAgeClass, Time[] newTimes, Time newStartTime,
                  boolean newIsValid) throws Exception
    {
        this(newName, newClub, newCourse, newAgeClass, newTimes, newStartTime,
             -1, newIsValid);
    }

    /**
     *  Gets the absolute time at the specified control
     *
     * @param  numControl  Index into the elapsed times array (-1 is start, 0 is first control)
     * @return    The absolute {@link Time Time} value at the specified control. Returns INVALIDTIME when starttime is not set
     */
    public Time getAbsoluteTime(int numControl) {
        if (startTime == null) {
            return Time.INVALIDTIME;
        }

        return startTime.add(getTime(numControl));
    }

    /**
     *  Gets the AgeClass
     *
     * @return    The {@link AgeClass AgeClass} value
     */
    public AgeClass getAgeClass() {
        return ageClass;
    }

    /**
     *  Sets the club name
     *
     * @param  newClub  The name of the club
     */
    public void setClub(String newClub) {
        club = newClub;
    }

    /**
     *  Gets the club
     *
     * @return    The club
     */
    public String getClub() {
        return club;
    }

    /**
     *  Gets the course
     *
     * @return    The {@link Course Course} value
     */
    public Course getCourse() {
        return course;
    }

    /**
     *  Gets the finish time
     *
     * @return    The finish time
     */
    public Time getFinishTime() {
        return getTime(totalTimes.length - 1);
    }

    /**
     *  Gets the name of the runner
     *
     * @return    The name of the runner
     */
    public String getName() {
        return name;
    }

    /**
     *  Gets the name of the runner padded to a given width
     *
     * @param  width  The width to pad/truncate to
     * @return        The name of the runner padded/truncated to width
     */
    public String getName(int width) {
        String s =
            name + "                                                      ";

        // Assumes that width is reasonable, i.e. up to 30
        return s.substring(0, width);
    }

    /**
     * Gets the number of controls
     *
     * @return    Number of controls
     */
    public int getNumControls() {
        // To be consistent with course.getNumControls() don't count the finish control !
        return totalTimes.length - 1;
    }

    /**
     *  Sets the sortTime
     *
     * @param  newSortTime  The new sortTime value
     */
    public void setSortTime(Time newSortTime) {
        sortTime = newSortTime;
    }

    /**
     *  Gets the sortTime
     *
     * @return    The sortTime value
     */
    public Time getSortTime() {
        return sortTime;
    }

    /**
     *  Gets the leg position at the given control
     *
     * @param  numControl      The number of the control for which the leg position is to be retrieved
     * @return           The position for this leg
     */
    public int getSplitPos(int numSplit) {
        return splitPosition[numSplit];
    }

    /**
     * Get an split time for the specified control<br><br>
     * Calculation of splittimes is delayed until the first splittime is requested<br>
     *
     * @param  numControl  Index into the elapsed times array (0 is first control)
     * @return    The splittime value
     */
    public Time getSplitTime(int numControl) {
        if (splitTimes == null) {
            calcSplitTimes();
        }

        if ((numControl < 0) || (numControl >= totalTimes.length)) {
            System.out.println("Result.getSplitTime for " + name + "/" +
                               ageClass.getName() + " numControl=" +
                               numControl + "[" + (totalTimes.length - 1) +
                               "]");

            return Time.ZEROTIME;
        } else {
            return splitTimes[numControl];
        }
    }

    /**
     *  Sets the starttime
     *
     * @param  newStartTime  The new starttime
     */
    public void setStartTime(Time newStartTime) {
        startTime = newStartTime;
    }

    /**
     *  Gets the starttime
     *
     * @return    The starttime value
     */
    public Time getStartTime() {
        if (startTime == null) {
            return Time.INVALIDTIME;
        }

        return startTime;
    }

    /**
     *  Sets the starttime in seconds
     *
     * @param  newStartTimeAsSeconds  The new starttime given as seconds
     */
    public void setStartTimeAsSeconds(int newStartTimeAsSeconds) {
        startTime.setSeconds(newStartTimeAsSeconds);
    }

    /**
     * Sets the time for control 'numControl'
     *
     * @param  numControl      The number of the control for which the time is to be set
     * @param  newTime                  The new {@link Time Time} value
     */
    public void setTime(int numControl, Time newTime) {
        totalTimes[numControl] = newTime;
    }

    /**
     * Sets the time for control 'numControl'
     *
     * @param  numControl                The number of the control for which the time is to be set
     * @param  seconds                  The new time in seconds
     * @param  isValid                  Validity flag for the time
     */
    public void setTime(int numControl, int seconds, boolean isValid) {
        totalTimes[numControl] = new Time(seconds, isValid);
    }

    /**
     * Get an elapsed time for the specified control
     *
     * @param  numControl  Index into the elapsed times array (-1 is start, 0 is first control)
     * @return    The elapsed {@link Time Time} up to the specified control
     */
    public Time getTime(int numControl) {
        if (numControl == -1) {
            // Starttime
            return Time.ZEROTIME;
        }

        if (totalTimes[numControl] == null) {
            addMissingTimes();

            return Time.INVALIDTIME;
        }

        return totalTimes[numControl];
    }

    /**
     *  Gets the time loss for a specific leg
     *
     * @return    The time loss
     */
    public Time getTimeLoss(int i) {
        if (timeLoss == null) {
            calculateTimeLoss();
        }

        return (timeLoss[i]);
    }

    /**
     *  Gets the total position at the given control
     *
     * @param  numControl      The number of the control for which the position is to be retrieved
     * @return           The total position
     */
    public int getTotalPos(int numControl) {
        return position[numControl];
    }

    /**
     *  Sets the valid flag of the result
     *
     * @param  newIsValid  A boolean flag which determines whether the result is valid
     */
    public void setValid(boolean newIsValid) {
        isValid = newIsValid;
    }

    /**
     *  Returns true if the result is valid
     *
     * @return    True if the result is valid, false otherwise
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Adds missing times to the end of the array of elapsed times
     */
    public void addMissingTimes() {
        int i;

        for (i = totalTimes.length - 1; i >= 0; i--) {
            if (totalTimes[i] == null) {
                totalTimes[i] = Time.NULLTIME;
            } else {
                break;
            }
        }
    }

    /**
     * Calculates an estimate for time loss all the results splits
     *
     * The algorithm used to calculate the time loss is as follows.<BR>
     *
     *  1. The fastest time of each leg for the class is calculated<BR>
     *  2. Calculate rate of loss for each leg<BR>
     *  3. Find the median time loss rate<BR>
     *     The median is chosen as it is not influenced by large losses<BR>
     *  4. Using this median loss rate calculate the target time for each control<BR>
     */
    protected void calculateTimeLoss() {
        double[] lossRate = new double[numSplits];

        timeLoss = new Time[numSplits];

        // Get fastest times for each leg
        int[] fastest = new int[numSplits];

        for (int i = 0; i < numSplits; i++) {
            fastest[i] =
                ageClass.getFastestLegTime(i).getSplitTime(i).asSeconds();

            lossRate[i] =
                ((double) (getSplitTime(i).asSeconds() - fastest[i])) / fastest[i];
        }
        
        // Find median loss rate
        lossRate = sort(lossRate);
        double medianLossRate = quant(0.5, lossRate);

        // Compute time loss for each control
        for (int i = 0; i < numSplits; i++) {
            int targetTime = (int) (medianLossRate * fastest[i]) + fastest[i];
            timeLoss[i] = new Time(getSplitTime(i).asSeconds() - targetTime);
        }
    }

    /**
     *  Sets the leg position (=position for a given leg) for a given control
     *
     * @param  numControl      The number of the control for which the leg position is to be set
     * @param  newPosition  The new leg position value
     */
    void setSplitPos(int numControl, int newSplitPosition) {
        splitPosition[numControl] = newSplitPosition;
    }

    /**
     * Gets the startnumber
     * @return The startnumber
     */
    int getStartNumber() {
        return startNumber;
    }

    /**
     *  Sets the total position for a given control
     *
     * @param  numControl      The number of the control for which the total position is to be set
     * @param  newPosition  The new total position value
     */
    void setTotalPos(int numControl, int newPosition) {
        position[numControl] = newPosition;
    }

    private void calcSplitTimes() {
        Time lastValidTime = new Time(0, true);
        int i;
        int len = totalTimes.length;
        splitTimes = new Time[len];

        for (i = 0; i < len; i++) {
            if (course.isValidControl(i)) {
                splitTimes[i] = getTime(i).subtract(lastValidTime);

                if (splitTimes[i].asSeconds() < 0) {

                    System.out.println("[" + ageClass.getName() + "]/[" + name +
                                       "] " +                                    
                                     Message.get("Result.SplitTime", i + 1));
                }

                lastValidTime = getTime(i);
            } else {
                splitTimes[i] = Time.NULLTIME;
            }
        }
    }

    /**
     * Returns an estimate of the value at a given  from a sorted array
     * eg use q=0.5 for the median
     * @param q
     * @param sortedArray
     * @return
     */
    private double quant(double q, double[] sortedArray) {
        int n = sortedArray.length;

        if ((q > 1) || (q < 0)) {
            return (0);
        } else {
            double index = (n + 1) * q;

            if ((index - (int) index) == 0) {
                return sortedArray[(int) index - 1];
            } else {
                double ret;
                ret = (q * sortedArray[(int) Math.floor(index) - 1]) +
                      ((1 - q) * sortedArray[(int) Math.ceil(index) - 1]);

                return (ret);
            }
        }
    }

    /**
     * Sorts an array of double values into asending order.
     * The input and output arrays may be the same variable.
     *
     * @param inputArray
     * @return Array sorted into asending order
     */
    private double[] sort(double[] inputArray) {
        int n = inputArray.length;

        double[] sortx = (double[]) inputArray.clone();
        int incr = (int) (n * .5);

        while (incr >= 1) {
            for (int i = incr; i < n; i++) {
                double temp = sortx[i];
                int j = i;

                while ((j >= incr) && (temp < sortx[j - incr])) {
                    sortx[j] = sortx[j - incr];
                    j -= incr;
                }

                sortx[j] = temp;
            }

            incr /= 2;
        }

        return (sortx);
    }
    /**
     * @return
     */
    public String getStatus() {
        return status;
    }

}
