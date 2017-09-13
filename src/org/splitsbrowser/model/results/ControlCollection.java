/*
 *    Splitsbrowser - ControlCollection class.
 *
 *    Copyright (C) 2002  Reinhard Balling
 *    Copyright (C) 2002  Dave Ryder
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
package org.splitsbrowser.model.results;


import org.splitsbrowser.util.ICompare;
import org.splitsbrowser.util.Message;
import org.splitsbrowser.util.Sorter;

import java.util.Vector;


/**
 * Manages a collection of controls.
 * It provides facilitied for the courses a control is used on to be
 * rapidly looked up.
 *
 * This functionalty may distributed between a free-standing control class
 * and the course at aome point.
 *
 */
public class ControlCollection {
    /**
     * Number of seconds  before selected time
     *   results are displayed by getTimesNear
     */
    private static final int SECONDSBEFORE = 120;

    /**
     * Number of seconds  after selected time
     * results are displayed by getTimesNear
     */
    private static final int SECONDSAFTER = 120;
    LessThanControl lt = new LessThanControl();
    LessThanString lts = new LessThanString();
    LessThanTime ltt = new LessThanTime();

    /**  Description of the Field */
    Reference[] controls = null;

    /**  Description of the Field */
    private EventResults Event;

    /**
     * Build a list of control code cross references and sort them by control code
     *
     * @param  newEvent  The event for which the cross reference is to be built
     */
    public ControlCollection(EventResults newEvent) {
        Event = newEvent;

        Vector newControls = new Vector(200, 40);
        int nC;

        for (nC = 0; nC < Event.getNumCourses(); nC++) {
            Course course = Event.getCourse(nC);

            for (int i = -1; i <= course.getNumControls(); i++) {
                newControls.addElement(new Reference(course, i,
                                                     course.getControlCode(i)));
            }
        }

        newControls.addElement(new Reference(null, -1, "zzz")); // Stopper

        controls = new Reference[newControls.size()];

        for (int i = newControls.size() - 1; i >= 0; i--) {
            controls[i] = (Reference) newControls.elementAt(i);
        }

        Sorter.Sort(controls, 0, controls.length - 1, lt);

        // Now the array controls is ordered by the controlcode
    }

    /**
     * Returns a list of course names which use the given control ('controlCode')
     */
    public String[] getCoursesForControlNo(String controlCode) {
        int i = findFirstCourseForControl(padControl(controlCode));
        int iStart = i;

        while (controls[i].controlCode.equals(padControl(controlCode))) {
            i++;
        }

        String[] courses = new String[i - iStart + 1];
        int j = 1;

        i = iStart;

        courses[0] = Message.get("ControlCollection.Control") + " (" + controlCode +
            "ControlCollection.Control" + " (" + controlCode + ")";

        while (controls[i].controlCode.equals(padControl(controlCode))) {
            String nextControl =
                controls[i].course.getControlCode(controls[i].splitNo + 1);

            if (!nextControl.equals("")) {
                nextControl = " --> (" + nextControl + ")";
            }

            courses[j++] = controls[i++].course.getName() + nextControl;
        }

        Sorter.Sort(courses, 1, courses.length - 1, lts);

        return courses;
    }

    /**
     * Returns an array of strings listing the fastest times between the two given controls. Each string has the
     * format 'time / AgeClass.name / runner.name'.
     */
    public String[] getFastestTimesForLeg(String fromControlCode,
                                          String toControlCode,
                                          Vector ageClasses) {
        int iFrom = findFirstCourseForControl(padControl(fromControlCode));
        int storediTo = findFirstCourseForControl(padControl(toControlCode));
        Vector v = new Vector(0);
        String suffix = "";

        while (controls[iFrom].controlCode.equals(padControl(fromControlCode))) {
            Course courseFrom = controls[iFrom].course;
            int iTo = storediTo;

            while (controls[iTo].controlCode.equals(padControl(toControlCode))) {
                Course courseTo = controls[iTo].course;

                if (courseFrom.equals(courseTo) &&
                        ((controls[iFrom].splitNo + 1) == controls[iTo].splitNo)) {
                    // Found a course that has same controls
                    for (int i = courseFrom.getNumAgeClasses() - 1; i >= 0;
                             i--) {
                        if (isInVector(ageClasses, courseFrom.getAgeClass(i))) {
                            suffix = "*";
                        } else {
                            suffix = "";
                        }

                        AgeClass ageclass = courseFrom.getAgeClass(i);
                        int isplit = controls[iTo].splitNo;
                        Result fastest = ageclass.getFastestLegTime(isplit);

                        if (fastest != null) {
                            String s;

                            if (fastest.getName().equals("")) {
                                s = "----- /" + ageclass.getName() + suffix;
                            } else {
                                s = fastest.getSplitTime(isplit).toString() +
                                                                        " / " +
                                                                        ageclass.getName() +
                                                                        " / " +
                                                                        fastest.getName() +
                                                                        suffix;
                            }

                            v.addElement(s);
                        }
                    }
                }

                iTo++;
            }

            iFrom++;
        }

        String[] s = new String[v.size() + 1];

        s[0] =
            Message.get("Graph.FastestSplit") + "  " + fromControlCode +
            " -> " + toControlCode;

        for (int i = v.size() - 1; i >= 0; i--) {
            s[i + 1] = (String) v.elementAt(i);
        }

        Sorter.Sort(s, 1, v.size(), lts); // Sort by time

        return s;
    }


    public Time[] getTimesAtControl(String controlCode, Time timeFrom,
                                    Time timeTo) {
        Result[] runners = getRunnersAtControl(controlCode, timeFrom, timeTo);

        Time[] times = new Time[runners.length];

        for (int i = 0; i < runners.length; i++) {
            times[i] = runners[i].getSortTime();
        }

        return times;
    }

    /**
     * Returns an array of strings with each string being a time followed by and AgeClass and a name
     * Each strings represents one runner visiting the given control ('controlCode') shortly before
     * or after a certain time ('timeAtControl'). If the runner's AgeClass is part of the AgeClasses
     * in the vector 'ageClasses', the string is prefixed with an asterisk (*)
     */
    public String[] getTimesNear(Time timeAtControl, String controlCode,
                                 Vector ageClasses) {
        Time timeFrom = new Time(timeAtControl.asSeconds() - SECONDSBEFORE);
        Time timeTo = new Time(timeAtControl.asSeconds() + SECONDSAFTER);
        Result[] runners = getRunnersAtControl(controlCode, timeFrom, timeTo);
        String[] res = new String[runners.length + 1];

        res[0] =
            "[" + timeFrom.toString() + "-" + timeTo.toString() + "]  " +
            Message.get("ControlCollection.Control") + " (" + controlCode + ")";

        for (int i = 0; i < runners.length; i++) {
            String suffix = "";
            int j = 0;
            AgeClass ac = runners[i].getAgeClass();

            while (j < ageClasses.size()) {
                if (((AgeClass) ageClasses.elementAt(j)).isEqual(ac)) {
                    suffix = "*";

                    break;
                }

                j++;
            }

            res[i + 1] =
                runners[i].getSortTime().toString() + "  " + ac.getName() +
                " / " + runners[i].getName() + suffix;
        }

        return res;
    }

    private boolean isInVector(Vector ageClasses, AgeClass ageClass) {
        for (int i = ageClasses.size() - 1; i >= 0; i--) {
            if (((AgeClass) ageClasses.elementAt(i)).equals(ageClass)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return a sorted array (by time) of runners visiting control with 'controlCode' between Time 'timeFrom' and Time 'timeTo'
     *
     * @param controlCode The controlcode String
     * @param timeFrom The earliest time we are considering
     * @param timeTo The latest time
     * @return The array of results
     */
    private Result[] getRunnersAtControl(String controlCode, Time timeFrom,
                                         Time timeTo) {
        Vector vRunners = new Vector(100, 20);
        int i = findFirstCourseForControl(padControl(controlCode));

        while (controls[i].controlCode.equals(padControl(controlCode))) {
            Course course = controls[i].course;
            int iSplit = controls[i].splitNo;

            for (int nA = 0; nA < course.getNumAgeClasses(); nA++) {
                AgeClass ageClass = course.getAgeClass(nA);

                for (int nR = 0; nR < ageClass.getNumResults(); nR++) {
                    Result result = ageClass.getResult(nR);
                    Time splitAbsoluteTime = result.getAbsoluteTime(iSplit);

                    if (splitAbsoluteTime.isValid() &&
                            (timeFrom.lessThan(splitAbsoluteTime)) &&
                            (splitAbsoluteTime.lessThan(timeTo))) {
                        result.setSortTime(splitAbsoluteTime);
                        vRunners.addElement(result);
                    }
                }
            }

            i++;
        }

        if (vRunners.size() > 0) {
            // The toArray method is not availably in some VMs, therefore we copy elements by hand
            Result[] runners = new Result[vRunners.size()];

            for (i = vRunners.size() - 1; i >= 0; i--) {
                runners[i] = (Result) vRunners.elementAt(i);
            }

            Sorter.Sort(runners, 0, runners.length - 1, ltt);

            return runners;
        } else {
            return new Result[0];
        }
    }

    /**
     * Searches the array controls for the first entry for a given control code and returns the position
     * of this entry.
     * @param controlCode The control code to find
     * @return The first cross reference entry with the given control code
     */
    private int findFirstCourseForControl(String controlCode) {
        String control = padControl(controlCode);
        int i;

        for (i = 0; i < controls.length; i++) {
            if (controls[i].controlCode.equals(control)) {
                return i;
            }
        }

        return controls.length - 1;

        // point to stopper
    }

    private String padControl(String controlCode) {
        if (controlCode.length() >= 3) {
            return controlCode;
        } else if (controlCode.length() >= 2) {
            return " " + controlCode;
        } else {
            return "  " + controlCode;
        }
    }

    private class LessThanControl implements ICompare {
        public boolean lessThan(Object a, Object b) {
            return ((Reference) a).controlCode.compareTo(((Reference) b).controlCode) < 0;
        }
    }

    private class LessThanString implements ICompare {
        public boolean lessThan(Object a, Object b) {
            return ((String) a).compareTo(((String) b)) < 0;
        }
    }

    private class LessThanTime implements ICompare {
        public boolean lessThan(Object a, Object b) {
            return ((Result) a).getSortTime().lessThan(((Result) b).getSortTime());
        }
    }

    /**
     * Stored a cross reference entry for a control consisting of course, splitNumber and controlcode
     */
    private class Reference {
        public Course course;
        public String controlCode;
        public int splitNo;

        public Reference() {
        }

        public Reference(Course newCourse, int newSplitNo, String newControlNo) {
            course = newCourse;
            splitNo = newSplitNo;
            controlCode = padControl(newControlNo);
        }
    }
}
