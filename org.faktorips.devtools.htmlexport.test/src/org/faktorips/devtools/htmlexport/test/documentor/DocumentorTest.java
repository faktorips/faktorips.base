package org.faktorips.devtools.htmlexport.test.documentor;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
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

    public void testWriteOverview() throws Exception {
        createStandardProjekt();
        
        documentorConfig.setPath(zielpfad);

        documentorConfig.setLinkedIpsObjectClasses(documentorConfig.getIpsProject().getIpsModel().getIpsObjectTypes());
        
        try {
            documentor.run();
        } catch (Exception e) {
            throw e;
        }
    }
}
