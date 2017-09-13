package org.splitsbrowser.util;
/*
 *  Splitsbrowser - Compare interface.
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
 * Created:    Dave Ryder  May 7, 2003
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:40:18 $
 * Changed by: $Author: daveryder $
 */
public interface ICompare {
    public boolean lessThan(Object a, Object b);
}
