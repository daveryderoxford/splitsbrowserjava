package tests;


/*
 *    Splitsbrowser - SIHTML tests.
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
 * Created:    Dave Ryder
 * Version:    $Revision: 1.4 $
 * Changed:    $Date: 2003/09/18 20:23:31 $
 * Changed by: $Author: daveryder $
 */
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.splitsbrowser.model.results.Debug;
import org.splitsbrowser.model.results.EventResults;
import org.splitsbrowser.model.results.io.SIEventLoader;

public class SIHTMLLoaderTests extends TestCase {
    static final String testDataDir =
        "C:\\Development\\splitsbrowser\\src\\tests\\si\\";

    public SIHTMLLoaderTests(String name) {
        super(name);
    }

    public void gzFile() throws IOException, Exception {
        String[] filename = { "46.gz" };

        for (int i = 0; i < filename.length; i++) {
            String file = testDataDir + filename[i];
            EventResults event = loadEvent(file);
            printEventStats(event);
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new SIHTMLLoaderTests("testSpecialFiles"));
//        suite.addTest(new SIHTMLLoaderTests("testStandardFiles"));
//        suite.addTest(new SIHTMLLoaderTests("testNationalities"));
//        suite.addTest(new SIHTMLLoaderTests("zippedFile"));
//        suite.addTest(new SIHTMLLoaderTests("gzFile"));

        return suite;
    }

    public void testNationalities() throws IOException, Exception {
        // create new event
        String[] filename = { "german.html" };

        for (int i = 0; i < filename.length; i++) {
            String file = testDataDir + filename[i];
            EventResults event = loadEvent(file);
            printEventStats(event);
        }
    }

    public void testSpecialFiles() throws IOException, Exception {
        // create new event
        String[] filename = {"jok.html", "rendlesham.html","missingclub.html"};

        for (int i = 0; i < filename.length; i++) {
            String file = testDataDir + filename[i];
            EventResults event = loadEvent(file);
            printEventStats(event);
        }
    }

    public void testStandardFiles() throws IOException, Exception {
        // create new event
        String[] filename =
        { "46.html", "jok.html", "rendlesham.html", "YorkParkRace.html" };

        for (int i = 0; i < filename.length; i++) {
            String file = testDataDir + filename[i];
            EventResults event = loadEvent(file);
            printEventStats(event);
        }
    }

    public void zippedFile() throws IOException, Exception {
        String[] filename = { "jok.zip" };

        for (int i = 0; i < filename.length; i++) {
            String file = testDataDir + filename[i];
            EventResults event = loadEvent(file);
            printEventStats(event);
        }
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    private EventResults loadEvent(String inputFilename)
                      throws IOException, Exception
    {
        EventResults newEvent = null;

        newEvent = new EventResults();

        SIEventLoader loader = new SIEventLoader();

        loader.loadEvent(newEvent, inputFilename, false, 0);

        return (newEvent);
    }

    private void printEventStats(EventResults event) {
        Debug debug = new Debug(event);
        debug.listAll();
    }
}
