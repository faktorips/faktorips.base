/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
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
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.util.XmlUtil;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Migration_3_6_2Test extends AbstractIpsPluginTest {

    private ArgumentCaptor<IIpsProjectProperties> propertiesCaptor;

    @Test
    public void shouldConvertAttributeEnabledToAttributeValue_forAllSettingNodes() throws Exception {
        Document testDocument = getTestDocument();
        Element documentElement = testDocument.getDocumentElement();
        Element originalAdditionalSettingsEl = XmlUtil.getFirstElement(documentElement, "AdditionalSettings"); //$NON-NLS-1$
        assertNotNull(originalAdditionalSettingsEl);
        NodeList originalSettingElements = originalAdditionalSettingsEl.getElementsByTagName("Setting"); //$NON-NLS-1$
        assertNotNull(originalSettingElements);
        int length = originalSettingElements.getLength();
        assertEquals(5, length);
        for (int i = 0; i < originalSettingElements.getLength(); i++) {
            Element originalSetting = (Element)originalSettingElements.item(i);
            assertTrue(originalSetting.hasAttribute("enable"));
            assertFalse(originalSetting.hasAttribute("value"));
        }

        IIpsProject ipsProject = spyProject();
        Migration_3_6_2 migration = new Migration_3_6_2(ipsProject, "testFeatureId");
        migration.migrate(new NullProgressMonitor());

        verify(ipsProject, times(1)).setProperties(propertiesCaptor.capture());
        IpsProjectProperties properties = (IpsProjectProperties)propertiesCaptor.getValue();
        documentElement = properties.toXml(newDocument());

        Element additionalSettingsEl = XmlUtil.getFirstElement(documentElement, "AdditionalSettings"); //$NON-NLS-1$
        assertNotNull(additionalSettingsEl);
        NodeList settings = additionalSettingsEl.getElementsByTagName("Setting"); //$NON-NLS-1$
        assertNotNull(settings);
        assertEquals(8, settings.getLength());
        for (int i = 0; i < originalSettingElements.getLength(); ++i) {
            Element originalSetting = (Element)originalSettingElements.item(i);
            Element migratedSetting = (Element)settings.item(i);
            assertFalse(migratedSetting.hasAttribute("enable"));
            assertTrue(migratedSetting.hasAttribute("value"));
            String settingName = migratedSetting.getAttribute("name");
            assertEquals(originalSetting.getAttribute("name"), settingName);
            assertEquals("Setting \"" + settingName + "\":", originalSetting.getAttribute("enable"),
                    migratedSetting.getAttribute("value"));
        }
        Element migratedFormulaLanguageLocaleEl = (Element)settings.item(5);
        assertFalse(migratedFormulaLanguageLocaleEl.hasAttribute("enable"));
        assertTrue(migratedFormulaLanguageLocaleEl.hasAttribute("value"));
        // default value has been persisted
        assertEquals("de", migratedFormulaLanguageLocaleEl.getAttribute("value"));
    }

    IpsProject spyProject() throws CoreException {
        IpsProject ipsProject = (IpsProject)spy(newIpsProject("MigrationTestProject"));
        IFile file = mock(IFile.class);
        when(file.exists()).thenReturn(true);
        InputStream is = getClass().getResourceAsStream(getXmlResourceName());
        when(file.getContents(true)).thenReturn(is);
        when(file.getModificationStamp()).thenReturn(123456L);
        doReturn(file).when(ipsProject).getIpsProjectPropertiesFile();
        propertiesCaptor = ArgumentCaptor.forClass(IIpsProjectProperties.class);
        return ipsProject;
    }
}
