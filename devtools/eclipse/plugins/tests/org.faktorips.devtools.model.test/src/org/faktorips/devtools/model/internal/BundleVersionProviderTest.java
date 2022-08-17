/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BundleVersionProviderTest {
    private static final String VERSION_STRING = "1.2.3.test";

    private static final String VERSION_STRING_NEW = "1.2.4.test";

    private BundleVersionProvider provider;

    private BundleVersionProvider providerSpy;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private AProject project;

    @Mock
    private AFile file;

    @Mock
    private Attributes attributes;

    @Mock
    private InputStream inputStream;

    @Before
    public void setUp() throws IOException {
        doReturn(project).when(ipsProject).getProject();
        when(project.getFile(JarFile.MANIFEST_NAME)).thenReturn(file);
        doReturn(inputStream).when(file).getContents();

        provider = new BundleVersionProvider(ipsProject);
        providerSpy = spy(provider);

        doReturn(attributes).when(providerSpy).getManifestMainAttributes();
    }

    @Test
    public void test_getVersion() {

        IVersion<OsgiVersion> version = providerSpy.getVersion(VERSION_STRING);

        assertNotNull(version);
    }

    @Test
    public void test_getProjectVersion() {
        when(providerSpy.getManifestMainAttributes()).thenReturn(attributes);
        when(attributes.getValue(org.osgi.framework.Constants.BUNDLE_VERSION)).thenReturn(VERSION_STRING);

        IVersion<OsgiVersion> version = providerSpy.getProjectVersion();

        assertNotNull(version);
        assertEquals(version.asString(), VERSION_STRING);
    }

    @Test
    public void test_setProjectVersion() {
        when(project.getFile(JarFile.MANIFEST_NAME)).thenReturn(file);

        providerSpy.setProjectVersion(new OsgiVersion(VERSION_STRING_NEW));

        verify(attributes).putValue(org.osgi.framework.Constants.BUNDLE_VERSION, VERSION_STRING_NEW);
    }
}
