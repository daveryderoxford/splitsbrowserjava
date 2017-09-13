/*
 *  Splitsbrowser - Emit N3 Sport CVS file loader
 *
 *  Original Copyright (C) 2003  Andris Strazdins
 *
 * This  library is free software; you can redistribute it and/or  modify it
 * under the terms of the GNU Library General Public  License as published by
 * the Free Software Foundation; either  version 2 of the License, or (at your
 * option) any later version.
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
 * Created:    Andris Strazdins
 * Version:    $Revision:$
 * Changed:    $Date:$
 * Changed by: $Author:$
 */
package org.splitsbrowser.model.results.io;


import org.splitsbrowser.model.SplitsbrowserException;
import org.splitsbrowser.model.results.AgeClass;
import org.splitsbrowser.model.results.Course;
import org.splitsbrowser.model.results.EventResults;
import org.splitsbrowser.model.results.Result;
import org.splitsbrowser.model.results.Time;
import org.splitsbrowser.util.Message;

import java.io.BufferedReader;
import java.io.IOException;

import java.util.StringTokenizer;

/**
 * Loads data from a N3Sport style CVS export file.
 * The format of the data is
 * Id,Name,time,class,place,status,course,team,Split1...SPLITn
 *
 * @author <b>Andris Strazdins</b>
 * @info <b>based on work of Dave Ryder</b>
 */
public class EmitN3SportEventLoader extends EventLoader {
    private AgeClass ageClass = null;
    private Course course = null;
    private EventResults event;
    private String oldAgeClassName = "";
    private String oldcourseName = "";

    public void loadEvent(EventResults newEvent, String fileName,
                          boolean urlInput, int newbyCourse)
                   throws IOException, SplitsbrowserException
    {
        event = newEvent;

        int lineCount = 0;

        BufferedReader reader = openReader(fileName, urlInput);

        try {
            String line = null;

            // Read to first data on the third line
            for (int i = 0; i < 3; i++) {
                lineCount++;
                line = reader.readLine();
            }

            while (line != null) {
                if (notBlank(line)) {
                    parseResult(line);
                }

                lineCount++;
                line = reader.readLine();
            }
        } catch (Exception e) {
            throw new SplitsbrowserException(Message.get("Loader.Error",
                                                         new Object[] {
                                                             fileName,
                                                             new Integer(lineCount).toString()
                                                         }) + e.toString());
        }
    }

    private boolean notBlank(String line) {
        return ((line != null) && (line.trim().length() != 0));
    }

    private void parseResult(String line) throws Exception {
        String status;
        String club;
        String courseName;
        String ageClassName;
        StringTokenizer st;
        int numControls;
        boolean validRun;
        String name;
        int START_SPLIT_COLUMN = 9;

        st = new StringTokenizer(line, ",");

        numControls = st.countTokens() - START_SPLIT_COLUMN;

        st.nextToken(); // skip competitor id
        name = stripQuotes(st.nextToken());
        st.nextToken(); // skip total time
        ageClassName = stripQuotes(st.nextToken());
        st.nextToken(); // skips place

        // TODO Check if the staus string used varies with nationality
        status = stripQuotes(st.nextToken().trim()); // reads status A-OK,D-DSQ
        validRun = (status.equalsIgnoreCase("A"));

        if (validRun) {
            courseName = st.nextToken();
            club = stripQuotes(st.nextToken());

            Time startTime = Time.NULLTIME;

            // Read the splits data
            Time[] totalTimes = new Time[numControls + 1];

            for (int i = 0; i < (numControls + 1); i++) {
                int secs = Integer.parseInt(st.nextToken().trim());
                totalTimes[i] = new Time(secs);
            }

            // Course
            if (!courseName.equals(oldcourseName)) {
                course = new Course(courseName, numControls, 0, 0); // no distance/climb

                course = event.addCourse(course);
                oldcourseName = courseName;
            }

            // age class 
            if (!ageClassName.equals(oldAgeClassName)) {
                ageClass = new AgeClass(ageClassName);
                ageClass = course.addAgeClass(ageClass);
                oldAgeClassName = ageClassName;
            }

            // Create the result 
            Result result =
                new Result(name, club, course, ageClass, totalTimes, startTime,
                           validRun);
            ageClass.addResult(result);
        }
    }

    private String stripQuotes(String str) {
        String res = str;

        if ((str.length() > 2) && str.startsWith("\"") && str.endsWith("\"")) {
            res = str.substring(1, str.length() - 1);
        }

        return (res);
    }
}
