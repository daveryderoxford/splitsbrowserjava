/*
 *  Splitsbrowser - Course.
 *
 *  Original Copyright (c) 2000  Dave Ryder
 *  Version 2 Copyright (c) 2002 Reinhard Balling
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
 * Created:    Dave Ryder 2000
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:30:55 $
 * Changed by: $Author: daveryder $
 */
package org.splitsbrowser.model.results;

import java.util.Vector;

import org.splitsbrowser.util.Message;

/**
 * An orienteering <b>course</b> which can be shared by a number of classes<br><br>
 * The course may have distance, climb and number of controls.<br>
 * It may also have alist of controls
 *
 *
 * @author     Dave Ryder
 * @author     Reinhard Balling
 * @version    2.0
 */
public class Course {
    /**  The climb of the course in m */
    private float climb = 0;

    /**  The distance of the course as a string (can be in m or km) */
    private float distance = 0;

    /**  The name of the course */
    private String name;

    /**  The control codes concatenated by a comma. Redundant information (also in controlCodes) for easier comparisons */
    private String strControlCodes = "";

    /**  The list of ageclasses sharing this course */
    private Vector ageClasses = new Vector(3);

    /**  The (optional) control codes */
    private String[] controlCodes;

    /**  True if the corresponding control is valid */
    private boolean[] validControl;

    /**  Are contol codes avaliable for the course */
    private boolean hasControlCodes = false;
    
    /**  The number of controls (not counting the finish control) */
    private int numControls;

    /**  Constructor for the Course object */
    public Course() {
    }

    /**
     *  Constructor for the Course object
     *
     * @param  newName         Name of the course (can be empty)
     * @param  newNumControls  Number of controls excluding Finish control
     * @param  newDistance     Distance (in m or km)
     * @param  newClimb        Climb in m
     */
    public Course(String newName, int newNumControls, float newDistance,
                  float newClimb) {
        name = newName;
        numControls = newNumControls;
        distance = newDistance;
        climb = newClimb;
        strControlCodes = null;
        controlCodes = new String[newNumControls + 1];

        controlCodes[newNumControls] = Message.get("Graph.Finish");

        validControl = new boolean[newNumControls + 1];

        // include Finish
        int i;

        for (i = 0; i <= newNumControls; i++) {
            validControl[i] = true;
        }
    }

    /**
     *  Gets the i'th {@link AgeClass AgeClass} of the Course
     *
     * @param  i  Number of AgeClass to get
     * @return    The i'th AgeClass
     */
    public AgeClass getAgeClass(int i) {
        return ((AgeClass) ageClasses.elementAt(i));
    }

    /**
     *  Gets the climb of this course
     *
     * @return    The climb or 0 if the climb was not set
     */
    public float getClimb() {
            return climb;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////		
    // C o n t r o l c o d e     functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the control code for a given control<br>
     * Controls are numbered starting with 0 (= first control). The start is -1
     *
     * @param  i  Number of control for which the control code is to be retrieved (starting with 0 for first control). Special case: -1 = start
     * @return    The control code
     */
    public String getControlCode(int i) {
        if (i == -1) { //-1 = Start

            return Message.get("Graph.Start");
        }

        if (i < -1) {
            return "";
        }

        if (i > getNumControls()) {
            return "";
        }

        if (controlCodes[i] == null) {
            return "";
        } else {
            return controlCodes[i];
        }

        // 0 - (numControls-1): Controls,  numControls=Finish
    }

    /**
     *  Gets the distance of this course
     *
     * @return    The distance or 0 if the distance was not set
     */
    public float getDistance() {
            return distance;
    }

    /**
     * Checks whether two courses are equal<br><br>
     * The comparison whether two courses are equal takes all parameters into account (name, number of controls,
     * distance, climb, control codes) but only if the parameter is set for both courses.
     *
     * @param  compareCourse  The course to compare with
     * @return                true if the courses are equal, false otherwise
     */
    public boolean isEqual(Course compareCourse) {
        boolean eq = true;

        // TODO equlity should just be based on the course name 
        // We should used additional function to find a course if that is what is required. 
        if ((name != null) && (compareCourse.name != null)) {
            eq = eq && name.equals(compareCourse.name);
        }

        if ((numControls != 0) && (compareCourse.numControls != 0)) {
            eq = eq && (numControls == compareCourse.numControls);
        }

        if ((strControlCodes != null) &&
                (compareCourse.strControlCodes != null)) {
            eq = eq && strControlCodes.equals(compareCourse.strControlCodes);
        }

        return eq;
    }

    public Result getFastestLegTime(int numControl) {
        Time fastestTime = Time.MAXTIME;
        Result fastestResult = null;

        for (int i = 0; i < getNumAgeClasses(); i++) {
            Result res = getAgeClass(i).getFastestLegTime(numControl);

            if (res != null) {
                if (res.getSplitTime(numControl).lessThan(fastestTime)) {
                    fastestResult = res;
                    fastestTime = res.getSplitTime(numControl);
                }
            }
        }

        return fastestResult;
    }

    /**
     *  Sets the name of the Course
     *
     * @param  newName  The new name
     */
    public void setName(String newName) {
        name = newName;
    }

    /**
     * Gets the name of the Course<br><br>
     * If the name is empty, the names of all {@link ÁgeClass ageclasses} are concatenated using a /
     *
     * @return    The name of the Course
     */
    public String getName() {
        if (name == "") {
            String s = "";

            for (int j = 0; j < getNumAgeClasses(); j++) {
                if (s == "") {
                    s = getAgeClass(j).getName();
                } else {
                    s = s + "/" + getAgeClass(j).getName();
                }
            }

            return s;
        } else {
            return name;
        }
    }

    /**
     *  Gets the number of AgeClasses sharing this course
     *
     * @return    The number of AgeClasses sharing this course
     */
    public int getNumAgeClasses() {
        return (ageClasses.size());
    }

    /**
     *  Gets the number of controls for this course (excluding finish control)
     *
     * @return    The number of controls
     */
    public int getNumControls() {
        return numControls;
    }

    /**
     * Marks a control as valid/invalid
     *
     * @param  controlNum  The number of the control for which the validity is to be set (starting at 0)
     * @param  newIsValid  True if the control is valid, false otherwise
     */
    public void setValidControl(int controlNum, boolean newIsValid) {
        validControl[controlNum] = newIsValid;
    }

    /**
     * Returns true if the control is valid<br>
     * A control may be invalidated (voided) if the SI box fails during the race
     *
     * @param  controlNum  Number of control to check (starting with 0 for first control)
     * @return             True if control is valid, false otherwise
     */
    public boolean isValidControl(int controlNum) {
        return validControl[controlNum];
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////		
    // A g e C l a s s     functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds an {@link AgeClass AgeClass} to the course<br><br>
     * Before the AgeClass is added, the list of AgeClasses for the course is searched. If the AgeClass is found,
     * a reference to the existing AgeClass is returned. If the AgeClass is not found
     *
     * @param  newAgeClass  The {@link AgeClass AgeClass} to be added to the course
     * @return              The reference to the same {@link AgeClass AgeClass} if it was already added
     */
    public AgeClass addAgeClass(AgeClass newAgeClass) {
        AgeClass ageClass = this.findAgeClass(newAgeClass);

        if (ageClass == null) {
            // ageclass not yet in list
            ageClasses.addElement(newAgeClass);
            newAgeClass.setCourse(this);

            return newAgeClass;
        } else {
            return ageClass;
        }
    }

    /**
     * Add a control code for a control<br>
     * The control codes must be added in sequence, starting with 0 (for the first control)
     *
     * @param  numControl  The number of the control (starting at 0) for which the control code is to be added
     * @param  newCC       The control code to be added (e.g. "99" for last control)
     */
    public void addControlCode(int numControl, String newCC) {
        controlCodes[numControl - 1] = newCC;

        if ((strControlCodes == null) || strControlCodes.equals("")) {
            strControlCodes = newCC;
        } else {
            strControlCodes += ("," + newCC);
        }
    }

    /**
     * Checks whether the given {@link AgeClass AgeClass} is part of the list of AgeClasses for this course.
     * Returns the found AgeClass object or null if not found. The comparison is done using the AgeClasses
     * {@link AgeClass#isEqual isEqual} method.
     *
     * @param  newAgeClass  AgeClass to find
     * @return              Reference of found AgeClass or null if not found
     */
    public AgeClass findAgeClass(AgeClass newAgeClass) {
        int i = getNumAgeClasses() - 1;

        while (i >= 0) {
            AgeClass a = getAgeClass(i);

            if (a.isEqual(newAgeClass)) {
                return a;
            }

            i--;
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////		
    // R e s u l t     functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Find the result for a runner given his/her name by searching through all classes for this course
     * @param name The name to search for
     * @return The {@link Result Result} or null if not found
     */
    public Result findResult(String name) {
        for (int i = 0; i < getNumAgeClasses(); i++) {
            Result res = getAgeClass(i).findResult(name);

            if (res != null) {
                return res;
            }
        }

        return null;
    }

    /**
     * Find the result for a runner given his/her startnumber by searching through all classes
     * @param startNumber The startnumber to search for
     * @return The {@link Result Result} or null if not found
     */
    public Result findResult(int startNumber) {
        for (int i = 0; i < getNumAgeClasses(); i++) {
            Result res = getAgeClass(i).findResult(startNumber);

            if (res != null) {
                return res;
            }
        }

        return null;
    }

    public boolean hasControlCodes() {
        for (int i = 0; i < numControls; i++) {
            if (controlCodes[i] == "") {
                return (false);
            }
        }

        return (true);
    }

}
