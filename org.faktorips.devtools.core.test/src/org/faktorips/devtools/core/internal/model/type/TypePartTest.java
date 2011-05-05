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

import static org.junit.Assert.assertSame;

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

    private ITypePart typePart;

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
    public void testSetModifier() {
        testPropertyAccessReadWrite(TypePart.class, ITypePart.PROPERTY_MODIFIER, typePart, Modifier.PUBLIC);
    }

    @Test(expected = NullPointerException.class)
    public void testSetModifierNullPointer() {
        typePart.setModifier(null);
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
