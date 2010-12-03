/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.standard.AbstractXmlUnitHtmlExportTest;
import org.faktorips.devtools.htmlexport.pages.standard.ContentPageUtil;

public class AttributesTablePageElementTest extends AbstractXmlUnitHtmlExportTest {

    private PolicyCmptType policy;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        policy = newPolicyCmptType(ipsProject, "Vertrag");
    }

    private void assertXPathFromTable(PageElement objectContentPage, String subXPath) throws Exception {
        assertXPathExists(objectContentPage, getXPathAttributeTable() + subXPath);
    }

    private String getXPathAttributeTable() {
        return "//table[@id= '" + policy.getName() + "_attributes" + "']";
    }

    public void testAttributesTableVorhanden() throws Exception {

        IAttribute attributeString = createStringAttribute();
        IAttribute attributeInteger = createIntegerAttribute();

        PageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathExists(objectContentPage, getXPathAttributeTable());

        assertXPathFromTable(objectContentPage, "[count(.//tr)=3]");

        assertXPathFromTable(objectContentPage, "//tr[2][td='" + attributeString.getName() + "']");

        assertXPathFromTable(objectContentPage, "//tr[3][td='" + attributeInteger.getName() + "']");
    }

    public void testAttributesTableNichtVorhandenOhneAttribute() throws Exception {

        PageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathNotExists(objectContentPage, getXPathAttributeTable());
    }

    public void testAttributesTableAufbau() throws Exception {
        IAttribute attributeString = createStringAttribute();
        createIntegerAttribute();

        PageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathFromTable(objectContentPage, "//tr[2][td='" + attributeString.getDatatype() + "']");

        assertXPathFromTable(objectContentPage, "//tr[2][td='" + attributeString.getDefaultValue() + "']");

        assertXPathFromTable(objectContentPage, "//tr[2][td='" + attributeString.getModifier().getId() + "']");

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
