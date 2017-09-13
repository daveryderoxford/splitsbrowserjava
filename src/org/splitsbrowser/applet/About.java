/*
 *    Splitsbrowser - About class.
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
 * Created:    Keith Ryder
 * Version:    $Revision: 1.2 $
 * Changed:    $Date: 2003/08/24 18:35:50 $
 * Changed by: $Author: daveryder $
 */
 
package org.splitsbrowser.applet;

/**
 *  Contains Information describing the project and its version
 */
public class About {
    private static String NAME = "Splitsbrowser";
    private static String VERSION = "V2.1.4";
    private static String Copyright = "D Ryder/R Balling 2003";
    private static String ABOUT =
        "Orienteering results analysis\n" + VERSION + "\n" + "Copyright " +
        Copyright + "\n \nHomepage:  www.splitsbrowser.org.uk";

    public About() {
    }

    /**
     *  Returns a version string
     * @return The version string
     */
    public static String Version() {
        return NAME + " " + VERSION;
    }

    /**
     * Returns an information string
     * @return The information string
     */
    public static String about() {
        return ABOUT;
    }

    /**
     * Returns the name of the programme
     * @return The name of the programme
     */
    public static String name() {
        return NAME;
    }
}
