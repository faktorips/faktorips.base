/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.standard.AbstractXmlUnitHtmlExportTest;
import org.faktorips.devtools.htmlexport.pages.standard.ContentPageUtil;
import org.faktorips.devtools.htmlexport.pages.standard.PolicyCmptTypeContentPageElement;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.type.IAttribute;
import org.junit.Before;
import org.junit.Test;

public class AttributesTablePageElementTest extends AbstractXmlUnitHtmlExportTest {

    private IPolicyCmptType policy;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        policy = newPolicyCmptType(ipsProject, "Vertrag");
    }

    private void assertXPathFromTable(IPageElement objectContentPage, String subXPath) throws Exception {
        assertXPathExists(objectContentPage, getXPathAttributeTable() + subXPath);
    }

    private String getXPathAttributeTable() {
        return "//table[@id= '" + policy.getName() + "_attributes" + "']";
    }

    @Test
    public void testAttributesTableVorhanden() throws Exception {

        IAttribute attributeString = createStringAttribute();
        IAttribute attributeInteger = createIntegerAttribute();

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathExists(objectContentPage, getXPathAttributeTable());

        assertXPathFromTable(objectContentPage, "[count(.//tr)=3]");

        assertXPathFromTable(objectContentPage, "//tr[2][td='" + attributeString.getName() + "']");

        assertXPathFromTable(objectContentPage, "//tr[3][td='" + attributeInteger.getName() + "']");
    }

    @Test
    public void testAttributesTableNichtVorhandenOhneAttribute() throws Exception {

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathNotExists(objectContentPage, getXPathAttributeTable());
    }

    @Test
    public void testAttributesTableAufbau() throws Exception {
        IAttribute attributeString = createStringAttribute();
        createIntegerAttribute();

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathFromTable(objectContentPage, "//tr[2][td='" + attributeString.getDatatype() + "']");

        assertXPathFromTable(objectContentPage, "//tr[2][td='" + attributeString.getDefaultValue() + "']");

        assertXPathFromTable(objectContentPage, "//tr[2][td='" + attributeString.getModifier().getId() + "']");

    }

    @Test
    public void testInheritedAttributeJavaDocStyle() throws Exception {
        IPolicyCmptType superPolicy = newPolicyCmptType(ipsProject, "BasisVertrag");
        IAttribute attribute = superPolicy.newAttribute();
        attribute.setName("Geerbt");
        attribute.setDatatype("String");
        attribute.setDefaultValue("test");
        attribute.setModifier(Modifier.PUBLIC);

        policy.setSupertype(superPolicy.getQualifiedName());
        createStringAttribute();

        context.setShowInheritedObjectPartsInTable(false);

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathNotExists(objectContentPage, getXPathAttributeTable() + "/tr[3]");

        assertXPathNotExists(objectContentPage, getXPathAttributeTable() + "/tr[1]/td[. = 'inherited from']");

        assertXPathExists(objectContentPage, "//h3[starts-with(., 'Geerbte Attribute')]");

    }

    @Test
    public void testInheritedAttributeInTable() throws Exception {
        IPolicyCmptType superPolicy = newPolicyCmptType(ipsProject, "BasisVertrag");
        IAttribute attribute = superPolicy.newAttribute();
        attribute.setName("Geerbt");
        attribute.setDatatype("String");
        attribute.setDefaultValue("test");
        attribute.setModifier(Modifier.PUBLIC);

        policy.setSupertype(superPolicy.getQualifiedName());
        createStringAttribute();

        context.setShowInheritedObjectPartsInTable(true);
        context.setDocumentationLocale(Locale.GERMANY);

        PolicyCmptTypeContentPageElement objectContentPage = (PolicyCmptTypeContentPageElement)ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);

        objectContentPage.build();

        int anzahlExtensionProperties = 0;
        List<IPageElement> subElements = objectContentPage.getSubElements();

        basis: for (IPageElement pageElement : subElements) {
            if (pageElement instanceof WrapperPageElement wrapperPageElement) {
                for (IPageElement iPageElement : wrapperPageElement.getSubElements()) {
                    if (iPageElement instanceof PolicyCmptTypeAttributesTablePageElement policyCmptTypeAttributesTablePageElement) {
                        anzahlExtensionProperties = policyCmptTypeAttributesTablePageElement
                                .getPropertyDefinitions().length;
                        break basis;
                    }
                }
            }
        }

        assertXPathExists(objectContentPage, getXPathAttributeTable() + "/tr[3]");
        assertXPathExists(objectContentPage, getXPathAttributeTable() + "/tr[1]/td[starts-with(., 'Geerbt')]");

        assertXPathExists(objectContentPage, getXPathAttributeTable()
                + "/tr[td/a/@id='Vertrag.PolicyCmptTypeAttribute.Stringname']/td[count(../td) - "
                + (anzahlExtensionProperties + 1) + "][. = '-']");
        assertXPathExists(objectContentPage, getXPathAttributeTable()
                + "/tr[td/a/@id='BasisVertrag.PolicyCmptTypeAttribute.Geerbt']/td[count(../td) - "
                + (anzahlExtensionProperties + 1) + "]/a[contains(., 'BasisVertrag')]");

        assertXPathNotExists(objectContentPage, "//h3[starts-with(., 'Geerbte Attribute')]");
    }

    private IAttribute createIntegerAttribute() {
        IAttribute attributeInteger = policy.newAttribute();
        attributeInteger.setName("Integername");
        attributeInteger.setDatatype("Integer");
        attributeInteger.setDefaultValue("Hallo");
        attributeInteger.setModifier(Modifier.PUBLISHED);
        return attributeInteger;
    }

    private IAttribute createStringAttribute() {
        IAttribute attributeString = policy.newAttribute();
        attributeString.setName("Stringname");
        attributeString.setDatatype("String");
        attributeString.setDefaultValue("Hallo");
        attributeString.setModifier(Modifier.PUBLISHED);
        return attributeString;
    }

}
