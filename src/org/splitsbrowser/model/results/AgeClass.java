/*
 *  Splitsbrowser - AgeClass.
 *
 *  Copyright (C) 2002  Reinhard Balling
 *  Copyright (C) 2003  Dave Ryder
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
 * Created:    Reinhard Balling
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:30:55 $
 * Changed by: $Author: daveryder $
 */
package org.splitsbrowser.model.results;
import java.util.Vector;

/**
 * Container for a group of {@link Result results} belonging to the same ageclass. All ageclasses sharing
 * the same course are grouped together under a {@link Course Course} object.
 *
 * @version $Revision: 1.1 $
 */
public class AgeClass {
    /**  Name of the ageClass */
    String name;

    // Allocate at least 10 results, extend by 5 results

    /**  Pointer to parent course */
    private Course course;

    /**  List of results for this ageclass */
    private Vector results = new Vector(10, 5);

    /** Array holding the result with the fastest split for each leg */
    private Result[] fastestResults;

    /**
     * Constructs an AgeClass object with a given name
     *
     * @param  newName  Name of the AgeClass (used when searching/comparing)
     */
    public AgeClass(String newName) {
        name = newName;
    }

    /**  Constructor for the AgeClass object */
    private AgeClass() {
    }

    /**
     *  Gets the parent {@link Course course} of the AgeClass
     *
     * @return    The parent Course object
     */
    public Course getCourse() {
        return course;
    }

    /**
     *  Return true if two AgeClasses are equal
     *
     * @param  newAgeClass  Description of the Parameter
     * @return              The equal value
     */
    public boolean isEqual(AgeClass newAgeClass) {
        return name.equals(newAgeClass.name);
    }

    /**
     * Returns the result with the fastest leg time for this AgeClass for a given control
     *
     * @param numControl Index of the control for which the leg time is to be obtained (0=first control)
     * @return result with the fastest leg time
     */
    public Result getFastestLegTime(int numControl) {
        if ((course != null) && (fastestResults[numControl] == null)) {
            computeFastestResult(numControl);
        }

        return (fastestResults[numControl]);
    }

    /**
     *  Gets the name of the AgeClass
     *
     * @return    The name of the AgeClass
     */
    public String getName() {
        return name;
    }

    /**
     * Get the number of results for this AgeClass
     *
     * @return    The number of results
     */
    public int getNumResults() {
        return (results.size());
    }

    /**
     *  Gets a result of this AgeClass
     *
     * @param  i  Number of the result to get
     * @return    The {@link Result Result}
     */
    public Result getResult(int i) {
        return (Result) results.elementAt(i);
    }

    /**
     * Adds a Result to the AgeClass
     *
     * @param  newResult  The {@link Result Result} to be added
     */
    public void addResult(Result newResult) {
        results.addElement(newResult);
    }

    /**
     * Find the result for a runner given his/her name<br><br>
     * When comparing the names, the comparison is only performed over the length of the shorter string<br>
     * @param name The name to search for
     * @return The {@link Result Result} or null if not found
     */
    public Result findResult(String name) {
        int nR = getNumResults() - 1;
        int len = name.length();

        while (nR >= 0) {
            // Only compare to the length of the shorter string
            String name2 = getResult(nR).getName();
            int len2 = name2.length();

            if (len < len2) {
                if (name2.substring(0, len).equalsIgnoreCase(name)) {
                    return getResult(nR);
                }
            } else {
                if (name.substring(0, len2).equalsIgnoreCase(name2)) {
                    return getResult(nR);
                }
            }

            nR--;
        }

        return null;
    }

    /**
     * Find the result for a runner given his/her startnumber
     * @param startNumber The startnumber to search for
     * @return The {@link Result Result} or null if not found
     */
    public Result findResult(int startNumber) {
        int nR = getNumResults() - 1;

        while (nR >= 0) {
            if (getResult(nR).getStartNumber() == startNumber) {
                return getResult(nR);
            }

            nR--;
        }

        return null;
    }

    /**
     *  Sets the parent course of the AgeClass
     *
     * @param  newCourse  The parent {@link Course course} for this AgeClass
     */
    void setCourse(Course newCourse) {
        course = newCourse;

        // Reset the fastest leg times
        fastestResults = new Result[course.getNumControls() + 1];
    }

    private void computeFastestResult(int numControl) {
        Time fastestTime = Time.MAXTIME;

        for (int i = 0; i < getNumResults(); i++) {
            Time split = getResult(i).getSplitTime(numControl);

            if (split.isValid() && split.lessThan(fastestTime)) {
                fastestResults[numControl] = getResult(i);
                fastestTime = split;
            }
        }
    }
}
