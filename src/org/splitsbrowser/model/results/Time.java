package org.splitsbrowser.model.results;
/*
 *  Splitsbrowser - Time class.
 *
 *  (c) Reinhard Balling, October 2002
 *  (c) Keith Ryder
 *  (c)  David Ryder
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
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:30:55 $
 * Changed by: $Author: daveryder $
 */
 
import org.splitsbrowser.util.Message;

/**
 *  A utility time class.
 *
 * @version  $Revision: 1.1 $

 */
public class Time {
    /**  Valid time 0 seconds */
    public static final Time ZEROTIME = new Time(0, true);

    /**  Valid time Integer.MAX_VALUE seconds  */
    public static final Time MAXTIME = new Time(Integer.MAX_VALUE, true);

    /**  Invalid time Integer.MAX_VALUE seconds */
    public static final Time INVALIDTIME = new Time(Integer.MAX_VALUE, false);

    /**  Invalid time 0 seconds */
    public static final Time NULLTIME = new Time(0, false);

    /**  Flag specifying a valid time */
    private boolean validTime = true;

    /**  The time as seconds */
    private int secondsFromMidnight = 0;
    
    /**
     * Constructs a time from minutes and seconds
     *
     * @param  minutes   number of minutes. May be greater than 60.
     * @param  seconds   Seconds
     */
    public Time(int hour, int minutes, int seconds) {
        secondsFromMidnight = (hour*60 + minutes) * 60 + seconds;
        validTime = true;
    }

    /**
     * Constructs a time from minutes and seconds
     *
     * @param  minutes   number of minutes. May be greater than 60.
     * @param  seconds   Seconds
     */
    public Time(int minutes, int seconds) {
        secondsFromMidnight = (minutes * 60) + seconds;
        validTime = true;
    }

    /**
     *  Constructs a time with validity parameter
     *
     * @param  seconds  Description of the Parameter
     * @param  valid    Description of the Parameter
     */
    public Time(int seconds, boolean valid) {
        secondsFromMidnight = seconds;
        validTime = valid;
    }

    /**
     *  Constructs a time from number of seconds from midnight
     *
     * @param  newSecondsFromMidnight  number of seconds.from midnight
     */
    public Time(int newSecondsFromMidnight) {
        secondsFromMidnight = newSecondsFromMidnight;
        validTime = true;
    }

    /**
     *  Construct a time from a string. New in V2.0. RB
     *
     * @param  timeStr        String representing time. Can be hh:mm:ss or mmm:ss or -----
     * @exception  Exception  Invalid time format
     */
    public Time(String timeStr) throws Exception {
        String hourString;
        String minString;
        String secString;
        int firstSep;
        int lastSep;

        // If no : is found in the time it is invalid
        if (timeStr.indexOf(":") == -1) {
            if (timeStr.equals("0.00")) {
                secondsFromMidnight = 0;
            } else {
                secondsFromMidnight = Integer.MAX_VALUE;
            }

            validTime = false;
        } else {
            try {
                if ((firstSep = timeStr.indexOf(":")) == (lastSep =
                                            timeStr.lastIndexOf(":"))) {
                    //  mmm:ss format
                    minString = timeStr.substring(0, firstSep);
                    secString = timeStr.substring(firstSep + 1);
                    secondsFromMidnight =
                        (Integer.parseInt(minString) * 60) +
                        Integer.parseInt(secString);
                    validTime = true;
                } else {
                    //  hh:mm:ss format
                    if (firstSep == 0) {
                        hourString = "1";

                        // Fixes SI bug: Sometimes times are printed :23:34 instead of 1:23:34
                    } else {
                        hourString = timeStr.substring(0, firstSep);
                    }

                    minString = timeStr.substring(firstSep + 1, lastSep);
                    secString = timeStr.substring(lastSep + 1);
                    secondsFromMidnight =
                        (((Integer.parseInt(hourString) * 60) +
                        Integer.parseInt(minString)) * 60) +
                        Integer.parseInt(secString);
                    validTime = true;
                }
            } catch (NumberFormatException e) {
                throw (new Exception(Message.get("Time.BadSplit").concat(timeStr)));
            }
        }
    }

    /**  Constructor for the Time object */
    private Time() {
    }

    /**
     *  Adjusts the seconds
     *
     * @param  newSeconds  The new seconds value
     */
    public void setSeconds(int newSeconds) {
        secondsFromMidnight = newSeconds;
    }

    /**
     *  Sets the valid attribute of the Time object
     *
     * @param  newValid  The new valid value
     */
    public void setValid(boolean newValid) {
        validTime = newValid;
    }

    /**
     *  Returns time validity indicator
     *
     * @return    True if the time is valid, false otherwise
     */
    public boolean isValid() {
        return validTime;
    }

    /**
     * Add a time<br>
     * If either time is invalid, the result is invalid too.
     *
     * @param  time2  Time to add
     * @return     The sum of the times
     */
    public Time add(Time time2) {
        if (validTime && time2.isValid()) {
            return new Time(secondsFromMidnight + time2.asSeconds());
        } else {
            return INVALIDTIME;
        }
    }

    /**
     *  Returns the number of seconds from midnightgetm
     *
     * @return          The number of seconds from midnight
     */
    public int asSeconds() {
        return (secondsFromMidnight);
    }

    /**
     *  Compare two times and return true if the first time is less than the second time
     *
     * @param  time2  Time to compare with
     * @return     True if the time is less than the time t2
     */
    public boolean lessThan(Time time2) {
        if (!validTime || !time2.isValid()) {
            return false;
        } else {
            return secondsFromMidnight < time2.asSeconds();
        }
    }

    /**
     *  Compare two times and return true if the first time is less  than or equal to the second time
     *
     * @param  time2  Time to compare with
     * @return     True if the time is less than the time t2
     */
    public boolean lessThanEq(Time time2) {
        if (!validTime || !time2.isValid()) {
            return false;
        } else {
            return secondsFromMidnight <= time2.asSeconds();
        }
    }

    /**
     * Subtract a time<br>
     * If either time is invalid, the result is invalid too.
     *
     * @param  time2  Time to subtract
     * @return     The difference of the times
     */
    public Time subtract(Time time2) {
        if (validTime && time2.isValid()) {
            return new Time(secondsFromMidnight - time2.asSeconds());
        } else {
            return INVALIDTIME;
        }
    }

    /**
     *  Returns the time as a string in mm:ss format. Prefacing it by a - sign if
     *  the time is negative.<br>
     *  Invalid time returns -----<br>
     * Times above 9 hours are formatted as hh:mm:ss<br>
     *
     * @return    The formatted time
     */
    public String toString() {
        // If we have negative seconds we need to add the - sign by hand
        if (validTime) {
            if (secondsFromMidnight < 0) {
                return ("-" + pad(getMins()) + ':' + pad(getSecs()));
            } else {
                if (secondsFromMidnight > 32400) {
                    return pad(getHour()) + ":" +
                           (pad(getMinsPerHour()) + ':' + pad(getSecs()));
                } else {
                    return (pad(getMins()) + ':' + pad(getSecs()));
                }
            }
        } else {
            return ("-----");
        }
    }

    /**
     *  Gets the hour attribute of the Time object
     *
     * @return    The hour value
     */
    private int getHour() {
        return (secondsFromMidnight / 3600);
    }

    /**
     *  Gets the number of minutes.
     *
     * @return    The mins value
     */
    private int getMins() {
        return (secondsFromMidnight / 60);
    }

    /**
     *  Gets the minsPerHour attribute of the Time object
     *
     * @return    The minsPerHour value
     */
    private int getMinsPerHour() {
        return (secondsFromMidnight / 60) % 60;
    }

    /**
     *  Gets the number of seconds.
     *
     * @return    The secs value
     */
    private int getSecs() {
        return (Math.abs(secondsFromMidnight - (getMins() * 60)));
    }

    private String pad(int value) {
        if (value < 10) {
            return ("0" + new Integer(Math.abs(value)).toString());
        } else {
            return (new Integer(Math.abs(value)).toString());
        }
    }
}
