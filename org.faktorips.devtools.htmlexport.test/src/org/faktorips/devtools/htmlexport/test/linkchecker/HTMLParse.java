package org.faktorips.devtools.htmlexport.test.linkchecker;

import javax.swing.text.html.HTMLEditorKit;

/**
 * A VERY simple class meant only to subclass the HTMLEditorKit class to make the getParser method
 * public so that we can gain access to an HTMLEditorKit.Parser object.
 * 
 * 
 * @author Jeff Heaton
 * @version 1.0
 */
public class HTMLParse extends HTMLEditorKit {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    /**
     * Call to obtain a HTMLEditorKit.Parser object.
     * 
     * @return A new HTMLEditorKit.Parser object.
     */
    @Override
    public HTMLEditorKit.Parser getParser() {
        return super.getParser();
    }
}
