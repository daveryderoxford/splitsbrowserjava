/*
 *  Splitsbrowser Statistics.java.
 *
 *  Copyright (C) 2002  Dave Ryder
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
 * Created:    Dave Ryder 2 November 2002
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:40:18 $
 * Changed by: $Author: daveryder $
 */
package org.splitsbrowser.util;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Statistics extends TreeMap {
    //     member-variables              

    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 2323285276760466962L;

	/** an <CODE>Integer</CODE> containing the value <VAR>1</VAR>. */
    public static final Integer ONE = new Integer(1);

    /** the largest value stored in the list of entered values. */
    protected double maximumValue = Double.MIN_VALUE;

    /** the smallest value stored in the list of entered values. */
    protected double minimumValue = Double.MAX_VALUE;

    /** the average of all the entered values. */
    protected double mu = 0.0;

    /** the standard deviation. */
    protected double sigma = 0.0;

    /** the skewness. */
    protected double skew = 0.0;

    /** the number of entered values. */
    protected int n = 0;

    /**
     * Returns the frequency of a value in the list.
     *
     * @return  a frequency
     */
    public int getFrequency(double key) {
        Integer i = (Integer) get(new Double(key));

        if (i == null) {
            return 0;
        }

        return i.intValue();
    }

    /**
     * Returns the width of a representative interval in the list.
     */
    public double getIntervalWidth() {
        if (size() == 0) {
            return 0;
        }

        return getRange() / Math.sqrt(n);
    }

    /**
     * Returns the maximum value in the list.
     */
    public double getMaximumValue() {
        return maximumValue;
    }

    /**
     * Returns the average value of the list.
     */
    public double getMean() {
        return mu;
    }

    /**
     * Returns the minimum value in the list.
     */
    public double getMinimumValue() {
        return minimumValue;
    }

    /**
     * Returns the frequency as a percentage.
     *
     * @return  a percentage
     */
    public double getPercentage(double key) {
        return (100.0 * getFrequency(key)) / n;
    }

    /**
     * Calculates the percentile corresponding with a certain value.
     *
     * @param   a value
     * @return  the corresponding percentile
     */
    public double getPercentile(double value) {
        return percentageBelow(value) + (getPercentage(value) / 2);
    }

    /**
     * Returns the range of the list.
     */
    public double getRange() {
        return maximumValue - minimumValue;
    }

    /**
     * Returns the standard deviation of the list.
     */
    public double getSigma() {
        return sigma;
    }

    /**
     * Returns the skewness of the list.
     */
    public double getSkewness() {
        return skew;
    }

    /**
     * Adds a double to the list of discrete values.
     *
     * @param   double  a value to add
     * @return  the total number of values after adding this one.
     */
    public int add(double value) {
        synchronized (ONE) {
            this.insert(new Double(value));
            compute();
        }

        return n;
    }

    /**
     * Inserts a list of <CODE>Double</CODE>s.
     *
     * @param   i   an <CODE>Iterator</CODE> over a list of <CODE>Double</CODE>s.
     * @return  <CODE>void</CODE>
     */
    public void add(Iterator i) {
        synchronized (ONE) {
            while (i.hasNext()) {
                try {
                    insert((Double) i.next());
                } catch (ClassCastException cce) {
                    // do nothing
                }
            }

            compute();
        }
    }

    /**
     * The method <CODE>put</CODE> has to be overriden, so that only objects of the type <CODE>Double</CODE> can be
     * entered.
     *
     * @param   key     the key of the object to add to the <CODE>TreeMap</CODE>
     * @param   value   the value of the object to add to the <CODE>TreeMap</CODE>
     * @return  the previous value corresponding with the given key
     */
    public Object put(Object key, Object value) {
        try {
            synchronized (ONE) {
                insert((Double) key);
                compute();
            }

            return get(key);
        } catch (ClassCastException cce) {
            return null;
        }
    }

    /**
     * The method <CODE>putAll</CODE> has to be overriden, so that only objects of the type <CODE>Double</CODE> can be
     * entered.
     *
     * @param   map     a Map containing keys and values of objects to add to this <CODE>TreeMap</CODE>
     * @return  <CODE>void</CODE>
     */
    public void putAll(Map map) {
        synchronized (ONE) {
            Double key;
            int value;

            for (Iterator iterator = map.keySet().iterator();
                     iterator.hasNext();) {
                try {
                    key = (Double) iterator.next();
                    value = ((Integer) map.get(key)).intValue();

                    for (int i = 0; i < value; i++) {
                        insert(key);
                    }
                } catch (ClassCastException cce) {
                    // do nothing
                }
            }

            compute();
        }
    }

    /**
     * Returns an estimation the value corresponding with a given percentile.
     *
     * @param   a percentile
     * @return  a value
     */
    public double value(double percentile) {
        double valueBelow = minimumValue;
        double percentileBelow = getPercentile(minimumValue);

        if (percentile <= percentileBelow) {
            return minimumValue;
        }

        double valueAbove = maximumValue;
        double percentileAbove = getPercentile(valueAbove);

        if (percentile > percentileAbove) {
            return maximumValue;
        }

        for (Iterator i = keySet().iterator(); i.hasNext();) {
            valueAbove = ((Double) i.next()).doubleValue();
            percentileAbove = getPercentile(valueAbove);

            if ((percentileBelow <= percentile) &&
                    (percentile < percentileAbove)) {
                double fraction =
                    (percentile - percentileBelow) / (percentileAbove -
                    percentileBelow);

                return valueBelow + (fraction * (valueAbove - valueBelow));
            }

            valueBelow = valueAbove;
            percentileBelow = percentileAbove;
        }

        return valueAbove;
    }

    /**
     * Everytime a new double is added to the list of discrete values,
     * the changes have to be computed.
     *
     * @return  <CODE>void</CODE>
     */
    private void compute() {
        mu = 0.0;
        sigma = 0.0;
        skew = 0.0;

        Double key;
        Integer value;

        for (Iterator i = keySet().iterator(); i.hasNext();) {
            key = (Double) i.next();
            value = (Integer) get(key);
            mu += ((key.doubleValue() * value.intValue()) / n);
        }

        for (Iterator i = keySet().iterator(); i.hasNext();) {
            key = (Double) i.next();
            value = (Integer) get(key);
            sigma += ((Math.pow(key.doubleValue() - mu, 2.0) * value.intValue()) / n);
            skew += ((Math.pow(key.doubleValue() - mu, 3.0) * value.intValue()) / n);
        }

        sigma = Math.sqrt(sigma);
        skew = skew / (Math.pow(sigma, 3.0));
    }

    /**
     * Returns the total number of values below a given value.
     *
     * @return  a frequency
     */
    private int frequencyBelow(double key) {
        int f = 0;
        Double keyBelow;
        Integer value;

        for (Iterator i = keySet().iterator(); i.hasNext();) {
            keyBelow = (Double) i.next();

            if (keyBelow.doubleValue() < key) {
                value = (Integer) get(keyBelow);
                f += value.intValue();
            } else {
                return f;
            }
        }

        return f;
    }

    /**
     * Returns the total number of values between two given values.
     *
     * @return  a frequency
     */
    private int frequencyBetween(double key1, double key2) {
        int f = 0;

        if (key1 > key2) {
            double tmp = key2;
            key2 = key1;
            key1 = tmp;
        }

        if (key2 > maximumValue) {
            key2 = maximumValue;
            f += getFrequency(key2);
        }

        double key;

        for (Iterator i = keySet().iterator(); i.hasNext();) {
            key = ((Double) i.next()).doubleValue();

            if (key >= key2) {
                return f;
            } else if (key >= key1) {
                f += getFrequency(key);
            }
        }

        return f;
    }

    //     methods

    /**
     * Inserts a value into the sorted list of values.
     *
     * @param   value   the <CODE>Double</CODE> that has to be inserted
     * @return  the number of values entered sofar
     */
    private int insert(Double key) {
        if (containsKey(key)) {
            Integer i = new Integer(((Integer) get(key)).intValue() + 1);
            super.put(key, i);
        } else {
            super.put(key, ONE);
        }

        if (maximumValue < key.doubleValue()) {
            maximumValue = key.doubleValue();
        }

        if (minimumValue > key.doubleValue()) {
            minimumValue = key.doubleValue();
        }

        return ++n;
    }

    /**
     * Returns the total percentage of values below a given value.
     *
     * @return  a percentage
     */
    private double percentageBelow(double key) {
        return (100.0 * frequencyBelow(key)) / n;
    }

}
