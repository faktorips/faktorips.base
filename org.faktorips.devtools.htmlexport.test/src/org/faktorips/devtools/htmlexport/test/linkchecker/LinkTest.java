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

package org.faktorips.devtools.htmlexport.test.linkchecker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.htmlexport.test.documentor.AbstractHtmlExportTest;

public class LinkTest extends AbstractHtmlExportTest {

    public void testCheckLinks() throws CoreException, MalformedURLException {

        final List<URL> errorUrls = new ArrayList<URL>();
        final List<URL> searchUrlsWithError = new ArrayList<URL>();
        Spider spider = new Spider(new ISpiderReportable() {
            URL actUrl;

            @Override
            public void spiderURLError(URL url) {
                errorUrls.add(url);
                searchUrlsWithError.add(actUrl);
            }

            @Override
            public boolean spiderFoundURL(URL base, URL url) {
                actUrl = base;
                return true;
            }

            @Override
            public void spiderFoundEMail(String email) {
                //
            }
        });

        deletePreviousGeneratedFiles();

        createMassivProjekt();
        operation.run(new NullProgressMonitor());

        spider.addURL(new URL("file://" + config.getPath() + "/index.html"));
        spider.begin();

        // TODO test should be fixed

        // if (errorUrls.isEmpty()) {
        // return;
        // }
        //
        // StringBuilder sb = new StringBuilder().append("Fehlerhafte Urls gefunden: ");
        //
        // for (int i = 0; i < errorUrls.size(); i++) {
        // URL url = searchUrlsWithError.get(i);
        // URL errorUrl = errorUrls.get(i);
        //
        // sb.append('\n').append('\t').append(url == null ? "null" :
        // url.toString()).append('\t').append(
        // errorUrl == null ? "null" : errorUrl.toString());
        // }
        // fail(sb.toString());

    }

}
