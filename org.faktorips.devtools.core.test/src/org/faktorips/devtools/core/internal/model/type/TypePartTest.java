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

package org.faktorips.devtools.core.internal.model.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ITypePart;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TypePartTest extends AbstractIpsPluginTest {

    private IType type;

    private TypePart typePart;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsProject ipsProject = newIpsProject();
        type = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Foo");
        typePart = new TestTypePart(type, "bar");
    }

    @Test
    public void testGetType() {
        assertSame(type, typePart.getType());
    }

    @Test
    public void testIsOfType() {
        assertTrue(typePart.isOfType(type.getQualifiedName()));
        assertFalse(typePart.isOfType("bar"));
    }

    @Test
    public void testSetModifier() {
        testPropertyAccessReadWrite(TypePart.class, ITypePart.PROPERTY_MODIFIER, typePart, Modifier.PUBLIC);
    }

    @Test
    public void testSetCategory() {
        typePart.setCategory("foo");
        assertEquals("foo", typePart.getCategory());
        assertPropertyChangedEvent(typePart, ITypePart.PROPERTY_CATEGORY, "", "foo");
    }

    @Test
    public void testHasCategory() {
        typePart.setCategory("foo");
        assertTrue(typePart.hasCategory());

        typePart.setCategory("");
        assertFalse(typePart.hasCategory());
    }

    @Test
    public void testInitPropertiesFromXml() {
        Element element = mock(Element.class);
        when(element.getAttribute(ITypePart.PROPERTY_MODIFIER)).thenReturn(Modifier.PUBLIC.getId());
        when(element.getAttribute(ITypePart.PROPERTY_CATEGORY)).thenReturn("foo");

        typePart.initPropertiesFromXml(element, null);

        assertEquals(Modifier.PUBLIC, typePart.getModifier());
        assertEquals("foo", typePart.getCategory());
    }

    @Test
    public void testInitPropertiesFromXmlNoCategoryAttribute() {
        Element element = mock(Element.class);

        typePart.initPropertiesFromXml(element, null);

        assertEquals("", typePart.getCategory());
    }

    @Test
    public void testPropertiesToXml() {
        Element element = mock(Element.class);
        typePart.setCategory("foo");
        typePart.setModifier(Modifier.PUBLIC);

        typePart.propertiesToXml(element);

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
