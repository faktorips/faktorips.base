/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject.bundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsJarBundleContentIndexTest {

    @Mock
    private JarFile jarFile;

    private List<JarEntry> jarEntries;

    private IPath pathRelative;

    private IPath pathAbsolute;

    private IPath pathTrailing;

    private QualifiedNameType qntRelative;

    private QualifiedNameType qntAbsolute;

    private QualifiedNameType qntTrailing;

    @Before
    public void setUp() {
        jarEntries = new ArrayList<JarEntry>();

        pathRelative = new Path("src/model");
        pathAbsolute = new Path("/src2/model");
        pathTrailing = new Path("src3/model/");

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

        List<IPath> modelFolders = Arrays.asList(pathRelative, pathAbsolute, pathTrailing);
        IpsJarBundleContentIndex index = new IpsJarBundleContentIndex(jarFile, modelFolders);

        assertEquals(pathRelative, index.getModelPath(new Path("life/policy.ipspolicycmpttype")));
        assertEquals(pathAbsolute, index.getModelPath(new Path("base/sub/coverage.ipspolicycmpttype")));
        assertEquals(pathTrailing, index.getModelPath(new Path("base/sub/reduction.ipspolicycmpttype")));

    }

    @Test
    public void testGetQualifiedNameTypes() {

        List<IPath> modelFolders = Arrays.asList(pathRelative, pathAbsolute, pathTrailing);
        IpsJarBundleContentIndex index = new IpsJarBundleContentIndex(jarFile, modelFolders);

        Set<QualifiedNameType> qualifiedNameTypes = index.getQualifiedNameTypes();

        assertEquals(3, qualifiedNameTypes.size());

        assertTrue(qualifiedNameTypes.contains(qntRelative));
        assertTrue(qualifiedNameTypes.contains(qntAbsolute));
        assertTrue(qualifiedNameTypes.contains(qntTrailing));
    }

    @Test
    public void testGetQualifiedNameTypesByPackage() {

        List<IPath> modelFolders = Arrays.asList(pathRelative, pathAbsolute, pathTrailing);
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

        List<IPath> modelFolders = Arrays.asList(pathRelative, pathAbsolute, pathTrailing);
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
