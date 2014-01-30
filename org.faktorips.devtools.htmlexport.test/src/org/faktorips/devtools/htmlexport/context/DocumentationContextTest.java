/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.htmlexport.MockPluginResourcesFacade;
import org.junit.Test;

public class DocumentationContextTest {

    @Test
    public void testExportStatus() {
        DocumentationContext context = new DocumentationContext(new MockPluginResourcesFacade());

        assertEquals(IStatus.OK, context.getExportStatus().getSeverity());

        IpsStatus statusWarning = new IpsStatus(IStatus.WARNING, "blabla");
        context.addStatus(statusWarning);

        assertEquals(IStatus.WARNING, context.getExportStatus().getSeverity());
        assertEquals(statusWarning.getMessage(), context.getExportStatus().getMessage());

        IpsStatus statusError = new IpsStatus(IStatus.ERROR, "Fehler");
        context.addStatus(statusError);

        assertEquals(IStatus.ERROR, context.getExportStatus().getSeverity());
        assertTrue(context.getExportStatus() instanceof MultiStatus);

        MultiStatus multiStatus = (MultiStatus)context.getExportStatus();

        IStatus[] children = multiStatus.getChildren();
        for (IStatus status : children) {
            assertTrue(status.equals(statusError) || status.equals(statusWarning));
        }

    }
}
