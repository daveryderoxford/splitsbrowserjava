/*
 *  Splitsbrowser - CSVEventLoader.
 *
 *  Original Copyright (c) 2000  Dave Ryder
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
package org.splitsbrowser.model.results.io;

import org.splitsbrowser.model.SplitsbrowserException;
import org.splitsbrowser.model.results.AgeClass;
import org.splitsbrowser.model.results.Course;
import org.splitsbrowser.model.results.EventResults;
import org.splitsbrowser.model.results.Result;
import org.splitsbrowser.model.results.Time;

import java.io.BufferedReader;
import java.io.IOException;

import java.util.StringTokenizer;

/*
 * Version control info - Do not edit
 * Created:    Dave Ryder 2000
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:33:13 $
 * Changed by: $Author: daveryder $
 */

/**
 *  Loads a data from a CVS file. Two formats are possible:
 *  - Results by course. On course may be run by many classes.
 * - Results by class. Several classes may share a course.
 * The dataformat for Results by Course is:
 *  CourseName,NumControls Name,Club,StartMins:StartSecs,Class
 * where Class is optional Total1Mins:Total1Sec,....Numcontrols+1 times The dataformat for
 *  Results by Class is: ClassName,NumControls,Length,Climb where Length and
 *  Climb are optional Name,Club,StartMins:StartSecs
 *  Total1Mins:Total1Sec,....Numcontrols+1 times Runners competing out of
 *  competition must have a reference in their name. For disqualified and
 *  retired runners, the missing totals can be left empty or ----- Each class is
 *  terminated by a blank line and the file terminated by two blank lines or the
 *  end of the file
 *
 * @version    $Revision: 1.1 $
 */
public class CSVEventLoader extends EventLoader {
    private EventResults event;

    /**
     *  Constructor for the CSVEventLoader object
     *
     * @param  newEvent  Description of the Parameter
     */
    public CSVEventLoader() {
    }

    /**
     *  Loads a
     *
     * @param  fileName         Description of the Parameter
     * @param  urlInput         Description of the Parameter
     * @param  zipped           Description of the Parameter
     * @param  byCourse         Description of the Parameter
     * @exception  IOException  Description of the Exception
     * @exception  Exception    Description of the Exception
     */
    public void loadEvent(EventResults newEvent, String fileName,
                          boolean urlInput, int byCourse)
                   throws IOException, SplitsbrowserException
    {
        event = newEvent;

        BufferedReader reader = openReader(fileName, urlInput);

        // Read course info
        int lineCount = 0;

        try {
            lineCount++;

            String line = reader.readLine();

            while (notBlank(line)) {
                StringTokenizer st = new StringTokenizer(line, ",:");
                String name = new String(st.nextToken());
                int numControls = Integer.parseInt(st.nextToken().trim());

                Course course = new Course(name, numControls, 0, 0);
                course = event.addCourse(course);

                AgeClass ageClass = new AgeClass(name);
                ageClass = course.addAgeClass(ageClass);

                // Read results until a blank line
                lineCount++;
                line = reader.readLine();

                while (notBlank(line)) {
                    // Read the result
                    st = new StringTokenizer(line, ",:");

                    // Parse the data
                    String firstName = st.nextToken();
                    String surname = st.nextToken();
                    String club = st.nextToken();

                    // Try to parse the start time, ignoring any errors
                    Time startTime;
                    try {
                        int startHour = Integer.parseInt(st.nextToken().trim());
                        int startMin = Integer.parseInt(st.nextToken().trim());
                        startTime = new Time(startHour, startMin, 0);
                    } catch (Exception e) {
                        startTime = Time.INVALIDTIME;
                    }

                    /* Read the splits data */
                    Time[] totalTimes = new Time[numControls + 1];
                    int mins;
                    int secs;

                    Time lastTime = Time.ZEROTIME;

                    for (int i = 0; i < (numControls + 1); i++) {
                        mins = Integer.parseInt(st.nextToken().trim());

                        if ((mins < 0)) {
                            throw new Exception("Invalid minutes");
                        }

                        secs = Integer.parseInt(st.nextToken().trim());

                        if ((secs < 0) || (secs > 59)) {
                            throw new Exception("Invalid seconds");
                        }

                        Time split = new Time(mins, secs);
                        totalTimes[i] = lastTime.add(split);
                        lastTime = totalTimes[i];
                    }

                    // ... and create the result
                    Result result =
                        new Result(firstName + " " + surname, club, course,
                                   ageClass, totalTimes, startTime, true);

                    ageClass.addResult(result);
                    
                    result.addMissingTimes();

                    // Read the next results line
                    lineCount++;
                    line = reader.readLine();
                }

                // Read next class line
                lineCount++;
                line = reader.readLine();
            }
        } catch (Exception e) {
            // TODO Sort out internationlsation
            throw new SplitsbrowserException("Loader.Error" +
                                             new Integer(lineCount).toString() +
                                             e.toString());

            //   throw new Exception(Message.get("Loader.Error",
            //                                  new Object[] {
            //                                      fileName,
            //                                      new Integer(lineCount).toString()
            //                                  }) + e.toString());
        }
    }

    private boolean notBlank(String line) {
        return ((line != null) && (line.trim().length() != 0));
    }
}
