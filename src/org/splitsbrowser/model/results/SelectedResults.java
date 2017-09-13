/*
 *  Splitsbrowser - SelectedResults.
 *
 *  Copyright (C) 2002  Reinhard Balling
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
import org.splitsbrowser.util.*;

/**
 * This class manipulates the results for the classes selected in splitsGraph
 * <br>
 * Multiple classes sharing the same course can be selected and compared <br>
 * 
 * @author Reinhard Balling
 */
public class SelectedResults {
	private static LessThanResult lt = new LessThanResult();

	/** The optimum result, i.e. consisting of best split times for each leg */
	private Result optimum = null;

	/** The fastest time +100% */
	private Result optimum100 = null;

	/** The optimum time +25% */
	private Result optimum25 = null;

	/** The fastest time +5% */
	private Result optimum5 = null;

	/** The fastest time +50% */
	private Result optimum50 = null;

	/** The fastest time +75% */
	private Result optimum75 = null;

	/**
	 * The currently selected reference runner: Can be any of the above or any
	 * runner with a valid run
	 */
	private Result reference = null;

	/** The selected AgeClasses */
	private Vector ageClasses = new Vector(5);

	/**
	 * The results for the selected classes (which must all be sharing the same
	 * course)
	 */
	private Result[] results = new Result[0];

	/** Number of controls */
	private int numControls = 0;

	/** Constructor for the SelectedResults object */
	public SelectedResults() {
	}

	/**
	 * Gets the ageClasses attribute of the SelectedResults class
	 * 
	 * @return The ageClasses value
	 */
	public Vector getAgeClasses() {
		return ageClasses;
	}

	/**
	 * Gets the number of selected results
	 * 
	 * @return The number of results in the list of selected results
	 */
	public int getNumResults() {
		return results.length;
	}

	/**
	 * Sets the reference result:
	 * 
	 * @param i
	 *            The number of the reference result to select <br>
	 *            -6 = Optimum +100% <br>
	 *            -5 = Optimum +75% <br>
	 *            -4 = Optimum +50% <br>
	 *            -3 = Optimum +25% <br>
	 *            -2 = Optimum +5% <br>
	 *            -1 = Optimum (fastest) time <br>
	 *            0 = Winner <br>
	 *            1 ff = remaining runners <br>
	 */
	public void setReferenceResult(int i) {
		if (results.length == 0) {
			return;
		}

		switch (i) {
			case -6 :
				reference = optimum100;

				break;

			case -5 :
				reference = optimum75;

				break;

			case -4 :
				reference = optimum50;

				break;

			case -3 :
				reference = optimum25;

				break;

			case -2 :
				reference = optimum5;

				break;

			case -1 :
				reference = optimum;

				break;

			default :
				reference = results[i];

				break;

		// i=0 => winner, i=1 => second place etc.
		}
	}

	/**
	 * Gets the current reference result
	 * 
	 * @return The reference result
	 */
	public Result getReferenceResult() {
		return reference;
	}

	/**
	 * Gets a particular result
	 * 
	 * @param i
	 *            Index into the list of selected results
	 * @return The result
	 */
	public Result getResult(int i) {
		return results[i];
	}

	/**
	 * Add all results for this ageclass to the list of selected results
	 * 
	 * @param ageClass
	 *            The {@link AgeClass AgeClass}for which the results are to be
	 *            added
	 */
	public void addClass(AgeClass ageClass) {
		int size = results.length;
		Result[] newResults = new Result[size + ageClass.getNumResults()];

		for (int i = 0; i < size; i++) {
			newResults[i] = results[i];
		}

		for (int i = ageClass.getNumResults() - 1; i >= 0; i--) {
			newResults[size + i] = ageClass.getResult(i);
		}

		results = newResults;
		numControls = results[0].getNumControls();
		newResults = null;
		ageClasses.addElement(ageClass);
	}

	/**
	 * Calculates the winner's time, the optimum time and the other possible
	 * reference times
	 */
	public void calcOptimumTimes() {
		Course course;

		if (results.length == 0) {
			return;
		}

		course = results[0].getCourse();
		optimum = new Result(numControls, course);
		optimum5 = new Result(numControls, course);
		optimum25 = new Result(numControls, course);
		optimum50 = new Result(numControls, course);
		optimum75 = new Result(numControls, course);
		optimum100 = new Result(numControls, course);

		Time accumulatedBestSplits = new Time(0, true);

		for (int i = 0; i <= numControls; i++) {
			if (course.isValidControl(i)) {
				Time currentMin = Time.MAXTIME;

				for (int j = 0; j < results.length; j++) {
					Time time = results[j].getSplitTime(i);

					if (time.isValid() && time.lessThan(currentMin)) {
						currentMin = time;
					}
				}

				if (currentMin != Time.MAXTIME) {
					accumulatedBestSplits = accumulatedBestSplits
							.add(currentMin);
				}
			}

			optimum.setTime(i, accumulatedBestSplits);

			Time opt = optimum.getTime(i);

			optimum5.setTime(i, (int) Math.min(opt.asSeconds() * 1.05,
					Time.MAXTIME.asSeconds()), opt.isValid());
			optimum25.setTime(i, (int) Math.min(opt.asSeconds() * 1.25,
					Time.MAXTIME.asSeconds()), opt.isValid());
			optimum50.setTime(i, (int) Math.min(opt.asSeconds() * 1.50,
					Time.MAXTIME.asSeconds()), opt.isValid());
			optimum75.setTime(i, (int) Math.min(opt.asSeconds() * 1.75,
					Time.MAXTIME.asSeconds()), opt.isValid());
			optimum100.setTime(i, (int) Math.min(opt.asSeconds() * 2.00,
					Time.MAXTIME.asSeconds()), opt.isValid());
		}

		optimum.setValid(accumulatedBestSplits.isValid());
		optimum25.setValid(results[0].isValid());
		optimum50.setValid(results[0].isValid());
		optimum75.setValid(results[0].isValid());
		optimum100.setValid(results[0].isValid());
	}

	/**
	 * Calculate positions after each leg and leg positions
	 */
	public void calcPositions() {
		if (results.length == 0) {
			return;
		}

		int numControls = results[0].getNumControls();
		Time lastTime;
		int lastPos;

		for (int j = 0; j <= numControls; j++) {
			// Leg position
			for (int i = results.length - 1; i >= 0; i--) {
				if (results[i].getSplitTime(j).isValid()) {
					results[i].setSortTime(results[i].getSplitTime(j));
				} else {
					results[i].setSortTime(Time.MAXTIME);
				}
			}

			Sorter.Sort(results, 0, results.length - 1, lt);

			// Set position
			lastTime = Time.ZEROTIME;
			lastPos = 1;

			for (int i = 0; i < results.length; i++) {
				if (!results[i].getSortTime().isValid()) {
					results[i].setSplitPos(j, -1);
				} else {
					// Handle ex aequo results
					if (results[i].getSortTime().asSeconds() == lastTime
							.asSeconds()) {
						results[i].setSplitPos(j, lastPos);
					} else {
						results[i].setSplitPos(j, i + 1);
						lastPos = i + 1;
						lastTime = results[i].getSortTime();
					}
				}
			}
		}

		// If a runner is disqualified, we will only include total positions for
		// him up to the last valid control
		// We therefore need to keep track of how far we are in sorttime. We do
		// this by looking at sorttime for the
		// previous split: is it invalid, then it can never become valid again
		// for any of the remaining splits
		for (int i = results.length - 1; i >= 0; i--) {
			results[i].setSortTime(Time.ZEROTIME);
		}

		for (int j = 0; j <= numControls; j++) {
			// Total position
			for (int i = results.length - 1; i >= 0; i--) {
				if (((!results[i].getSortTime().isValid()) && (!results[i]
						.isValid()))
						|| (!results[i].getTime(j).isValid())) {
					results[i].setSortTime(Time.INVALIDTIME);
				} else {
					results[i].setSortTime(results[i].getTime(j));
				}
			}

			Sorter.Sort(results, 0, results.length - 1, lt);

			// Set position
			lastTime = Time.ZEROTIME;
			lastPos = 1;

			for (int i = 0; i < results.length; i++) {
				if (results[i].getSortTime().isValid()) {
					// Handle ex aequo results
					if (results[i].getSortTime().asSeconds() == lastTime
							.asSeconds()) {
						results[i].setTotalPos(j, lastPos);
					} else {
						results[i].setTotalPos(j, i + 1);
						lastPos = i + 1;
						lastTime = results[i].getSortTime();
					}
				} else {
					results[i].setTotalPos(j, -1);
				}
			}
		}
	}

	/**
	 * Remove all results and AgeClasses
	 */
	public void removeAll() {
		results = new Result[0];
		numControls = 0;
		ageClasses.removeAllElements();
	}

	/**
	 * Gets the n fastest results for a specified leg sorted into
	 * 
	 * @param maxNumber
	 *            Maximum number of results to get
	 * @param split
	 *            Split number to get the times for
	 * @return Array of fastest results for the specified split in increasing
	 *         time order If there are less competitors than maxNumber then an
	 *         array
	 */
	public Result[] getFastestResults(int maxNumber, int split) {
		int numResults = Math.min(maxNumber, results.length);
		Result[] fastestResults = new Result[numResults];

		int found = 0;
		int iresult = 0;
		while (found < numResults) {
			int pos = results[iresult].getSplitPos(split);

			if (pos <= numResults) {
				// Handle runners with equal positions
				while ((fastestResults[pos - 1] != null) && (pos < numResults)) {
					pos = pos + 1;
				}

				if (fastestResults[pos - 1] == null) {
					fastestResults[pos - 1] = results[iresult];
					found++;
				}

			}
			iresult++;
		}

		return (fastestResults);
	}

	/**
	 * Sort the selected results by finish time (used to fill the list box)
	 */
	public void sortByFinishTime() {
		if (results.length == 0) {
			return;
		}

		int numControls = results[0].getNumControls();

		for (int i = results.length - 1; i >= 0; i--) {
			results[i].setSortTime(results[i].getTime(numControls));

			if (!results[i].isValid()) {
				results[i].setSortTime(Time.MAXTIME);
			}
		}

		Sorter.Sort(results, 0, results.length - 1, lt);
	}

	private static class LessThanResult implements ICompare {
		public boolean lessThan(Object a, Object b) {
			return ((Result) a).getSortTime().asSeconds() < ((Result) b)
					.getSortTime().asSeconds();
		}
	}
}
