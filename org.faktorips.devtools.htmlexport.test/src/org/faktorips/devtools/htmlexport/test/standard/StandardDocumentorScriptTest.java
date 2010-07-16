package org.faktorips.devtools.htmlexport.test.standard;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;
import org.faktorips.devtools.htmlexport.test.documentor.AbstractFipsDocTest;
import org.faktorips.devtools.htmlexport.test.linkchecker.ISpiderReportable;
import org.faktorips.devtools.htmlexport.test.linkchecker.Spider;

public class StandardDocumentorScriptTest extends AbstractFipsDocTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        documentorConfig.addDocumentorScript(new StandardDocumentorScript());
        documentorConfig.setLinkedIpsObjectTypes(documentorConfig.getIpsProject().getIpsModel().getIpsObjectTypes());
    }

    public void testWriteWithoutException() throws Exception {
        deletePreviousGeneratedFiles();

        createMassivProjekt();
        operation.run(new NullProgressMonitor());

        System.out.println("Ausgabe nach: " + documentorConfig.getPath());
    }

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
            }
        });

        deletePreviousGeneratedFiles();

        createMassivProjekt();
        operation.run(new NullProgressMonitor());

        spider.addURL(new URL("file://" + documentorConfig.getPath() + "/index.html"));
        spider.begin();

        if (errorUrls.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder().append("Fehlerhafte Urls gefunden: ");

        for (int i = 0; i < errorUrls.size(); i++) {
            sb.append('\n').append('\t').append(searchUrlsWithError.get(i).toString()).append('\t').append(
                    errorUrls.get(i).toString());
        }
        fail(sb.toString());

    }
    /*
     * public void testPaths() { createMassivProjekt(); for (IIpsObject ipsObject :
     * documentorConfig.getLinkedObjects()) {
     * System.out.println("=================================================");
     * System.out.println(ipsObject.getName() + " " + ipsObject.getIpsObjectType());
     * System.out.println(ipsObject.getIpsPackageFragment().getRelativePath());
     * 
     * String upPath = HtmlPathUtil.getPathToRoot(ipsObject); System.out.println(upPath); } }
     */
}
