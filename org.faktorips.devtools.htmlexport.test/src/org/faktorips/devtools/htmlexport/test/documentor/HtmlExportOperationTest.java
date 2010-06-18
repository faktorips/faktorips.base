package org.faktorips.devtools.htmlexport.test.documentor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;

public class HtmlExportOperationTest extends AbstractFipsDocTest {

    public void testExistIIpsProjects() throws CoreException {
        assertTrue("Keine IIpsProject-Instanzen gefunden",
                IpsPlugin.getDefault().getIpsModel().getIpsProjects().length > 0);
    }

    public void testConstructorParameters() {

        try {
            new HtmlExportOperation(null);
            fail("fehlende DocumentorConfiguration nicht moniert");
        } catch (IllegalArgumentException e) {
        }
        new HtmlExportOperation(documentorConfig);
    }

    public void testWriteOverview() throws Exception {
        createStandardProjekt();

        documentorConfig.setPath(zielpfad);

        documentorConfig.setLinkedIpsObjectTypes(documentorConfig.getIpsProject().getIpsModel().getIpsObjectTypes());

        try {
            operation.run(new NullProgressMonitor());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
