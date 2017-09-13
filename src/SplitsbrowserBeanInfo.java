/*
 *    Splitsbrowser - BeanInfo
 *
 *    Copyright (C) 2000  Dave Ryder
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
 * Created:    Dave Ryder
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/08/25 06:31:56 $
 * Changed by: $Author: daveryder $
 */

/**
 * A dummy BeanInfo class for the WinRes Applet
 * This is only required to get fround a feature of
 * Internet Explorer which produces intermittent errors if a BeanInfo
 * class is not provided for an applet.
 *
 * @version    $Revision: 1.1 $
 */
import java.beans.SimpleBeanInfo;

public class SplitsbrowserBeanInfo extends SimpleBeanInfo {
}
