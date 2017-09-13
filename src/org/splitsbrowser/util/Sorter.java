package org.splitsbrowser.util;
/*
 *  Splitsbrowser - Sorter class
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
 *  the Free Software Foundation, Inc.,show 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
/*
 * Version control info - Do not edit
 * Created:    Reinhard Balling
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:40:18 $
 * Changed by: $Author: daveryder $
 */

/**
 *  Static sort object can sort an array of object using a variable 'lessThan' function
 *
 * @author  Reinhard Balling
 */
public class Sorter {
    /**  Constructor for the Sorter object */
    public Sorter() {
    }

    /**
     *  Simple Quicksort
     *
     * @param  a   Array of objects to sort
     * @param  l   Index of first object to sort (usually 0)
     * @param  r   Index of last object to sort (usually 'a.length-1')
     * @param  lt  'lessThan' function used to compare and order elements
     */
    public static void Sort(Object[] a, int l, int r, ICompare lt) {
        int i;
        int last;

        if (l >= r) {
            return;
        }

        swap(a, l, (l + r) / 2);
        last = l;

        for (i = l + 1; i <= r; i++) {
            if (lt.lessThan(a[i], a[l])) {
                swap(a, ++l, i);
            }
        }

        swap(a, l, last);
        Sort(a, l, last - 1, lt);
        Sort(a, last + 1, r, lt);
    }

    private static void swap(Object[] a, int i, int j) {
        Object T;

        T = a[i];
        a[i] = a[j];
        a[j] = T;
    }
}
