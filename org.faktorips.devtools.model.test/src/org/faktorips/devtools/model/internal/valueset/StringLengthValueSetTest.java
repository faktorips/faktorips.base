/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.valueset;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.GregorianCalendar;

import org.eclipse.osgi.util.NLS;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.model.valueset.IStringLengthValueSet;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StringLengthValueSetTest extends AbstractIpsPluginTest {

    private IPolicyCmptTypeAttribute attr;
    private IConfiguredValueSet cValueSet;
    private IConfiguredValueSet eValueSet;

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

        cValueSet = generation.newPropertyValue(attr, IConfiguredValueSet.class);
    }

    @Test
    public void testCreateFromXml() {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();
        IStringLengthValueSet sl = new StringLengthValueSet(cValueSet, "1");
        Element element = XmlUtil.getElement(root, 0);
        sl = new StringLengthValueSet(cValueSet, "1");

        sl.initFromXml(element);

        assertThat(sl.getMaximumLength(), is("0"));
        assertThat(sl.isContainsNull(), is(true));
        assertThat(sl.isEmpty(), is(true));

        // empty
        element = XmlUtil.getElement(root, 1);
        sl = new StringLengthValueSet(cValueSet, "2");
        sl.initFromXml(element);

        assertThat(sl.getMaximumLength(), is("0"));
        assertThat(sl.isContainsNull(), is(false));
        assertThat(sl.isEmpty(), is(true));
    }

    @Test
    public void testToXml() {
        IStringLengthValueSet sl = new StringLengthValueSet(cValueSet, "1", "10", true);
        Element element = sl.toXml(newDocument());
        IStringLengthValueSet sl2 = new StringLengthValueSet(cValueSet, "1");

        sl2.initFromXml(element);

        assertEquals(sl.getMaximumLength(), sl2.getMaximumLength());
        assertEquals(sl.isContainsNull(), sl2.isContainsNull());
        assertEquals(sl.isEmpty(), sl2.isEmpty());
    }

    @Test
    public void testToXmlNull() {
        IStringLengthValueSet sl = new StringLengthValueSet(cValueSet, "1");
        Element element = sl.toXml(newDocument());
        IStringLengthValueSet sl2 = new StringLengthValueSet(cValueSet, "1");

        sl2.initFromXml(element);

        assertEquals(sl.getMaximumLength(), sl2.getMaximumLength());
        assertEquals(sl.isContainsNull(), sl2.isContainsNull());
        assertEquals(sl.isEmpty(), sl2.isEmpty());
    }

    @Test
    public void testToXmlEmpty() {
        StringLengthValueSet sl = new StringLengthValueSet(cValueSet, "1");
        Element element = sl.toXml(newDocument());
        StringLengthValueSet sl2 = new StringLengthValueSet(cValueSet, "1");
        sl2.initFromXml(element);

        assertEquals(sl.getMaximumLength(), sl2.getMaximumLength());
        assertEquals(sl.isContainsNull(), sl2.isContainsNull());
        assertEquals(sl.isEmpty(), sl2.isEmpty());
    }

    @Test
    public void testContainsValue() throws Exception {
        StringLengthValueSet sl = new StringLengthValueSet(cValueSet, "50", "20");

        assertFalse(sl.containsValue("", ipsProject));
        assertTrue(sl.containsValue("A", ipsProject));
        assertTrue(sl.containsValue("maximumlength-string", ipsProject));
        assertFalse(sl.containsValue("maximumlength-string2", ipsProject));
        assertFalse(sl.containsValue("toolongforthisvaluesetsosorry", ipsProject));

        sl.setContainsNull(false);
        assertFalse(sl.containsValue(null, ipsProject));
        assertFalse(sl.containsValue("", ipsProject));
        assertFalse(sl.containsValue(" ", ipsProject));

        sl.setContainsNull(true);
        assertTrue(sl.containsValue(null, ipsProject));
        assertTrue(sl.containsValue("", ipsProject));
        assertTrue(sl.containsValue(" ", ipsProject));
    }

    @Test
    public void testContainsValue_Empty() throws Exception {
        StringLengthValueSet range = new StringLengthValueSet(cValueSet, "idXY", "0");

        assertFalse(range.containsValue("abc", ipsProject));
    }

    @Test
    public void testContainsValueSet() throws Exception {
        StringLengthValueSet sl = new StringLengthValueSet(cValueSet, "50", "20");
        StringLengthValueSet sl2 = new StringLengthValueSet(cValueSet, "50", "12");
        StringLengthValueSet sl3 = new StringLengthValueSet(cValueSet, "50", "100");

        assertTrue(sl.containsValueSet(sl2));
        assertFalse(sl.containsValueSet(sl3));
    }

    @Test
    public void testContainsValueSet_EnumValueSet() throws Exception {
        StringLengthValueSet sl = new StringLengthValueSet(cValueSet, "50", "10");
        StringLengthValueSet sl2 = new StringLengthValueSet(cValueSet, "50", "12");
        StringLengthValueSet sl3 = new StringLengthValueSet(cValueSet, "50", "15");
        EnumValueSet enumSet = new EnumValueSet(eValueSet, Arrays.asList("five", "ten-charas", "fifteen-charas."), "1");

        assertFalse(sl.containsValueSet(enumSet));
        assertFalse(sl2.containsValueSet(enumSet));
        assertTrue(sl3.containsValueSet(enumSet));
    }

    @Test
    public void testGetCanonicalString() {
        StringLengthValueSet sl = new StringLengthValueSet(cValueSet, "50", "20");
        StringLengthValueSet sl2 = new StringLengthValueSet(cValueSet, "50");
        StringLengthValueSet sl3 = new StringLengthValueSet(cValueSet, "50", "0", true);

        assertThat(sl.getCanonicalString(), is(NLS.bind(Messages.StringLength_canonicalDesc, sl.getMaximumLength())));
        assertThat(sl2.getCanonicalString(),
                is(NLS.bind(Messages.StringLength_canonicalDesc, Messages.StringLength_unlimitedLength)));
        assertThat(sl3.getCanonicalString(),
                is(NLS.bind(Messages.StringLength_canonicalDesc, "0")
                        + String.format(" (%1$s)", NLS.bind(Messages.ValueSet_includingNull,
                                IIpsModelExtensions.get().getModelPreferences().getNullPresentation()))));
    }

    @Test
    public void testCompareTo() {
        StringLengthValueSet sl = new StringLengthValueSet(cValueSet, "50", "12");
        StringLengthValueSet sl2 = new StringLengthValueSet(cValueSet, "50", "0");
        StringLengthValueSet sl3 = new StringLengthValueSet(cValueSet, "50", "15");
        StringLengthValueSet sl4 = new StringLengthValueSet(cValueSet, "50", "12");

        assertThat(sl.compareTo(sl2), is(1));
        assertThat(sl.compareTo(sl3), is(-1));
        assertThat(sl.compareTo(sl4), is(0));
    }

    @Test
    public void testCompareTo_ContainsNull() {
        StringLengthValueSet sl = new StringLengthValueSet(cValueSet, "partId", "10", true);
        StringLengthValueSet sl2 = new StringLengthValueSet(cValueSet, "partId", "10", false);
        StringLengthValueSet sl3 = new StringLengthValueSet(cValueSet, "partId", "10", true);

        assertThat(sl.compareTo(sl2), is(1));
        assertThat(sl.compareTo(sl3), is(0));
    }

    @Test
    public void testIsEmpty() {
        StringLengthValueSet sl = new StringLengthValueSet(cValueSet, "50", "0", true);
        assertTrue(sl.isEmpty());

        sl.setContainsNull(false);

        assertTrue(sl.isEmpty());

        sl.setMaximumLength(null);
        assertFalse(sl.isEmpty());
    }
}
