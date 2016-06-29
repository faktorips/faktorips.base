package org.faktorips.runtime.modeltype.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.ValueSetType;
import org.junit.Test;

public class AbstractAttributeModelTest {

    private final IModelType modelType = Models.getModelType(Policy.class);

    private final IModelTypeAttribute constant = modelType.getAttribute("const");

    private final IModelTypeAttribute attr1 = modelType.getAttribute("attr1");

    private final IModelTypeAttribute attr2 = modelType.getAttribute("attr2");

    private final IModelType subModelType = Models.getModelType(SubPolicy.class);

    private final IModelTypeAttribute subConstant = subModelType.getAttribute("const");

    private final IModelTypeAttribute subAttr1 = subModelType.getAttribute("attr1");

    private final IModelTypeAttribute subAttr2 = subModelType.getAttribute("attr2");

    @Test
    public void testGetDatatype() throws ClassNotFoundException {
        assertEquals(String.class, constant.getDatatype());
        assertEquals(Integer.TYPE, attr1.getDatatype());
        assertEquals(Date.class, attr2.getDatatype());
        assertEquals(String.class, subConstant.getDatatype());
        assertEquals(Integer.TYPE, subAttr1.getDatatype());
        assertEquals(Date.class, subAttr2.getDatatype());
    }

    @Test
    public void testGetAttributeType() {
        assertEquals(AttributeType.CONSTANT, constant.getAttributeType());
        assertEquals(AttributeType.CHANGEABLE, attr1.getAttributeType());
        assertEquals(AttributeType.DERIVED_ON_THE_FLY, attr2.getAttributeType());
        assertEquals(AttributeType.CONSTANT, subConstant.getAttributeType());
        assertEquals(AttributeType.CHANGEABLE, subAttr1.getAttributeType());
        assertEquals(AttributeType.CHANGEABLE, subAttr2.getAttributeType());
    }

    @Test
    public void testGetValueSetType() {
        assertEquals(ValueSetType.AllValues, constant.getValueSetType());
        assertEquals(ValueSetType.Enum, attr1.getValueSetType());
        assertEquals(ValueSetType.Range, attr2.getValueSetType());
        assertEquals(ValueSetType.AllValues, subConstant.getValueSetType());
        assertEquals(ValueSetType.Enum, subAttr1.getValueSetType());
        assertEquals(ValueSetType.Range, subAttr2.getValueSetType());
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

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsAttributes({ "const", "attr1", "attr2" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.modeltype.internal.test", defaultLocale = "de")
    private static class Policy implements IModelObject {

        @IpsAttribute(name = "const", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)
        public final String CONSTANT = "const";

        private int attr1;

        @IpsAttribute(name = "attr1", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.Enum)
        @IpsConfiguredAttribute(changingOverTime = true)
        public int getAttr1() {
            return attr1;
        }

        @IpsAttributeSetter("attr1")
        public void setAttr1(int i) {
            attr1 = i;
        }

        @IpsAttribute(name = "attr2", type = AttributeType.DERIVED_ON_THE_FLY, valueSetType = ValueSetType.Range)
        public Date getAttr2() {
            return null;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
    }

    @IpsPolicyCmptType(name = "MySubPolicy")
    @IpsAttributes({ "const", "attr1", "attr2" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.modeltype.internal.test", defaultLocale = "de")
    private static class SubPolicy extends Policy {

        private Date attr2 = new Date(0);

        @Override
        @IpsAttribute(name = "attr2", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.Range)
        public Date getAttr2() {
            return attr2;
        }

        @IpsAttributeSetter("attr2")
        public void setAttr2(Date d) {
            attr2 = d;
        }
    }

}
