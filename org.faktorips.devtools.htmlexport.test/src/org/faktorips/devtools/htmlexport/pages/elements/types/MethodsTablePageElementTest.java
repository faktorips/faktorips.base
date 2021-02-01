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

import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.standard.AbstractXmlUnitHtmlExportTest;
import org.faktorips.devtools.htmlexport.pages.standard.ContentPageUtil;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.type.IMethod;
import org.junit.Before;
import org.junit.Test;

public class MethodsTablePageElementTest extends AbstractXmlUnitHtmlExportTest {

    private IPolicyCmptType policy;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        policy = newPolicyCmptType(ipsProject, "Vertrag");
    }

    private void assertXPathFromTable(IPageElement objectContentPage, String subXPath) throws Exception {
        assertXPathExists(objectContentPage, getXPathMethodTable() + subXPath);
    }

    private String getXPathMethodTable() {
        return "//table[@id= '" + policy.getName() + "_methods" + "']";
    }

    @Test
    public void testMethodsTableVorhanden() throws Exception {

        IMethod methodString = createStringMethod();
        IMethod methodInteger = createIntegerMethod();

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathExists(objectContentPage, getXPathMethodTable());

        assertXPathFromTable(objectContentPage, "[count(.//tr)=3]");

        assertXPathFromTable(objectContentPage, "//tr[2][td='" + context.getLabel(methodString) + "']");

        assertXPathFromTable(objectContentPage, "//tr[3][td='" + context.getLabel(methodInteger) + "']");
    }

    @Test
    public void testMethodsTableNichtVorhandenOhneAttribute() throws Exception {

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathNotExists(objectContentPage, getXPathMethodTable());
    }

    @Test
    public void testMethodsTableAufbau() throws Exception {
        createIntegerMethod();
        createStringMethod();
        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);

        int row = 2;

        List<IMethod> methods = policy.getMethods();
        for (IMethod method : methods) {
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + method.getDatatype() + "']");
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + method.getModifier().getId() + "']");
            assertXPathFromTable(objectContentPage,
                    "//tr[" + row + "][td='" + (method.isAbstract() ? "X" : "-") + "']");
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + context.getLabel(method) + "']");
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
