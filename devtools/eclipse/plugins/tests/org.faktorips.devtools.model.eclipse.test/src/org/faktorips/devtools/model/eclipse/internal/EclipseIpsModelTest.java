/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.eclipse.internal.EclipseImplementation;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.stubbing.Answer;

public class EclipseIpsModelTest extends AbstractIpsPluginTest {

    @Category(EclipseImplementation.class)
    @Test
    public void testForceReloadOfCachedIpsSrcFileContents_forExternalResources() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            IIpsProject ipsProject = newIpsProject();
            IpsModel ipsModel = (IpsModel)ipsProject.getIpsModel();

            // Resource does not exist in workspace, therefore will always return -1 as modification
            // stamp
            AResource externalResource = ipsProject.getProject().getFile("foo.bar");
            IpsObjectType ipsObjectType = mock(IpsObjectType.class);
            IpsSrcFile ipsSrcFile = mock(IpsSrcFile.class);
            when(ipsSrcFile.exists()).thenReturn(true);
            when(ipsSrcFile.getIpsObjectType()).thenReturn(ipsObjectType);
            when(ipsSrcFile.getEnclosingResource()).thenReturn(externalResource);
            when(ipsSrcFile.getContentFromEnclosingResource()).thenAnswer(withNewXmlInputStream());

            IpsObject ipsObject = mock(IpsObject.class);
            when(ipsObjectType.newObject(ipsSrcFile)).thenReturn(ipsObject);
            when(ipsObject.getIpsSrcFile()).thenReturn(ipsSrcFile);

            when(ipsObject.getIpsProject()).thenReturn(ipsProject);

            // prime the cache
            ipsModel.getIpsSrcFileContent(ipsSrcFile);

            // clear the cache
            IResourceChangeEvent event = mock(IResourceChangeEvent.class);
            when(event.getType()).thenReturn(IResourceChangeEvent.PRE_REFRESH);
            ((EclipseIpsModel)ipsModel).resourceChanged(event);

            // reload, as cache entry should be marked invalid
            ipsModel.getIpsSrcFileContent(ipsSrcFile);
            verify(ipsSrcFile, times(2)).getContentFromEnclosingResource();
        }
    }

    private Answer<InputStream> withNewXmlInputStream() {
        return $ -> new ByteArrayInputStream("<Foo/>".getBytes());
    }

}
