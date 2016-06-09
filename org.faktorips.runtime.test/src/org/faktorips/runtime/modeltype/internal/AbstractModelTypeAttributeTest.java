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

public class AbstractModelTypeAttributeTest {

    private final IModelType modelType = Models.getModelType(Policy.class);

    private final IModelTypeAttribute constant = modelType.getAttribute("const");

    private final IModelTypeAttribute attr1 = modelType.getAttribute("attr1");

    private final IModelTypeAttribute attr2 = modelType.getAttribute("attr2");

    @Test
    public void testGetDatatype() throws Exception {
        assertEquals(String.class, constant.getDatatype());
        assertEquals(Integer.TYPE, attr1.getDatatype());
        assertEquals(Date.class, attr2.getDatatype());
    }

    @Test
    public void testGetAttributeType() throws Exception {
        assertEquals(AttributeType.CONSTANT, constant.getAttributeType());
        assertEquals(AttributeType.CHANGEABLE, attr1.getAttributeType());
        assertEquals(AttributeType.DERIVED_ON_THE_FLY, attr2.getAttributeType());
    }

    @Test
    public void testGetValueSetType() throws Exception {
        assertEquals(ValueSetType.AllValues, constant.getValueSetType());
        assertEquals(ValueSetType.Enum, attr1.getValueSetType());
        assertEquals(ValueSetType.Range, attr2.getValueSetType());
    }

    @Test
    public void testIsProductRelevant() throws Exception {
        assertFalse(constant.isProductRelevant());
        assertTrue(attr1.isProductRelevant());
        assertFalse(attr2.isProductRelevant());
    }

    @Test
    public void testGetValue() throws Exception {
        Policy modelObject = new Policy();
        modelObject.attr1 = 123;

        assertEquals("const", constant.getValue(modelObject));
        assertEquals(123, attr1.getValue(modelObject));
        assertEquals(null, attr2.getValue(modelObject));
    }

    @Test
    public void testSetValue() throws Exception {
        Policy modelObject = new Policy();

        attr1.setValue(modelObject, 412);

        assertEquals(412, modelObject.attr1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValue_CannotModifyConstant() throws Exception {
        Policy modelObject = new Policy();

        constant.setValue(modelObject, "asd");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValue_CannotDerivedAttribute() throws Exception {
        Policy modelObject = new Policy();

        attr2.setValue(modelObject, "asd");
    }

    @IpsPolicyCmptType(name = "MySource")
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

}
