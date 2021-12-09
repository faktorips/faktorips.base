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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.htmlexport.MockPluginResourcesFacade;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.junit.Test;

public class DocumentationContextTest extends AbstractIpsPluginTest {

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

    @Test
    public void testGetDocumentedSourceFiles_NoDuplicates() throws CoreRuntimeException {
        IIpsProject referencedIpsProject = newIpsProject();
        IIpsSrcFile ipsSrcFile1 = newPolicyCmptType(referencedIpsProject, "Policy").getIpsSrcFile();
        IIpsSrcFile ipsSrcFile2 = newPolicyCmptType(referencedIpsProject, "Coverage").getIpsSrcFile();
        IIpsProject ipsProject = mock(IIpsProject.class);
        DocumentationContext context = new DocumentationContext(new MockPluginResourcesFacade());
        when(ipsProject.findAllIpsSrcFiles(context.getDocumentedIpsObjectTypes()))
                .thenReturn(List.of(ipsSrcFile1, ipsSrcFile2, ipsSrcFile1));
        context.setIpsProject(ipsProject);

        Set<IIpsSrcFile> documentedSourceFiles = context.getDocumentedSourceFiles();

        assertThat(documentedSourceFiles.size(), is(2));
        assertThat(documentedSourceFiles, hasItems(ipsSrcFile1, ipsSrcFile2));
    }
}
