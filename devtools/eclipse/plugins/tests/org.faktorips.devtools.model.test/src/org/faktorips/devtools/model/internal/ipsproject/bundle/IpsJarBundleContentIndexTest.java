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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IpsJarBundleContentIndexTest {

    @Mock
    private JarFile jarFile;

    private List<JarEntry> jarEntries;

    private Path pathRelative;

    private Path pathAbsolute;

    private Path pathTrailing;

    private QualifiedNameType qntRelative;

    private QualifiedNameType qntAbsolute;

    private QualifiedNameType qntTrailing;

    @Before
    public void setUp() {
        jarEntries = new ArrayList<>();

        pathRelative = Path.of("src/model");
        pathAbsolute = Path.of("/src2/model");
        pathTrailing = Path.of("src3/model/");

        JarEntry entryRelative = createJarEntry(pathRelative + "/life/policy.ipspolicycmpttype");
        JarEntry entryAbsolute = createJarEntry(pathAbsolute + "/base/sub/coverage.ipspolicycmpttype");
        JarEntry entryTrailing = createJarEntry(pathTrailing + "/base/sub/reduction.ipspolicycmpttype");
        JarEntry entryNoIpsObject = createJarEntry(pathRelative + "/base/non.sens");

        jarEntries.add(entryRelative);
        jarEntries.add(entryAbsolute);
        jarEntries.add(entryTrailing);
        jarEntries.add(entryNoIpsObject);

        Enumeration<JarEntry> entries = Collections.enumeration(jarEntries);
        when(jarFile.entries()).thenReturn(entries);

        qntRelative = new QualifiedNameType("life.policy", IpsObjectType.POLICY_CMPT_TYPE);
        qntAbsolute = new QualifiedNameType("base.sub.coverage", IpsObjectType.POLICY_CMPT_TYPE);
        qntTrailing = new QualifiedNameType("base.sub.reduction", IpsObjectType.POLICY_CMPT_TYPE);
    }

    @Test
    public void testGetModelPath() {

        List<Path> modelFolders = Arrays.asList(pathRelative, pathAbsolute, pathTrailing);
        IpsJarBundleContentIndex index = new IpsJarBundleContentIndex(jarFile, modelFolders);

        assertEquals(pathRelative, index.getModelPath(Path.of("life/policy.ipspolicycmpttype")));
        assertEquals(pathAbsolute, index.getModelPath(Path.of("base/sub/coverage.ipspolicycmpttype")));
        assertEquals(pathTrailing, index.getModelPath(Path.of("base/sub/reduction.ipspolicycmpttype")));

    }

    @Test
    public void testGetQualifiedNameTypes() {

        List<Path> modelFolders = Arrays.asList(pathRelative, pathAbsolute, pathTrailing);
        IpsJarBundleContentIndex index = new IpsJarBundleContentIndex(jarFile, modelFolders);

        Set<QualifiedNameType> qualifiedNameTypes = index.getQualifiedNameTypes();

        assertEquals(3, qualifiedNameTypes.size());

        assertTrue(qualifiedNameTypes.contains(qntRelative));
        assertTrue(qualifiedNameTypes.contains(qntAbsolute));
        assertTrue(qualifiedNameTypes.contains(qntTrailing));
    }

    @Test
    public void testGetQualifiedNameTypesByPackage() {

        List<Path> modelFolders = Arrays.asList(pathRelative, pathAbsolute, pathTrailing);
        IpsJarBundleContentIndex index = new IpsJarBundleContentIndex(jarFile, modelFolders);

        Set<QualifiedNameType> qualifiedNameTypesRoot = index.getQualifiedNameTypes("");
        assertTrue("Size instead of 0: " + qualifiedNameTypesRoot.size(), qualifiedNameTypesRoot.isEmpty());
        assertTrue(index.getQualifiedNameTypes("base.household").isEmpty());

        Set<QualifiedNameType> qualifiedNameTypes = index.getQualifiedNameTypes("base.sub");

        assertEquals(2, qualifiedNameTypes.size());

        assertTrue(qualifiedNameTypes.contains(qntAbsolute));
        assertTrue(qualifiedNameTypes.contains(qntTrailing));
    }

    @Test
    public void testGetNonEmptyPackagePaths() {

        List<Path> modelFolders = Arrays.asList(pathRelative, pathAbsolute, pathTrailing);
        IpsJarBundleContentIndex index = new IpsJarBundleContentIndex(jarFile, modelFolders);

        Set<String> nonEmptyPackagePaths = index.getNonEmptyPackagePaths();

        assertEquals(2, nonEmptyPackagePaths.size());

        assertTrue(nonEmptyPackagePaths.contains("base.sub"));
        assertTrue(nonEmptyPackagePaths.contains("life"));
    }

    private JarEntry createJarEntry(String name) {
        JarEntry entry = mock(JarEntry.class);
        when(entry.getName()).thenReturn(name);

        return entry;
    }
}
