/*
 *  Splitsbrowser - EventLoader base class
 *
 *  Original Copyright (c) 2000  Dave Ryder
 *  Version 2 Copyright (c) 2002 Reinhard Balling
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
 * Created:    Dave Ryder/Reinheart Balling
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:33:13 $
 * Changed by: $Author: daveryder $
 */
package org.splitsbrowser.model.results.io;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.splitsbrowser.model.SplitsbrowserException;
import org.splitsbrowser.model.results.EventResults;

/**
 * Eventloader - Base class for loading event results and start times
 * Refactored from CSVEventLoader
 *
 * @version $Revision: 1.1 $
 */
public abstract class EventLoader {

    /**
     *  Abstract method for loading event data
     *
     * @param fileName                Local filename or URL
     * @param urlInput                True if fileName is a URL, false if it is a local filename
     * @param zipped                 True if file is a ZIP archive
     * @param  byCourse    True if results are by Course rather than (default) by class
     * @exception  IOException  Error when reading file
     * @exception  Exception    Error when opening file
     */
    public abstract void loadEvent(EventResults newEvent, String fileName, boolean urlInput,
                          int byCourse) throws IOException, SplitsbrowserException;

    /**
     * Open a file for input
     * @param fileName  Local filename or URL
     * @param urlInput  True if fileName is a URL, false if it is a local filename
     * @param zipped True if file is a ZIP archive
     * @return A BufferedReader object
     */
    protected BufferedReader openReader(String fileName, boolean urlInput)
                                 throws IOException
    {
        InputStream inputStream;
        BufferedReader reader;

        try {
            if (urlInput) {
                URL myURL = new URL(fileName);
                DataInputStream stream =
                    new DataInputStream(myURL.openStream());

                inputStream = stream;
            } else {
                FileInputStream stream = new FileInputStream(fileName);
                inputStream = stream;
            }

            String extension = getExtension(fileName);

            if (extension.equals("zip")) {
                ZipInputStream zippedStream = new ZipInputStream(inputStream);

                // Move to the first file entry
                zippedStream.getNextEntry();
                reader =
                    new BufferedReader(new InputStreamReader(zippedStream));
            } else if (extension.equals("gz")) {
                GZIPInputStream zippedStream = new GZIPInputStream(inputStream);
                reader =
                    new BufferedReader(new InputStreamReader(zippedStream));
            } else if (extension.startsWith("php")) { //$NON-NLS-1$

                GZIPInputStream zippedStream = new GZIPInputStream(inputStream);
                reader =
                    new BufferedReader(new InputStreamReader(zippedStream));
            } else {
                reader = new BufferedReader(new InputStreamReader(inputStream));
            }
        } catch (Exception e) {
           // TO Sort out internationaliatiom 
            throw (new IOException("Loader.OpenError" + fileName));
           // throw (new IOException(Message.get("Loader.OpenError", fileName)));
        }

        return (reader);
    }

    private String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');

        if ((1 > 0) && (i < (fileName.length() - 1))) {
            return fileName.substring(i + 1).toLowerCase();
        } else {
            return "";
        }
    }
}
