package org.splitsbrowser.util;
/*
 *  Splitsbrowser - HTML utilities
 *
 *  Copyright (c) 2000  Dave Ryder
 *  Copyright (c) 2000  Ed Nash
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *  This  library is distributed in the hope that it will be useful,  but
 * WITHOUT ANY WARRANTY; without even the implied warranty of  MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  Library General Public
 * License for more details.
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
 * Changed:    $Date: 2003/09/18 19:40:18 $
 * Changed by: $Author: daveryder $
 */

/**
 * HTML utilities. Refactored from SIEventLoader by RB.
 *
 * @author    David Ryder
 */
public class HTMLutils {
    // HTML defined special characters and Unicode equivalents

    /**  Description of the Field */
    private static final String[] specialChars =
    {
        "&nbsp;", "&iexcl;", "&cent;", "&pound;", "&curren;", "&yen;",
        "&brvbar;", "&sect;", "&uml;", "&copy;", "&ordf;", "&laquo;", "&not;",
        "&shy;", "&reg;", "&macr;", "&deg;", "&plusmn;", "&sup2;", "&sup3;",
        "&acute;", "&micro;", "&para;", "&middot", "&cedil;", "&sup1;", "&ordm;",
        "&raquo;", "&frac14;", "&frac12;", "&frac34;", "&iquest;", "&Agrave;",
        "&Aacute;", "&Acirc;", "&Atilde;", "&Auml;", "&Aring;", "&AElig;",
        "&Ccedil;", "&Egrave;", "&Eacute;", "&Ecirc;", "&Euml;", "&Igrave;",
        "&Iacute", "&Icirc;", "&Iuml;", "&ETH;", "&Ntilde;", "&Ograve;",
        "&Oacute;", "&Ocirc;", "&Otilde;", "&Ouml;", "&times;", "&Oslash;",
        "&Ugrave;", "&Uacute;", "&Ucirc;", "&Uuml;", "&Yacute;", "&THORN;",
        "&szlig;", "&agrave;", "&aacute;", "&acirc;", "&atilde;", "&auml;",
        "&aring;", "&aelig;", "&ccedil;", "&egrave;", "&eacute;", "&ecirc;",
        "&euml;", "&igrave;", "&iacute;", "&icirc;", "&iuml;", "&eth;",
        "&ntilde;", "&ograve;", "&oacute;", "&ocirc;", "&otilde;", "&ouml;",
        "&divide;", "&oslash;", "&ugrave;", "&uacute;", "&ucirc;", "&uuml;",
        "&yacute;", "&thorn;", "&yuml;", "&quot;", "&amp;", "&lt;", "&gt;",
        "&OElig;", "&oelig;", "&Scaron;", "&scaron;", "&Yuml;", "&circ;",
        "&tilde;", "&ensp;", "&emsp;", "&thinsp;", "&zwnj;", "&zwj;", "&lrm;",
        "&rlm;", "&ndash;", "&mdash;", "&lsquo;", "&rsquo;", "&sbquo;",
        "&ldquo;", "&rdquo;", "&bdquo;", "&dagger;", "&Dagger;", "&permil;",
        "&lsaquo;", "&rsaquo;", "&euro;"
    };

    /**  Description of the Field */
    private static final char[] translatedChar =
    {
        '\u00a0', '\u00a1', '\u00a2', '\u00a3', '\u00a4', '\u00a5', '\u00a6',
        '\u00a7', '\u00a8', '\u00a9', '\u00aa', '\u00ab', '\u00ac', '\u00ad',
        '\u00ae', '\u00af', '\u00b0', '\u00b1', '\u00b2', '\u00b3', '\u00b4',
        '\u00b5', '\u00b6', '\u00b7', '\u00b8', '\u00b9', '\u00ba', '\u00bb',
        '\u00bc', '\u00bd', '\u00be', '\u00bf', '\u00c0', '\u00c1', '\u00c2',
        '\u00c3', '\u00c4', '\u00c5', '\u00c6', '\u00c7', '\u00c8', '\u00c9',
        '\u00ca', '\u00cb', '\u00cc', '\u00cd', '\u00ce', '\u00cf', '\u00d0',
        '\u00d1', '\u00d2', '\u00d3', '\u00d4', '\u00d5', '\u00d6', '\u00d7',
        '\u00d8', '\u00d9', '\u00da', '\u00db', '\u00dc', '\u00dd', '\u00de',
        '\u00df', '\u00e0', '\u00e1', '\u00e2', '\u00e3', '\u00e4', '\u00e5',
        '\u00e6', '\u00e7', '\u00e8', '\u00e9', '\u00ea', '\u00eb', '\u00ec',
        '\u00ed', '\u00ee', '\u00ef', '\u00f0', '\u00f1', '\u00f2', '\u00f3',
        '\u00f4', '\u00f5', '\u00f6', '\u00f7', '\u00f8', '\u00f9', '\u00fa',
        '\u00fb', '\u00fc', '\u00fd', '\u00fe', '\u00ff', '\u0022', '\u0026',
        '\u003c', '\u003e', '\u0152', '\u0153', '\u0160', '\u0161', '\u0178',
        '\u02c6', '\u02dc', '\u2002', '\u2003', '\u2009', '\u200c', '\u200d',
        '\u200e', '\u200f', '\u2013', '\u2014', '\u2018', '\u2019', '\u201a',
        '\u201c', '\u201d', '\u201e', '\u2020', '\u2021', '\u2030', '\u2039',
        '\u203a', '\u20ac'
    };

    HTMLutils() {
    }

    /**
     *  Description of the Method
     *
     * @param  buffer  Description of the Parameter
     * @return         Description of the Return Value
     */
    public static String translateString(String buffer) {
        // strip HTML representations of 'special' chars and replace with chars
        String returnString = buffer;
        int charStart;
        int charEnd;
        int charCode;
        String beforeChar;
        String afterChar;
        String charNum;
        Character newChar;

        // first look for &#nnn; or &#xnnnn etc. representation
        // and convert these to characters
        try {
            while ((charStart = returnString.indexOf("&#")) != -1) {
                if (charStart != 0) {
                    beforeChar = returnString.substring(0, charStart);
                } else {
                    beforeChar = "";
                }

                charEnd = returnString.indexOf(";", charStart);

                if ((charEnd == -1) || (charEnd > (charStart + 8))) {
                    charEnd = returnString.indexOf(" ", charStart);
                }

                charNum = returnString.substring(charStart + 2, charEnd);
                afterChar =
                    returnString.substring(charEnd + 1, returnString.length());

                if (charNum.indexOf("x") != -1) {
                    charCode = Integer.decode("0".concat(charNum)).intValue();
                } else {
                    charCode = Integer.decode(charNum).intValue();
                }

                newChar = new Character((char) charCode);
                returnString =
                    beforeChar.concat(newChar.toString()).concat(afterChar);
            }
        } catch (Exception e) {
            // leave the string unaltered & assume it's just dodgy html char encoding
        }

        // next do the defined 'specials'
        for (int i = 0; i < specialChars.length; i++) {
            while ((charStart = returnString.indexOf(specialChars[i])) != -1) {
                if (charStart != 0) {
                    beforeChar = returnString.substring(0, charStart);
                } else {
                    beforeChar = "";
                }

                afterChar =
                    returnString.substring(charStart +
                                           specialChars[i].length(),
                                           returnString.length());
                returnString =
                    beforeChar.concat(new Character(translatedChar[i]).toString())
                              .concat(afterChar);
            }
        }

        return (returnString);
    }
}
