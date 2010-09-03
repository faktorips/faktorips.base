package org.faktorips.devtools.htmlexport.test.documentor;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;

public class HtmlExportOperationTest extends AbstractHtmlExportTest {

    public void testConstructorParameters() {

        try {
            new HtmlExportOperation(null);
            fail("fehlende DocumentorConfiguration nicht moniert");
        } catch (IllegalArgumentException e) {
        }
        new HtmlExportOperation(config);
    }

    public void testWriteOverview() throws Exception {
        createStandardProjekt();

        config.setPath(zielpfad);

        config.setDocumentedIpsObjectTypes(config.getIpsProject().getIpsModel().getIpsObjectTypes());

        try {
            operation.run(new NullProgressMonitor());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
