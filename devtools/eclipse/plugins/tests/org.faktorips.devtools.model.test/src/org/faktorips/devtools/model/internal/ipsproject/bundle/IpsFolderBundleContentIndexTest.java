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
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IpsFolderBundleContentIndexTest {

    @Mock
    private FolderExplorer indexer;
    private Path srcPath;
    private Path testPath;
    private Path srcFile;
    private Path srcFile2;
    private Path srcCoveragesPath;
    private Path srcFileCoverage;
    private Path srcFileCoverage2;
    private Path testFile;
    private IpsFolderBundleContentIndex contentIndex;
    private Path srcPathAbsolute;
    private Path testPathAbsolute;
    private Path ressourceFile;
    private Path ressourceFileCoverage;

    @Before
    public void setUp() {
        Path bundleRoot = Path.of("/root/base/folder");

        srcPath = Path.of("src");
        srcPathAbsolute = bundleRoot.resolve(srcPath);

        testPath = Path.of("test");
        testPathAbsolute = bundleRoot.resolve(testPath);

        List<Path> modelFolders = Arrays.asList(srcPath, testPath);

        srcCoveragesPath = srcPathAbsolute.resolve("coverage");
        Path srcEmptyPath = srcPathAbsolute.resolve("empty");

        List<Path> srcDirs = Arrays.asList(srcCoveragesPath, srcEmptyPath);

        when(indexer.getFolders(srcPathAbsolute)).thenReturn(srcDirs);

        srcFile = Path.of("policy.ipsproductcmpttype");
        srcFile2 = Path.of("contract.ipsproductcmpttype");

        ressourceFile = Path.of("res.txt");

        List<Path> srcFiles = makeAbsolutePaths(srcPathAbsolute, srcFile, srcFile2, ressourceFile);

        when(indexer.getFiles(srcPathAbsolute)).thenReturn(srcFiles);

        when(indexer.getFolders(srcCoveragesPath)).thenReturn(new ArrayList<>());

        srcFileCoverage = Path.of("coverage/basecoverage.ipsproductcmpttype");
        srcFileCoverage2 = Path.of("coverage/additionalcoverage.ipsproductcmpttype");
        ressourceFileCoverage = Path.of("coverage/resCov.txt");

        List<Path> srcFilesCoverage = makeAbsolutePaths(srcPathAbsolute, srcFileCoverage, srcFileCoverage2,
                ressourceFileCoverage);
        when(indexer.getFiles(srcCoveragesPath)).thenReturn(srcFilesCoverage);

        when(indexer.getFolders(srcEmptyPath)).thenReturn(new ArrayList<>());
        when(indexer.getFiles(srcEmptyPath)).thenReturn(new ArrayList<>());

        when(indexer.getFolders(testPathAbsolute)).thenReturn(new ArrayList<>());

        testFile = Path.of("test.ipstestcasetype");
        List<Path> testFiles = makeAbsolutePaths(testPathAbsolute, testFile);
        when(indexer.getFiles(testPathAbsolute)).thenReturn(testFiles);

        contentIndex = new IpsFolderBundleContentIndex(modelFolders, bundleRoot, indexer);
    }

    private List<Path> makeAbsolutePaths(Path base, Path... files) {
        List<Path> srcFiles = new ArrayList<>();
        for (Path iPath : Arrays.asList(files)) {
            srcFiles.add(base.resolve(iPath));
        }
        return srcFiles;
    }

    @Test
    public void testGetModelPathDefaultPackage() {

        assertEquals(srcPath, contentIndex.getModelPath(srcFile));
        assertEquals(srcPath, contentIndex.getModelPath(srcFile2));

        assertEquals(srcPath, contentIndex.getModelPath(ressourceFile));

        assertEquals(testPath, contentIndex.getModelPath(testFile));
    }

    @Test
    public void testGetModelPath() {
        assertEquals(srcPath, contentIndex.getModelPath(srcFileCoverage));
        assertEquals(srcPath, contentIndex.getModelPath(srcFileCoverage2));
        assertEquals(srcPath, contentIndex.getModelPath(ressourceFileCoverage));

    }

    @Test
    public void testGetNonEmptyPackagePaths() {
        Set<String> nonEmptyPackagePaths = contentIndex.getNonEmptyPackagePaths();

        assertEquals(2, nonEmptyPackagePaths.size());
        assertTrue(nonEmptyPackagePaths.contains(IpsStringUtils.EMPTY));
        assertTrue(nonEmptyPackagePaths.contains("coverage"));
    }

    @Test
    public void testGetQualifiedNameTypes() {
        Set<QualifiedNameType> qualifiedNameTypes = contentIndex.getQualifiedNameTypes();

        assertEquals(5, qualifiedNameTypes.size());
    }

    @Test
    public void testGetQualifiedNameTypesPackageName() {
        Set<QualifiedNameType> defaultTypes = contentIndex.getQualifiedNameTypes("");
        assertEquals(3, defaultTypes.size());

        Set<QualifiedNameType> coveragesTypes = contentIndex.getQualifiedNameTypes("coverage");
        assertEquals(2, coveragesTypes.size());

        assertTrue(contentIndex.getQualifiedNameTypes("empty").isEmpty());
    }
}
