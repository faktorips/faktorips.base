package org.faktorips.devtools.htmlexport.test.documentor;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.htmlexport.Documentor;

public class DocumentorTest extends AbstractFipsDocTest {

    public void testExistIIpsProjects() throws CoreException {
        assertTrue("Keine IIpsProject-Instanzen gefunden", IpsPlugin.getDefault().getIpsModel().getIpsProjects().length > 0);
    }

    public void testConstructorParameters() {

        try {
            new Documentor(null);
            fail("fehlende DocumentorConfiguration nicht moniert");
        } catch (IllegalArgumentException e) {
        }
        new Documentor(documentorConfig);
    }

    public void testLinkedSources() {
        createStandardProjekt();
        
        List<IIpsSrcFile> linkedSources = documentor.getConfig().getLinkedSources();
        assertEquals(0, linkedSources.size());

        documentorConfig.setLinkPolicyClasses(true);
        linkedSources = documentor.getConfig().getLinkedSources();
        assertEquals(3, linkedSources.size());

        documentorConfig.setLinkPolicyClasses(false);
        linkedSources = documentor.getConfig().getLinkedSources();
        assertEquals(0, linkedSources.size());

        documentorConfig.setLinkProductClasses(true);
        linkedSources = documentor.getConfig().getLinkedSources();
        assertEquals(3, linkedSources.size());

        documentorConfig.setLinkProductClasses(false);
        linkedSources = documentor.getConfig().getLinkedSources();
        assertEquals(0, linkedSources.size());

        documentorConfig.setLinkPolicyClasses(true);
        documentorConfig.setLinkProductClasses(true);
        linkedSources = documentor.getConfig().getLinkedSources();
        assertEquals(6, linkedSources.size());

        documentorConfig.setLinkPolicyClasses(false);
        documentorConfig.setLinkProductClasses(false);
        linkedSources = documentor.getConfig().getLinkedSources();
        assertEquals(0, linkedSources.size());
    }

    public void testWriteOverview() throws Exception {
        createStandardProjekt();
        
        documentorConfig.setPath(FIPSDOC_GENERIERT_HOME);
        documentorConfig.setLinkPolicyClasses(true);
        documentorConfig.setLinkProductClasses(true);
        
        try {
            documentor.run();
        } catch (Exception e) {
            throw e;
        }
    }
}
