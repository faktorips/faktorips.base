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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IpsJarBundleTest {

    private static final String ROOT_PATH = "root";

    private static final String ANY_PATH = "anyPath";

    private static final String JAR_NAME = "any/folder/test.jar";

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private JarFileFactory jarFileFactory;

    @Mock
    private JarFile jarFile;

    @Mock
    private QualifiedNameType qualifiedNameType;

    @Mock
    private IpsJarBundleContentIndex bundleContentIndex;

    private IpsJarBundle ipsJarBundle;

    @Before
    public void createIpsJarBundle() throws Exception {
        when(jarFileFactory.createJarFile()).thenReturn(jarFile);
        Path path = Path.of(JAR_NAME);
        when(jarFileFactory.getJarPath()).thenReturn(path);
        ipsJarBundle = new IpsJarBundle(ipsProject, jarFileFactory);
        ipsJarBundle.setBundleContentIndex(bundleContentIndex);
    }

    @Test
    public void testInitJarFile() throws Exception {
        Manifest manifest = mock(Manifest.class);
        when(jarFile.getManifest()).thenReturn(manifest);
        when(jarFile.entries()).thenReturn(Collections.enumeration(new ArrayList<>()));

        ipsJarBundle.initBundle();

        assertNotNull(ipsJarBundle.getBundleManifest());
    }

    @Test
    public void testIsValid() throws Exception {
        Manifest manifest = mock(Manifest.class);
        when(jarFile.getManifest()).thenReturn(manifest);
        when(jarFile.entries()).thenReturn(Collections.enumeration(new ArrayList<>()));
        ipsJarBundle.initBundle();
        mockObjectDirs();

        assertTrue(ipsJarBundle.isValid());
    }

    @Test
    public void testGetResourcePath() {
        Path rootFolder = Path.of(ROOT_PATH);
        Path element = Path.of(ANY_PATH);
        when(ipsJarBundle.getRootFolder(element)).thenReturn(rootFolder);

        Path result = ipsJarBundle.getResourcePath(element);

        assertThat(result, is(rootFolder.resolve(element)));
    }

    private void mockObjectDirs() {
        IpsBundleManifest bundleManifest = spy(new IpsBundleManifest(mock(Manifest.class)));
        ipsJarBundle.setBundleManifest(bundleManifest);
        List<Path> pathList = new ArrayList<>();
        pathList.add(Path.of(ANY_PATH));
        doReturn(pathList).when(bundleManifest).getObjectDirs();
    }

    @Test
    public void testIsValid_ioException() throws Exception {
        assertFalse(ipsJarBundle.isValid());
    }

    @Test
    public void testIsValid_invalid_NoManifest() throws Exception {
        assertFalse(ipsJarBundle.isValid());
    }

    @Test
    public void testIsValid_invalid_EmptyObjectDirs() throws Exception {
        Manifest manifest = mock(Manifest.class);
        when(jarFile.getManifest()).thenReturn(manifest);
        when(jarFile.entries()).thenReturn(Collections.enumeration(new ArrayList<>()));
        ipsJarBundle.initBundle();

        assertFalse(ipsJarBundle.isValid());
    }

    @Test
    public void testGetContent() throws Exception {
        mockQualifiedNameTypes();
        mockJarFileWithResource();

        InputStream ressourceAsStream = ipsJarBundle.getContent(qualifiedNameType.toPath());

        assertEquals('F', ressourceAsStream.read());
        assertEquals('o', ressourceAsStream.read());
        assertEquals('o', ressourceAsStream.read());
        verify(jarFileFactory).closeJarFile();
    }

    @Test
    public void testGetResourceAsStream() throws Exception {
        mockJarFileWithResource();

        InputStream ressourceAsStream = ipsJarBundle.getResourceAsStream(ANY_PATH);

        assertEquals('F', ressourceAsStream.read());
        assertEquals('o', ressourceAsStream.read());
        assertEquals('o', ressourceAsStream.read());
        verify(jarFileFactory).closeJarFile();
    }

    private void mockJarFileWithResource() throws IOException {
        Path rootPath = Path.of(ANY_PATH);
        when(bundleContentIndex.getModelPath(rootPath)).thenReturn(Path.of(ROOT_PATH));
        ZipEntry zipEntry = mock(ZipEntry.class);
        when(jarFile.getEntry(ROOT_PATH + "/" + ANY_PATH)).thenReturn(zipEntry);
        InputStream inputStream = new ByteArrayInputStream("Foo".getBytes());
        when(jarFile.getInputStream(zipEntry)).thenReturn(inputStream);
    }

    @Test
    public void testGetLocation() throws Exception {
        Path location = ipsJarBundle.getLocation();

        assertEquals(Path.of(JAR_NAME), location);
    }

    @Test
    public void testGetArchivePath() throws Exception {
        Path location = ipsJarBundle.getLocation();

        assertEquals(Path.of(JAR_NAME), location);
    }

    @Test
    public void testGetRootFolder() throws Exception {
        Path myPath = Path.of("myTestPath");
        Path objectDir = Path.of("myObjectDir");
        when(bundleContentIndex.getModelPath(objectDir)).thenReturn(myPath);
        ipsJarBundle.setBundleContentIndex(bundleContentIndex);

        Path rootFolder = ipsJarBundle.getRootFolder(objectDir);

        assertEquals(myPath, rootFolder);
    }

    @Test
    public void testGetNonEmptyPackages_empty() throws Exception {
        when(bundleContentIndex.getNonEmptyPackagePaths()).thenReturn(new HashSet<>());

        String[] nonEmptyPackages = ipsJarBundle.getNonEmptyPackages();

        assertEquals(0, nonEmptyPackages.length);
    }

    @Test
    public void testGetNonEmptyPackages_notEmpty() throws Exception {
        HashSet<String> packagePaths = new HashSet<>();
        packagePaths.add("org.test.one");
        packagePaths.add("org.test.any.two");
        when(bundleContentIndex.getNonEmptyPackagePaths()).thenReturn(packagePaths);

        List<String> nonEmptyPackages = Arrays.asList(ipsJarBundle.getNonEmptyPackages());

        assertEquals(2, nonEmptyPackages.size());
        assertThat(nonEmptyPackages, hasItem("org.test.one"));
        assertThat(nonEmptyPackages, hasItem("org.test.any.two"));
    }

    @Test
    public void testContainsPackage_false() throws Exception {
        HashSet<String> packagePaths = new HashSet<>();
        packagePaths.add("org.test.one");
        when(bundleContentIndex.getNonEmptyPackagePaths()).thenReturn(packagePaths);

        boolean result = ipsJarBundle.containsPackage("org.not.in.jar");

        assertEquals(false, result);
    }

    @Test
    public void testContainsPackage_true() throws Exception {
        HashSet<String> packagePaths = new HashSet<>();
        packagePaths.add("org.test.one");
        when(bundleContentIndex.getNonEmptyPackagePaths()).thenReturn(packagePaths);

        boolean result = ipsJarBundle.containsPackage("org.test.one");

        assertEquals(true, result);
    }

    @Test
    public void testContainsPackage_defaultPackage() throws Exception {

        boolean result = ipsJarBundle.containsPackage("");

        assertEquals(true, result);
    }

    @Test
    public void testContainsPackage_subPackage() throws Exception {
        HashSet<String> packagePaths = new HashSet<>();
        packagePaths.add("org.test.one");
        when(bundleContentIndex.getNonEmptyPackagePaths()).thenReturn(packagePaths);

        boolean result = ipsJarBundle.containsPackage("org.test");

        assertEquals(true, result);
    }

    @Test
    public void testContainsPackage_invalidSubPackage() throws Exception {
        HashSet<String> packagePaths = new HashSet<>();
        packagePaths.add("org.test.one");
        when(bundleContentIndex.getNonEmptyPackagePaths()).thenReturn(packagePaths);

        boolean result = ipsJarBundle.containsPackage("org.te");

        assertEquals(false, result);
    }

    @Test
    public void testGetNonEmptySubpackages_fountNone() throws Exception {
        HashSet<String> packagePaths = new HashSet<>();
        packagePaths.add("org.test.one");
        when(bundleContentIndex.getNonEmptyPackagePaths()).thenReturn(packagePaths);

        String[] result = ipsJarBundle.getNonEmptySubpackages("org.te");

        assertEquals(0, result.length);
    }

    @Test
    public void testGetNonEmptySubpackages_defaultPackage() throws Exception {
        HashSet<String> packagePaths = new HashSet<>();
        packagePaths.add("org");
        packagePaths.add("de");
        when(bundleContentIndex.getNonEmptyPackagePaths()).thenReturn(packagePaths);

        List<String> result = Arrays.asList(ipsJarBundle.getNonEmptySubpackages(""));

        assertEquals(2, result.size());
        assertThat(result, hasItem("org"));
        assertThat(result, hasItem("de"));
    }

    @Test
    public void testGetNonEmptySubpackages() throws Exception {
        HashSet<String> packagePaths = new HashSet<>();
        packagePaths.add("org.test.one");
        packagePaths.add("org.one");
        when(bundleContentIndex.getNonEmptyPackagePaths()).thenReturn(packagePaths);

        String[] result = ipsJarBundle.getNonEmptySubpackages("org.test");

        assertEquals(1, result.length);
        assertEquals("org.test.one", result[0]);
    }

    @Test
    public void testGetQNameTypes() throws Exception {
        HashSet<QualifiedNameType> qnameTypes = new HashSet<>();
        when(bundleContentIndex.getQualifiedNameTypes()).thenReturn(qnameTypes);

        Set<QualifiedNameType> result = ipsJarBundle.getQNameTypes();

        assertSame(qnameTypes, result);
    }

    @Test
    public void testGetQNameTypes_forPackageName() throws Exception {
        HashSet<QualifiedNameType> qnameTypes = new HashSet<>();
        when(bundleContentIndex.getQualifiedNameTypes("anyPackageName")).thenReturn(qnameTypes);

        Set<QualifiedNameType> result = ipsJarBundle.getQNameTypes("anyPackageName");

        assertSame(qnameTypes, result);
    }

    @Test
    public void testContains_notFound() throws Exception {

        boolean contained = ipsJarBundle.contains(qualifiedNameType.toPath());

        assertFalse(contained);
    }

    @Test
    public void testContains_found() throws Exception {
        mockQualifiedNameTypes();

        boolean contained = ipsJarBundle.contains(qualifiedNameType.toPath());

        assertTrue(contained);
    }

    private void mockQualifiedNameTypes() {
        when(qualifiedNameType.toPath()).thenReturn(Path.of(ANY_PATH));
        HashSet<QualifiedNameType> qnameTypes = new HashSet<>();
        qnameTypes.add(qualifiedNameType);
        when(bundleContentIndex.getModelPath(qualifiedNameType.toPath())).thenReturn(Path.of("modelPath"));
    }

}
