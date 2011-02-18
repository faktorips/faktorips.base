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
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.standard.AbstractXmlUnitHtmlExportTest;
import org.faktorips.devtools.htmlexport.pages.standard.ContentPageUtil;
import org.junit.Before;
import org.junit.Test;

public class MethodsTablePageElementTest extends AbstractXmlUnitHtmlExportTest {

    private PolicyCmptType policy;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        policy = newPolicyCmptType(ipsProject, "Vertrag");
    }

    private void assertXPathFromTable(PageElement objectContentPage, String subXPath) throws Exception {
        assertXPathExists(objectContentPage, getXPathMethodTable() + subXPath);
    }

    private String getXPathMethodTable() {
        return "//table[@id= '" + policy.getName() + "_methods" + "']";
    }

    @Test
    public void testMethodsTableVorhanden() throws Exception {

        IMethod methodString = createStringMethod();
        IMethod methodInteger = createIntegerMethod();

        PageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathExists(objectContentPage, getXPathMethodTable());

        assertXPathFromTable(objectContentPage, "[count(.//tr)=3]");

        assertXPathFromTable(objectContentPage, "//tr[2][td='" + methodString.getName() + "']");

        assertXPathFromTable(objectContentPage, "//tr[3][td='" + methodInteger.getName() + "']");
    }

    @Test
    public void testMethodsTableNichtVorhandenOhneAttribute() throws Exception {

        PageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathNotExists(objectContentPage, getXPathMethodTable());
    }

    @Test
    public void testMethodsTableAufbau() throws Exception {
        createIntegerMethod();
        createStringMethod();
        PageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), context);

        int row = 2;

        IMethod[] methods = policy.getMethods();
        for (IMethod method : methods) {
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + method.getDatatype() + "']");
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + method.getModifier().getId() + "']");
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + (method.isAbstract() ? "X" : "-") + "']");
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + method.getName() + "']");
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + method.getSignatureString() + "']");
            row++;
        }
    }

    private IMethod createIntegerMethod() {
        IMethod method = policy.newMethod();
        method.setName("integerMethod");
        method.setModifier(Modifier.PUBLIC);
        method.setAbstract(true);
        method.setDatatype("Integer");
        method.newParameter("Integer", "int1");
        method.newParameter("Integer", "int2");
        return method;
    }

    private IMethod createStringMethod() {
        IMethod method = policy.newMethod();
        method.setName("stringMethod");
        method.setModifier(Modifier.PUBLISHED);
        method.setAbstract(false);
        method.setDatatype("String");
        method.newParameter("Integer", "int1");
        return method;
    }
}
