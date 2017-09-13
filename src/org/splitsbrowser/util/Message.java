package org.splitsbrowser.util;
/*
 *  Splitsbrowser - Message class
 *
 *  (c) Dave Ryder, October 2002
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
 * Created:    Dave Ryder
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/08/25 06:31:56 $
 * Changed by: $Author: daveryder $
 */
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *  This class handles internationalisation.
 *
 * @author     Dave Ryder
 */
public class Message {

    private static final String BUNDLE_NAME = "splitsbrowser"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle(BUNDLE_NAME);


      /**  Constructor for the Message object */
    private Message() {
    }

    /**
     * Return a simple message string without parameters
     *
     * @param  msgID  The message identifier
     * @return        The message string
     */
    public static String get(String msgID) {
        return getString(msgID);
    }

    /**
     * Return a formatted message whose first parameter is a String
     *
     * @param  msgID  The message identifier
     * @param  arg0   The String parameter
     * @return        A formatted message
     */
    public static String get(String msgID, String arg0) {
        Object[] args = new Object[1];

        args[0] = arg0;

        MessageFormat msgFmt = new MessageFormat(getString(msgID));

        return msgFmt.format(args);
    }

    /**
     * Return a formatted message whose first parameter is a String and second parameter is an integer
     *
     * @param  msgID  The message identifier
     * @param  arg0   The String parameter
     * @param  arg1   The int parameter
     * @return        A formatted message
     */
    public static String get(String msgID, String arg0, int arg1) {
        Object[] args = new Object[2];

        args[0] = arg0;
        args[1] = new Integer(arg1);

        MessageFormat msgFmt = new MessageFormat(getString(msgID));

        return msgFmt.format(args);
    }

    /**
     *  Description of the Method
     *
     * @param  msgID  Description of the Parameter
     * @param  arg0   Description of the Parameter
     * @return        Description of the Return Value
     */
    public static String get(String msgID, int arg0) {
        Object[] args = new Object[1];

        args[0] = new Integer(arg0);

        MessageFormat msgFmt = new MessageFormat(getString(msgID));

        return msgFmt.format(args);
    }

    /**
     *  Description of the Method
     *
     * @param  msgID  Description of the Parameter
     * @param  args   Description of the Parameter
     * @return        Description of the Return Value
     */
    public static String get(String msgID, Object[] args) {
        MessageFormat msgFmt = new MessageFormat(getString(msgID));

        return msgFmt.format(args);
    }

    /**
     * Get a message string given the message identifier
     */
    private static String getString(String msgID) {

            try {
                return RESOURCE_BUNDLE.getString(msgID);
            } catch (MissingResourceException e) {
                return '!' + msgID + '!';
            }
    }
}
