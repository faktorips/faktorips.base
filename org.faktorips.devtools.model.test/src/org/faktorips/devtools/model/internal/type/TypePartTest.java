/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ITypePart;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TypePartTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IPolicyCmptType policyCmptType;

    private TestTypePart policyTypePart;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Foo", "Bar");
        policyTypePart = new TestTypePart(policyCmptType, "");
    }

    @Test
    public void testGetType() {
        assertSame(policyCmptType, policyTypePart.getType());
    }

    @Test
    public void testIsOfType() {
        assertTrue(policyTypePart.isOfType(policyCmptType.getQualifiedNameType()));
        assertFalse(policyTypePart.isOfType(new QualifiedNameType("bar", IpsObjectType.POLICY_CMPT_TYPE)));
    }

    @Test
    public void testSetModifier() {
        testPropertyAccessReadWrite(TypePart.class, ITypePart.PROPERTY_MODIFIER, policyTypePart, Modifier.PUBLIC);
    }

    @Test
    public void testSetCategory() {
        policyTypePart.setCategory("foo");
        assertEquals("foo", policyTypePart.getCategory());
        assertPropertyChangedEvent(policyTypePart, ITypePart.PROPERTY_CATEGORY, "", "foo");
    }

    @Test
    public void testHasCategory() {
        policyTypePart.setCategory("foo");
        assertTrue(policyTypePart.hasCategory());

        policyTypePart.setCategory("");
        assertFalse(policyTypePart.hasCategory());
    }

    @Test
    public void testInitPropertiesFromXml() {
        Element element = mock(Element.class);
        when(element.getAttribute(ITypePart.PROPERTY_MODIFIER)).thenReturn(Modifier.PUBLIC.getId());
        when(element.getAttribute(ITypePart.PROPERTY_CATEGORY)).thenReturn("foo");

        policyTypePart.initPropertiesFromXml(element, null);

        assertEquals(Modifier.PUBLIC, policyTypePart.getModifier());
        assertEquals("foo", policyTypePart.getCategory());
    }

    @Test
    public void testInitPropertiesFromXmlNoCategoryAttribute() {
        Element element = mock(Element.class);

        policyTypePart.initPropertiesFromXml(element, null);

        assertEquals("", policyTypePart.getCategory());
    }

    @Test
    public void testPropertiesToXml() {
        Element element = mock(Element.class);
        policyTypePart.setCategory("foo");
        policyTypePart.setModifier(Modifier.PUBLIC);

        policyTypePart.propertiesToXml(element);

        verify(element).setAttribute(ITypePart.PROPERTY_MODIFIER, Modifier.PUBLIC.getId());
        verify(element).setAttribute(ITypePart.PROPERTY_CATEGORY, "foo");
    }

    private static class TestTypePart extends TypePart {

        protected TestTypePart(IType parent, String id) {
            super(parent, id);
        }

        @Override
        protected IIpsElement[] getChildrenThis() {
            return null;
        }

        @Override
        protected Element createElement(Document doc) {
            return null;
        }

        @Override
        protected void reinitPartCollectionsThis() {

        }

        @Override
        protected boolean addPartThis(IIpsObjectPart part) {
            return false;
        }

        @Override
        protected boolean removePartThis(IIpsObjectPart part) {
            return false;
        }

        @Override
        protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
            return null;
        }

        @Override
        protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
            return null;
        }

    }

}
