/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.osgi.util.ManifestElement;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.util.QNameUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsObjectPathManifestReaderTest {

    private static final String MY_BAS_PACK = "myBasPack";

    private static final String MY_OBJECT_DIR = "myObjectDir";

    private static final String MY_TOC_FILE = "myTocFile";

    private static final String MY_VALIDATION_MESSAGE = "myValidationMessage";

    private static final String MY_SRC_OUT = "mySrcOut";

    private static final String MY_RESOURCE_OUT = "myResourceOut";

    private static final String MY_UNIQUE_QUALIFIER = "qualifier";

    @Mock
    private IpsBundleManifest bundleManifest;

    @Mock
    private IpsProject ipsProject;

    @Mock
    private AFolder myObjectDir;

    @Mock
    private AFolder mySrcOut;

    @Mock
    private AFolder myResourceOut;

    private IpsObjectPathManifestReader objectPathReader;

    @Before
    public void mockIpsProjectAndFolders() {
        AProject project = mock(AProject.class);
        when(ipsProject.getProject()).thenReturn(project);
        when(myObjectDir.getName()).thenReturn(MY_OBJECT_DIR);
        when(project.getFolder(MY_OBJECT_DIR)).thenReturn(myObjectDir);
        when(project.getFolder(MY_SRC_OUT)).thenReturn(mySrcOut);
        when(project.getFolder(MY_RESOURCE_OUT)).thenReturn(myResourceOut);
    }

    @Before
    public void createIpsObjectPathManifestReader() {
        objectPathReader = new IpsObjectPathManifestReader(bundleManifest, ipsProject);
    }

    @Test
    public void testReadIpsObjectPath_emptyOnlyContainer() {
        mockObjectDirElements();

        IIpsObjectPath ipsObjectPath = objectPathReader.readIpsObjectPath();

        assertEquals(1, ipsObjectPath.getEntries().length);
    }

    @Test
    public void testReadIpsObjectPath_minimalSettings() {
        mockObjectDirElements(MY_OBJECT_DIR);

        IIpsObjectPath ipsObjectPath = objectPathReader.readIpsObjectPath();

        assertEquals(2, ipsObjectPath.getEntries().length);
    }

    @Test
    public void testReadIpsObjectPath_twoEntries() {
        mockObjectDirElements(MY_OBJECT_DIR, MY_OBJECT_DIR);

        IIpsObjectPath ipsObjectPath = objectPathReader.readIpsObjectPath();

        assertEquals(3, ipsObjectPath.getEntries().length);
    }

    @Test
    public void testReadIpsObjectPath_checkEntry() {
        ManifestElement[] objectDirElements = mockObjectDirElements(MY_OBJECT_DIR);
        when(bundleManifest.getBasePackage(MY_OBJECT_DIR)).thenReturn(MY_BAS_PACK);
        when(bundleManifest.getUniqueQualifier(MY_OBJECT_DIR)).thenReturn(MY_UNIQUE_QUALIFIER);
        when(bundleManifest.getSourcecodeOutput(MY_OBJECT_DIR)).thenReturn(MY_SRC_OUT);
        when(bundleManifest.getResourceOutput(MY_OBJECT_DIR)).thenReturn(MY_RESOURCE_OUT);
        when(bundleManifest.getTocPath(objectDirElements[0])).thenReturn(MY_TOC_FILE);
        when(bundleManifest.getValidationMessagesBundle(objectDirElements[0])).thenReturn(MY_VALIDATION_MESSAGE);

        IIpsObjectPath ipsObjectPath = objectPathReader.readIpsObjectPath();
        IpsSrcFolderEntry ipsSrcFolderEntry = (IpsSrcFolderEntry)ipsObjectPath.getEntries()[0];

        assertEquals(ipsProject, ipsSrcFolderEntry.getIpsProject());
        assertEquals(MY_OBJECT_DIR, ipsSrcFolderEntry.getIpsPackageFragmentRootName());
        assertEquals(ipsObjectPath, ipsSrcFolderEntry.getIpsObjectPath());
        assertEquals(MY_BAS_PACK, ipsSrcFolderEntry.getBasePackageNameForMergableJavaClasses());
        assertEquals(MY_BAS_PACK, ipsSrcFolderEntry.getBasePackageNameForDerivedJavaClasses());
        assertEquals(QNameUtil.concat(MY_BAS_PACK, MY_UNIQUE_QUALIFIER),
                ipsSrcFolderEntry.getUniqueBasePackageNameForMergableArtifacts());
        assertEquals(QNameUtil.concat(MY_BAS_PACK, MY_UNIQUE_QUALIFIER),
                ipsSrcFolderEntry.getUniqueBasePackageNameForDerivedArtifacts());
        assertEquals(mySrcOut, ipsSrcFolderEntry.getSpecificOutputFolderForMergableJavaFiles());
        assertEquals(myResourceOut, ipsSrcFolderEntry.getSpecificOutputFolderForDerivedJavaFiles());
        assertEquals(MY_BAS_PACK + "/" + MY_TOC_FILE, ipsSrcFolderEntry.getFullTocPath());
        assertEquals(MY_VALIDATION_MESSAGE, ipsSrcFolderEntry.getValidationMessagesBundle());
    }

    @Test
    public void testReadIpsObjectPath_checkEntryNoUniqueQualifier() {
        ManifestElement[] objectDirElements = mockObjectDirElements(MY_OBJECT_DIR);
        when(bundleManifest.getBasePackage(MY_OBJECT_DIR)).thenReturn(MY_BAS_PACK);
        when(bundleManifest.getSourcecodeOutput(MY_OBJECT_DIR)).thenReturn(MY_SRC_OUT);
        when(bundleManifest.getResourceOutput(MY_OBJECT_DIR)).thenReturn(MY_RESOURCE_OUT);
        when(bundleManifest.getTocPath(objectDirElements[0])).thenReturn(MY_TOC_FILE);
        when(bundleManifest.getValidationMessagesBundle(objectDirElements[0])).thenReturn(MY_VALIDATION_MESSAGE);

        IIpsObjectPath ipsObjectPath = objectPathReader.readIpsObjectPath();
        IpsSrcFolderEntry ipsSrcFolderEntry = (IpsSrcFolderEntry)ipsObjectPath.getEntries()[0];

        assertEquals(ipsProject, ipsSrcFolderEntry.getIpsProject());
        assertEquals(MY_OBJECT_DIR, ipsSrcFolderEntry.getIpsPackageFragmentRootName());
        assertEquals(ipsObjectPath, ipsSrcFolderEntry.getIpsObjectPath());
        assertEquals(MY_BAS_PACK, ipsSrcFolderEntry.getBasePackageNameForMergableJavaClasses());
        assertEquals(MY_BAS_PACK, ipsSrcFolderEntry.getBasePackageNameForDerivedJavaClasses());
        assertEquals(MY_BAS_PACK, ipsSrcFolderEntry.getUniqueBasePackageNameForMergableArtifacts());
        assertEquals(MY_BAS_PACK, ipsSrcFolderEntry.getUniqueBasePackageNameForDerivedArtifacts());
        assertEquals(mySrcOut, ipsSrcFolderEntry.getSpecificOutputFolderForMergableJavaFiles());
        assertEquals(myResourceOut, ipsSrcFolderEntry.getSpecificOutputFolderForDerivedJavaFiles());
        assertEquals(MY_BAS_PACK + "/" + MY_TOC_FILE, ipsSrcFolderEntry.getFullTocPath());
        assertEquals(MY_VALIDATION_MESSAGE, ipsSrcFolderEntry.getValidationMessagesBundle());
    }

    private ManifestElement[] mockObjectDirElements(String... objectDirs) {
        ManifestElement[] manifestElements = new ManifestElement[objectDirs.length];
        int i = 0;
        for (String objectDir : objectDirs) {
            ManifestElement objectDirElement = mock(ManifestElement.class);
            when(objectDirElement.getValue()).thenReturn(objectDir);
            manifestElements[i++] = objectDirElement;
        }
        when(bundleManifest.getObjectDirElements()).thenReturn(manifestElements);
        return manifestElements;
    }

}
