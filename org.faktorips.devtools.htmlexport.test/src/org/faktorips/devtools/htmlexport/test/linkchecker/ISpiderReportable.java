package org.faktorips.devtools.htmlexport.test.linkchecker;

import java.net.URL;

/**
 * This interface specifies a class that can accept information from a spider.
 * 
 * @author Jeff Heaton (http://www.jeffheaton.com)
 * @version 1.0
 */
public interface ISpiderReportable {

    /**
     * Called when the spider finds a URL.
     * 
     * @param base The page that the URL was found on.
     * @param url The URL that the spider found.
     * @return True if the spider should scan for links on this page.
     */
    public boolean spiderFoundURL(URL base, URL url);

    /**
     * Called when the spider trys to process a URL but gets an error.
     * 
     * @param url The URL that generated an error.
     */
    public void spiderURLError(URL url);

    /**
     * Called when the spider finds an email address.
     * 
     * @param email The email address found by the spider.
     */
    public void spiderFoundEMail(String email);
}