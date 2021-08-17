/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.context;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
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
        context.setDocumentationLocale(Locale.ENGLISH);

        context.setDocumentedIpsObjectTypes(context.getIpsProject().getIpsModel().getIpsObjectTypes());

        Display.getDefault().syncExec(() -> {
            try {
                operation.run(new NullProgressMonitor());
            } catch (CoreException e) {
                fail(e.getMessage());
            }
        });
        assertTrue(context.getExportStatus().isOK());
    }
}
