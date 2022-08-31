/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.bundle;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.bundle.IpsBundleEntry.IpsStorageFactory;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IpsBundleEntryTest {

    @Mock
    private IpsObjectPath ipsObjectPath;

    @Mock
    private IpsJarBundle ipsJarBundle;

    @Mock
    private IpsFolderBundle ipsFolderBundle;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IpsStorageFactory ipsStorageFactory;

    @Mock
    private Path bundlePath;

    private IpsBundleEntry ipsBundleEntry;

    @Before
    public void createIpsJarBundleEntry() throws Exception {
        ipsBundleEntry = new IpsBundleEntry(ipsObjectPath);
    }

    @Test
    public void testValidate_valid() throws Exception {
        initStorage();
        when(ipsJarBundle.isValid()).thenReturn(true);

        MessageList messages = ipsBundleEntry.validate();

        assertNull(messages.getMessageByCode(IpsBundleEntry.MSGCODE_MISSING_BUNDLE));
    }

    @Test
    public void testValidate_invalid() throws Exception {
        initStorage();
        when(ipsJarBundle.isValid()).thenReturn(false);

        MessageList messages = ipsBundleEntry.validate();

        assertNotNull(messages.getMessageByCode(IpsBundleEntry.MSGCODE_MISSING_BUNDLE));
    }

    @Test
    public void testValidate_noInit() throws Exception {
        MessageList messages = ipsBundleEntry.validate();

        assertNotNull(messages.getMessageByCode(IpsBundleEntry.MSGCODE_MISSING_BUNDLE));
    }

    @Test
    public void testGetResourceAsStream() throws Exception {
        initStorage();
        ipsBundleEntry.getResourceAsStream("testAnyPath");

        verify(ipsJarBundle).getResourceAsStream("testAnyPath");
    }

    @Test
    public void testGetIpsPackageFragmentRootName() throws Exception {
        initStorage();
        when(ipsJarBundle.getLocation()).thenReturn(Path.of("any/where/test.jar"));

        String rootName = ipsBundleEntry.getIpsPackageFragmentRootName();

        assertEquals("test.jar", rootName);
    }

    @Test
    public void testExists_existing() throws Exception {
        initStorage();
        QualifiedNameType qnt = mock(QualifiedNameType.class);
        when(qnt.toPath()).thenReturn(Path.of("myObjectPath"));
        when(ipsJarBundle.contains(qnt.toPath())).thenReturn(true);

        boolean exists = ipsBundleEntry.exists(qnt);

        assertTrue(exists);
    }

    @Test
    public void testExists_notExisting() throws Exception {
        initStorage();
        QualifiedNameType qnt = mock(QualifiedNameType.class);

        boolean exists = ipsBundleEntry.exists(qnt);

        assertFalse(exists);
    }

    @Test
    public void testInitStorage_folder() throws Exception {
        mockInitDependencies(true);

        ipsBundleEntry.initStorage(bundlePath);

        assertThat(ipsBundleEntry.getIpsStorage(), instanceOf(IpsFolderBundle.class));
    }

    @Test
    public void testInitStorage_folderInitException() throws Exception {
        mockInitDependencies(true);
        doThrow(new IOException()).when(ipsFolderBundle).initBundle();

        try {
            ipsBundleEntry.initStorage(bundlePath);
            fail("IOException expected");
        } catch (IOException e) {
            assertNull(ipsBundleEntry.getIpsStorage());
        }
    }

    @Test
    public void testInitStorage_jar() throws Exception {
        mockInitDependencies(false);

        ipsBundleEntry.initStorage(bundlePath);

        assertThat(ipsBundleEntry.getIpsStorage(), instanceOf(IpsJarBundle.class));
    }

    @Test
    public void testInitStorage_jarInitException() throws Exception {
        mockInitDependencies(false);
        doThrow(new IOException()).when(ipsJarBundle).initBundle();

        try {
            ipsBundleEntry.initStorage(bundlePath);
            fail("IOException expected");
        } catch (IOException e) {
            assertNull(ipsBundleEntry.getIpsStorage());
        }
    }

    private void initStorage() throws IOException {
        mockIpsStorageFactory();
        ipsBundleEntry.initStorage(bundlePath);
    }

    private void mockIpsStorageFactory() {
        when(ipsObjectPath.getIpsProject()).thenReturn(ipsProject);
        File file = mock(File.class);
        when(bundlePath.toFile()).thenReturn(file);
        ipsBundleEntry.setIpsStorageFactory(ipsStorageFactory);
        when(ipsStorageFactory.createFolderBundle(ipsProject, bundlePath)).thenReturn(ipsFolderBundle);
        when(ipsStorageFactory.createJarBundle(eq(ipsProject), any(JarFileFactory.class))).thenReturn(ipsJarBundle);
    }

    private void mockInitDependencies(boolean directory) {
        mockIpsStorageFactory();
        File file = mock(File.class);
        when(bundlePath.toFile()).thenReturn(file);
        when(file.isDirectory()).thenReturn(directory);
    }

    @Test
    public void testContainsResource_true() throws Exception {
        initStorage();
        String path = "myResourcePath";
        when(ipsJarBundle.contains(Path.of(path))).thenReturn(true);

        boolean exists = ipsBundleEntry.containsResource(path);

        assertTrue(exists);
    }

    @Test
    public void testContainsResource_false() throws Exception {
        initStorage();
        String path = "myResourcePath";

        boolean exists = ipsBundleEntry.containsResource(path);

        assertFalse(exists);
    }

}
