import org.splitsbrowser.applet.About;
import org.splitsbrowser.applet.SortedAgeClasses;
import org.splitsbrowser.applet.SplashPanel;
import org.splitsbrowser.applet.SplitsGraph;
import org.splitsbrowser.applet.SplitsTable;

import org.splitsbrowser.model.results.AgeClass;
import org.splitsbrowser.model.results.ControlCollection;
import org.splitsbrowser.model.results.Course;
import org.splitsbrowser.model.results.EventResults;
import org.splitsbrowser.model.results.Result;
import org.splitsbrowser.model.results.SelectedResults;
import org.splitsbrowser.model.results.io.CSVEventLoader;
import org.splitsbrowser.model.results.io.EventLoader;
import org.splitsbrowser.model.results.io.SICSVEventLoader;
import org.splitsbrowser.model.results.io.SIEventLoader;
import org.splitsbrowser.util.Message;

/*
 *  Splitsbrowser - Splitsbrowser
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
 *  the Free Software Foundation, Inc.,show 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
/*
 * Version control info - Do not edit
 * Created:    Dave Ryder
 * Version:    $Revision: 1.2 $
 * Changed:    $Date: 2003/09/18 19:29:40 $
 * Changed by: $Author: daveryder $
 */
import java.applet.Applet;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Polygon;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;

import java.util.Locale;

/**
 * Splitsbrowser Applet
 * Displays split time form an orienteering race in a number of different formats.
 *
 * Parameters:<br>
 *  src         Filename/URL of the results data.
 *              If the file extension/MINE type is gz or zip then the conetents will be interpreted as a zipped file
 *  color1   hexadecimal color specification for background color1 (vertical stripes)"
 *  color2   hexadecimal color specification for background color2 (vertical stripes)
 *  graphbackground  hexadecimal color specification for background color"
 *  background
 *  dataformat      CVS/SI
 *  bycourse        1 if results are by course, any other by class
 *  thinline        1 if the lines are to be drawn thin, 0 if they are drawn thick (default)"
 *  language        user interface language (e.g. en, de, fr).  By default this will be taken from the enviroemt
 *
 * @version    $Revision: 1.2 $
 */
public class Splitsbrowser extends Applet implements Runnable {
    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	//////////////////////////////////////////////////////////////////////////////////
    //
    //   Data
    //
    //////////////////////////////////////////////////////////////////////////////////
    private static String DATAFORMAT_UNDEFINED = "";
    private static String DATAFORMAT_CVS = "SBCSV";
    private static String DATAFORMAT_SI = "SIHTML";
    private static String DATAFORMAT_SICSV = "SICSV";
    protected Insets insets = new Insets(7, 7, 7, 7);
    int lastIndex;
    private BorderLayout borderLayoutMsgPanel = new BorderLayout();
    private BorderLayout bottomPanelBorderLayout = new BorderLayout();
    private BorderLayout graphPanelBorderLayout = new BorderLayout();
    private BorderLayout resultsListPanelBorderLayout = new BorderLayout();
    private BorderLayout resultsListPanelNorthGridLayout = new BorderLayout();
    private BorderLayout resultsPanelBorderLayout = new BorderLayout();
    private BorderLayout splitsGraphBorderLayout = new BorderLayout();
    private BorderLayout textPanelBorderLayout = new BorderLayout();
    private BorderLayout topPanelBorderLayout = new BorderLayout();
    private Button selectAllRunnersButton = new Button();
    private Button selectNoRunnersButton = new Button();
    private Button showCrossingRunnersButton = new Button();

    // See description of screen design at end of file
    private CardLayout appletCardLayout = new CardLayout();
    private CardLayout mainPanelCardLayout = new CardLayout();
    private Checkbox splitTimeChk = new Checkbox();
    private Checkbox timeBehindChk = new Checkbox();
    private Checkbox timeLossChk = new Checkbox();
    private Checkbox totalTimeChk = new Checkbox();
    private Choice classChoice = new Choice();
    private Choice compareAgainstChoice = new Choice();
    private Choice referenceRunnerChoice = new Choice();
    private Choice viewChoice = new Choice();
    private ControlCollection cc = null;
    private EventLoader loader = null;
    private EventResults event = new EventResults();
    private FlowLayout bottomCenterPanelFlowLayout = new FlowLayout();
    private FlowLayout bottomEastPanelFlowLayout = new FlowLayout();
    private GridLayout resultsListPanelNorthR1GridLayout = new GridLayout(1, 2);
    private GridLayout resultsListPanelNorthR2GridLayout = new GridLayout(1, 1);
    private Label classLabel = new Label();
    private Label clickHereLabel = new Label();
    private Label compareAgainstLabel = new Label();
    private Label referenceRunnerLabel = new Label();
    private Label versionLabel = new Label();
    private Label viewLabel = new Label();

    // choose the class or the course
    private java.awt.List classList = new java.awt.List(2, true);
    private java.awt.List resultsList = new java.awt.List(1, true); // Allow multi select
    private Panel bottomCenterPanel = new Panel();
    private Panel bottomEastPanel = new Panel();
    private Panel bottomPanel = new Panel();
    private Panel graphPanel = new Panel();
    private Panel mainPanel = new Panel();
    private Panel msgPanel = new Panel();
    private Panel resultsListPanel = new Panel();
    private Panel resultsListPanelNorth = new Panel();
    private Panel resultsListPanelNorthR1 = new Panel();
    private Panel resultsListPanelNorthR2 = new Panel();
    private Panel resultsPanel = new Panel();
    private Panel textPanel = new Panel();
    private Panel topLeftPanel = new Panel();
    private Panel topPanel = new Panel();
    private SelectedResults selectedResults = new SelectedResults();
    private SortedAgeClasses sortedAgeClasses = null;
    private SplashPanel introPanel = new SplashPanel();
    private SplitsGraph splitsGraph = new SplitsGraph();
    private SplitsTable splitsTable = new SplitsTable();
    private String documentBase = "";

    // 0 if results are by class, 1 if results are by course
    private String paramDataFormat;
    private String paramFileNameStartTimes = "";

    // Default is not zipped
    private String paramSISymbols;

    // CVS, SI
    private String paramSrc = "";
    private TextArea msgTextArea =
        new TextArea("", 10, 10, TextArea.SCROLLBARS_NONE);
    private volatile Thread mainThread = null;
    private String[] args = null; // Arguments when run in commandline mode
    private boolean urlInput = true;

    //  private SelectedResults selResults      = new SelectedResults();
    // User parameters
    private int paramResultsByCourse = 0;
    private int paramStarttimeColumn = 0;

    /**
     * Constructor when running as an applet
     */
    public Splitsbrowser() { // Made public to fix problem with sun VM
        args = null;
        urlInput = true;
    }

    /**
     * Constructor when running stand-alone
     */
    public Splitsbrowser(String[] newArgs, String newDocumentBase) { // Made public to fix problem with sun VM
        args = newArgs;
        documentBase = newDocumentBase;
        urlInput = false;
    }

    /**
     *  Gets the appletInfo attribute of the Splitsbrowser object
     *
     * @return    The appletInfo value
     */
    public String getAppletInfo() {
        return About.about();
    }

    /**
     *  Gets the parameterInfo attribute of the Splitsbrowser object
     *
     * @return    The parameterInfo value
     */
    public String[][] getParameterInfo() {
        String[][] info =
        {
            {
                "src\t\t", "  String\t\t",
                "  Name of file containing results (can be ZIP archive)"
            },
            {
                "srcstarttimes\t", "  String\t\t",
                "  Name of file containing startlist (can be ZIP archive)"
            },
            {
                "bycourse\t\t", "  0-1\t\t",
                "  1 if results are by course, 0 if results are by age class (default)"
            },
            {
                "starttimecolumn\t", "  int\t\t",
                "  If srcstarttimes contains multiple days per row, this is the column where the start time starts"
            },
            {
                "sisymbols\t", "  String\t\t",
                "  Symbols used for parsing SI results file: Finish;DSQ;NonComp;NumberOfControls;Km;Climb, e.g.: 'Z;Fehlst;AK;P;km;Hm'"
            },
            {
                "thinline\t\t", "  0-1\t\t",
                "  1 if the lines are to be drawn thin, 0 if they are drawn thick (default)"
            },
            {
                "graphbackground\t", "  RRGGBB\t",
                "  hexadecimal color specification for background color"
            },
            {
                "color1\t\t", "  RRGGBB\t",
                "  hexadecimal color specification for background color1 (vertical stripes)"
            },
            {
                "color2\t\t", "  RRGGBB\t",
                "  hexadecimal color specification for background color2 (vertical stripes)"
            },
            {
                "language\t\t", "  String\t\t",
                "  user interface language (e.g. en, de, fr)"
            },
        };

        return info;
    }

    /**
     *  Sets the size attribute of the Splitsbrowser object
     *
     * @param  width   The new size value
     * @param  height  The new size value
     */
    public void setSize(int width, int height) {
        /*
         *  The setsize method is overwriden to get the applet to
         *  resize in the browser
         */
        super.setSize(width, height);
        validate();
    }

    /**
     *  Destroy the applet
     */
    public void destroy() {
        Thread.yield();
    }

    /**
     *  Fill the combobox with a list of available classes
     */
    public void fillClassChoice() {
        classChoice.removeAll();

        // Prepare to record size of longest field to adapt width of combobox
        int MINSIZE = 80;
        int maxSize = MINSIZE;
        FontMetrics fm = classChoice.getFontMetrics(classChoice.getFont());

        for (int i = 0; i < sortedAgeClasses.getNumAgeClasses(); i++) {
            AgeClass ageClass = sortedAgeClasses.getSortedAgeClass(i);
            String name = ageClass.getName();

            classChoice.add(name);
            maxSize = Math.max(maxSize, fm.stringWidth(name));
        }

        // resize choice to max string size
        int BUTTONSIZE = 25;

        // ensure that maximum width is 100 pixels
        int MAXSIZE = 150;

        maxSize = Math.min(maxSize, MAXSIZE);
        classChoice.setSize(maxSize + BUTTONSIZE, 0);
        classChoice.select(0);
        classChoice_itemStateChanged(null);
        classList_itemStateChanged(null);
        topPanel.invalidate();
    }

    /**
     *  Fills the class listbox
     *
     * @param  course  Course
     */
    public void fillClassList(Course course) {
        FontMetrics fm = classList.getFontMetrics(classList.getFont());
        int maxSize = Integer.MIN_VALUE;
        int BUTTONSIZE = 25;
        int MAXSIZE = 150;

        classList.removeAll();

        if (course.getNumAgeClasses() <= 1) {
            classList.setVisible(false);
        } else {
            classList.setVisible(true);
        }

        for (int i = 0; i < course.getNumAgeClasses(); i++) {
            String name = course.getAgeClass(i).getName();

            if (name.equals(classChoice.getSelectedItem())) {
                continue;
            }

            classList.add(name);
            maxSize = Math.max(maxSize, fm.stringWidth(name));
        }

        maxSize = Math.min(maxSize, MAXSIZE);
        classList.setSize(maxSize + BUTTONSIZE, 0);
    }

    /**
     *  Fill the results listbox
     */
    public void fillResultList() {
        Course course = getSelectedCourse();
        String[] selected = classList.getSelectedItems();
        selectedResults.removeAll();

        for (int i = 0; i < selected.length; i++) {
            AgeClass ageClass = event.findAgeClass(selected[i]);
            selectedResults.addClass(ageClass);
        }

        selectedResults.addClass(sortedAgeClasses.getSortedAgeClass(classChoice.getSelectedIndex()));
        selectedResults.calcPositions();
        selectedResults.sortByFinishTime();

        int nRes = selectedResults.getNumResults();

        // number of selected results
        resultsList.setVisible(false);

        boolean refRunnerShowing = referenceRunnerChoice.isVisible();
        referenceRunnerChoice.setVisible(false);
        resultsList.removeAll();
        referenceRunnerChoice.removeAll();

        for (int i = 0; i < nRes; i++) {
            String disc = "";

            // Identify retired and disqualified runners with a *
            String pos = "";

            if (!selectedResults.getResult(i).isValid()) {
                disc = "* ";
            } else {
                pos = "  (" + Integer.toString(i + 1) + ")";
            }

            if (disc == "") {
                referenceRunnerChoice.add(selectedResults.getResult(i).getName());
            }

            if (((course.getNumAgeClasses() > 1) &&
                    (classList.getSelectedIndexes().length > 0)) ||
                    (paramResultsByCourse > 0)) {
                resultsList.add(selectedResults.getResult(i).getAgeClass()
                                               .getName() + "   " + disc +
                                selectedResults.getResult(i).getName() + pos);
            } else {
                resultsList.add(disc + selectedResults.getResult(i).getName() +
                                pos);
            }
        }

        if (referenceRunnerChoice.getItemCount() > 0) {
            referenceRunnerChoice.select(0);
        }

        resultsList.setVisible(true);
        selectedResults.calcOptimumTimes();
        referenceRunnerChoice.setVisible(refRunnerShowing);
        compareAgainstChoice_itemStateChanged(null);
        splitsGraph.setCourse(course);
        updateViewShown();
    }

    public void hideIntro() {
        appletCardLayout.show(this, "resultsPanel");
        updateViewShown();
    }

    /**
     *  Initialise the applet.  This is the applet entry point.
     */
    public void init() {
        System.out.println("Java Runtime Environment: " +
                           System.getProperty("java.vendor") + " " +
                           System.getProperty("java.version"));
        splitsGraph.setSelectedResults(selectedResults);

        Locale loc = this.getLocale();
        System.out.println("Country=" + loc.getISO3Country() + "/Language=" +
                           loc.getISO3Language());

        try {
            // Whole Applet
            appletCardLayout.setVgap(3);
            this.setLayout(appletCardLayout);
            this.setFont(new java.awt.Font("Dialog", 0, 11));
            this.setSize(new Dimension(472, 478));
            this.add(introPanel, "introPanel");
            this.add(msgPanel, "msgPanel");
            msgPanel.setLayout(borderLayoutMsgPanel);
            msgPanel.add(msgTextArea, BorderLayout.CENTER);
            msgTextArea.setBackground(new Color(0xE0, 0xE0, 0xE0));
            msgTextArea.setFont(new java.awt.Font("SansSerif", 0, 12));
            msgTextArea.setRows(100);
            msgTextArea.setText(About.about() + "\n");
            resultsPanel.setLayout(resultsPanelBorderLayout);
            this.add(resultsPanel, "resultsPanel");
            initTopPanel();
            initMainPanel();
            initBottomPanel();
        } catch (Exception e) {
            showError(e, "Error initialising application");
        }

        // Get the color of the background
        Color c = getColorParameter("backgroundcolor", new Color(0xC0C0C0));

        setComponentBackground(this, c);
        this.setBackground(c);

        // Get the type of results (by class/by course
        if (getParameter("bycourse", "0").equals("1")) {
            paramResultsByCourse = 1;
        }

        if (getParameter("thinline", "0").equals("1")) {
            splitsGraph.setThickLine(false);
        }

        // get the dataformat
        paramDataFormat = getParameter("dataformat", DATAFORMAT_UNDEFINED);

        // get the filename
        paramSrc = getParameter("src", "<no filename given>");
        introPanel.setStrLoading(Message.get("Main.Loading", paramSrc));
        msgTextArea.append(Message.get("Main.LoadingShort", paramSrc)); // Loading file ...
        paramFileNameStartTimes = getParameter("srcstarttimes", "");

        //System.out.println("bycourse=" + paramResultsByCourse + "\ndataformat=" + paramDataFormat + "\nsrc=" + paramSrc );
        try {
            paramStarttimeColumn =
                Integer.parseInt(getParameter("starttimecolumn", "1")) - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // get the string containing the tokens
        paramSISymbols = getParameter("sisymbols", null);

        try {
            splitsGraph.setBackground(getColorParameter("graphbackground",
                                                        splitsGraph.getColor2()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            splitsGraph.setColor1(getColorParameter("color1",
                                                    splitsGraph.getColor1()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            splitsGraph.setColor2(getColorParameter("color2",
                                                    splitsGraph.getColor2()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.requestFocus();
    }

    public Insets insets() {
        // TODO: changed to getInserts - not sure if this is OK in 1.1 
        return this.insets;
    }

    /**
     *  Description of the Method
     *
     * @exception  RuntimeException  Description of the Exception
     * @exception  IOException       Description of the Exception
     * @exception  Exception         Description of the Exception
     */
    public void loadEvent() throws RuntimeException, IOException, Exception {
        boolean runtimeStats = true;

        if (args == null) {
            documentBase = getDocumentBase().toString();
        }

        while ((documentBase.length() > 0) &&
                   (documentBase.charAt(documentBase.length() - 1) != '\\') &&
                   (documentBase.charAt(documentBase.length() - 1) != '/')) {
            documentBase = documentBase.substring(0, documentBase.length() - 1);
        }

        long startTime = System.currentTimeMillis();
        long startMem = java.lang.Runtime.getRuntime().freeMemory();

        if (paramDataFormat.equalsIgnoreCase(DATAFORMAT_CVS)) {
            loader = new CSVEventLoader();
        } else if (paramDataFormat.equalsIgnoreCase(DATAFORMAT_SICSV)) {
            loader = new SICSVEventLoader();
        } else if (paramDataFormat.equalsIgnoreCase(DATAFORMAT_SI)) {
            // Allow user to override the strings which are used to differentiate the different lines in the input file
            if (!paramFileNameStartTimes.equals("")) {
                paramFileNameStartTimes =
                    documentBase + paramFileNameStartTimes;
            }

            loader =
                new SIEventLoader(paramSISymbols, paramFileNameStartTimes,
                                  paramStarttimeColumn);
        } else {
            throw new RuntimeException("Unknown value for parameter dataformat: " +
                                       paramDataFormat);
        }

        System.out.println("[Splitsbrowser.loadEvent] Loading ...");
        loader.loadEvent(event, documentBase + paramSrc, urlInput,
                         paramResultsByCourse);

        if (runtimeStats) {
            long endTime = System.currentTimeMillis();
            long endMem = java.lang.Runtime.getRuntime().freeMemory();
            System.out.println("Time taken to load event " +
                               String.valueOf(endTime - startTime));
            System.out.println("Memory used in loading event " +
                               String.valueOf(endMem - startMem));
        }

        sortedAgeClasses = new SortedAgeClasses(event);

        //Debug.listAll(myEvent);
        fillClassChoice();
    }

    public static void main(String[] cmdArgs) {
        if (cmdArgs.length > 0) {
            Frame frame = new Frame("Splitsbrowser");
            Applet applet = new Splitsbrowser(cmdArgs, "");

            // To close the application:
            frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
            frame.add(applet);
            frame.setSize(1200, 1000);
            applet.init();
            applet.start();
            frame.setVisible(true);
        } else {
            System.err.println("Usage: Win-Res [<parameter name> <value>]");
        }
    }

    /**
     *  Main processing method for the Splitsbrowser object
     */
    public void run() {
        try {
            loadEvent();
            System.out.println("Eventdata loaded. Building controls datastructures.");
            cc = new ControlCollection(event);
            splitsGraph.setControlCollection(cc);
            splitsGraph.setEventName(event.getName());

            //Debug.listAll();
            hideMessage();
        } catch (Exception e) {
            if (args == null) {
                getAppletContext().showStatus(Message.get("Main.FileError"));
            }

            System.out.println("===============");
            showError(e, Message.get("Main.FileError"));
        }

        topPanel.setVisible(true);
        bottomPanel.setVisible(true);
        topPanel.validate();
        this.validate();
    }

    // Start the applet when mouse enters it

    /**
     *  Description of the Method
     */
    public void start() {
        mainThread = new Thread(this);
        mainThread.start();
    }

    // Stop the applet whe mouse leaves it

    /**
     *  Description of the Method
     */
    public void stop() {
        mainThread = null;
    }
    
    //////////////////////////////////////////////////////////////////////////////////
    //
    //   Event handlers
    //
    //////////////////////////////////////////////////////////////////////////////////
    // User has selected a new class to display
    void classChoice_itemStateChanged(ItemEvent e) {
        // Find the course
        if (classChoice.getSelectedIndex() == -1) {
            return;
        }

        AgeClass ageClass =
            sortedAgeClasses.getSortedAgeClass(classChoice.getSelectedIndex());
        splitsTable.setAgeClass(ageClass.getCourse(), ageClass);

        //  Update the results list
        // Update the list of classes for this course
        fillClassList(ageClass.getCourse());

        // update the list for the classes
        classList_itemStateChanged(null);
        topPanel.validate();
    }

    // User has selected additional classes to display
    void classList_itemStateChanged(ItemEvent e) {
        fillResultList();
    }

    // User has selected a different reference to compare against
    void compareAgainstChoice_itemStateChanged(ItemEvent e) {
        // Change the comparison algorithm
        // Set the optimum time algorithm for all the courses
        switch (compareAgainstChoice.getSelectedIndex()) {
        case -1:
            break;

        case 0:

            // Winner
            selectedResults.setReferenceResult(0);
            referenceRunnersetVisibility(false);

            break;

        case 1:

            // Fastest time
            selectedResults.setReferenceResult(-1);
            referenceRunnersetVisibility(false);

            break;

        case 2:

            // Fastest time +5%
            selectedResults.setReferenceResult(-2);
            referenceRunnersetVisibility(false);

            break;

        case 3:

            // Fastest time +25%
            selectedResults.setReferenceResult(-3);
            referenceRunnersetVisibility(false);

            break;

        case 4:

            // Fastest time +50%
            selectedResults.setReferenceResult(-4);
            referenceRunnersetVisibility(false);

            break;

        case 5:

            // Fastest time +100%
            selectedResults.setReferenceResult(-5);
            referenceRunnersetVisibility(false);

            break;

        case 6:

            // Any runner
            selectedResults.setReferenceResult(0);
            referenceRunnerChoice.select(0);
            referenceRunnersetVisibility(true);

            break;

        default:
            break;
        }

        splitsGraph.invalidateDimensions();
    }

    // Reference runner has changed
    void referenceRunnerChoice_itemStateChanged(ItemEvent e) {
        selectedResults.setReferenceResult(referenceRunnerChoice.getSelectedIndex());
        splitsGraph.invalidateDimensions();
    }

    void referenceRunnersetVisibility(boolean visibility) {
        referenceRunnerLabel.setVisible(visibility);
        referenceRunnerChoice.setVisible(visibility);
        this.validate();
    }

    // Double click handler - a bodge to get round a bug where double clicking on a
    // selected item leaves it selected but only generates a single selection event
    void resultsList_actionPerformed(ActionEvent e) {

        if (splitsGraph.isDisplayed(lastIndex)) {
            resultsList.select(lastIndex);
        } else {
            resultsList.deselect(lastIndex);
        }
    }

    // User clicked on a runner's name to show/hide the graph for this runner
    void resultsList_itemStateChanged(ItemEvent e) {
        // Find the result we have selected
        Integer index = (Integer) e.getItem();
        lastIndex = index.intValue();

        if (lastIndex != -1) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                splitsGraph.displayResult(lastIndex);
            } else {
                splitsGraph.removeResult(lastIndex);
            }
        }

        if (resultsList.getSelectedIndexes().length == 1) {
            // Only enable button if the runner has a valid start time
            showCrossingRunnersButton.setEnabled(selectedResults.getResult(resultsList.getSelectedIndex())
                                                                .getStartTime()
                                                                .isValid());
        } else {
            showCrossingRunnersButton.setEnabled(false);
        }
    }

    void selectAllRunnersButton_actionPerformed(ActionEvent e) {
        splitsGraph.removeAllResults();

        for (int i = 0; i < selectedResults.getNumResults(); i++) {
            resultsList.select(i);
            splitsGraph.displayResult(i);
        }
    }

    void selectNoRunnersButton_actionPerformed(ActionEvent e) {
        for (int i = selectedResults.getNumResults() - 1; i >= 0; i--) {
            resultsList.deselect(i);
        }

        splitsGraph.removeAllResults();
    }

    void showCrossingRunnersButton_actionPerformed(ActionEvent e) {
        findCrossedRunners();
    }

    void splitTimeChk_itemStateChanged(ItemEvent e) {
        splitsGraph.setShowSplits(e.getStateChange() == ItemEvent.SELECTED);
        splitsTable.setShowSplits(e.getStateChange() == ItemEvent.SELECTED);
    }

    void timeBehindChk_itemStateChanged(ItemEvent e) {
        splitsGraph.setShowTimeBehind(e.getStateChange() == ItemEvent.SELECTED);
        splitsTable.setShowTimeBehind(e.getStateChange() == ItemEvent.SELECTED);
    }

    void timeLossChk_itemStateChanged(ItemEvent e) {
        splitsGraph.setShowTimeLoss(e.getStateChange() == ItemEvent.SELECTED);

        // TODO need to implemenet in results table
        //   splitsTable.setShowTimeLoss(e.getStateChange() == ItemEvent.SELECTED);
    }

    void totalTimeChk_itemStateChanged(ItemEvent e) {
        splitsGraph.setShowTotalTime(e.getStateChange() == ItemEvent.SELECTED);
        splitsTable.setShowTotalTime(e.getStateChange() == ItemEvent.SELECTED);
    }

    // User has selected a new type of view (graph or table)
    void viewChoice_itemStateChanged(ItemEvent e) {
        updateViewShown();
    }

    private String getCmdlineParameter(String paramName) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(paramName) && (i < (args.length - 1))) {
                return args[i + 1];
            }
        }

        return null;
    }

    private Color getColorParameter(String paramName, Color defaultColor)
                             throws NumberFormatException
    {
        // Gets a color parameter in web format of hex
        String str = getParameter(paramName, "").trim();

        // exit if the parameter was not set
        if (str.length() == 0) {
            return (defaultColor);
        }

        // ignore a # character on the start of the string as used in HTML
        int start = 0;

        if (str.startsWith("#")) {
            start = 1;
        }

        int color = 0;

        try {
            color = Integer.parseInt(str.substring(start), 16);

            //          System.out.println("color=" + color);
        } catch (NumberFormatException e) {
            showError(e,
                      "Error in hex parameter: " + paramName +
                      "\nValue is not a hex number: " + str);

            return defaultColor;
        }

        return (new Color((int) color));
    }

    /**
     *  Recursively set the background color of all child compnents
     */
    private void setComponentBackground(Component comp, Color color) {
        if (comp instanceof Container) {
            Container container = (Container) comp;

            for (int i = 0; i < container.countComponents(); i++) {
                comp = container.getComponent(i);
                setComponentBackground(comp, color);
            }
        }

        // Set background color if the component is not a text area
        if (!(comp instanceof TextArea) && !(comp instanceof java.awt.List) &&
                !(comp instanceof Choice)) {
            comp.setBackground(color);
        }
    }

    private String getParameter(String paramName, String defaultStr) {
        String param =
            (args == null) ? this.getParameter(paramName)
                           : getCmdlineParameter(paramName);

        if (param == null) {
            param = defaultStr;
        }

        return param;
    }

    private Course getSelectedCourse() {
        Course course =
            sortedAgeClasses.getSortedAgeClass(classChoice.getSelectedIndex())
                            .getCourse();

        return (course);
    }

    private void fillViewChoice() {
        viewChoice.add(Message.get("Main.TimeDifferenceGraph"));
        viewChoice.add(Message.get("Main.ActualTimeGraph"));
        viewChoice.add(Message.get("Main.PosGraph"));
        viewChoice.add(Message.get("Main.SplitPosGraph"));
        viewChoice.add(Message.get("Main.PercentBehindGraph"));
        viewChoice.add(Message.get("Main.ResultsTable"));
    }

    // Find runners that crossed the path of the selected runner
    private void findCrossedRunners() {
        if (resultsList.getSelectedIndexes().length != 1) {
            return;
        }

        int selIndex = resultsList.getSelectedIndex();
        Result refResult = selectedResults.getResult(selIndex);

        if (!refResult.getStartTime().isValid()) {
            return;
        }

        int i;

        //splitsGraph.displayResult(selIndex);
        //resultsList.select(selIndex);
        for (i = 0; i < selectedResults.getNumResults(); i++) {
            Result result = selectedResults.getResult(i);

            // Check for definites non-cross based on start and finish time 
            //            if (result.getFinishTime().lessThan(refResult.getStartTime())
            //                || refResult.getFinishTime().lessThan(result.getStartTime())) {
            //                continue;
            //            }
            // Check for definite crosses based on start and finish times  
            boolean startAfterRef =
                refResult.getStartTime().lessThan(result.getStartTime());

            // Otherwise we must look at every control to ceck there is not a 
            // cross and a cross back
            for (int j = 0; j <= result.getNumControls(); j++) {
                if (!result.getAbsoluteTime(j).isValid() ||
                        !refResult.getAbsoluteTime(j).isValid()) {
                    continue;
                }

                boolean visitAfterRef =
                    refResult.getAbsoluteTime(j).lessThan(result.getAbsoluteTime(j));

                if ((startAfterRef ^ visitAfterRef)) {
                    resultsList.select(i);
                    splitsGraph.displayResult(i);

                    break;
                }
            }
        }

        showCrossingRunnersButton.setEnabled(false);
    }

    private void hideMessage() {
        appletCardLayout.show(this, "resultsPanel");
        updateViewShown();

        // Force a layout on all components
        this.validate();
    }

    private void initBottomPanel() throws Exception {
        bottomPanel.setLayout(bottomPanelBorderLayout);
        versionLabel.setText(About.Version());
        bottomPanel.add(versionLabel, BorderLayout.WEST);
        bottomPanel.add(bottomCenterPanel, BorderLayout.CENTER);
        bottomCenterPanel.setLayout(bottomCenterPanelFlowLayout);
        bottomCenterPanel.add(clickHereLabel);
        clickHereLabel.setText(Message.get("Main.ClickHere"));
        clickHereLabel.setVisible(false);
        bottomPanel.add(bottomEastPanel, BorderLayout.EAST);
        bottomEastPanel.setLayout(bottomEastPanelFlowLayout);
        totalTimeChk.setLabel(Message.get("Main.TotalTime"));
        totalTimeChk.setState(splitsGraph.getShowTotalTime());
        totalTimeChk.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    totalTimeChk_itemStateChanged(e);
                }
            });
        bottomEastPanel.add(totalTimeChk, null);

        splitTimeChk.setLabel(Message.get("Main.SplitTime"));
        splitTimeChk.setState(splitsGraph.getShowSplits());
        splitTimeChk.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    splitTimeChk_itemStateChanged(e);
                }
            });
        bottomEastPanel.add(splitTimeChk, null);

        timeBehindChk.setLabel(Message.get("Main.TimeBehind"));
        timeBehindChk.setState(splitsGraph.getShowTimeBehind());

        timeBehindChk.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    timeBehindChk_itemStateChanged(e);
                }
            });
        bottomEastPanel.add(timeBehindChk, null);

        timeLossChk.setLabel(Message.get("Main.TimeLoss"));
        timeLossChk.setState(splitsGraph.getShowTimeLoss());

        timeLossChk.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    timeLossChk_itemStateChanged(e);
                }
            });
        bottomEastPanel.add(timeLossChk, null);
        resultsPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void initMainPanel() throws Exception {
        mainPanel.setLayout(mainPanelCardLayout);
        mainPanel.add(graphPanel, "graphPanel");
        graphPanel.setLayout(graphPanelBorderLayout);
        graphPanelBorderLayout.setHgap(6);
        graphPanel.add(resultsListPanel, BorderLayout.WEST);
        resultsListPanel.setLayout(resultsListPanelBorderLayout);
        resultsListPanel.add(resultsList, BorderLayout.CENTER);
        resultsList.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    resultsList_actionPerformed(e);
                }
            });
        resultsList.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    resultsList_itemStateChanged(e);
                }
            });
        resultsListPanel.add(resultsListPanelNorth, BorderLayout.NORTH);
        resultsListPanelNorth.setLayout(resultsListPanelNorthGridLayout);
        resultsListPanelBorderLayout.setVgap(3);
        resultsListPanelNorthR1GridLayout.setHgap(6);
        resultsListPanelNorthGridLayout.setVgap(3);
        resultsListPanelNorth.add(resultsListPanelNorthR1, BorderLayout.NORTH);
        resultsListPanelNorthR1.setLayout(resultsListPanelNorthR1GridLayout);
        resultsListPanelNorth.add(resultsListPanelNorthR2, BorderLayout.CENTER);
        resultsListPanelNorthR2.setLayout(resultsListPanelNorthR2GridLayout);
        resultsListPanelNorthR1.add(selectAllRunnersButton);
        selectAllRunnersButton.setLabel(Message.get("Main.SelectAll"));
        selectAllRunnersButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectAllRunnersButton_actionPerformed(e);
                }
            });
        resultsListPanelNorthR1.add(selectNoRunnersButton);
        selectNoRunnersButton.setLabel(Message.get("Main.DeselectAll"));
        selectNoRunnersButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectNoRunnersButton_actionPerformed(e);
                }
            });

        resultsListPanelNorthR2.add(showCrossingRunnersButton, null);
        resultsListPanelNorthR2.setVisible(false);
        showCrossingRunnersButton.setLabel(Message.get("Main.CrossingRunners"));
        showCrossingRunnersButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showCrossingRunnersButton_actionPerformed(e);
                }
            });
        showCrossingRunnersButton.setEnabled(false);

        graphPanel.add(splitsGraph, BorderLayout.CENTER);
        splitsGraph.setLayout(splitsGraphBorderLayout);
        splitsGraphBorderLayout.setVgap(5);
        splitsGraphBorderLayout.setHgap(5);
        mainPanel.add(textPanel, "textPanel");
        textPanel.setLayout(textPanelBorderLayout);
        textPanel.setBackground(Color.white);
        textPanel.add(splitsTable, BorderLayout.CENTER);
        splitsTable.setBackground(Color.white);
        resultsPanel.add(mainPanel, BorderLayout.CENTER);
    }

    private void initTopPanel() {
        // Selection of class to display/graph
        topPanel.setLayout(topPanelBorderLayout);
        topPanelBorderLayout.setHgap(3);
        topPanelBorderLayout.setVgap(6);

        topLeftPanel.add(new IconDisplayer(), null);

        // Selection of type of display/graph
        topLeftPanel.add(viewLabel, null);
        viewLabel.setAlignment(Label.RIGHT);
        viewLabel.setText(Message.get("Main.View"));
        fillViewChoice();
        viewChoice.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    viewChoice_itemStateChanged(e);
                }
            });
        topLeftPanel.add(viewChoice, null);

        classLabel.setAlignment(1);
        classLabel.setText(Message.get("Main.Class"));
        topLeftPanel.add(classLabel, null);
        classChoice.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    classChoice_itemStateChanged(e);
                }
            });
        topLeftPanel.add(classChoice, null);
        topLeftPanel.add(classList, null);
        classList.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    classList_itemStateChanged(e);
                }
            });

        // Selection of comparison
        compareAgainstLabel.setAlignment(Label.RIGHT);
        compareAgainstLabel.setText(Message.get("Main.CompareAgainst"));
        topLeftPanel.add(compareAgainstLabel, null);
        compareAgainstChoice.add(Message.get("Main.Winner"));
        compareAgainstChoice.add(Message.get("Main.FastestTime"));
        compareAgainstChoice.add(Message.get("Main.FastestTime") + " + 5%");
        compareAgainstChoice.add(Message.get("Main.FastestTime") + " + 25%");
        compareAgainstChoice.add(Message.get("Main.FastestTime") + " + 50%");
        compareAgainstChoice.add(Message.get("Main.FastestTime") + " + 100%");
        compareAgainstChoice.add(Message.get("Main.AnyRunner"));
        compareAgainstChoice.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    compareAgainstChoice_itemStateChanged(e);
                }
            });
        compareAgainstChoice.select(1);
        topLeftPanel.add(compareAgainstChoice, null);
        referenceRunnerLabel.setAlignment(1);
        referenceRunnerLabel.setText(Message.get("Main.Runner"));
        topLeftPanel.add(referenceRunnerLabel, null);
        topLeftPanel.add(referenceRunnerChoice, null);
        referenceRunnerChoice.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    referenceRunnerChoice_itemStateChanged(e);
                }
            });
        referenceRunnersetVisibility(false);
        topPanel.add(topLeftPanel, BorderLayout.WEST);
        resultsPanel.add(topPanel, BorderLayout.NORTH);

        //      topPanel.setVisible(false);
        //      bottomPanel.setVisible(false);
    }

    private void showError(Exception e, String msg) {
        // Show the message
        msgTextArea.append("\n" + e.getMessage() + msg);

        //System.out.println("\n"+ e.getMessage()+msg);
        appletCardLayout.show(this, "msgPanel");
        e.printStackTrace(System.out);
    }

    private void updateViewShown() {
        clickHereLabel.setVisible(false);

        try {
            switch (viewChoice.getSelectedIndex()) {
            case 0:
                mainPanelCardLayout.show(mainPanel, "graphPanel");
                splitsGraph.setGraphType(SplitsGraph.GRAPH_TYPE_COMPARISON);
                clickHereLabel.setVisible(true);
                break;

            case 1:
                mainPanelCardLayout.show(mainPanel, "graphPanel");
                splitsGraph.setGraphType(SplitsGraph.GRAPH_TYPE_ABSOLUTE);
                clickHereLabel.setVisible(true);
                break;

            case 2:               
                mainPanelCardLayout.show(mainPanel, "graphPanel");
                splitsGraph.setGraphType(SplitsGraph.GRAPH_TYPE_POS);
                break;

            case 3:
                mainPanelCardLayout.show(mainPanel, "graphPanel");
                splitsGraph.setGraphType(SplitsGraph.GRAPH_TYPE_SPLITPOS);
                break;

            case 4:
                mainPanelCardLayout.show(mainPanel, "graphPanel");
                splitsGraph.setGraphType(SplitsGraph.GRAPH_TYPE_PERCENTBEHIND);
                break;

            case 5:
                mainPanelCardLayout.show(mainPanel, "textPanel");
                break;
            }

            boolean absGraph =
                (viewChoice.getSelectedIndex() == SplitsGraph.GRAPH_TYPE_ABSOLUTE);
            resultsListPanelNorthR2.setVisible(absGraph);
            resultsListPanel.validate();

            bottomPanel.validate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class IconDisplayer extends Panel {
        /**
		 * Comment for <code>serialVersionUID</code>
		 */
		private static final long serialVersionUID = -8859223341080994008L;
		
		int w = 16;

        public IconDisplayer() {
        }

        public Dimension getMinimumSize() {
            return new Dimension(w, w);
        }

        public Dimension getPreferredSize() {
            return new Dimension(w + 4, w);
        }

        public void paint(Graphics g) {
            int[] xpoints = new int[3];
            int[] ypoints = new int[3];
            int x0 = 4;
            int y0 = 0;
            xpoints[0] = x0;
            ypoints[0] = y0;
            xpoints[1] = x0 + w;
            ypoints[1] = y0;
            xpoints[2] = x0;
            ypoints[2] = y0 + w;

            // draw the orienteering control
            g.setColor(Color.red);
            g.fillRect(x0, y0, w, w);
            g.setColor(Color.white);
            g.fillPolygon(new Polygon(xpoints, ypoints, 3));

            // Draw the chart symbol
            g.setColor(Color.blue);

            for (int i = 0; i <= 1; i++) {
                g.drawLine(x0, (int) (y0 + (w * 0.5) + i),
                           (int) (x0 + (w * 0.28)), (int) (y0 + (w * 0.41) + i));
                g.drawLine((int) (x0 + (w * 0.28)),
                           (int) (y0 + (w * 0.41) + i),
                           (int) (x0 + (w * 0.47)), (int) (y0 + (w * 0.79) + i));
                g.drawLine((int) (x0 + (w * 0.47)),
                           (int) (y0 + (w * 0.79) + i),
                           (int) (x0 + (w * 0.99)), (int) (y0 + (w * 0.59) + i));
            }
        }
    }    
}


//--------------------------------------------------------
// Screen design    
//--------------------------------------------------------
// Applet: CARDLAYOUT 
//      introPanel
//      msgPanel: BORDERLAYOUT
//          CENTER: msgTextArea
//      resultsPanel: BORDERLAYOUT
//          NORTH: topPanel: BORDERLAYOUT
//              WEST: topLeftPanel: FLOWLAYOUT
//                  viewLabel
//                  viewChoice
//                  classLabel
//                  classChoice
//                  classList
//                  compareAgainstLabel
//                  compareAgainstChoice
//                  referenceRunnerLabel
//                  referenceRunnerChoice
//          CENTER: mainPanel: CARDLAYOUT
//              graphPanel: BORDERLAYOUT
//                  WEST: resultListPanel: BORDERLAYOUT
//                      CENTER: resultsList
//                      NORTH: resultsListPanelNorth: GRIDLAYOUT (2,1)
//                       Row1: resultsListPanelNorthR1: GRIDLAYOUT (1,2) 
//                          selectAllButton
//                          selectNoneButton
//                       Row2: resultsListPanelNorthR2: GRIDLAYOUT (1,1)
//                          showCrossingRunnersButton
//                  CENTER: splitsGraph
//              textPanel: BORDERLAYOUT
//                  CENTER: splitsTable
//          SOUTH: bottomPanel: BORDERLAYOUT
//              WEST: versionLabel
//              CENTER: bottomCenterPanel: FLOWLAYOUT
//              EAST: bottomEastPanel: FLOWLAYOUT
//                  totalTimeChk
//                  totalPosChk
//                  splitTimeChk
//                  splitPosChk
//                  timeBehindChk
//--------------------------------------------------------
