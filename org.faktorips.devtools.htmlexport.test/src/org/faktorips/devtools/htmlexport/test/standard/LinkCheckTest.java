/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.test.standard;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.faktorips.devtools.htmlexport.test.linkchecker.ISpiderReportable;
import org.faktorips.devtools.htmlexport.test.linkchecker.Spider;

public class LinkCheckTest extends TestCase {
    public void testKfzProduktLinks() throws MalformedURLException {
        // Test vorerst nur am lokalen Rechner
        if (!new File("/home/dicker").exists()) {
            return;
        }

        URL url = new URL("file:///home/dicker/aaaKfz/index.html");

        assertLinkChecked(url);

    }

    public void testKrankenProduktLinks() throws MalformedURLException {
        // Test vorerst nur am lokalen Rechner
        if (!new File("/home/dicker").exists()) {
            return;
        }

        URL url = new URL("file:///home/dicker/aaaKranken/index.html");

        assertLinkChecked(url);

    }

    private void assertLinkChecked(URL url) {
        final List<URL> errorUrls = new ArrayList<URL>();
        Spider spider = new Spider(new ISpiderReportable() {

            @Override
            public void spiderURLError(URL url) {
                errorUrls.add(url);
            }

            @Override
            public boolean spiderFoundURL(URL base, URL url) {
                return true;
            }

            @Override
            public void spiderFoundEMail(String email) {
            }
        }) {
            @Override
            public void log(String entry) {
                // TODO Auto-generated method stub
            }
        };

        spider.addURL(url);
        spider.begin();

        System.out.println("Untersuchte Seiten: " + spider.getWorkloadProcessed().size());

        // keine Fehler: Test bestanden
        if (errorUrls.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder().append("Fehlerhafte Urls gefunden: ");

        for (int i = 0; i < errorUrls.size(); i++) {
            sb.append('\n').append('\t').append(errorUrls.get(i).toString());
        }

        fail(sb.toString());
    }

}
