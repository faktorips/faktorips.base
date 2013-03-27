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

package org.faktorips.devtools.core.internal.model.ipsproject.bundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsFolderBundleContentIndexTest {

    @Mock
    private FolderExplorer indexer;
    private IPath srcPath;
    private IPath testPath;
    private IPath srcFile;
    private IPath srcFile2;
    private IPath srcCoveragesPath;
    private IPath srcFileCoverage;
    private IPath srcFileCoverage2;
    private IPath testFile;
    private IpsFolderBundleContentIndex contentIndex;
    private IPath srcPathAbsolute;
    private IPath testPathAbsolute;
    private Path ressourceFile;
    private Path ressourceFileCoverage;

    @Before
    public void setUp() {
        IPath bundleRoot = new Path("/root/base/folder");

        srcPath = new Path("src");
        srcPathAbsolute = bundleRoot.append(srcPath);

        testPath = new Path("test");
        testPathAbsolute = bundleRoot.append(testPath);

        List<IPath> modelFolders = Arrays.asList(srcPath, testPath);

        srcCoveragesPath = srcPathAbsolute.append("coverage");
        IPath srcEmptyPath = srcPathAbsolute.append("empty");

        List<IPath> srcDirs = Arrays.asList(srcCoveragesPath, srcEmptyPath);

        when(indexer.getFolders(srcPathAbsolute)).thenReturn(srcDirs);

        srcFile = new Path("policy.ipsproductcmpttype");
        srcFile2 = new Path("contract.ipsproductcmpttype");

        ressourceFile = new Path("res.txt");

        List<IPath> srcFiles = makeAbsolutePaths(srcPathAbsolute, srcFile, srcFile2, ressourceFile);

        when(indexer.getFiles(srcPathAbsolute)).thenReturn(srcFiles);

        when(indexer.getFolders(srcCoveragesPath)).thenReturn(new ArrayList<IPath>());

        srcFileCoverage = new Path("coverage/basecoverage.ipsproductcmpttype");
        srcFileCoverage2 = new Path("coverage/additionalcoverage.ipsproductcmpttype");
        ressourceFileCoverage = new Path("coverage/resCov.txt");

        List<IPath> srcFilesCoverage = makeAbsolutePaths(srcPathAbsolute, srcFileCoverage, srcFileCoverage2,
                ressourceFileCoverage);
        when(indexer.getFiles(srcCoveragesPath)).thenReturn(srcFilesCoverage);

        when(indexer.getFolders(srcEmptyPath)).thenReturn(new ArrayList<IPath>());
        when(indexer.getFiles(srcEmptyPath)).thenReturn(new ArrayList<IPath>());

        when(indexer.getFolders(testPathAbsolute)).thenReturn(new ArrayList<IPath>());

        testFile = new Path("test.ipstestcasetype");
        List<IPath> testFiles = makeAbsolutePaths(testPathAbsolute, testFile);
        when(indexer.getFiles(testPathAbsolute)).thenReturn(testFiles);

        contentIndex = new IpsFolderBundleContentIndex(modelFolders, bundleRoot, indexer);
    }

    private List<IPath> makeAbsolutePaths(IPath base, IPath... files) {
        List<IPath> srcFiles = new ArrayList<IPath>();
        for (IPath iPath : Arrays.asList(files)) {
            srcFiles.add(base.append(iPath));
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
        assertTrue(nonEmptyPackagePaths.contains(StringUtils.EMPTY));
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
