/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.internal.DefaultVersion;
import org.faktorips.devtools.model.internal.DefaultVersionProvider;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.model.ipsobject.IDeprecation;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.util.XmlUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DeprecationTest extends AbstractIpsPluginTest {

    private IDeprecation deprecationInfo;

    private IIpsProject ipsProject;

    private PolicyCmptType policyCmptType;

    private PolicyCmptTypeAttribute attribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "TestPolicy");
        attribute = (PolicyCmptTypeAttribute)policyCmptType.newAttribute();
        deprecationInfo = attribute.newDeprecation();
        deprecationInfo.setForRemoval(false);
        deprecationInfo.setSinceVersionString("22.6.0");
        IDescription germanDescription = deprecationInfo.getDescription(Locale.GERMAN);
        germanDescription.setText("Beschreibung");
        IDescription englishDescription = deprecationInfo.getDescription(Locale.ENGLISH);
        englishDescription.setText("Description");
    }

    @Test
    public void testGetSinceVersion() {
        deprecationInfo.setSinceVersionString("22.6.0");
        IVersion<DefaultVersion> version = new DefaultVersionProvider(deprecationInfo.getIpsProject())
                .getVersion("22.6.0");
        assertThat(deprecationInfo.getSinceVersion(), is(version));
    }

    @Test
    public void testSinceVersionIsInitializedFromProject() {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setVersion("47.11");
        ipsProject.setProperties(properties);

        IDeprecation deprecation = policyCmptType.newDeprecation();

        assertThat(deprecation.getSinceVersionString(), is("47.11"));
    }

    @Test
    public void testIsValidSinceVersion() {
        deprecationInfo.setSinceVersionString("22ABC");
        assertThat(deprecationInfo.isValidSinceVersion(), is(false));
        deprecationInfo.setSinceVersionString("22.6.0");
        assertThat(deprecationInfo.isValidSinceVersion(), is(true));

    }

    @Test
    public void testCreateElement() {
        assertThat(((Deprecation)deprecationInfo).createElement(XmlUtil.getDefaultDocumentBuilder().newDocument())
                .getTagName(), is(IDeprecation.XML_TAG));
    }

    @Test
    public void testToXml() {
        Element xmlElement = deprecationInfo.toXml(XmlUtil.getDefaultDocumentBuilder().newDocument());
        NodeList deprecationDescriptions = xmlElement.getElementsByTagName(IDescription.XML_TAG_NAME);

        assertThat(xmlElement.getAttribute(IDeprecation.XML_ATTRIBUTE_DEPRECATION_VERSION), is("22.6.0"));
        assertThat(xmlElement.getAttribute(IDeprecation.XML_ATTRIBUTE_FOR_REMOVAL), is("false"));

        Element germanDescription = (Element)deprecationDescriptions.item(0);
        assertThat(germanDescription.getAttribute(IDescription.PROPERTY_LOCALE), is(Locale.GERMAN.getLanguage()));
        assertThat(germanDescription.getTextContent(), is("Beschreibung"));

        Element englishDescription = (Element)deprecationDescriptions.item(1);
        assertThat(englishDescription.getAttribute(IDescription.PROPERTY_LOCALE), is(Locale.US.getLanguage()));
        assertThat(englishDescription.getTextContent(), is("Description"));
    }

    @Test
    public void testInitFromXml() {
        Element xmlElement = deprecationInfo.toXml(XmlUtil.getDefaultDocumentBuilder().newDocument());
        IDeprecation loadedDeprecation = policyCmptType.newDeprecation();
        loadedDeprecation.initFromXml(xmlElement);
        assertThat(loadedDeprecation.getSinceVersionString(), is("22.6.0"));
        assertThat(loadedDeprecation.isForRemoval(), is(false));
        assertThat(loadedDeprecation.getDescriptionText(Locale.GERMAN), is("Beschreibung"));
        assertThat(loadedDeprecation.getDescriptionText(Locale.ENGLISH), is("Description"));
    }
}
