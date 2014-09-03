/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.valueset;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class UnrestrictedValueSetTest extends AbstractIpsPluginTest {

    private IPolicyCmptTypeAttribute attr;
    private IConfigElement ce;

    private IIpsProject ipsProject;
    private IProductCmptGeneration generation;

    private IPolicyCmptType policyCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "test.Base", "test.Product");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
        attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(Datatype.STRING.getQualifiedName());

        IProductCmpt cmpt = newProductCmpt(productCmptType, "test.Product");
        generation = (IProductCmptGeneration)cmpt.newGeneration(new GregorianCalendar(20006, 4, 26));

        ce = generation.newConfigElement();
        ce.setPolicyCmptTypeAttribute("attr");

    }

    @Test
    public void testUnrestrictedValueSet() {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        assertTrue(unrestricted.isContainsNull());

        unrestricted = new UnrestrictedValueSet(ce, "1", true);
        assertTrue(unrestricted.isContainsNull());

        unrestricted = new UnrestrictedValueSet(ce, "1", false);
        assertFalse(unrestricted.isContainsNull());
    }

    @Test
    public void testPropertiesToXml() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        Element xml = unrestricted.toXml(newDocument());

        IUnrestrictedValueSet unrestricted2 = new UnrestrictedValueSet(ce, "1");
        unrestricted2.initFromXml(xml);
        assertTrue(unrestricted2.isContainsNull());
    }

    @Test
    public void testInitPropertiesFromXml() throws Exception {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();

        // first
        Element element = XmlUtil.getFirstElement(root);
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        unrestricted.initFromXml(element);
        assertFalse(unrestricted.isContainsNull());

        // second
        element = XmlUtil.getElement(root, 1);
        unrestricted = new UnrestrictedValueSet(ce, "2");
        unrestricted.initFromXml(element);
        assertTrue(unrestricted.isContainsNull());

        // third
        element = XmlUtil.getElement(root, 2);
        unrestricted = new UnrestrictedValueSet(ce, "3");
        unrestricted.initFromXml(element);
        assertTrue(unrestricted.isContainsNull());
    }

    @Test
    public void testCopy() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        unrestricted.setContainsNull(false);

        IUnrestrictedValueSet unrestricted2 = (UnrestrictedValueSet)unrestricted.copy(ce, "2");
        assertFalse(unrestricted2.isContainsNull());
    }

    @Test
    public void testCopyPropertiesFrom() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        unrestricted.setContainsNull(false);

        IUnrestrictedValueSet unrestricted2 = new UnrestrictedValueSet(ce, "2");
        assertTrue(unrestricted2.isContainsNull());

        unrestricted2.copyFrom(unrestricted);
        assertFalse(unrestricted2.isContainsNull());
    }

    @Test
    public void testIsContainsNull() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        assertTrue(unrestricted.isContainsNull());
    }

    @Test
    public void testSetContainsNull() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        unrestricted.setContainsNull(false);
        assertFalse(unrestricted.isContainsNull());
    }

    @Test
    public void testValidateThis() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");

        MessageList list = unrestricted.validate(ipsProject);
        assertNull(list.getMessageByCode(IValueSet.MSGCODE_NULL_NOT_SUPPORTED));

        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        list = unrestricted.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IValueSet.MSGCODE_NULL_NOT_SUPPORTED));
    }
}
