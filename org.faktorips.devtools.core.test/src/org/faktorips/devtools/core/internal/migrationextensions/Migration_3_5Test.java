/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.devtools.core.internal.model.InternationalString;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsSrcFileMemento;
import org.faktorips.devtools.core.internal.model.ipsproject.SupportedLanguage;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRule;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Migration_3_5Test extends XmlAbstractTestCase {

    @Test
    public void shouldMigrateDoNothingForNonPolicyCmptType() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        Migration_3_5 migration_3_5 = new Migration_3_5(ipsProject, "testFeatureId");
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        migration_3_5.migrate(ipsSrcFile);
        verifyZeroInteractions(ipsProject);
        verify(ipsSrcFile).getIpsObjectType();
        verifyNoMoreInteractions(ipsSrcFile);
    }

    @Test
    public void shouldMigrateModifyIpsSrcFile() throws Exception {
        InputStream resourceAsStream = getClass().getResourceAsStream(getXmlResourceName());
        Locale locale = new Locale("test");

        IIpsProject ipsProject = mockProject(locale);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IFile file = mock(IFile.class);

        when(ipsSrcFile.getCorrespondingFile()).thenReturn(file);
        when(file.getContents()).thenReturn(resourceAsStream);

        Migration_3_5 migration_3_5 = new Migration_3_5(ipsProject, "testFeatureId");

        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);
        migration_3_5.migrate(ipsSrcFile);

        verify(ipsSrcFile).setMemento(any(IpsSrcFileMemento.class));
        verify(ipsSrcFile).markAsDirty();
    }

    @Test
    public void testMigrateXml() throws Exception {
        Document testDocument = getTestDocument();
        Element element = testDocument.getDocumentElement();

        Locale locale = new Locale("test");

        IIpsProject ipsProject = mockProject(locale);

        Migration_3_5 migration_3_5 = new Migration_3_5(ipsProject, "testFeatureId");

        boolean migrateXml = migration_3_5.migrateXml(element);

        assertTrue(migrateXml);
        Element ruleElement = (Element)element.getElementsByTagName("ValidationRuleDef").item(0);
        assertTrue(StringUtils.isEmpty(ruleElement.getAttribute("messageText")));

        Element msgTextElement = (Element)ruleElement.getElementsByTagName(ValidationRule.XML_TAG_MSG_TXT).item(0);
        assertNotNull(msgTextElement);

        Node isElement = msgTextElement.getElementsByTagName(InternationalString.XML_TAG).item(0);
        assertNotNull(isElement);

        Element lsElement = (Element)msgTextElement.getElementsByTagName(
                InternationalString.XML_ELEMENT_LOCALIZED_STRING).item(0);
        assertNotNull(lsElement);
        assertEquals(locale.toString(), lsElement.getAttribute(InternationalString.XML_ATTR_LOCALE));
        assertEquals("testMsgText", lsElement.getAttribute(InternationalString.XML_ATTR_TEXT));

        // a second call of the migration should change nothing
        migrateXml = migration_3_5.migrateXml(element);

        assertFalse(migrateXml);
        ruleElement = (Element)element.getElementsByTagName("ValidationRuleDef").item(0);
        assertTrue(StringUtils.isEmpty(ruleElement.getAttribute("messageText")));

        msgTextElement = (Element)ruleElement.getElementsByTagName(ValidationRule.XML_TAG_MSG_TXT).item(0);
        assertNotNull(msgTextElement);

        isElement = msgTextElement.getElementsByTagName(InternationalString.XML_TAG).item(0);
        assertNotNull(isElement);

        lsElement = (Element)msgTextElement.getElementsByTagName(InternationalString.XML_ELEMENT_LOCALIZED_STRING)
                .item(0);
        assertNotNull(lsElement);
        assertEquals(locale.toString(), lsElement.getAttribute(InternationalString.XML_ATTR_LOCALE));
        assertEquals("testMsgText", lsElement.getAttribute(InternationalString.XML_ATTR_TEXT));
    }

    IIpsProject mockProject(Locale locale) {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IIpsProjectProperties projectProperties = mock(IIpsProjectProperties.class);
        when(ipsProject.getReadOnlyProperties()).thenReturn(projectProperties);
        ISupportedLanguage supportedLanguage = new SupportedLanguage(locale, true);
        when(projectProperties.getDefaultLanguage()).thenReturn(supportedLanguage);
        return ipsProject;
    }
}
