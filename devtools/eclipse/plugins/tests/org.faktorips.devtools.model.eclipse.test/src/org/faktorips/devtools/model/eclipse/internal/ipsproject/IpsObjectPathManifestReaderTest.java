/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.internal.ipsproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.osgi.util.ManifestElement;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.model.eclipse.internal.ipsproject.jdtcontainer.IpsContainer4JdtClasspathContainer;
import org.faktorips.devtools.model.eclipse.internal.ipsproject.jdtcontainer.IpsContainer4JdtClasspathContainerType;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.internal.ipsproject.IpsContainerEntry;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPathManifestReader;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleException;
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class IpsObjectPathManifestReaderTest {

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

    @BeforeEach
    public void createIpsObjectPathManifestReader() {
        objectPathReader = new IpsObjectPathManifestReader(bundleManifest, ipsProject);
    }

    private ManifestElement[] mockObjectDirElements(String... objectDirs) {
        ManifestElement[] manifestElements = new ManifestElement[objectDirs.length];
        int i = 0;
        for (String objectDir : objectDirs) {
            ManifestElement objectDirElement = mock(ManifestElement.class);
            when(objectDirElement.getValue()).thenReturn(objectDir);
            manifestElements[i++] = objectDirElement;
        }
        try {
            when(bundleManifest.getObjectDirElements()).thenReturn(manifestElements);
        } catch (BundleException e) {
            // ignore
        }
        return manifestElements;
    }

    @Test
    public void testReadIpsObjectPath_checkContainerEntry() {
        mockObjectDirElements();

        IIpsObjectPath ipsObjectPath = objectPathReader.readIpsObjectPath();

        assertEquals(1, ipsObjectPath.getEntries().length);
        IpsContainerEntry ipsContainerEntry = (IpsContainerEntry)ipsObjectPath.getEntries()[0];
        assertEquals(IpsContainer4JdtClasspathContainerType.ID, ipsContainerEntry.getContainerTypeId());
        assertEquals(IpsContainer4JdtClasspathContainer.REQUIRED_PLUGIN_CONTAINER,
                ipsContainerEntry.getOptionalPath());
    }

}
