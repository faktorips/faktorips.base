/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
