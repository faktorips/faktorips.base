/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class IpsProjectChildrenProviderTest extends AbstractIpsPluginTest {

    private IpsProjectChildrenProvider hierarchyProvider;
    private IIpsProject project;
    private IIpsObjectPathContainer container;
    private IIpsObjectPathContainer emptyContainer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        hierarchyProvider = new IpsProjectChildrenProvider();

        IIpsProject newIpsProject = newIpsProject("TestProject");
        project = spy(newIpsProject);

        IIpsObjectPath ipsObjectPath = newIpsProject.getIpsObjectPath();
        IIpsObjectPathEntry[] projectEntries = ipsObjectPath.getEntries();

        IIpsObjectPath spiedIpsObjectPath = spy(ipsObjectPath);

        container = mock(IIpsObjectPathContainer.class);
        List<IIpsObjectPathEntry> containerEntries = Arrays.asList(mock(IIpsObjectPathEntry.class));
        when(container.resolveEntries()).thenReturn(containerEntries);

        IIpsContainerEntry containerEntry = mock(IIpsContainerEntry.class);
        when(containerEntry.getIpsObjectPathContainer()).thenReturn(container);
        when(containerEntry.isContainer()).thenReturn(true);

        emptyContainer = mock(IIpsObjectPathContainer.class);
        when(emptyContainer.resolveEntries()).thenReturn(new ArrayList<IIpsObjectPathEntry>());

        IIpsContainerEntry emptyContainerEntry = mock(IIpsContainerEntry.class);
        when(emptyContainerEntry.getIpsObjectPathContainer()).thenReturn(emptyContainer);
        when(emptyContainerEntry.isContainer()).thenReturn(true);

        IIpsObjectPathEntry[] entries = new IIpsObjectPathEntry[projectEntries.length + 2];
        System.arraycopy(projectEntries, 0, entries, 0, projectEntries.length);
        entries[projectEntries.length] = containerEntry;
        entries[projectEntries.length + 1] = emptyContainerEntry;

        doReturn(entries).when(spiedIpsObjectPath).getEntries();
        doReturn(spiedIpsObjectPath).when(project).getIpsObjectPath();

    }

    @Test
    public void testGetContainerEntries() throws CoreException {
        List<IIpsObjectPathContainer> entries = hierarchyProvider.getContainerEntries(project);

        assertEquals(1, entries.size());

        assertTrue(entries.contains(container));
        assertFalse(entries.contains(emptyContainer));

    }

}
