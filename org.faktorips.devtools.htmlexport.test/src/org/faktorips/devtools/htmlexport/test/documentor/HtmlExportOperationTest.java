package org.faktorips.devtools.htmlexport.test.documentor;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;

public class HtmlExportOperationTest extends AbstractHtmlExportTest {

    public void testConstructorParameters() {

        try {
            new HtmlExportOperation(null);
            fail("fehlende DocumentationContext nicht moniert");
        } catch (IllegalArgumentException e) {
        }
        new HtmlExportOperation(context);
    }

    public void testWriteOverview() throws Exception {
        createStandardProjekt();

        context.setPath(zielpfad);

        context.setDocumentedIpsObjectTypes(context.getIpsProject().getIpsModel().getIpsObjectTypes());

        operation.run(new NullProgressMonitor());
    }
}
