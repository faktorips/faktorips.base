package org.faktorips.runtime.modeltype.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;

import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.ValueSetType;
import org.junit.Test;

public class ModelTypeAttributeTest {
    private Method getter;

    private ModelType modelType;

    private ModelTypeAttribute modelTypeAttribute;

    @Test
    public void testIsProductRelevant_ProductModel() throws Exception {
        getter = ModelTypeAttributeTest.class.getMethod("noAnnotation");
        modelType = mock(ProductModel.class);
        modelTypeAttribute = new ModelTypeAttribute(modelType, getter, null);

        assertThat(modelTypeAttribute.isProductRelevant(), is(true));
    }

    @Test
    public void testIsProductRelevant_PolicyModel_notRelevant() throws Exception {
        getter = ModelTypeAttributeTest.class.getMethod("noAnnotation");
        modelType = mock(PolicyModel.class);
        modelTypeAttribute = new ModelTypeAttribute(modelType, getter, null);

        assertThat(modelTypeAttribute.isProductRelevant(), is(false));
    }

    @Test
    public void testIsProductRelevant_PolicyModel_relevant() throws Exception {
        getter = ModelTypeAttributeTest.class.getMethod("withAnnotation");
        modelType = mock(PolicyModel.class);
        modelTypeAttribute = new ModelTypeAttribute(modelType, getter, null);

        assertThat(modelTypeAttribute.isProductRelevant(), is(true));
    }

    @IpsAttribute(name = "no", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
    public int noAnnotation() {
        return 0;
    }

    @IpsAttribute(name = "yes", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
    @IpsConfiguredAttribute(changingOverTime = false)
    public int withAnnotation() {
        return 1;
    }

}
