/*
 *    Splitsbrowser - Event class.
 *
 *    Original Copyright (c) 2000  Dave Ryder
 *    Version 2 Copyright (c) 2002 Reinhard Balling
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
 * Created:    Dave Ryder
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:30:55 $
 * Changed by: $Author: daveryder $
 */
package org.splitsbrowser.model.results;
import java.util.Vector;

/**
 * The root datastructure for an orienteering event.<br><br>
 * It contains a number of {@link Course Courses}.<br>
 * A course is a collection of {@link AgeClass AgeClasses}.<br>
 * For each ageclass there are a number of {@link Result Results} consisting
 * of valid and invalid {@link Time Times}.<br><br>
 *
 * The results for the selected ageclass (and any ageclasses of the same course) are copied into
 * {@link SelectedResults SelectedResults}, where the split positions are calculated and the results are sorted.<br><br>
 *
 * For each control the split times of all runners passing through this control are processed by the class
 * {@link ControlCollection ControlCollection}.
 *
 * @see Course Course
 * @see AgeClass AgeClass
 * @see Result Results
 * @see Time Time
 * @see SelectedResults SelectedResults
 *
 * @author <b>Dave Ryder</b>
 * @author <b>Reinhard Balling</b>
 * @version 2.0
 */
public class EventResults {
    /** Selected results for viewing */
  //  public SelectedResults selectedResults = new SelectedResults();

    /** Name of the event */
    private String name = "";

    /** List of courses (which contain ageclasses) */
    private Vector courses = new Vector();

    /** Are control codes avaliable for the event */
    private boolean hasControlCodes;

    public EventResults() {
    }

    /**
     * Get the i'th course in the list of courses (i is not checked for validity).
     * @param i The number of course to get (i starts at 0)
     * @return The desired {@link Course Course}
     */
    public Course getCourse(int i) {
        return ((Course) courses.elementAt(i));
    }

    public void setHasControlCodes(boolean hasControlCodes) {
        this.hasControlCodes = hasControlCodes;
    }

    /**
     * Sets the name of the event
     * @param newName The name of the event
     */
    public void setName(String newName) {
        name = newName;
    }

    /**
     * Gets the name of the event
     * @return The name of the event
     */
    public String getName() {
        return name;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////		
    // C o u r s e     functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the number of courses for the event
     * @return Number of courses for the event
     */
    public int getNumCourses() {
        return (courses.size());
    }

    /**
     * Add a course to the list of courses (if it is not yet in the list ). If the course exists already, a reference to the
     * existing course is returned.
     * @param newCourse The course to add.
     * @return Reference to the found or added {@link Course course}
     */
    public Course addCourse(Course newCourse) {
        Course course = findCourse(newCourse);

        if (course == null) { // course not yet in list
            courses.addElement(newCourse);

            return newCourse;
        } else {
            return course; // return the already found course
        }
    }

    /*        // Find the (parent) course for a given ageclass string
            Course getCourse(String ageClassName) {
                    for (int i=0; i<getNumCourses(); i++) {
                            Course course=getCourse(i);
                            for (int j=0; j<course.getNumAgeClasses(); j++) {
                                    if (ageClassName.equals(course.getAgeClass(j).getName())) return course;
                            }
                    }
                    return null;
            }
    */
    
    /**
      * Check whether the newCourse exists already. If yes, return a reference to it otherwise return null
      * 
      * @param newCourse Course to look for
      * @return Reference to {@link Course Course} if it exists, null otherwise.
      */
     public Course findCourse(Course newCourse) {
         int i = getNumCourses() - 1;

         while (i >= 0) {
             Course c = getCourse(i);

             if (c.isEqual(newCourse)) {
                 return c;
             }

             i--;
         }

         return null;
     }

     /**
      * Check whether the newCourse exists already. If yes, return a reference to it otherwise return null
      * @param Name of course to look for
      * @return Reference to {@link Course Course} if it exists, null otherwise.
      */
     public Course findCourse(String name) {
         // TODO - This looks nasty to me.
         return findCourse(new Course(name, 0, 0, 0));
     }
   
   /**
    * Check whether the newCourse exists already. If yes, return a reference to it otherwise return null
    * 
    * @param newCourse Course to look for
    * @return Reference to {@link Course Course} if it exists, null otherwise.
    */
    public Course findCourse(String[] contolList) {
        
       // TODO - Implement this 
        return null;
        
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////		
    // A g e C l a s s     functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Checks whether the ageclass with the given name has been defined<br>
     * @see EventResults#findAgeClass(AgeClass).
     * @param String Name of ageclass to look for
     * @return Returns reference to {@link AgeClass AgeClass} or null if not found
     */
    public AgeClass findAgeClass(String name) {
        return findAgeClass(new AgeClass(name));
    }

    /**
     * Checks whether the given ageclass has been defined and is included in the Event's list of ageclasses<br>
     * @see EventResults#findAgeClass(String).
     * @param newAgeClass Reference to ageclass which is to be checked
     * @return Returns reference to {@link AgeClass AgeClass} or null if not found
     */
    public AgeClass findAgeClass(AgeClass newAgeClass) {
        int i = getNumCourses() - 1;

        while (i >= 0) {
            AgeClass ageClass = getCourse(i).findAgeClass(newAgeClass);

            if (ageClass != null) {
                return ageClass;
            }

            i--;
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////		
    // R e s u l t     functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Find the result for a runner given his/her name by searching through all courses and all classes
     * @param name The name to search for
     * @return The {@link Result Result} or null if not found
     */
    public Result findResult(String name) {
        for (int i = 0; i < getNumCourses(); i++) {
            Result res = getCourse(i).findResult(name);

            if (res != null) {
                return res;
            }
        }

        return null;
    }

    /**
     * Find the result for a runner given his/her startnumber by searching through all courses and all classes
     * @param startNumber The startnumber to search for
     * @return The {@link Result Result} or null if not found
     */
    public Result findResult(int startNumber) {
        for (int i = 0; i < getNumCourses(); i++) {
            Result res = getCourse(i).findResult(startNumber);

            if (res != null) {
                return res;
            }
        }

        return null;
    }

    public boolean hasControlCodes() {
        return hasControlCodes;
    }
}
