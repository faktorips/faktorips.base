/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.util.XmlUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Migration_3_12_0Test extends AbstractIpsPluginTest {

    private ArgumentCaptor<IIpsProjectProperties> propertiesCaptor;
    private IIpsProject ipsProject;

    @Before
    public void setup() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        ipsProject = spy(ipsProject);

        IFile file = mock(IFile.class);
        when(file.exists()).thenReturn(true);
        InputStream is = getClass().getResourceAsStream(getXmlResourceName());
        when(file.getContents(true)).thenReturn(is);
        when(file.getModificationStamp()).thenReturn(123456L);
        when(ipsProject.getIpsProjectPropertiesFile()).thenReturn(file);
        propertiesCaptor = ArgumentCaptor.forClass(IIpsProjectProperties.class);
    }

    @Test
    public void shouldRenameProductReleaseTag() throws Exception {
        Document testDocument = getTestDocument();
        Element documentElement = testDocument.getDocumentElement();
        Element productReleaseEl = XmlUtil.getFirstElement(documentElement, "productRelease"); //$NON-NLS-1$
        String oldValueReleaseExtensionId = productReleaseEl.getAttribute("releaseExtensionId");
        assertNotNull(productReleaseEl);

        documentElement = getMigratedDocumentElement();

        productReleaseEl = XmlUtil.getFirstElement(documentElement, "productRelease"); //$NON-NLS-1$
        assertNull(productReleaseEl);
        Element renamedProductReleaseEl = XmlUtil.getFirstElement(documentElement, "ProductRelease"); //$NON-NLS-1$
        assertNotNull(renamedProductReleaseEl);
        assertTrue(renamedProductReleaseEl.hasAttribute("releaseExtensionId"));
        assertEquals(oldValueReleaseExtensionId, renamedProductReleaseEl.getAttribute("releaseExtensionId"));
        assertFalse(renamedProductReleaseEl.hasAttribute("version"));
    }

    @Test
    public void shouldCreateVersionTag() throws Exception {
        Document testDocument = getTestDocument();
        Element documentElement = testDocument.getDocumentElement();
        Element versionEl = XmlUtil.getFirstElement(documentElement, "Version"); //$NON-NLS-1$
        assertNull(versionEl);

        documentElement = getMigratedDocumentElement();

        Element versionElNew = XmlUtil.getFirstElement(documentElement, "Version"); //$NON-NLS-1$
        assertNotNull(versionElNew);
        assertTrue(versionElNew.hasAttribute("version"));
    }

    @Test
    public void shouldMoveVersionValue() throws Exception {
        Document testDocument = getTestDocument();
        Element documentElement = testDocument.getDocumentElement();
        Element productReleaseEl = XmlUtil.getFirstElement(documentElement, "productRelease"); //$NON-NLS-1$
        String oldValueVersion = productReleaseEl.getAttribute("version");
        assertNotNull(productReleaseEl);

        documentElement = getMigratedDocumentElement();

        Element versionElNew = XmlUtil.getFirstElement(documentElement, "Version"); //$NON-NLS-1$
        assertNotNull(versionElNew);
        assertTrue(versionElNew.hasAttribute("version"));
        assertEquals(oldValueVersion, versionElNew.getAttribute("version"));
    }

    private Element getMigratedDocumentElement() throws CoreException {
        Migration_3_12_0 migration = new Migration_3_12_0(ipsProject, "testFeatureId");
        migration.migrate(new NullProgressMonitor());

        verify(ipsProject, times(1)).setProperties(propertiesCaptor.capture());
        IpsProjectProperties properties = (IpsProjectProperties)propertiesCaptor.getValue();
        return properties.toXml(newDocument());
    }

}
