/*
 *  Splitsbrowser - IOF standard XML file format loader
 *
 *  Original Copyright (C) 2003  Dave Ryder
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
 * Created:    Dave Ryder
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:33:13 $
 * Changed by: $Author: daveryder $
 */
package org.splitsbrowser.model.results.io;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.splitsbrowser.model.results.AgeClass;
import org.splitsbrowser.model.results.Course;
import org.splitsbrowser.model.results.EventResults;
import org.splitsbrowser.model.results.Result;
import org.splitsbrowser.model.results.Time;
import org.splitsbrowser.util.Message;
import org.splitsbrowser.util.XmlPullWrapper;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import org.splitsbrowser.model.SplitsbrowserException;

/**
 *  Loads event details from an IOF standard XML format file.
 *
 * @author      D Ryder
 * @modified    April 2003
 */
public class IOFXMLEventLoader extends EventLoader {
    private AgeClass ageClass = null;
    private Course course = null;
    private EventResults event;

    /**
     *  Constructor for the CSVEventLoader object
     *
     * @param  newEvent  Description of the Parameter
     */
    public IOFXMLEventLoader() {
    }

    /**
     *  Load results in IOF format from a specified file.
     *
     * @param  base             base URL
     * @param  fileName         filename
     * @param  urlInput         Is filename a URL or a local file
     * @param  zipped           Is the file zipped
     * @param  byCourse         Description of the Parameter
     * @exception  IOException
     * @exception  Exception
     */
    public void loadEvent(EventResults newEvent, String fileName, boolean urlInput,
                          int newbyCourse) throws IOException, SplitsbrowserException
    {
        event = newEvent;

        BufferedReader reader;
        
        reader = openReader(fileName, urlInput);

        XmlPullWrapper xr = new XmlPullWrapper();
        
        try {
            xr.setInput(reader);
        } catch (XmlPullParserException e1) {
            throw new SplitsbrowserException(e1.getMessage());
        }

        try {
            parseResultsList(xr);
        } catch (Exception e) {
            throw new SplitsbrowserException(Message.get("Loader.Error",
                                            new Object[] {
                                                fileName,
                                                new Integer(xr.getLineNumber())
                                            }) + e.toString() +
                                xr.getPositionDescription());
        }
    }

    /**
     *
     * Parses a clock time ignoring the Date element (if any).
     *
     * On entry the parser must be located on the start tag of the clock element
     * On exit the parser is located on the end tag of the clock element
     *
     * @param xr
     * @return Time
     * @throws IOException
     * @throws XmlPullParserException
     *
     * DTD:
     * <pre>
     *    <!ELEMENT Time (#PCDATA)>
     *    <!ATTLIST Time timeFormat CDATA "MM:SS">
     * </pre>
     */
    private Time parseClock(XmlPullWrapper xr)
                     throws IOException, XmlPullParserException
    {
        xr.require(XmlPullWrapper.START_TAG, null, "Clock");

        // Skip to required time field
        if (!xr.skipToSubTree(null, "Time")) {
            throw new XmlPullParserException("Time element not present in clock");
        }

        // Parse out the time
        Time t;
        t = timeFromString(xr.getText(),
                           xr.getAttributeValue("Clock", "clockFormat"));

        xr.skipOut();

        return (t);
    }

    /**
     *  Parses Club element returning the club short name.
     *
     * On entry the parser must be located on the start tag of the Club element
     * On exit the parser is located on the end tag of the Club element
     *
         * @param xr
         * @return
         * @throws IOException
         * @throws XmlPullParserException
         *
         * DTD:
         * <pre>
         *         <!ELEMENT Club (ClubId, Name?, ShortName, OrganisationId?,
         *      (CountryId|Country), Address*, Tele*, WebURL*, Account*,
     *      Contact*, ModifyDate?)>
     * </pre>
         */
    private String parseClub(XmlPullWrapper xr)
                      throws IOException, XmlPullParserException
    {
        String shortName;

        xr.require(XmlPullWrapper.START_TAG, null, "Club");

        if (xr.skipToSubTree(null, "shortName")) {
            shortName = xr.nextText();
        } else {
            throw new XmlPullParserException("Short name for club must be specified");
        }

        xr.skipOut();

        return (shortName);
    }

    /**
     * Parse PersonName element returning a single string comprising of the name
     *
     * On entry the parser must be located on the start tag of the PersonName element
     * On exit the parser is located on the end tag of the PersonName element
     *
     * @param xr
     * @return
     *
     * DTD:
     * <pre>
     * <!ELEMENT PersonName (Family,Given+)>
     * </pre>
     */
    private String parsePersonName(XmlPullWrapper xr)
                            throws IOException, XmlPullParserException
    {
        String surname;
        String firstName = "";

        xr.require(XmlPullWrapper.START_TAG, null, "PersonName");

        // Family name
        xr.nextStartTag("Family");
        surname = xr.nextText();
        xr.nextEndTag();

        // Given name - one or more
        xr.nextStartTag();

        while (xr.getName().compareTo("Given") == 0) {
            firstName = firstName + " " + xr.nextText();
            xr.nextEndTag();

            xr.nextTag();
        }

        firstName = firstName.trim();

        return (firstName + " " + surname);
    }

    /**
     * Parse a PersonResult element. This contains a Person and Result element
     *
     * On entry the parser must be located on the start tag of the PersonResult element
     * On exit the parser is located on the end tag of the PersonResult element
     *
     * @param xr
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     *
     * It is assumed that
     * 1. all the data is embedded rather than (ie Person rather than PersonId, Club or Country vs ClubId, CountryId)
     * 2. Only single day events are currently supported (ie 1 Result and no RaceResults)
     *
     * DTD:
     * <pre>
           PersonResult ((PersonId|Person),
               (ClubId|Club|CountryId|Country)?, Rank*, Result, RaceResult*
        </pre>
     */
    private Result parsePersonResult(XmlPullWrapper xr)
                              throws IOException, XmlPullParserException
    {
        String clubName;
        String name;
        Result result;

        xr.require(XmlPullWrapper.START_TAG, null, "PersonResult");

        // Person is a requiuired element in our format
        xr.nextStartTag("Person");
        name = parsePersonName(xr);

        // Country or club must be specified
        xr.nextStartTag();

        if (xr.getName() == "Club") {
            clubName = parseClub(xr);
        } else if (xr.getName() == "Country") {
            clubName = parseClub(xr);
        } else {
            throw (new XmlPullParserException("Club or country must be specified"));
        }

        // Required result element
        // TODO I think skiping to sub tee is wrong 
        if (xr.skipToSubTree(null, "Result")) {
            result = parseResult(xr, name, clubName);
        } else {
            throw new XmlPullParserException("Results element must be specified");
        }

        xr.skipOut();

        return (result);
    }

    /**
    *
    * Parses an IOF XML Result element returning a Result object.
    *
    * The Result element contains the start/finish times, splits and
    * competitor status.
    *
    * On entry the parser must be located on the start tag of the Result element
    * On exit the parser is located on the end tag of the Result element
    *
     * @param xr
     * @param name Competitor name
     * @param clubName Club name
     * @return newly created Result object
     * @throws IOException
     * @throws XmlPullParserException
    *
    * DTD:
    * <pre>
    *      <!ELEMENT Result (StartNumber?, BibNumber?, (CCardId|CCard)?,
        StartTime?, FinishTime?, Time?, ResultPosition?,
        CompetitorStatus, TeamSequence?,
        (CourseVariationId|CourseVariation|CourseLength)?, SplitTime*,
        (BadgeValueId|BadgeValue)?, Point*, ModifyDate
    </pre>

    Example XML:
    <pre>
        <Result>
             <CCardId>305000</CCardId>
             <StartTime>
              <Clock clockFormat="HH:MM:SS">19:46:00</Clock>
             </StartTime>
             <FinishTime>
              <Clock clockFormat="HH:MM:SS">20:13:35</Clock>
             </FinishTime>
             <Time timeFormat="HH:MM:SS">00:27:35</Time>
             <CompetitorStatus value="OK"></CompetitorStatus>
             <CourseLength>4370</CourseLength>
             <SplitTime sequence="1">
              <ControlCode>184</ControlCode>
              <Time timeFormat="HH:MM:SS">00:01:45</Time>
             </SplitTime>
             <SplitTime sequence="2">
         </Result>
     </pre>
    */
    private Result parseResult(XmlPullWrapper xr, String name, String clubName)
                        throws IOException, XmlPullParserException
    {
        Time startTime = Time.NULLTIME;
        Time finishTime = Time.NULLTIME;
        Time totalTime = Time.NULLTIME;
        boolean validRun = true;
        boolean gotSplits = false;
        double courseLength = 0.0;
        Vector splits = new Vector(15, 10);

        xr.require(XmlPullWrapper.START_TAG, null, "Result");

        do {
            if (xr.getName().equals("StartTime")) {
                // Start time (optional)
                startTime = parseClock(xr);
                xr.nextEndTag();
            } else if (xr.getName().equals("FinishTime")) {
                // Finish time (optional)
                finishTime = parseClock(xr);
                xr.nextEndTag();
            } else if (xr.getName().equals("Time")) {
                // Total time (optional)
                totalTime = parseClock(xr);
                xr.nextEndTag();
            } else if (xr.getName().equals("CompetitorStatus")) {
                // CompetitorStatus (required) 
                String s = xr.getAttributeValue("CompetitorStatus", "value");
                validRun = (s.compareToIgnoreCase("OK") == 0);
            } else if (xr.getName().equals("CourseLength")) {
                // Course length (optional ) 
                String s = xr.nextText();

                try {
                    courseLength = Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    throw (new XmlPullParserException("Invalid course length " +
                                                      e.getMessage()));
                }
            } else if (xr.getName().equals("SplitTime")) {
                // Split times (optional) 
                gotSplits = true;
                splits.add(parseSplitTime(xr));
            } else {
                // Skip any other elements 
                xr.skipSubTree();
            }
        } while ((xr.getEventType() != XmlPullParser.END_DOCUMENT) &&
                     (xr.getEventType() != XmlPullParser.END_TAG));

        Time[] totalTimes= null;

        if (gotSplits) {
            totalTimes = new Time[splits.size()];

            for (int i = 0; i < splits.size(); i++) {
                totalTimes[i] = (Time) splits.elementAt(i);
            }
        }

        if (course==null) {
         //  course =new Course(courseName, numControls, distance, climb);

            // set the control codes              
            for (int i = 0; i < splits.size(); i++) {
           //      String controlCode = splits.c;
           String controlCode = null;

                if (controlCode != null) {
                    course.addControlCode(i + 1, controlCode);
                }
            }

            course = event.addCourse(course);
            ageClass = course.addAgeClass(ageClass);
        }

        // Create the result 
      //  Result result =
         //   new Result(name, club, course, ageClass, totalTimes,
         //              startTime, validRun);
      //  ageClass.addResult(result);

        // If the result is the first for the course then
        // Now create the result 
        Result result;
        try {
            result = new Result(name, clubName, course, ageClass, totalTimes, startTime, validRun);
            ageClass.addResult(result);
        } catch (Exception e) {
            throw new XmlPullParserException(e.getMessage());
        }


        return (result);
    }

    /**
     *
     * Root parser for the ResultsList IOF transfer file containg the results to an event.
     *
     * This can be a complete list (i.e. after the event), a snapshot (current standings - while the event is
     * under way), or a delta list (changes since last list, meant for frequent exchange of results).
     *
     * On entry the parser must be located on the start tag of the ResultsList element
     * On exit the parser is located on the end tag of the ResultsList element
     *
     * @param xr
     * @throws IOException
     * @throws XmlPullParserException
     *
     * DTD:
     * <pre>
     *   < ELEMENT ResultList (IOFVersion?, (EventId|Event)?, ClassResult*,ModifyDate?)>
     *   <!ATTLIST ResultList status (complete|snapshot|delta) "complete"
     * </pre>
     */
    private void parseResultsList(XmlPullWrapper xr)
                           throws IOException, XmlPullParserException
    {
        ageClass = null;

        Vector resultsList = new Vector(10, 10);

        xr.require(XmlPullWrapper.START_TAG, null, "ResultList");

        // Verify the file header 
        do {
            xr.nextTag();

            if (xr.skipToSubTree(null, "Event")) {
                /*
                 * <!ELEMENT Event (EventId, Name,
                     (EventClassificationId|EventClassification), StartDate,
                     FinishDate?, EventOfficial*, Organiser?, EventClass*, EventRace*,
                     WebURL*, EntryData?, Service*, Account*, ModifyDate?)>
                 */
                if (xr.getName().equals("ClassShortName")) {
                    String ageClassName;
                    ageClassName = xr.nextText();
                    xr.nextTag();

                    ageClass = new AgeClass(ageClassName);
                }
            } else {
                throw (new XmlPullParserException("Event details not specified"));
            }

            // Loop over adding to a list
            if (xr.getName().equals("PersonResult")) {
                Result result = parsePersonResult(xr);
                resultsList.addElement(result);
            }
        } while ((xr.getEventType() != XmlPullParser.END_DOCUMENT) &&
                     (xr.getEventType() != XmlPullParser.END_TAG));

        // Add results to the class
        if (ageClass == null) {
            throw new XmlPullParserException("Age class short name not specified");
        }

        for (int i = 0; i < resultsList.size(); i++) {
            ageClass.addResult((Result) resultsList.elementAt(i));
        }
    }

    /**
     * Parse SplitTime element returning a vector of split times returning a vector of Split objects
     *
     * On entry the parser must be located on the start tag of the SplitData element
     * On exit the parser is located on the end tag of the SplitData element
     *
     * DTD:
     * <pre>
     * <!ELEMENT SplitTime ((ControlCode|Control)?, Time?)>
     * <!ATTLIST SplitTime sequence CDATA #REQUIRED>
     * </pre>
     *
     * */
    private Split parseSplitTime(XmlPullWrapper xr)
                          throws IOException, XmlPullParserException
    {
        xr.require(XmlPullWrapper.START_TAG, null, "SplitTime");

        String sequence = xr.getRequiredAttributeValue("null", "sequence");
        String controlCode = null;
        Time time = Time.NULLTIME;

        xr.nextStartTag();

        do {
            xr.nextStartTag();

            if (xr.getName().compareTo("ControlCode") == 0) {
                controlCode = xr.nextText();
                xr.nextEndTag();
            } else if (xr.getName().compareTo("Time") == 0) {
                time = parseTime(xr);
            }

            xr.nextTag();
        } while (xr.getEventType() != XmlPullWrapper.END_TAG);

        Split split = new Split(controlCode, sequence, time);

        return split;
    }

    /**
     * Parse a time element retuirning the Time object
     *
     * On entry the file cursor is assumed to be on the
     * On exit the cursor will be on the end tag of the last split
     *
     * DTD:
     * <pre>
     * <!ELEMENT SplitTime ((ControlCode|Control)?, Time?)>
     * <!ATTLIST SplitTime sequence CDATA #REQUIRED>
     * </pre>
     * <!ELEMENT Time (#PCDATA)>
    * <!ATTLIST Time timeFormat CDATA "MM:SS">
     *
     * */
    private Time parseTime(XmlPullWrapper xr)
                    throws IOException, XmlPullParserException
    {
        xr.require(XmlPullWrapper.START_TAG, null, "Time");

        String format = xr.getAttributeValue("null", "timeFormat");

        if (format == null) {
            format = "MM:SS";
        }

        Time t = timeFromString(xr.nextText(), format);

        xr.nextEndTag();

        return t;
    }

    private Time timeFromString(String str, String format)
                         throws XmlPullParserException
    {
        StringTokenizer st = new StringTokenizer(str, ":");

        int hours = 0;
        int min = 0;
        int sec = 0;
        Time t;

        try {
            if (format.compareToIgnoreCase("HH:MM:SS") == 0) {
                hours = Integer.valueOf(st.nextToken()).intValue();
                min = Integer.valueOf(st.nextToken()).intValue();
                sec = Integer.valueOf(st.nextToken()).intValue();
            } else if (format.compareToIgnoreCase("HH:MM") == 0) {
                hours = Integer.valueOf(st.nextToken()).intValue();
                min = Integer.valueOf(st.nextToken()).intValue();
            } else if (format.compareToIgnoreCase("MM:SS") == 0) {
                min = Integer.valueOf(st.nextToken()).intValue();
                sec = Integer.valueOf(st.nextToken()).intValue();
            } else {
                throw new XmlPullParserException("Invalid time format attribute " +
                                                 format);
            }

            t = new Time((hours * 60) + min, sec);
        } catch (Exception e) {
            throw (new XmlPullParserException("Invalid time" + e.getMessage()));
        }

        return (t);
    }

    /** Helper class to hold data for an individual split */
    class Split {
        public String controlCode;
        public Time totalTime;
        public int sequence;

        public Split(String newControlCode, String strSequence, Time newTime)
              throws XmlPullParserException
        {
            controlCode = newControlCode;
            totalTime = newTime;

            try {
                sequence = Integer.parseInt(strSequence);
            } catch (NumberFormatException e) {
                throw (new XmlPullParserException("Invalid split sequence number" +
                                                  " " + strSequence));
            }
        }
    }
}
