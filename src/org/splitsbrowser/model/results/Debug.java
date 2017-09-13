package org.splitsbrowser.model.results;
/*
 *  Splitsbrowser - Debug
 *
 *  (c) Reinhard Balling, October 2002
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
 * Created:    Reinheart Balling  2 November 2002
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:30:55 $
 * Changed by: $Author: daveryder $
 */

/**
 *  The Debug Object lists the Event, Course, AgeClass and Results datastructures to the console
 *
 */
public class Debug {
    /**  The event */
    private EventResults Event;

    /**
     *  Constructor for the Debug object
     *
     * @param  newEvent  Description of the Parameter
     */
    public Debug(EventResults newEvent) {
        Event = newEvent;
    }

    /**  Description of the Method */
    public void listAll() {
        int nC;
        int nA;
        int nR;
        int i;

        System.out.println("---------------------------------------------------------");

        for (nC = 0; nC < Event.getNumCourses(); nC++) {
            Course course = Event.getCourse(nC);

            System.out.println(course.getName() + "/" + course.getDistance() +
                               "/" + course.getClimb() + "m/" +
                               course.getNumControls() + " C\n");

            if (course.hasControlCodes()) {
                String sep = "";

                for (i = 0; i < course.getNumControls(); i++) {
                    if (!course.isValidControl(i)) {
                        System.out.print("*");
                    }

                    System.out.print(sep + course.getControlCode(i));
                    sep = ",";
                }
            } else {
                System.out.print("Control codes not specified\n");
            }

            System.out.println("");

            for (nA = 0; nA < course.getNumAgeClasses(); nA++) {
                AgeClass ageClass = course.getAgeClass(nA);

                System.out.println("    " + ageClass.getName() + "  " +
                                   ageClass.getNumResults() + " runners");

                for (nR = 0; nR < ageClass.getNumResults(); nR++) {
                    Result result = ageClass.getResult(nR);
                    String Valid = result.isValid() ? " " : "*";
                    String s = "";

                    for (i = 0; i <= course.getNumControls(); i++) {
                        if (result.getTime(i) == null) {
                            s = s + "  null";
                        } else {
                            s = s + " " + result.getTime(i).toString();
                        }
                    }

                    s = s + "   [";

                    if (result.getStartTime() == null) {
                        s = s + "null]";
                    } else {
                        s = s + result.getStartTime().toString() + "]";
                    }
                    
                     String pClub =
                              result.getClub() + "                                                      ";
                    pClub.substring(0, 20);

                    System.out.println("        " + "[" +
                                       result.getStartNumber() + "]" + Valid +
                                       " " + result.getName(30) + " " +
                                       pClub + s);
                }
            }
        }
    }
}
