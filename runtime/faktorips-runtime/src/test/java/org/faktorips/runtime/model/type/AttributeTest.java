/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Locale;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.junit.Test;

public class AttributeTest {

    private final Type superType = IpsModel.getType(SuperPolicy.class);

    private final Attribute superAttr3 = superType.getAttribute("attr3");

    private final Type type = IpsModel.getType(Policy.class);

    private final Attribute constant = type.getAttribute("const");

    private final Attribute attr1 = type.getAttribute("attr1");

    private final Attribute attr2 = type.getAttribute("attr2");

    private final Attribute attr3 = type.getAttribute("attr3");

    private final Type subtype = IpsModel.getType(SubPolicy.class);

    private final Attribute subConstant = subtype.getAttribute("const");

    private final Attribute subAttr1 = subtype.getAttribute("attr1");

    private final Attribute subAttr2 = subtype.getAttribute("attr2");

    private final Attribute subAttr3 = subtype.getAttribute("attr3");

    @Test
    public void testGetDatatype() {
        assertEquals(String.class, constant.getDatatype());
        assertEquals(Integer.TYPE, attr1.getDatatype());
        assertEquals(Date.class, attr2.getDatatype());
        assertEquals(String.class, subConstant.getDatatype());
        assertEquals(Integer.TYPE, subAttr1.getDatatype());
        assertEquals(Date.class, subAttr2.getDatatype());
    }

    @Test
    public void testGetAttributeKind() {
        assertEquals(AttributeKind.CONSTANT, constant.getAttributeKind());
        assertEquals(AttributeKind.CHANGEABLE, attr1.getAttributeKind());
        assertEquals(AttributeKind.DERIVED_ON_THE_FLY, attr2.getAttributeKind());
        assertEquals(AttributeKind.CONSTANT, subConstant.getAttributeKind());
        assertEquals(AttributeKind.CHANGEABLE, subAttr1.getAttributeKind());
        assertEquals(AttributeKind.CHANGEABLE, subAttr2.getAttributeKind());
    }

    @Test
    public void testGetValueSetKind() {
        assertEquals(ValueSetKind.AllValues, constant.getValueSetKind());
        assertEquals(ValueSetKind.Enum, attr1.getValueSetKind());
        assertEquals(ValueSetKind.Range, attr2.getValueSetKind());
        assertEquals(ValueSetKind.AllValues, subConstant.getValueSetKind());
        assertEquals(ValueSetKind.Enum, subAttr1.getValueSetKind());
        assertEquals(ValueSetKind.Range, subAttr2.getValueSetKind());
    }

    @Test
    public void testIsProductRelevant() {
        assertFalse(constant.isProductRelevant());
        assertTrue(attr1.isProductRelevant());
        assertFalse(attr2.isProductRelevant());
        assertFalse(subConstant.isProductRelevant());
        assertTrue(subAttr1.isProductRelevant());
        assertFalse(subAttr2.isProductRelevant());
    }

    @Test
    public void testIsOverriding() throws Exception {
        assertFalse(attr1.isOverriding());
        assertTrue(subAttr1.isOverriding());
    }

    @Test
    public void testGetSuperAttribute() throws Exception {
        assertThat(attr1.getSuperAttribute(), is(nullValue()));
        assertThat(subAttr1.getSuperAttribute(), is(attr1));
    }

    @Test
    public void testGetDocumentation() {
        assertThat(superAttr3.getDescription(Locale.GERMAN), is("Description of attr3"));
        assertThat(attr3.getDescription(Locale.GERMAN), is("Description of attr3 in Policy"));
        assertThat(subAttr3.getDescription(Locale.GERMAN), is("Description of attr3 in Policy"));
    }

    @IpsPolicyCmptType(name = "MySuperPolicy")
    @IpsAttributes({ "attr3" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private static class SuperPolicy implements IModelObject {

        private String attr3;

        @IpsAttribute(name = "attr3", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public String getAttr3() {
            return attr3;
        }

        @IpsAttributeSetter("attr3")
        public void setAttr3(String s) {
            attr3 = s;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsAttributes({ "const", "attr1", "attr2", "attr3" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private static class Policy extends SuperPolicy {

        @IpsAttribute(name = "const", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public final String CONSTANT = "const";

        private int attr1;

        @IpsAttribute(name = "attr1", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        @IpsConfiguredAttribute(changingOverTime = true)
        public int getAttr1() {
            return attr1;
        }

        @IpsAttributeSetter("attr1")
        public void setAttr1(int i) {
            attr1 = i;
        }

        @IpsAttribute(name = "attr2", kind = AttributeKind.DERIVED_ON_THE_FLY, valueSetKind = ValueSetKind.Range)
        public Date getAttr2() {
            return null;
        }
    }

    @IpsPolicyCmptType(name = "MySubPolicy")
    @IpsAttributes({ "const", "attr1", "attr2", "attr3" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private static class SubPolicy extends Policy {

        private Date attr2 = new Date(0);

        @Override
        @IpsAttribute(name = "attr2", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
        public Date getAttr2() {
            return attr2;
        }

        @IpsAttributeSetter("attr2")
        public void setAttr2(Date d) {
            attr2 = d;
        }
    }

}
