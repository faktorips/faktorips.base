package org.faktorips.devtools.htmlexport.context;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;
import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.TestUtil;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;
import org.junit.Test;

public class HtmlExportOperationTest extends AbstractHtmlExportPluginTest {

    @Test
    public void testConstructorParameters() {

        try {
            new HtmlExportOperation(null);
            fail("fehlende DocumentationContext nicht moniert");
        } catch (IllegalArgumentException e) {
        }
        new HtmlExportOperation(context);
    }

    @Test
    public void testWriteOverview() throws Exception {
        createStandardProjekt();

        context.setPath(zielpfad);

        IDocumentorScript script = new StandardDocumentorScript(new TestUtil().createMockIoHandler());
        context.addDocumentorScript(script);

        context.setDocumentedIpsObjectTypes(context.getIpsProject().getIpsModel().getIpsObjectTypes());

        operation.run(new NullProgressMonitor());
        assertTrue(context.getExportStatus().isOK());
    }
}
