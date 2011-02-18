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

package org.faktorips.devtools.htmlexport.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.htmlexport.FakePluginResourcesFacade;
import org.junit.Test;

public class DocumentationContextTest {

    @Test
    public void testExportStatus() {
        DocumentationContext context = new DocumentationContext(new FakePluginResourcesFacade());

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
