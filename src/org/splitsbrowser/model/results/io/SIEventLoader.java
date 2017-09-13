/*
 *  Splitalyzer - SportIdentHTML format loader.
 *
 *  Original Copyright (C) 2000  Dave Ryder
 *  Version T Copyright (C) 2001 Ed Nash
 *  Version 2 Copyright (C) 2002 Reinhard Balling
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
 * Created:    Dave Ryder
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:33:13 $
 * Changed by: $Author: daveryder $
 */
package org.splitsbrowser.model.results.io;


import org.splitsbrowser.model.SplitsbrowserException;
import org.splitsbrowser.model.results.AgeClass;
import org.splitsbrowser.model.results.Course;
import org.splitsbrowser.model.results.Debug;
import org.splitsbrowser.model.results.EventResults;
import org.splitsbrowser.model.results.Result;
import org.splitsbrowser.model.results.Time;

import org.splitsbrowser.util.HTMLutils;
import org.splitsbrowser.util.Message;

import java.io.BufferedReader;
import java.io.IOException;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 *  Loads event details from an .html in SI format. Assumes as little as possible,
 *  since the exact format seems to change frequently. Can read normal results and full results, where each split is
 *  followed by another time (difference to winner)<br>
 *  Reads each line then attempts to class it as a<br>
 *  1. course header<br>
 *  2. control info<br>
 *  3. competitor first line<br>
 *  4. retiral first line<br>
 *  5. non-comp finisher first line<br>
 *  6. competitor second line<br>
 *  7. competitor subsequent line<br>
 *  Then processes the line depending on what it is. Lines 3.,4. and 5. are treated identically.<br>
 *  Original 10/2001 by Ed Nash<br>
 *  Incorporates some ideas from Convert 2.0 by Ed Nash (1999)<br>
 *  Incorporates other ideas from SiteSearch by Ed Nash (2001)<br>
 *  Incorporates further ideas from Dave Ryder's v1.0 SIEventLoader class a TROLL production 2001<br>
 *  Major extension to cope with full SI results and to read start times by R Balling, 2002<br>
 *  <br>
 *  Version 2.00<br>
 *  <br>
 *  version history:<br>
 *  v T.00 - Ed's original effort<br>
 *  v T.01 - added HTML special character decoding and Hm as a courseClimb symbol<br>
 *  1.02 Code added to make make id optional by KLR.<br>
 *  1.03 Added code to handle blank lines within the splits blocks DKR<br>
 *  1.04 Code added to allow blank fields in split times KLR, coded structure modified to introduce more subroutines<br>
 *  2.00 Major changes in line with new datastructures, by R Balling 2002<br>
 *
 * @author      E Nash
 * @author      D Ryder
 * @author      R Balling
 * @modified    October 2002
 */
public class SIEventLoader extends EventLoader {
    //input states - describe the type of the last line

    /**  Description of the Field */
    private static final int ST_INITIAL = 0;

    /**  Waiting for list of controls, e.g. 1(xxx) 2(yyy) 3(zzz) .... */
    private static final int ST_WAIT_FOR_CONTROLS_LIST = 1;

    /**  Competitor complete, wait for new competitor or new class */
    private static final int ST_WAIT_FOR_RESULT_FIRST_LINE = 2;

    /**  Competitor's first line read, wait for second line with club and leg times */
    private static final int ST_WAIT_FOR_RESULT_SECOND_LINE = 3;

    /**  Waiting for first succession line (containing totals) */
    private static final int ST_WAIT_FOR_RESULT_FIRST_CONTINUATION_LINE = 4;

    /**  Waiting for second succession line (containing splits -> ignored)
         RT - Results types
         The change from RT_NORMAL to RT_FULL is done during parsing */
    private static final int ST_WAIT_FOR_RESULT_SECOND_CONTINUATION_LINE = 5;

    /**  For every control only the elapsed time since the start is shown */
    private static final int RT_NORMAL = 0;

    /** For every control the elapsed time, the difference to the fastest runner and the position are shown
         SR - Starttime de-referencing
         How to reference the starttimes to the results: S)tarttime R)eference*/
    private static final int RT_FULL = 1;

    /** Use the startnumber */
    private static final int SR_BYSTARTNO = 0;

    /** No startnumber in the results - have to use name
        Debugging of token types */
    private static final int SR_BYNAME = 1;
    private static final String[] stringTT =
    {
        "STRING", "FLOAT", "INT", "TIME", "DISTANCE_UNIT", "CLIMB_UNIT",
        "N_CONTR", "N_COMP", "CC", "FINISH_SYMBOL", "NON_CMP", "MISPUNCH"
    };
    private static final String[] stringLT =
    {
        "LT_UNKNOWN", "LT_COURSE_HEADER", "LT_COURSE_INFO", "LT_COMP_FIRST",
        "NC_FIRST", "DSQ_FIRST", "LT_COMP_SECOND", "COMP_SUBSEQ",
        "LT_COMP_STARTTIME"
    };
    private static final String[] stringST =
    {
        "ST_INITIAL", "ST_WAIT_FOR_CONTROLS_LIST",
        "ST_WAIT_FOR_RESULT_FIRST_LINE", "ST_WAIT_FOR_RESULT_SECOND_LINE",
        "ST_WAIT_FOR_RESULT_FIRST_CONTINUATION_LINE",
        "ST_WAIT_FOR_RESULT_SECOND_CONTINUATION_LINE"
    };

    /** Age class currently being parsed*/
    private AgeClass ageClass = null;

    /** Reader */
    private BufferedReader reader;

    /** Course being parsed */
    private Course course = null;
    private EventResults event;

    /** REsult being parsed */
    private Result result;

    /** Symbols for course header for the climb */
    private String ClimbSymbols = "~Cm~Hm~hm~m~";

    /** Symbols marking runner as disqualified  */
    private String DsqSymbols = "~pm~mp~dnf~Felst.~Fehlst~Aufg~disk~Disk~";

    /*
    * The following strings contain the symbols used for parsing the SI file.
    * Each string contains any number of symbols separated by ~.
    * They can be overridden using an applet parameter
    * Note that each string must start and end with a '~' to ensure that the tokenType algorithm
    * matches the complete symbol
    */

    /** Symbols for the finish  */
    private String FinishSymbols = "~A~F~Z~M~C~";

    /** Symbols for course header for the distance */
    private String KmSymbols = "~km~m~Km~";

    /**  Symbols marking runner as non-competitive */
    private String NonCompSymbols = "~nc~aK~NC~AK~";

    /**  Symbols for course header for number of controls */
    private String NumControlSymbols = "~C~P~K~";

    /** Name of the course */
    private String courseName;

    /** Nmaes of the input file being read */
    private String fileName;

    /**  Name of the (optional) file containing statrt times */
    private String fileNameStartTimes;

    /**  Description of the Field */
    private String st = "";

    /**  Description of the Field */
    private String stOld = "";

    /**  Description of the Field */
    private Time maxStartTime = new Time(0);

    // Sometimes SI starttimes are in the format hh:mm. To identify this case we keep track of the minimum/maximum start times
    // If they are less than 10 minutes apart, we assume that the times which we parsed as mm:ss are really hh:mm. We therefore
    // multiply all start times by 60

    /**  Description of the Field */
    private Time minStartTime = new Time(Time.MAXTIME.asSeconds());

    // !=0 if results are by course

    /**  Description of the Field */
    private Tokenizer tokenList = new Tokenizer();

    // The ageclass being processed

    /**  Description of the Field */
    private Time[] splits = null;

    // Sometimes for disqualified competitor the finishing time is omitted from the results causing problems
    // when counting the number of splits. We therefore also terminate a competitor's result if the number of double rows has been read.
    private boolean firstCompPerClass;
    private boolean validRun;
    private int byCourse;
    private int currentSplitNumber;

    /**  Debuglevel
     *   The debug level is made up of individual bits. The lower byte is for results, the higher byte for start times
     * 0x0001 ... List each results line and state before processing line
     * 0x0002 ... List tokenized version of line with token types
     * 0x0004 ... List course header information
     * 0x0080 ... List all data after reading of results
     * 0x0100 ... List each line of starttimes file
     * 0x0200 ... List tokenized version of line with token types
     * 0x0400 ... List runners for which start time was found
     * 0x8000 ... List all data after reading of start times
     * Attention: Debugging slows down execution significantly
     */
    private int debugLevel = 0x0000;

    /**  Description of the Field */
    private int doubleRowsPerResult = 0;

    /** Number of double rows making up one result. Changed for each class */
    private int doubleRowsRead = 0;
    private int line = 0;
    private int numberOfControlsForCourse = 0;
    private int resultsType = RT_NORMAL;
    private int startnoRef = -1;
    private int starttimeColumn;

    /**  Internal state of the parser */
    private int state = ST_INITIAL;

    public SIEventLoader() throws java.lang.Exception {
        this(null, "", -1);
    }

    /**
     *  Constructor for the SIEventLoader object
     *
     * @param  newEvent                 Root of event tree
     * @param  newSISymbols             Symbols used for parsing SI results file:
     *      Finish;DSQ;NonComp;NumberOfControls;Km;Climb, e.g.:
     *      'Z;Fehlst;AK;P;km;Hm'
     * @param  newFilenameStartTimes    (Optional) Filename for startlist
     * @param  newStarttimeColumn       Description of the Parameter
     * @exception  java.lang.Exception  Parse error or IO Error
     */
    public SIEventLoader(String newSISymbols, String newFilenameStartTimes,
                         int newStarttimeColumn)
                  throws IOException, SplitsbrowserException
    {
        if (newSISymbols != null) {
            StringTokenizer sT = new StringTokenizer(newSISymbols, ";");

            if (sT.countTokens() != 6) {
                throw (new SplitsbrowserException("Parameter 'sisymbols' must consist of 6 strings separated by ';'. Only " +
                                                  sT.countTokens() +
                                                  " strings found\nParameter sisymbols=" +
                                                  newSISymbols));
            }

            FinishSymbols = "~" + sT.nextToken() + "~";
            DsqSymbols = "~" + sT.nextToken() + "~";
            NonCompSymbols = "~" + sT.nextToken() + "~";
            NumControlSymbols = "~" + sT.nextToken() + "~";
            KmSymbols = "~" + sT.nextToken() + "~";
            ClimbSymbols = "~" + sT.nextToken() + "~";
        }

        fileNameStartTimes = newFilenameStartTimes;
        starttimeColumn = newStarttimeColumn;
    }

    /**
     *  Main method of SI Event loader class
     *
     * @param  newFileName      Name of HTML file containing results in SI format
     * @param  urlInput         Read results from URL or local file system
     * @param  zipped           true if results are zipped
     * @param  newbyCourse      0 if results are by class (default), 1 if results
     *      are by course (containing multiple classes)
     * @exception  IOException  Description of the Exception
     * @exception  Exception    Description of the Exception
     */
    public void loadEvent(EventResults newEvent, String newFileName,
                          boolean urlInput, int newbyCourse)
                   throws IOException, SplitsbrowserException
    {
        event = newEvent;

        byCourse = newbyCourse;
        fileName = newFileName;
        loadResults(urlInput);

        if ((debugLevel & 0x0080) == 0x0080) {
            new Debug(event).listAll();
        }

        if (!fileNameStartTimes.equals("")) {
            loadStartTimes(urlInput);

            if ((debugLevel & 0x8000) == 0x8000) {
                new Debug(event).listAll();
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //   F I L E   U T I L I T I E S
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    private String getLine(BufferedReader reader) throws IOException {
        // Read the next non-blank line - stripping the HTML tags
        String buffer;

        do {
            buffer = reader.readLine();
            line++;

            if (buffer == null) {
                return buffer;
            }

            buffer = StripHTMLTags(buffer, reader);
        } while (buffer.trim().length() == 0);

        return (buffer);
    }

    private String StripHTMLTags(String buffer, BufferedReader reader)
                          throws IOException
    {
        // Strips the HTML tags from a string
        // If a tag spans a line an extra line is read
        int tagStart;

        while ((tagStart = buffer.indexOf("<")) != -1) {
            int tagEnd;

            try {
                // Read an extra line into the buffer
                while ((tagEnd = buffer.indexOf(">", tagStart)) == -1) {
                    buffer = buffer.concat(" ".concat(reader.readLine()));
                    line++;
                }

                String beforeTag =
                    (tagStart > 0) ? buffer.substring(0, tagStart) : "";
                String afterTag =
                    (tagEnd < buffer.length())
                    ? buffer.substring(tagEnd + 1, buffer.length()) : "";

                if (event.getName().equals("")) {
                    if (buffer.substring(tagStart + 1).startsWith("TD><NOBR><B>")) {
                        int headerEnd = buffer.indexOf("<", tagStart + 13);

                        if (headerEnd > 0) {
                            event.setName(buffer.substring(tagStart + 13,
                                                           headerEnd));
                        } else {
                            event.setName(buffer.substring(tagStart + 13));
                        }
                    }
                }

                buffer = beforeTag.concat(afterTag);
            } catch (Exception e) {
                throw new IOException(Message.get("SILoader.HTMLError"));
            }
        }

        return (buffer);
    }

    /**
     *  First row of for a result - either comp or non-comp<br>
     *  There are two types of results: by Course<br><pre>
     *  1    332 Joe Bloggs     M21E      100:15 ....<br>
     *  or by class<br>
     *  1   416 Joe Bloggs     18:20 ....<br>
     *  175 Fred Bloggs    Disk .... </pre>
     */
    private void compFirst() throws java.lang.Exception {
        String competitorName;

        if ((state == ST_WAIT_FOR_RESULT_FIRST_LINE) ||
                (state == ST_WAIT_FOR_RESULT_SECOND_CONTINUATION_LINE) ||
                (state == ST_WAIT_FOR_RESULT_FIRST_CONTINUATION_LINE)) {
            if (firstCompPerClass) {
                doubleRowsPerResult = 0;
            } else {
                doubleRowsRead = 0;
            }

            boolean misPunch = false;

            if ((state == ST_WAIT_FOR_RESULT_SECOND_CONTINUATION_LINE) ||
                    (state == ST_WAIT_FOR_RESULT_FIRST_CONTINUATION_LINE)) {
                // The last competitor's line was not completed.
                reportMissingTimes();
                result.addMissingTimes();
            }

            // Extract the competitor name
            int firstTime = tokenList.getNumberOfTokens() - 1;

            validRun = true;

            while ((tokenList.typeAt(firstTime) == Tokenizer.TT_TIME) ||
                       ((resultsType == RT_FULL) &&
                       (tokenList.typeAt(firstTime) == Tokenizer.TT_NUM_COMPETITORS))) {
                firstTime--;
            }

            // If finish-time is mispunch
            if (tokenList.typeAt(firstTime) == Tokenizer.TT_MISPUNCH) {
                firstTime--;
                misPunch = true;
            }

            // firstTime now points to just BEFORE the finish time, the first split is at firstTime+2
            splits = new Time[course.getNumControls() + 1];
            currentSplitNumber = 0;

            // pointer into splits
            // If results are by course, the finish time must be preceded by the ageclass name
            if (byCourse > 0) {
                ageClass =
                    course.addAgeClass(new AgeClass(tokenList.stringAt(firstTime--)));
            }

            // Now start from beginning of line skipping position | noncomp, startno , chip ID number
            int firstNameIndex = 0;

            while ((tokenList.typeAt(firstNameIndex) == Tokenizer.TT_INTEGER) ||
                       (tokenList.typeAt(firstNameIndex) == Tokenizer.TT_NON_COMP)) {
                firstNameIndex++;

                //skip position, id, noncomp
            }

            // Check if position is followed by start number
            if ((startnoRef == -1) &&
                    (tokenList.typeAt(0) == Tokenizer.TT_INTEGER) && !misPunch) {
                if (tokenList.typeAt(1) != Tokenizer.TT_INTEGER) {
                    startnoRef = SR_BYNAME;
                } else {
                    startnoRef = SR_BYSTARTNO;
                }
            }

            // We now point to first name. This is preceded by the start number (if it is there) and the position (if not disqualified)
            int startNumber = -1;

            if ((startnoRef == SR_BYSTARTNO) && (firstNameIndex > 0) &&
                    (tokenList.typeAt(firstNameIndex - 1) == Tokenizer.TT_INTEGER)) {
                startNumber =
                    Integer.parseInt(tokenList.stringAt(firstNameIndex - 1));
            }

            // Build competitor's name
            competitorName = tokenList.stringAt(firstNameIndex);

            for (int i = firstNameIndex + 1; i <= firstTime; i++) {
                competitorName =
                    competitorName.concat(" ").concat(tokenList.stringAt(i))
                                  .trim();
            }

            // Precede name of non-comp competitors with non-comp symbol
            if (tokenList.typeAt(0) == Tokenizer.TT_NON_COMP) {
                competitorName =
                    tokenList.stringAt(0).concat(" ").concat(competitorName);
            }

            competitorName = HTMLutils.translateString(competitorName);
            result =
                new Result(competitorName, "", course, ageClass, splits, null,
                           startNumber, validRun);

            if (tokenList.typeAt(0) == Tokenizer.TT_NON_COMP) {
                result.setValid(false);
            }

            if (byCourse > 0) {
                firstTime++;
            }

            processSplitTimes(firstTime + 2);

            // Skip ageclass if we are by course
            state = ST_WAIT_FOR_RESULT_SECOND_LINE;
        } else {
            fatalError(Message.get("SILoader.MissingCourse"));
        }
    }

    /**
     * Process the third and ff. lines for a runner
     */
    private void compMultipleLines() throws java.lang.Exception {
        if (state == ST_WAIT_FOR_RESULT_FIRST_CONTINUATION_LINE) {
            processSplitTimes(0);
            state = ST_WAIT_FOR_RESULT_SECOND_CONTINUATION_LINE;
        } else if (state == ST_WAIT_FOR_RESULT_SECOND_CONTINUATION_LINE) {
            // If we have not read enough splits, check whether we have read same number of continuation lines as winner
            if (currentSplitNumber <= course.getNumControls()) {
                state = ST_WAIT_FOR_RESULT_FIRST_CONTINUATION_LINE;

                if (!firstCompPerClass &&
                        (doubleRowsRead >= doubleRowsPerResult)) {
                    reportMissingTimes();
                    result.addMissingTimes();
                    state = ST_WAIT_FOR_RESULT_FIRST_LINE;
                }
            } else {
                state = ST_WAIT_FOR_RESULT_FIRST_LINE;
            }
        }
    }

    /**
     *  Get the club and ignore first line of their split times
     */
    private void compSecond() throws java.lang.Exception {
        // EJN20040216: modified (3 items) to accept case where no club is given
        // EJN20040216a: assign initial empty string to club
        String club = "";

        if (state == ST_WAIT_FOR_RESULT_SECOND_LINE) {
            // EJN20040216b: don't assume first token is club
            //club = tokenList.stringAt(0);
            // EJN20040216c: change start token for checking & building club to 0 (was 1)
            int i = 0;
            int size = tokenList.getNumberOfTokens();

            while ((i < size) && (tokenList.typeAt(i) != Tokenizer.TT_TIME)) {
                club = club.concat(" ").concat(tokenList.stringAt(i));
                i++;
            }

            club = HTMLutils.translateString(club);
            result.setClub(club);

            // Add the result to the ageClass. If results are by ageClass, the variable ageClass is not updated while the results for
            // one ageclass are processed. If the results are by course, the ageclass is modified for each runner
            ageClass.addResult(result);

            if (currentSplitNumber < (course.getNumControls() + 1)) {
                state = ST_WAIT_FOR_RESULT_FIRST_CONTINUATION_LINE;

                if (!firstCompPerClass &&
                        (doubleRowsRead >= doubleRowsPerResult)) {
                    reportMissingTimes();
                    result.addMissingTimes();
                    state = ST_WAIT_FOR_RESULT_FIRST_LINE;
                }
            } else {
                firstCompPerClass = false;
                state = ST_WAIT_FOR_RESULT_FIRST_LINE;
            }
        } else if ((state == ST_INITIAL) ||
                       (state == ST_WAIT_FOR_RESULT_FIRST_LINE)) {
            //ignore line
        } else {
            fatalError(Message.get("SILoader.MissingFirstLine"));
        }
    }

    /**
     *  Process a course header, e.g.<br>
     *  <pre>H16A  (54)                      4.000 km  0 hm   27 C</pre>
     */
    private void courseHeader() throws java.lang.Exception {
        if ((state == ST_WAIT_FOR_RESULT_SECOND_CONTINUATION_LINE) ||
                (state == ST_WAIT_FOR_RESULT_FIRST_CONTINUATION_LINE)) {
            // The last competitor's line was not completed.
            reportMissingTimes();
            result.addMissingTimes();
            state = ST_WAIT_FOR_RESULT_FIRST_LINE;
        }

        if ((state == ST_WAIT_FOR_RESULT_FIRST_LINE) || (state == ST_INITIAL)) {
            parseCourseHeader();
            numberOfControlsForCourse = 0;

            // Delay adding the course to the list of courses in the event class because we don't have details about the control codes yet.
            // It is theoretically possible that the course header information (distance/climb ...) could be the same for two courses although they are different.
            firstCompPerClass = true;

            if ((debugLevel & 0x0004) == 0x0004) {
                System.out.println("\n[courseHeader] " + course.getName() +
                                   " courseLength: " + course.getDistance() +
                                   " / courseClimb: " + course.getClimb() +
                                   " / nContr: " + course.getNumControls());
            }

            state = ST_WAIT_FOR_CONTROLS_LIST;
        } else {
            fatalError(Message.get("SILoader.UnexpectedCourseHeader"));
        }
    }

    /**
     *  Process the second ff. header lines of a course listing control number and control code, e.g.
     *  1(102) 2(103) 3(104) 4(99) F
     */
    private void courseInfo() throws java.lang.Exception {
        if (state == ST_WAIT_FOR_CONTROLS_LIST) {
            int tokenListSize = tokenList.getNumberOfTokens();
            int i;

            for (i = 0; i < tokenListSize; i++) {
                // Extract control code (the number framed by brackets)
                String cc = tokenList.stringAt(i);
                int start = 0;
                int end;
                int len = cc.length();

                while ((start < len) && (cc.charAt(start) != '(')) {
                    start++;
                }

                end = ++start;

                while ((end < len) && (cc.charAt(end) != ')')) {
                    end++;
                }

                if ((start < len) && (end < len)) {
                    course.addControlCode(numberOfControlsForCourse + i + 1,
                                          cc.substring(start, end));
                }
            }

            if ((tokenList.typeAt(tokenListSize - 1) == Tokenizer.TT_FINISH_SYMBOL) ||
                    (tokenList.typeAt(tokenListSize - 1) == Tokenizer.TT_NUM_CONTROLS)) {
                // Finish symbol found
                numberOfControlsForCourse--;

                // Don't count Finish symbol
                // Add the course to the event class but only if it does not exist
                course = event.addCourse(course);

                if (byCourse == 0) {
                    // Results are by class, add the class to this course
                    ageClass = course.addAgeClass(ageClass);
                }

                state = ST_WAIT_FOR_RESULT_FIRST_LINE;
            }

            numberOfControlsForCourse =
                numberOfControlsForCourse + tokenListSize;

            if ((state == ST_WAIT_FOR_RESULT_FIRST_LINE) &&
                    (numberOfControlsForCourse != course.getNumControls())) {
                throw (new Exception(Message.get("SILoader.WrongNoControls",
                                                 course.getName(),
                                                 course.getNumControls())));
            }
        } else {
            fatalError(Message.get("SILoader.UnexpectedCourseHeader"));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //    E R R O R   H A N D L I N G
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     *  Fatal error - terminate loading the SI file
     */
    private void fatalError(String s) throws java.lang.Exception {
        tokenList.printTokentypes();
        new Debug(event).listAll();
        throw (new Exception(s + "\n" + st));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // P R O C E S S    R E S U L T S
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void loadResults(boolean urlInput)
                      throws IOException, SplitsbrowserException
    {
        // ----- Read results ------
        reader = openReader(fileName, urlInput);

        line = 0;
        state = ST_INITIAL;

        try {
            stOld = st;
            st = getLine(reader);

            if (st == null) {
                fatalError("No data");
            }

            while ((st != null) && (st.trim() != null)) {
                // determine line type
                tokenList.splitIntoTokens(st);

                int linet = tokenList.lineType();

                if ((debugLevel & 0x0001) == 0x0001) {
                    System.out.println("[loadEvent][" + line +
                                       "] State (before)=" +
                                       stringST[state - ST_INITIAL] +
                                       "  Linetype:=" + stringLT[linet] +
                                       " >> " + st);
                }

                if ((debugLevel & 0x0002) == 0x0002) {
                    tokenList.printTokentypes();
                }

                switch (linet) {
                case Tokenizer.LT_COURSE_HEADER:
                    courseHeader();

                    break;

                case Tokenizer.LT_COURSE_INFO:
                    courseInfo();

                    break;

                case Tokenizer.LT_COMP_FIRST:
                case Tokenizer.LT_NONCOMP_FIRST:
                case Tokenizer.LT_DSQ_FIRST:
                    compFirst();

                    break;

                case Tokenizer.LT_COMP_SECOND:
                    compSecond();

                    break;

                case Tokenizer.LT_COMP_SUBSQ:
                    compMultipleLines();

                    break;

                case Tokenizer.LT_UNKNOWN:
                    unknownLine();

                    break;
                }

                stOld = st;
                st = getLine(reader);
            }

            ;
        } catch (Exception e) {
            tokenList.printTokentypes();
            throw (new SplitsbrowserException("\n" +
                                              Message.get("SILoader.Error") +
                                              " " + fileName + "\n" +
                                              e.getMessage() + "\n" + "  [" +
                                              new Integer(line).toString() +
                                              "] " + st + "\n" + "  [" +
                                              new Integer(line - 1).toString() +
                                              "] " + stOld + "\n"));
        } finally {
            reader.close();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // P R O C E S S    S T A R T L I S T
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void loadStartTimes(boolean urlInput)
                         throws IOException, SplitsbrowserException
    {
        // ----- Start times ------
        if (fileNameStartTimes.equals("")) {
            return;
        }

        reader = openReader(fileNameStartTimes, urlInput);
        System.out.println("[loadStartTimes] Loading start times from " +
                           fileNameStartTimes);

        switch (startnoRef) {
        case -1:
            System.out.println("Internal Error: startnoRef not set.");

            break;

        case SR_BYSTARTNO:
            System.out.println("Looking up start time by start number.");

            break;

        case SR_BYNAME:
            System.out.println("Looking up start time by name. No start numbers in results.");

            break;
        }

        line = 0;
        st = "";
        course = null;
        ageClass = null;

        try {
            st = getLine(reader);

            if (st == null) {
                fatalError("No data");
            }

            while ((st != null) && (st.trim() != null)) {
                if ((debugLevel & 0x0100) == 0x0100) {
                    System.out.println("[loadStartTimes] " + st);
                }

                tokenList.splitIntoTokens(st);

                if (st.charAt(0) > ' ') {
                    // Start of ageClass, Course or club info
                    if ((debugLevel & 0x0200) == 0x0200) {
                        tokenList.printTokentypes();
                    }

                    if (tokenList.lineType() == Tokenizer.LT_COURSE_HEADER) {
                        startListCourseHeader();
                    }
                } else {
                    // No character in first column, can be startlistentry
                    if ((tokenList.getNumberOfTokens() > 2) &&
                            (tokenList.typeAt(0) == Tokenizer.TT_INTEGER) &&
                            ((tokenList.typeAt(tokenList.getNumberOfTokens() -
                                                   1) == Tokenizer.TT_TIME) ||
                            (tokenList.typeAt(tokenList.getNumberOfTokens() -
                                                  1) == Tokenizer.TT_INTEGER))) {
                        startListAddStartTime();
                    }
                }

                st = getLine(reader);
            }
        } catch (Exception e) {
            throw (new SplitsbrowserException("Error reading SportIdent HTML startlist file " +
                                              fileNameStartTimes + "\n" +
                                              e.toString() + "\n" + "Line " +
                                              new Integer(line).toString() +
                                              ": " + st + "\n"));
        } finally {
            reader.close();
        }

        // If the start interval spans less than 10 minutes, assume that start-times were in hh:mm format (rather than mm:ss)
        // Correct for this by multiplying all start times by 60
        if (maxStartTime.subtract(minStartTime).asSeconds() < 600) {
            multiplyStartTimesBy60();
        }
    }

    private void multiplyStartTimesBy60() {
        int nC;
        int nA;
        int nR;

        for (nC = 0; nC < event.getNumCourses(); nC++) {
            Course course = event.getCourse(nC);

            for (nA = 0; nA < course.getNumAgeClasses(); nA++) {
                AgeClass ageClass = course.getAgeClass(nA);

                for (nR = 0; nR < ageClass.getNumResults(); nR++) {
                    Result result = ageClass.getResult(nR);

                    if ((result.getStartTime() != null) &&
                            result.getStartTime().isValid()) {
                        result.setStartTimeAsSeconds(result.getStartTime()
                                                           .asSeconds() * 60);
                    }
                }
            }
        }
    }

    /**
     *  Parse the course header and set the class variables: course and ageClass
     */
    private void parseCourseHeader() {
        float courseLength;
        float courseClimb;
        int tNum;

        tNum = tokenList.getNumberOfTokens() - 1;

        // Find number of controls, courseClimb and courseLength starting from end of line
        numberOfControlsForCourse = 0;
        courseClimb = 0;
        courseLength = 0;

        // If Last token is an int: If it is <=30 we assume it is number of controls, else it must be courseClimb.
        if (tokenList.typeAt(tNum) == Tokenizer.TT_INTEGER) {
            if (Integer.parseInt(tokenList.stringAt(tNum)) <= 30) {
                numberOfControlsForCourse =
                    Integer.parseInt(tokenList.stringAt(tNum--));
            } else {
                courseClimb = toFloat(tokenList.stringAt(tNum--));
            }
        }

        while (tNum > 1) {
            switch (tokenList.typeAt(tNum)) {
            case Tokenizer.TT_NUM_CONTROLS:
                numberOfControlsForCourse =
                    Integer.parseInt(tokenList.stringAt(--tNum));

                break;

            case Tokenizer.TT_CLIMB_UNIT:
                courseClimb = toFloat(tokenList.stringAt(--tNum));

                break;

            case Tokenizer.TT_DISTANCE_UNIT:
                courseLength = toFloat(tokenList.stringAt(--tNum));

                break;
            }

            tNum--;
        }

        // Find name of course
        tNum = 0;
        courseName = "";

        while ((tokenList.typeAt(tNum) != Tokenizer.TT_FLOAT) &&
                   ((tokenList.typeAt(tNum)) != Tokenizer.TT_NUM_COMPETITORS) &&
                   (tNum < tokenList.getNumberOfTokens())) {
            courseName =
                courseName.concat(" ").concat(tokenList.stringAt(tNum++)).trim();
        }

        courseName = HTMLutils.translateString(courseName);

        // Create a new course but don't yet add it to the event
        course =
            new Course(courseName, numberOfControlsForCourse, courseLength,
                       courseClimb);

        if (byCourse == 0) {
            // Results are by class, clear the course name and create an ageclass as well (will eventually be added to the course)
            // We can't add the ageclass to this course because the same course may already exist within Event and we have not yet
            // read the details of the control codes
            course.setName("");
            ageClass = new AgeClass(courseName);
        } else {
            // Results are by course, ageClass is not valid
            ageClass = null;
        }
    }

    /**
     *  Process the tail of a line containing elapsed times
     *          tokens(startAt) is the first TT_TIME token
     */
    private void processSplitTimes(int startAt) throws java.lang.Exception {
        if (firstCompPerClass) {
            doubleRowsPerResult++;
        } else {
            doubleRowsRead++;
        }

        boolean voidControl = false;

        while ((startAt < tokenList.getNumberOfTokens()) &&
                   (currentSplitNumber <= numberOfControlsForCourse)) {
            voidControl = false;
            splits[currentSplitNumber] = new Time(tokenList.stringAt(startAt));

            if (!splits[currentSplitNumber].isValid()) {
                if (splits[currentSplitNumber].asSeconds() == 0) {
                    // Treat special case 0.00 which is used when SI box is defect
                    if (currentSplitNumber > 0) {
                        splits[currentSplitNumber].setSeconds(splits[currentSplitNumber -
                                                              1].asSeconds());
                    }

                    // Copy time from last control
                    voidControl = true;

                    //course.setValidControl(currentSplitNumber,false);
                } else {
                    validRun = false;
                    result.setValid(false);
                }
            }

            currentSplitNumber++;
            startAt++;

            // Now process the (redundant) time and position following each split time if FULL results are being used
            if ((resultsType == RT_FULL) && validRun &&
                    (currentSplitNumber <= numberOfControlsForCourse) &&
                    !voidControl) {
                // Hack for V0.91: Sometimes the split time is not followed by another time and a position but
                // by the position and the time instead. Very strange !!
                if ((startAt < (tokenList.getNumberOfTokens() - 1)) &&
                        (tokenList.typeAt(startAt) == Tokenizer.TT_NUM_COMPETITORS) &&
                        (tokenList.typeAt(startAt + 1) == Tokenizer.TT_TIME)) {
                    startAt += 2;
                } else {
                    // time must be followed by another time and by a position
                    if ((startAt < tokenList.getNumberOfTokens()) &&
                            (tokenList.typeAt(startAt) != Tokenizer.TT_TIME)) {
                        fatalError(Message.get("SILoader.MissingTime",
                                               currentSplitNumber));
                    }

                    startAt++;

                    if ((startAt < tokenList.getNumberOfTokens()) &&
                            (tokenList.typeAt(startAt) != Tokenizer.TT_NUM_COMPETITORS)) {
                        startAt--;
                        validRun = false;
                        result.setValid(false);
                    } else {
                        startAt++;
                    }
                }
            }
        }
    }

    /**
     *  Report missing times only to console - Continue processing
     */
    private void reportMissingTimes() {
        System.out.println(Message.get("SILoader.MissingTimes",
                                       ageClass.getName() + ":" +
                                       result.getName(), line));
    }

    private void startListAddStartTime() throws java.lang.Exception {
        String strStartTime;
        String competitorName;
        int last = tokenList.getNumberOfTokens() - 1;

        if (startnoRef == SR_BYNAME) {
            int firstNameIndex = 0;

            competitorName = "";

            while ((firstNameIndex <= last) &&
                       (tokenList.typeAt(firstNameIndex) == Tokenizer.TT_INTEGER)) {
                firstNameIndex++;
            }

            competitorName = tokenList.stringAt(firstNameIndex++);

            while ((firstNameIndex <= last) &&
                       (tokenList.typeAt(firstNameIndex) == Tokenizer.TT_STRING)) {
                competitorName += (" " + tokenList.stringAt(firstNameIndex++));
            }

            result = null;

            if (ageClass != null) {
                result = ageClass.findResult(competitorName);
            } else if (course != null) {
                result = course.findResult(competitorName);
            }

            if (result == null) {
                result = event.findResult(competitorName);
            }
        } else {
            if (tokenList.typeAt(0) != Tokenizer.TT_INTEGER) {
                System.out.println(Message.get("SILoader.MissingStartNumber",
                                               line));

                return;
            }

            int startNumber = Integer.parseInt(tokenList.stringAt(0));

            // use start number
            result = null;

            if (ageClass != null) {
                result = ageClass.findResult(startNumber);
            } else if (course != null) {
                result = course.findResult(startNumber);
            }

            if (result == null) {
                result = event.findResult(startNumber);
            }
        }

        if (result == null) {
            return;
        }

        if (starttimeColumn > 0) {
            st = HTMLutils.translateString(st);

            // remove HTML specials like '&nbsp';
            int startCol = starttimeColumn;

            if (st.charAt(startCol) == ' ') {
                startCol++;
            }

            if (st.charAt(startCol) == ' ') {
                startCol++;
            }

            int endCol = startCol + 1;

            while ((endCol < st.length()) && (st.charAt(endCol) > ' ')) {
                endCol++;
            }

            strStartTime = st.substring(startCol, endCol);
        } else {
            // Check for malformed start time: mmm without seconds.
            strStartTime = tokenList.stringAt(last);

            if (tokenList.typeAt(last) == Tokenizer.TT_INTEGER) {
                strStartTime += ":00";
            }
        }

        Time startTime = new Time(strStartTime);

        result.setStartTime(startTime);

        if ((debugLevel & 0x0400) == 0x0400) {
            System.out.println("Found starttime " +
                               ((startnoRef == SR_BYNAME) ? "by name"
                                                          : "by number") +
                               " using " +
                               ((ageClass != null)
                                ? (" ageclass [" + ageClass.getName() + "]")
                                : ((course != null)
                                   ? (" course [" + course.getName() + "]")
                                   : " full search of all courses/ageclasses")) +
                               " for " + result.getName() + "/" +
                               startTime.toString());
        }

        // update min/max starttime
        if (startTime.lessThan(minStartTime)) {
            minStartTime.setSeconds(startTime.asSeconds());
        }

        if (maxStartTime.lessThan(startTime)) {
            maxStartTime.setSeconds(startTime.asSeconds());
        }
    }

    private void startListCourseHeader() {
        // The current line can be: classHeader, courseHeader, clubHeader (if startlist is by club)
        parseCourseHeader();

        // course and ageClass have been set
        if (ageClass != null) {
            ageClass = event.findAgeClass(ageClass);
        }

        if (ageClass != null) {
            // ageclass FOUND
            course = null;

            return;
        }

        // Failed to find the ageclass, try the course name
        if (course != null) {
            course = event.findCourse(course.getName());
        }
    }

    /**
      *  Converts a string to a floating point number.
      *  If the string is not a valid floating point number then 0 is returned
      */
    private float toFloat(String s) {
        float f = 0;

        try {
            f = Float.valueOf(s).floatValue();
        } catch (NumberFormatException e) {
            f = 0;
        }

        return (f);
    }

    private void unknownLine() throws java.lang.Exception {
        if (state != ST_INITIAL) {
            fatalError(Message.get("SILoader.LT_UNKNOWNLine", line));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //   T O K E N I Z E R
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class Tokenizer {
        // LT - line types
        public static final int LT_UNKNOWN = 0;

        // line unrecognised
        public static final int LT_COURSE_HEADER = 1;

        // course name, courseLength [, courseClimb, no. controls]
        public static final int LT_COURSE_INFO = 2;

        // list of control codes
        public static final int LT_COMP_FIRST = 3;

        // posn [, id], name [, class], total time, splits
        public static final int LT_NONCOMP_FIRST = 4;

        // aK|nc|NC [, id], name [, class], total time, splits
        public static final int LT_DSQ_FIRST = 5;

        // [id,] name [,class], mp|pm|Felst., splits
        public static final int LT_COMP_SECOND = 6;

        // club, splits
        public static final int LT_COMP_SUBSQ = 7;

        // splits
        public static final int LT_COMP_STARTTIME = 8;

        // Start time(s) for a competitor-
        // stno, id, name, year, club,start1, [start2, ...]
        // TT - token types
        public static final int TT_STRING = 0;
        public static final int TT_FLOAT = 1;
        public static final int TT_INTEGER = 2;
        public static final int TT_TIME = 3;
        public static final int TT_DISTANCE_UNIT = 4;
        public static final int TT_CLIMB_UNIT = 5;
        public static final int TT_NUM_CONTROLS = 6;
        public static final int TT_NUM_COMPETITORS = 7;
        public static final int TT_CONTROL_CODE = 8;
        public static final int TT_FINISH_SYMBOL = 9;
        public static final int TT_NON_COMP = 10;
        public static final int TT_MISPUNCH = 11;
        private Vector tokens = new Vector(40, 10);

        Tokenizer() {
        }

        public int getNumberOfTokens() {
            return tokens.size();
        }

        public int lineType() {
            //returns a line type (integer) constant describing the line for results
            // EJN20040217: modified (2 items) to accept case where no club is given on
            // competitor second line (i.e. line consists only of split times - this was
            // getting returned as a subsequent line LT_COMP_SUBSQ)
            int last = tokenList.getNumberOfTokens() - 1;
            int firstType = tokenList.typeAt(0);
            int lastType = tokenList.typeAt(last);

            if (((last >= 1) && (lastType == TT_NUM_CONTROLS) &&
                    (tokenList.typeAt(last - 1) == TT_INTEGER)) ||
                    (lastType == TT_DISTANCE_UNIT) ||
                    (lastType == TT_CLIMB_UNIT) ||
                    ((lastType == TT_NUM_COMPETITORS) &&
                    (tokenList.typeAt(last - 1) != TT_TIME))) {
                return LT_COURSE_HEADER;
            }

            // Check for malformed course header where symbol for number of controls is missing
            if ((last > 3) && (lastType == TT_INTEGER) &&
                    ((tokenList.typeAt(last - 1) == TT_CLIMB_UNIT) ||
                    (tokenList.typeAt(last - 1) == TT_DISTANCE_UNIT))) {
                return LT_COURSE_HEADER;
            }

            if (((last >= 1) &&
                    (tokenList.typeAt(last - 1) == TT_CONTROL_CODE)) ||
                    ((last == 0) && (lastType == TT_FINISH_SYMBOL))) {
                // Special case: Just the numbers 1 2 3 4 5 ... with no control codes
                return LT_COURSE_INFO;
            }

            if (isDisqualified()) {
                return LT_DSQ_FIRST;
            }

            if ((last >= 2) && (firstType == TT_INTEGER) &&
                    (lastType == TT_TIME) && (state != ST_INITIAL)) {
                return LT_COMP_FIRST;
            }

            // With full results we cannot easily distinguish first and second line. We thus have to use the state
            if ((last >= 2) && (lastType == TT_NUM_COMPETITORS) &&
                    (tokenList.typeAt(last - 1) == TT_TIME)) {
                resultsType = RT_FULL;

                if ((firstType == TT_TIME) &&
                        (state != ST_WAIT_FOR_RESULT_SECOND_LINE)) {
                    // EJN20040217a: added the state clause so that if it looks like a subsequent
                    // line but we're waiting for a second line then it's assumed not to be a
                    // subsequent line (in the case where no club is given then these lines are identical)
                    return LT_COMP_SUBSQ;
                }

                if (state == ST_WAIT_FOR_RESULT_SECOND_LINE) {
                    // we are waiting for second line
                    return LT_COMP_SECOND;
                } else {
                    return LT_COMP_FIRST;
                }
            }

            if ((last >= 0) && (firstType == TT_NON_COMP)) {
                return LT_NONCOMP_FIRST;
            }

            if ((last >= 0) && (firstType == TT_STRING)) {
                return LT_COMP_SECOND;
            }

            if (firstType == TT_TIME) {

                // EJN20040217b: added the check for state so we only return it as a
                // subsequent line if we're not waiting for a second line - if we are
                // in this state then we assume that it is a second line
                // (in the case where no club is given then these lines are identical)
                if (state != ST_WAIT_FOR_RESULT_SECOND_LINE) {
                    // Also works for FULL results
                    return LT_COMP_SUBSQ;
                } else {
                    return LT_COMP_SECOND;
                }
            }

            return (LT_UNKNOWN);
        }

        public void splitIntoTokens(String buffer) {
            // breaks down buffer by spaces and returns vector of tokens
            int start = 0;
            Token t;
            boolean notDone = true;

            tokens.removeAllElements();

            while ((start < buffer.length()) && notDone) {
                t = sub(buffer, start);

                if (t.value != "") {
                    tokens.addElement(t);
                    start = t.end;
                } else {
                    notDone = false;
                }
            }
        }

        public int startListLineType() {
            return LT_UNKNOWN;
        }

        public String stringAt(int i) {
            Token t = (Token) tokens.elementAt(i);

            return t.value;
        }

        public int tokenType(String token) {
            // return an integer constant describing the token
            String framedToken = "~" + token + "~";

            try {
                if (KmSymbols.indexOf(framedToken) != -1) {
                    return (TT_DISTANCE_UNIT);
                }

                if ((token.length() == 2) &&
                        (ClimbSymbols.indexOf(framedToken) != -1)) {
                    return (TT_CLIMB_UNIT);
                }

                if (NumControlSymbols.indexOf(framedToken) != -1) {
                    // Must be before TT_FINISH_SYMBOL because 'C' is in both sysmbol lists
                    return (TT_NUM_CONTROLS);
                }

                if ((token.length() == 1) &&
                        (FinishSymbols.indexOf(framedToken) != -1)) {
                    return (TT_FINISH_SYMBOL);
                }

                if (token.indexOf("(") != -1) {
                    if ("0123456789".indexOf(token.substring(0, 1)) != -1) {
                        return (TT_CONTROL_CODE);
                    }

                    if ("0123456789".indexOf(token.substring(1, 1)) != -1) {
                        return (TT_NUM_COMPETITORS);
                    }
                }

                if (NonCompSymbols.indexOf(framedToken) != -1) {
                    return (TT_NON_COMP);
                }

                if ((token.length() >= 1) &&
                        (DsqSymbols.indexOf(framedToken) != -1)) {
                    return (TT_MISPUNCH);
                }

                // Note strings starting with : are also treated as numbers to fix a bug in SI software which sometimes
                // prints splits as ':mm:ss' for LT_UNKNOWN reasons
                if ((((token.indexOf(":") != -1) &&
                        ("0123456789".indexOf(token.substring(0, 1)) != -1)) ||
                        (token.charAt(0) == ':')) ||
                        (token.indexOf("---") != -1) ||
                        (token.charAt(0) == '*') || (token.equals("0.00"))) {
                    return (TT_TIME);
                }

                if ((token.indexOf(".") != -1) &&
                        ((new Float(token).floatValue()) > 0)) {
                    return (TT_FLOAT);
                }

                if ((Integer.parseInt(token)) > 0) {
                    return (TT_INTEGER);
                }
            } catch (NumberFormatException e) {
                return (TT_STRING);
            }

            return (TT_STRING);
        }

        public int typeAt(int i) {
            Token t = (Token) tokens.elementAt(i);

            return t.type;
        }

        private boolean isDisqualified() {
            // true if first time is preceeded by misspunch
            // will be in error if last name is misspinch token!
            int mispunchIndex = 0;
            int size = tokenList.getNumberOfTokens();
            int type;

            do {
                type = tokenList.typeAt(mispunchIndex);

                if (type == TT_MISPUNCH) {
                    return true;
                }

                mispunchIndex++;
            } while ((type != TT_TIME) && (mispunchIndex < size));

            return false;
        }

        /*
         *  Debugging function
         */
        private void printTokentypes() {
            StringBuffer s =
                new StringBuffer("        [" + stringLT[tokenList.lineType()] +
                                 "]  ");

            for (int i = 0; i < tokenList.getNumberOfTokens(); i++) {
                s.append(tokenList.stringAt(i) + "{" +
                         stringTT[tokenType(tokenList.stringAt(i))] + "},  ");
            }

            System.out.println("  " + s);
        }

        private Token sub(String buffer, int start) {
            int s = start;
            int e;
            String w;

            while ((s < buffer.length()) && (buffer.charAt(s) == ' ')) {
                s++;
            }

            if (s == buffer.length()) {
                return new Token("", s, TT_STRING);
            }

            e = s;

            while ((e < buffer.length()) && (buffer.charAt(e) != ' ')) {
                e++;
            }

            start = e;
            w = buffer.substring(s, e);

            return new Token(w, e, tokenType(w));
        }

        private class Token {
            String value;
            int end;
            int type;

            /**  Constructor for the Token object  */
            public Token() {
            }

            /**
             *  Constructor for the Token object
             *
             * @param  s  Description of the Parameter
             * @param  e  Description of the Parameter
             * @param  t  Description of the Parameter
             */
            public Token(String s, int e, int t) {
                value = s;
                end = e;
                type = t;
            }
        }
    }
}
