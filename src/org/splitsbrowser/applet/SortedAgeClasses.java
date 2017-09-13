/*
 *    Splitsbrowser - SortedAgeClasses class.
 *
 *    Copyright (C) 2002  Reinhard Balling
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Library General Public
 *    License as published by the Free Software Foundation; either
 *    version 2 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Library General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this library; see the file COPYING.  If not, write to
 *    the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *    Boston, MA 02111-1307, USA.
 */
/*
 * Version control info - Do not edit
 * Created:    Reinhard Balling
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:30:55 $
 * Changed by: $Author: daveryder $
 */
 
package org.splitsbrowser.applet;

/**
 * @author Reinhard Balling
 * @version 1.0
 *
 * This class provides access to the ageclasses in an sorted way, i.e. for filling the listbox of classes<br>
 */

import org.splitsbrowser.model.results.AgeClass;
import org.splitsbrowser.model.results.Course;
import org.splitsbrowser.model.results.EventResults;
import org.splitsbrowser.util.*;

public class SortedAgeClasses {
    private static LessThanAgeClass lt = new LessThanAgeClass();

    /**
     * List of ageclasses as seen in the listbox. This an array holding a copy of all ageclasses to ensure the ageclasses
     * are presented to the user in a sensible sort order
     */
    private AgeClass[] sortedAgeClassList = null;

    /**
     * Constructor for OrderedAgeClass.<br><br>
     * The constructor must be called, when all courses, classes and results for an event have been read.<br>
     */
    public SortedAgeClasses(EventResults Event) {
        sortedAgeClassList = new AgeClass[countNumAgeClasses(Event)];

        int nC;
        int nA;
        int i;
        i = 0;

        // Build array of all ageclasses
        for (nC = 0; nC < Event.getNumCourses(); nC++) {
            Course course = Event.getCourse(nC);

            for (nA = 0; nA < course.getNumAgeClasses(); nA++) {
                sortedAgeClassList[i++] = course.getAgeClass(nA);
            }
        }

        // Sort them
        Sorter.Sort(sortedAgeClassList, 0, sortedAgeClassList.length - 1, lt);
    }

    /**
     * Counts the number of ageclasses in the event<br>
     * The count is performed by iterating over all courses and totalising the number of ageclasses for each course.
     *
     * @param The event for which the ageclasses are to be ordered
     * @return The number of ageclasses in the event
     */
    public int getNumAgeClasses() {
        return sortedAgeClassList.length;
    }

    /**
     * Return the i'th element of the list of sorted ageclasses
     *
     * @param The position of the AgeClass to return
     * @return The {@link AgeClass AgeClass} at position i
     */
    public AgeClass getSortedAgeClass(int i) {
        return sortedAgeClassList[i];
    }

    /**
     * Counts the number of ageclasses in the event<br>
     * The count is performed by iterating over all courses and totalising the number of ageclasses for each course.
     *
     * @param The event for which the ageclasses are to be ordered
     * @return The number of ageclasses in the event
     */
    private int countNumAgeClasses(EventResults Event) {
        int i = Event.getNumCourses() - 1;
        int count = 0;

        while (i >= 0) {
            count += Event.getCourse(i--).getNumAgeClasses();
        }

        return count;
    }

    private static class LessThanAgeClass implements ICompare {
        public boolean lessThan(Object a, Object b) {
            return ((AgeClass) a).getName().compareTo(((AgeClass) b).getName()) < 0;
        }
    }
}
