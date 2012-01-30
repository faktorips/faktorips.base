/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.migrationextensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.devtools.core.internal.migrationextensions.Migration_3_6_0_rc2;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.util.XmlUtil;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Migration_3_6_0_rc2Test extends XmlAbstractTestCase {

    private ArgumentCaptor<IIpsProjectProperties> propertiesCaptor;

    @Test
    public void shouldRenameOptionalConstraintsToAdditionalSettings() throws Exception {
        Document testDocument = getTestDocument();
        Element documentElement = testDocument.getDocumentElement();
        Element optionalConstraintsEl = XmlUtil.getFirstElement(documentElement, "OptionalConstraints"); //$NON-NLS-1$
        assertNotNull(optionalConstraintsEl);
        NodeList constraints = optionalConstraintsEl.getElementsByTagName("Constraint"); //$NON-NLS-1$
        assertNotNull(constraints);
        int length = constraints.getLength();
        assertEquals(5, length);

        IpsProject ipsProject = mockProject();
        Migration_3_6_0_rc2 migration = new Migration_3_6_0_rc2(ipsProject, "testFeatureId");
        migration.migrate(new NullProgressMonitor());

        verify(ipsProject, times(1)).setProperties(propertiesCaptor.capture());
        IpsProjectProperties properties = (IpsProjectProperties)propertiesCaptor.getValue();
        documentElement = properties.toXml(newDocument());

        optionalConstraintsEl = XmlUtil.getFirstElement(documentElement, "OptionalConstraints"); //$NON-NLS-1$
        assertNull(optionalConstraintsEl);
        Element additionalSettingsEl = XmlUtil.getFirstElement(documentElement, "AdditionalSettings"); //$NON-NLS-1$
        assertNotNull(additionalSettingsEl);
        NodeList nl = additionalSettingsEl.getElementsByTagName("Constraint"); //$NON-NLS-1$
        assertNotNull(nl);
        length = nl.getLength();
        assertEquals(0, length);
        NodeList settings = additionalSettingsEl.getElementsByTagName("Setting"); //$NON-NLS-1$
        assertNotNull(settings);
        length = settings.getLength();
        assertEquals(5, length);
        for (int i = 0; i < length; ++i) {
            Element constraint = (Element)constraints.item(i);
            Element setting = (Element)settings.item(i);
            assertEquals(constraint.getAttribute("name"), setting.getAttribute("name"));
            assertEquals(constraint.getAttribute("enabled"), setting.getAttribute("enabled"));
        }
    }

    IpsProject mockProject() throws CoreException {
        IpsProject ipsProject = mock(IpsProject.class);
        IFile file = mock(IFile.class);
        when(file.exists()).thenReturn(true);
        InputStream is = getClass().getResourceAsStream(getXmlResourceName());
        when(file.getContents(true)).thenReturn(is);
        when(file.getModificationStamp()).thenReturn(123456L);
        when(ipsProject.getIpsProjectPropertiesFile()).thenReturn(file);
        propertiesCaptor = ArgumentCaptor.forClass(IIpsProjectProperties.class);
        return ipsProject;
    }
}
