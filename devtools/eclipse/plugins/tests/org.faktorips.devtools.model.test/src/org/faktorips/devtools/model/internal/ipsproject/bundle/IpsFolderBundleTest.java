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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.jar.Manifest;

import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.internal.ipsproject.bundle.IpsFolderBundle.IOFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IpsFolderBundleTest {

    @Mock
    private Path folder;

    @Mock
    private Path manifestPath;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private File manifestFile;

    @Mock
    private FileInputStream expectedInputStream;

    @Mock
    private IOFactory ioFactory;

    @Mock
    private Path absolutePath;

    @Mock
    private Path myPath;

    @Mock
    private AbstractIpsBundleContentIndex bundleContentIndex;

    private IpsFolderBundle ipsFolderBundle;

    @Before
    public void createIpsFolderBundle() throws Exception {
        ipsFolderBundle = new IpsFolderBundle(ipsProject, folder);
        ipsFolderBundle.setIOFactory(ioFactory);
    }

    @Test
    public void testGetManifest() throws Exception {
        mockManifestFile();

        Manifest manifest = ipsFolderBundle.getManifest();

        assertNotNull(manifest);
    }

    @Test(expected = IOException.class)
    public void testGetManifest_exception() throws Exception {
        mockManifestFile();
        when(ioFactory.createInputStream(manifestFile)).thenThrow(new FileNotFoundException());

        ipsFolderBundle.getManifest();
    }

    @Test
    public void testInitBundle() throws Exception {
        mockManifestFile();

        ipsFolderBundle.initBundle();

        assertNotNull(ipsFolderBundle.getBundleManifest());
        assertNotNull(ipsFolderBundle.getBundleContentIndex());
    }

    private void mockManifestFile() throws FileNotFoundException {
        when(folder.resolve(IpsBundleManifest.MANIFEST_NAME)).thenReturn(manifestPath);
        when(manifestPath.toFile()).thenReturn(manifestFile);
        when(ioFactory.createInputStream(manifestFile)).thenReturn(expectedInputStream);
    }

    @Test
    public void testGetResourceAsStream() throws Exception {
        when(folder.resolve(myPath)).thenReturn(absolutePath);
        File file = mock(File.class);
        when(absolutePath.toFile()).thenReturn(file);
        when(ioFactory.createInputStream(file)).thenReturn(expectedInputStream);

        InputStream inputStream = ipsFolderBundle.getResourceAsStream(myPath);

        assertEquals(expectedInputStream, inputStream);
    }

    @Test
    public void testGetResourcePath() {
        ipsFolderBundle = new IpsFolderBundle(ipsProject, folder);
        ipsFolderBundle.setIOFactory(ioFactory);
        ipsFolderBundle.setBundleContentIndex(bundleContentIndex);
        Path rootFolder = Path.of("any/folder/test");
        Path element = Path.of("anyPath");
        when(ipsFolderBundle.getRootFolder(element)).thenReturn(rootFolder);

        Path result = ipsFolderBundle.getResourcePath(element);

        assertThat(result, is(rootFolder.resolve(element)));
    }

}
