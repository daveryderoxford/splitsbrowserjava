package org.splitsbrowser.util;
import org.kxml2.io.KXmlParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

/*
 * Version control info - Do not edit
 * Created:    Dave Ryder
 * Version:    $Revision: 1.1 $
 * Changed:    $Date: 2003/09/18 19:40:18 $
 * Changed by: $Author: daveryder $
 */
import java.io.IOException;

/**
 *  
 */
public class XmlPullWrapper extends KXmlParser {
    public XmlPullWrapper() {
        super();
    }

    /**
     * Read attribute value and return it or throw exception if
     * current element does not have such attribute.
     */
    public String getRequiredAttributeValue(String namespace,
                                            String name)
                                     throws IOException, XmlPullParserException
    {
        String value = getAttributeValue(namespace, name);

        if (value == null) {
            throw new XmlPullParserException("required attribute " + name +
                                             " is not present");
        } else {
            return value;
        }
    }

    /**
     * This method bypasses all events until it finds a start tag that has
     * passed in namesapce (if not null) and namespace (if not null).
     *
     * @return true if such START_TAG was found or false otherwise (and parser is on END_DOCUMENT).
     */
    public boolean skipToStartTag(final String tagNamespace,
                                  final String tagName)
                           throws XmlPullParserException, IOException
    {
        if ((tagNamespace == null) && (tagName == null)) {
            throw new IllegalArgumentException("namespace and name argument can not be bith null:" +
                                               getPositionDescription());
        }

        while (true) {
            int eventType = next();

            if (eventType == XmlPullParser.START_TAG) {
                String name = getName();
                String namespace = getNamespace();
                boolean matches =
                    ((tagNamespace != null) && tagNamespace.equals(namespace)) ||
                    ((tagName != null) && tagName.equals(name));

                if (matches) {
                    return true;
                }
            } else if (eventType == XmlPullParser.END_DOCUMENT) {
                return false;
            }
        }
    }
    

    /**
     * This method bypasses all child subtrees until it finds a child subtree with start tag
     * that matches the tag name (if not null) and namespsce (if not null)
     * passed in. Parser must be positioned on START_TAG.
     * <p>If succesful positions parser on such START_TAG and return true
     * otherwise this method returns false and parser is positioned on END_TAG
     * signaling last element in curren subtree.
     */
    public boolean skipToSubTree(final String tagNamespace, final String tagName)
                          throws XmlPullParserException, IOException
    {
        if ((tagNamespace == null) && (tagName == null)) {
            throw new IllegalArgumentException("namespace and name argument can not be bith null:" +
                                               getPositionDescription());
        }

        require(XmlPullParser.START_TAG, null, null);

        while (true) {
            int eventType = next();

            if (eventType == XmlPullParser.START_TAG) {
                String name = getName();
                String namespace = getNamespace();
                boolean matches =
                    ((tagNamespace != null) && tagNamespace.equals(namespace)) ||
                    ((tagName != null) && tagName.equals(name));

                if (matches) {
                    return true;
                }

                skipSubTree();
                require(XmlPullParser.END_TAG, name, namespace);
                next(); //skip end tag
            } else if (eventType == XmlPullParser.END_TAG) {
                return false;
            }
        }
    }

    /**
     * Tests if the current event is of the given type and if the namespace and name match.
     * null will match any namespace and any name. If the test passes a true is returned
     * otherwise a false is returned.
     */
    public boolean matches(int type, String namespace,
                           String name) throws XmlPullParserException
    {
        boolean matches =
            (type == getEventType()) &&
            ((namespace == null) || namespace.equals(getNamespace())) &&
            ((name == null) || name.equals(getName()));

        return matches;
    }

    /**
     * Move to the next tage and verify that it is an END_TAG. 
     * An exception is raised if it is not. 
     * @param namespace
     * @param name 
     * @throws XmlPullParserException
     * @throws IOException
     */
    public void nextEndTag(String namespace, String name)
                    throws XmlPullParserException, IOException
    {
        nextTag();
        require(XmlPullParser.END_TAG, namespace, name);
    }

    /**
     * Call parser nextTag() and check that it is END_TAG, throw exception if not.
     */
    public void nextEndTag()
                    throws XmlPullParserException, IOException
    {
        if (nextTag() != XmlPullParser.END_TAG) { 
            throw new XmlPullParserException("expected END_TAG and not" +
                                             getPositionDescription());
        }
    }

    /**
     * call parser nextTag() and check that it is START_TAG, throw exception if not.
     *    
     * @throws XmlPullParserException
     * @throws IOException
     */
    public void nextStartTag()
                      throws XmlPullParserException, IOException
    {
        if (nextTag() != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("expected START_TAG and not " +
                                             getPositionDescription());
        }
    }

    /**
     * combine nextTag(); require(START_TAG, null, name);
     */
    public void nextStartTag(String name)
                      throws XmlPullParserException, IOException
    {
        nextTag();
        require(XmlPullParser.START_TAG, null, name);
    }

    /**
     * combine nextTag(); require(START_TAG, namespace, name);
     */
    public void nextStartTag(String namespace, String name)
                      throws XmlPullParserException, IOException
    {
        nextTag();
        require(XmlPullParser.START_TAG, namespace, name);
    }

    /**
     * Read text content of element ith given namespace and name
     * (use null namespace do indicate that nemspace should not be checked)
     */
    public String nextText(String namespace, String name)
                    throws IOException, XmlPullParserException
    {
        if (name == null) {
            throw new XmlPullParserException("name for element can not be null");
        }

        require(XmlPullParser.START_TAG, namespace, name);

        return nextText();
    }
    
	/**
	 * Jumps out one depth level
	 * On entry the parser can be positioned on any element type.
	 * On exit the parser will be positioned on the END_TAG of 
	 * the enclosing  element.
	 * See @getDepth for a definition of depth
	 * 
	 */
    public void skipOut() throws IOException, XmlPullParserException {
    	int startDepth = getDepth();
    	do {
    		next();
    	} while (getDepth() <= startDepth);
    }
   

    /**
     * Skip sub tree that is currently porser positioned on.
     * <br>NOTE: parser must be on START_TAG and when funtion returns
     * parser will be positioned on corresponding END_TAG
     */
    public void skipSubTree() throws XmlPullParserException, IOException {
        require(XmlPullParser.START_TAG, null, null);

        int level = 1;

        while (level > 0) {
            int eventType = next();

            if (eventType == XmlPullParser.END_TAG) {
                --level;
            } else if (eventType == XmlPullParser.START_TAG) {
                ++level;
            }
        }
    }
}
