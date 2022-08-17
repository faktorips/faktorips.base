/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;
import org.junit.Test;

public class ProjectSearchTest {

    @Test
    public void testProcessEntry_invalidProjectRef() throws Exception {
        ProjectSearch projectSearch = new ProjectSearch();
        IIpsProjectRefEntry entry = mock(IIpsProjectRefEntry.class);
        when(entry.getType()).thenReturn(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE);

        assertTrue(projectSearch.getProjects().isEmpty());
        projectSearch.processEntry(entry);

        assertTrue(projectSearch.getProjects().isEmpty());
    }

    @Test
    public void testProcessEntry_validProjectRef() throws Exception {
        ProjectSearch projectSearch = new ProjectSearch();
        IIpsProjectRefEntry entry = mock(IIpsProjectRefEntry.class);
        when(entry.getType()).thenReturn(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE);
        when(entry.getReferencedIpsProject()).thenReturn(mock(IIpsProject.class));

        assertTrue(projectSearch.getProjects().isEmpty());
        projectSearch.processEntry(entry);

        assertFalse(projectSearch.getProjects().isEmpty());
    }

    @Test
    public void testProcessEntry_srcFolderEntry() throws Exception {
        ProjectSearch projectSearch = new ProjectSearch();
        IIpsProjectRefEntry entry = mock(IIpsProjectRefEntry.class);
        when(entry.getType()).thenReturn(IIpsObjectPathEntry.TYPE_SRC_FOLDER);

        assertTrue(projectSearch.getProjects().isEmpty());
        projectSearch.processEntry(entry);

        assertTrue(projectSearch.getProjects().isEmpty());
    }

}
