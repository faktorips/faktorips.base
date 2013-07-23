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

package org.faktorips.devtools.core.internal.model;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResourceDeltaVisitorTest {

    @Mock
    private IpsModel ipsModel;
    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IProject project;
    @Mock
    private ValidationResultCache validationResultCache;
    private ResourceDeltaVisitor visitor;

    @Before
    public void setUp() {
        when(ipsModel.getValidationResultCache()).thenReturn(validationResultCache);
        when(ipsModel.getIpsObjectTypes()).thenReturn(new IpsObjectType[] {});
        when(ipsModel.getIpsProject(project)).thenReturn(ipsProject);

        visitor = new ResourceDeltaVisitor(ipsModel);

    }

    @Test
    public void testChangeIpsProjectProperties() {
        IResourceDelta delta = mock(IResourceDelta.class);

        IFile resource = mock(IFile.class);
        when(resource.getType()).thenReturn(IResource.FILE);
        when(resource.getProject()).thenReturn(project);
        when(resource.getFileExtension()).thenReturn(IpsProject.PROPERTY_FILE_EXTENSION);

        when(ipsProject.getIpsProjectPropertiesFile()).thenReturn(resource);

        boolean result = visitor.visitInternal(delta, resource);

        verify(validationResultCache).clear();

        assertFalse(result);
    }

    @Test
    public void testChangeManifest() {
        IResourceDelta delta = mock(IResourceDelta.class);

        IFile resource = mock(IFile.class);
        when(resource.exists()).thenReturn(true);
        when(resource.getType()).thenReturn(IResource.FILE);
        when(resource.getProject()).thenReturn(project);
        when(resource.getFileExtension()).thenReturn("MF");
        when(resource.getProjectRelativePath()).thenReturn(new Path(IpsBundleManifest.MANIFEST_NAME));

        boolean result = visitor.visitInternal(delta, resource);

        assertFalse(result);
        verify(ipsModel).clearProjectSpecificCaches(ipsProject);

    }
}
