/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.internal.ipsproject.jdtcontainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.abstraction.eclipse.internal.EclipseImplementation;
import org.faktorips.devtools.model.eclipse.internal.ipsproject.jdtcontainer.IpsContainer4JdtClasspathContainer.JdtClasspathResolver;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Category(EclipseImplementation.class)
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IpsContainer4JdtClasspathContainerTest {

    private static final String MY_NAME = "myName";

    private String optionalPath = "myOptionalPath";

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IpsObjectPath ipsObjectPath;

    @Mock
    private IClasspathContainer jdtContainer;

    @Mock
    private IClasspathEntry entry;

    @Mock
    private IIpsObjectPathEntry objectPathEntry;

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    private IJavaProject javaProject;

    private IpsContainer4JdtClasspathContainer ipsContainer4JdtClasspathContainer;

    @Before
    public void createIpsContainer4JdtClasspathContainer() throws Exception {
        ipsContainer4JdtClasspathContainer = new IpsContainer4JdtClasspathContainer(optionalPath, ipsProject);
    }

    @Test
    public void testName() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            mockJdtClasspathResolver();
            when(jdtContainer.getDescription()).thenReturn(MY_NAME);

            String name = ipsContainer4JdtClasspathContainer.getName();

            assertEquals(MY_NAME, name);
        }
    }

    @Test
    public void testName_noContainer() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            String name = ipsContainer4JdtClasspathContainer.getName();

            assertEquals("Unresolved: " + IpsContainer4JdtClasspathContainerType.ID + '[' + optionalPath + ']', name);
        }
    }

    @Test
    public void testResolveEntries_noContainer() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            mockJdtClasspathResolver(null);

            List<IIpsObjectPathEntry> resolveEntries = ipsContainer4JdtClasspathContainer.resolveEntries();

            assertEquals(0, resolveEntries.size());
        }
    }

    @Test
    public void testResolveEntries_emptyContainer() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            mockJdtClasspathResolver();
            when(jdtContainer.getClasspathEntries()).thenReturn(new IClasspathEntry[] {});

            List<IIpsObjectPathEntry> resolveEntries = ipsContainer4JdtClasspathContainer.resolveEntries();

            assertEquals(0, resolveEntries.size());
        }
    }

    @Test
    public void testResolveEntries_withEntry() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            mockEntryCreator(objectPathEntry);

            List<IIpsObjectPathEntry> resolveEntries = ipsContainer4JdtClasspathContainer.resolveEntries();

            assertEquals(1, resolveEntries.size());
            assertEquals(objectPathEntry, resolveEntries.get(0));
        }
    }

    @Test
    public void testResolveEntries_withEntryCached() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            mockEntryCreator(objectPathEntry);
            List<IIpsObjectPathEntry> expectedEntries = ipsContainer4JdtClasspathContainer.resolveEntries();

            List<IIpsObjectPathEntry> sameEntries = ipsContainer4JdtClasspathContainer.resolveEntries();

            assertEquals(expectedEntries, sameEntries);
        }
    }

    @Test
    public void testResolveEntries_withNullEntry() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            mockEntryCreator(null);

            List<IIpsObjectPathEntry> resolveEntries = ipsContainer4JdtClasspathContainer.resolveEntries();

            assertEquals(0, resolveEntries.size());
        }
    }

    @Test
    public void testResolveEntries_emptyContainerCached() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            mockEntryCreator(null);
            List<IIpsObjectPathEntry> expectedEntries = ipsContainer4JdtClasspathContainer.resolveEntries();

            List<IIpsObjectPathEntry> sameEntries = ipsContainer4JdtClasspathContainer.resolveEntries();

            assertEquals(expectedEntries, sameEntries);
        }
    }

    private void mockEntryCreator(IIpsObjectPathEntry objectPathEntry) throws Exception {
        mockJdtClasspathResolver();
        JdtClasspathEntryCreator entryCreator = mock(JdtClasspathEntryCreator.class);
        ipsContainer4JdtClasspathContainer.setEntryCreator(entryCreator);
        when(jdtContainer.getClasspathEntries()).thenReturn(new IClasspathEntry[] { entry });
        when(entryCreator.createIpsEntry(entry)).thenReturn(objectPathEntry);
    }

    @Test
    public void testFindClasspathContainer_notJavaProject() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            IClasspathContainer classpathContainer = ipsContainer4JdtClasspathContainer.findClasspathContainer();

            assertNull(classpathContainer);
        }
    }

    @Test
    public void testFindClasspathContainer_emptyClasspath() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            mockProject();

            IClasspathContainer classpathContainer = ipsContainer4JdtClasspathContainer.findClasspathContainer();

            assertNull(classpathContainer);
        }
    }

    @Test
    public void testFindClasspathContainer_existingEntry() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            mockJdtClasspathResolver();

            IClasspathContainer classpathContainer = ipsContainer4JdtClasspathContainer.findClasspathContainer();

            assertEquals(jdtContainer, classpathContainer);
        }
    }

    private void mockJdtClasspathResolver() throws Exception {
        mockJdtClasspathResolver(jdtContainer);
    }

    private void mockJdtClasspathResolver(IClasspathContainer container) throws Exception {
        mockProjectAndClasspath();
        IPath classpathContainerPath = new Path(optionalPath);
        JdtClasspathResolver containerResolver = mock(JdtClasspathResolver.class);
        ipsContainer4JdtClasspathContainer.setContainerResolver(containerResolver);
        when(containerResolver.getClasspathContainer(javaProject, classpathContainerPath)).thenReturn(container);
        when(containerResolver.getResolvedClasspathEntry(entry)).thenReturn(entry);
    }

    private void mockProjectAndClasspath() throws Exception {
        mockProject();
        when(entry.getEntryKind()).thenReturn(IClasspathEntry.CPE_CONTAINER);
        when(entry.getPath()).thenReturn(new Path(optionalPath));
        when(javaProject.getRawClasspath()).thenReturn(new IClasspathEntry[] { entry });
    }

    private void mockProject() throws Exception {
        when(ipsProject.getJavaProject()).thenReturn(Wrappers.wrap(javaProject).as(AJavaProject.class));
    }

}
