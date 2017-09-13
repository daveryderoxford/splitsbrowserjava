/*
 *  Splitsbrowser - SportIdent CVS file loader
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
 * Loads event details from an SportIdent CVS format file.
 * 
 * @author D Ryder
 * @modified April 2003
 */
public class SICSVEventLoader extends EventLoader {
	private int NAME_INDEX;
	private int START_TIME_INDEX;
	private int TOTAL_TIME_INDEX;
	private int CLUB_INDEX;
	private int CLASS_INDEX;
	private int COURSE_INDEX;
	private int DISTANCE_INDEX;
	private int CLIMB_INDEX;
	private int NUM_CONTROLS_INDEX;
	private int START_PUNCH_INDEX;
	private int FIRST_SPLIT_INDEX;
	private int FIRSTNAME_INDEX;

	private static final char[] DELIMITERS = {';', ',', '\\', '\t'};

	protected EventResults event;
	private AgeClass ageClass = null;
	private Course course = null;
	private String delimiter;

	private boolean isOE2002OrLater;

	/**
	 * Constructor for the CSVEventLoader object
	 */
	public SICSVEventLoader() {
		super();
	}

	/**
	 * Description of the Method
	 * 
	 * @param base
	 *            Description of the Parameter
	 * @param fileName
	 *            Description of the Parameter
	 * @param urlInput
	 *            Description of the Parameter
	 * @param zipped
	 *            Is the file zipped
	 * @param byCourse
	 *            Description of the Parameter
	 * @exception IOException
	 *                Description of the Exception
	 * @exception Exception
	 *                Description of the Exception
	 */
	public void loadEvent(EventResults newEvent, String fileName,
			boolean urlInput, int newbyCourse) throws IOException,
			SplitsbrowserException {
		event = newEvent;

		BufferedReader reader = openReader(fileName, urlInput);

		// Read course info
		int lineCount = 0;
		String oldCourseName = "";
		String oldAgeClassName = "";

		try {
			// Read header line and get columns to use
			String line = reader.readLine();
			lineCount++;

			setDelimiter(line);
			setFileFormat(line);
			setColumnsIndices();

			line = reader.readLine();
			lineCount++;

			while (line != null) {
				// Tokenise string
				String[] tokens = getTokens(line);

				// Name
				String name;

				if (isOE2002OrLater) {
					String surname = stripQuotes(tokens[NAME_INDEX]);
					String firstname = stripQuotes(tokens[FIRSTNAME_INDEX]);
					name = firstname + " " + surname;
				} else {
					name = tokens[NAME_INDEX];
					name = stripQuotes(name);
				}

				// Club
				String club = tokens[CLUB_INDEX];
				club = stripQuotes(club);

				// Course
				String courseName = tokens[COURSE_INDEX];
				courseName = stripQuotes(courseName);

				// Distance
				float distance = getFloat(tokens[DISTANCE_INDEX]);

				// Climb
				float climb = getFloat(tokens[CLIMB_INDEX]);

				// num controls
				String strNumControls = tokens[NUM_CONTROLS_INDEX];
				int numControls = getInt(strNumControls);

				// Start time
				Time startTime = parseTime(tokens[START_TIME_INDEX]);

				// If a punching start is set use this instead
				Time punchingStartTime = parseTime(tokens[START_PUNCH_INDEX]);

				if (punchingStartTime.isValid()) {
					startTime = punchingStartTime;
				}

				// Splits
				Time[] totalTimes = new Time[numControls + 1];
				boolean validRun = true;

				for (int i = 0; i < numControls; i++) {
					int index = FIRST_SPLIT_INDEX + (i * 2) + 1;
					if (index < tokens.length) {
						totalTimes[i] = parseTime(tokens[index]);
					} else {
						totalTimes[i] = Time.INVALIDTIME;
					}

					if (!totalTimes[i].isValid()) {
						validRun = false;
					}
				}

				// Finish time
				if (TOTAL_TIME_INDEX < tokens.length) {
					totalTimes[numControls] = parseTime(tokens[TOTAL_TIME_INDEX]);
				} else {
					totalTimes[TOTAL_TIME_INDEX] = Time.INVALIDTIME;
				}

				// Course
				if (!courseName.equals(oldCourseName)) {
					course = new Course(courseName, numControls, distance,
							climb);

					// set the control codes
					for (int i = 0; i < numControls; i++) {
						int index = FIRST_SPLIT_INDEX + (i * 2);
						String controlCode = tokens[index].trim();

						if (controlCode != null) {
							course.addControlCode(i + 1, controlCode);
						}
					}

					course = event.addCourse(course);
					oldCourseName = courseName;
				}

				// age class
				String ageClassName = tokens[CLASS_INDEX];
				ageClassName = stripQuotes(ageClassName);

				if (!ageClassName.equals(oldAgeClassName)) {
					ageClass = new AgeClass(ageClassName);
					ageClass = course.addAgeClass(ageClass);
					oldAgeClassName = ageClassName;
				}

				// Create the result
				Result result = new Result(name, club, course, ageClass,
						totalTimes, startTime, validRun);
				ageClass.addResult(result);

				// Read the next results line
				lineCount++;
				line = reader.readLine();
			}
		} catch (Exception e) {
			throw new SplitsbrowserException(Message.get("Loader.Error",
					new Object[]{fileName, new Integer(lineCount).toString()})
					+ e.toString());
		}
	}

	/**
	 * Sets the column indicies to use based on the file format
	 */
	private void setColumnsIndices() {
		if (isOE2002OrLater) {
			NAME_INDEX = 3;
			START_TIME_INDEX = 9;
			TOTAL_TIME_INDEX = 11;
			CLUB_INDEX = 15;
			CLASS_INDEX = 18;
			COURSE_INDEX = 39;
			DISTANCE_INDEX = 40;
			CLIMB_INDEX = 41;
			NUM_CONTROLS_INDEX = 42;
			START_PUNCH_INDEX = 44;
			FIRST_SPLIT_INDEX = 46;
			FIRSTNAME_INDEX = 4;
		} else {
			NAME_INDEX = 3;
			START_TIME_INDEX = 7;
			TOTAL_TIME_INDEX = 9;
			CLUB_INDEX = 13;
			CLASS_INDEX = 16;
			COURSE_INDEX = 37;
			DISTANCE_INDEX = 38;
			CLIMB_INDEX = 39;
			NUM_CONTROLS_INDEX = 40;
			START_PUNCH_INDEX = 42;
			FIRST_SPLIT_INDEX = 44;
			FIRSTNAME_INDEX = -1; // Set to -1 so it will raise an error is it
								  // is used.
		}
	}

	/**
	 * Determines the character used to delimit fields in the file by inspecting
	 * the header line.
	 * 
	 * @param SI
	 *            CSV header line
	 * @return delimiter character
	 */
	private void setDelimiter(String line) {
		char cdelimiter = '\0';
		int i = 0;

		do {
			for (int idelim = 0; idelim < DELIMITERS.length; idelim++) {
				if (line.charAt(i) == DELIMITERS[idelim]) {
					cdelimiter = DELIMITERS[idelim];

					break;
				}
			}

			i++;
		} while ((i < line.length()) && (cdelimiter == '\0'));

		delimiter = String.valueOf(cdelimiter);
	}

	/**
	 * Converts a string to a floating point number. If the string is not a
	 * valid floating point number then 0 is returned
	 * 
	 * @param s
	 *            Input string
	 * @return Converted floating point number or 0 if number could not be
	 *         parsed
	 */
	private float getFloat(String s) {
		float f = 0;

		if (s == null) {
			f = 0;
		} else {
			try {
				f = Float.valueOf(s).floatValue();
			} catch (NumberFormatException e) {
				f = 0;
			}
		}

		return (f);
	}

	private int getInt(String s) {
		if ((s == null) || (s.length() == 0)) {
			return (0);
		} else {
			return (Integer.parseInt(s));
		}
	}

	/**
	 * Sets the file Format.
	 * 
	 * The default format for SportIdent CSV output changed in 2003. The name
	 * was separated into surname and firstname. The first name colum headings
	 * defined in the the SportIdent translation file, OEinzel.mlf, are unique.
	 * 
	 *  @param line headerLine header line in the SportIdent csv file
	 */
	private void setFileFormat(String headerLine) {

		isOE2002OrLater = (headerLine.indexOf("First name") != -1) || // English
				(headerLine.indexOf("Förnamn") != -1) || // Swedish
				(headerLine.indexOf("Prénom") != -1) || // French
				(headerLine.indexOf("Nome") != -1) || // Italian
				(headerLine.indexOf("Jméno (køest.)") != -1) || // Chetz
				(headerLine.indexOf("Utónév") != -1) || // Magyar
				(headerLine.indexOf("Fornavn") != -1) || // Danish
				(headerLine.indexOf("Imiê") != -1) || // Polish
				(headerLine.indexOf("Ime") != -1) || // Hungerian
				(headerLine.indexOf("Nombre") != -1) ||  // Spanish
				(headerLine.indexOf("Vorname") != -1) ; // Austrian
	} 
	
	/**
	 * Tokenised string based on the current delimiter set. Repeated delimiters
	 * are returned as a space character.
	 *  
	 * @param line
	 * @return array of tokens
	 */
	private String[] getTokens(String line) {
		StringTokenizer st = new StringTokenizer(line, delimiter, true);

		String[] tokens = new String[st.countTokens()];

		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = " ";
		}

		int tokenindex = 0;

		while (st.hasMoreTokens()) {
			String t = st.nextToken();

			if (t.equals(delimiter)) {
				tokenindex++;
			} else {
				tokens[tokenindex] = t;
			}
		}

		return (tokens);
	}

	private Time parseTime(String s) {
		Time t;

		try {
			t = new Time(s);
		} catch (Exception e) {
			t = Time.INVALIDTIME;
		}

		return (t);
	}

	/**
	 * Strips all double quotes character from a string.
	 * 
	 * @param s
	 *            Input string
	 * @return String with quotes stripped
	 */
	private String stripQuotes(String s) {
		StringBuffer output = new StringBuffer();

		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != '\"') {
				output.append(s.charAt(i));
			}
		}

		return output.toString();
	}
}
