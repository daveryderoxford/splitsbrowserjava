package org.splitsbrowser.model;
/*
 *  Splitsbrowser SplitsbrowserException.java.
 *
 *  Copyright (C) 2003 Dave Ryder
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
 * Created:    Dave Ryder - Jun 18, 2003
 * Version:    $Revision: 1.2 $
 * Changed:    $Date: 2003/09/18 19:29:40 $
 * Changed by: $Author: daveryder $
 */

/**
 *  Exception raised by Splitsbrowser application
 */
public class SplitsbrowserException extends Exception {
    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 4372828708575960259L;

	public SplitsbrowserException(String msg) {
        super(msg);
    }
}
