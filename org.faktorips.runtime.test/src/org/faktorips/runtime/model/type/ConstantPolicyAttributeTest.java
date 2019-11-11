package org.faktorips.runtime.model.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.values.Decimal;
import org.faktorips.valueset.OrderedValueSet;
import org.junit.Test;

public class ConstantPolicyAttributeTest {

    private static final InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();

    private static final GregorianCalendar effectiveDate = new GregorianCalendar(1999, 1, 1);

    @Test
    public void testIsProductRelevant() throws Exception {
        ConstPolicy policy = new ConstPolicy();
        PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(policy);
        PolicyAttribute policyAttribute = policyCmptType.getAttribute(ConstPolicy.PROPERTY_ATTR);

        assertFalse(policyAttribute.isProductRelevant());
    }

    @Test
    public void testGetValue() throws Exception {
        ConstPolicy policy = new ConstPolicy();
        PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(policy);
        PolicyAttribute policyAttribute = policyCmptType.getAttribute(ConstPolicy.PROPERTY_ATTR);

        assertEquals(ConstPolicy.ATTR, policyAttribute.getValue(policy));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValue() throws Exception {
        ConstPolicy policy = new ConstPolicy();
        PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(policy);
        PolicyAttribute policyAttribute = policyCmptType.getAttribute(ConstPolicy.PROPERTY_ATTR);

        policyAttribute.setValue(policy, 4711);
    }

    @Test
    public void testGetDefaultValueIConfigurableModelObject() throws Exception {
        ConstPolicy policy = new ConstPolicy();
        PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(policy);
        PolicyAttribute policyAttribute = policyCmptType.getAttribute(ConstPolicy.PROPERTY_ATTR);

        assertEquals(ConstPolicy.ATTR, policyAttribute.getDefaultValue(policy));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDefaultValueIProductComponentCalendar() throws Exception {
        ConstPolicy policy = new ConstPolicy();
        PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(policy);
        PolicyAttribute policyAttribute = policyCmptType.getAttribute(ConstPolicy.PROPERTY_ATTR);

        policyAttribute.getDefaultValue(policy.getProductComponent(), effectiveDate);
    }

    @Test
    public void testGetValueSetIModelObjectIValidationContext() throws Exception {
        ConstPolicy policy = new ConstPolicy();
        PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(policy);
        PolicyAttribute policyAttribute = policyCmptType.getAttribute(ConstPolicy.PROPERTY_ATTR);

        assertEquals(new OrderedValueSet<Integer>(false, null, 1), policyAttribute.getValueSet(policy, null));
    }

    @Test
    public void testGetValueSetIModelObjectIValidationContext_Null() throws Exception {
        ConstPolicy policy = new ConstPolicy();
        PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(policy);
        PolicyAttribute policyAttribute = policyCmptType.getAttribute(ConstPolicy.PROPERTY_NULL_ATTR);

        assertEquals(new OrderedValueSet<Integer>(true, null), policyAttribute.getValueSet(policy, null));
    }

    @Test
    public void testGetValueSetIModelObjectIValidationContext_NullObject() throws Exception {
        ConstPolicy policy = new ConstPolicy();
        PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(policy);
        PolicyAttribute policyAttribute = policyCmptType.getAttribute(ConstPolicy.PROPERTY_NULL_OBJECT_ATTR);

        assertEquals(new OrderedValueSet<Decimal>(true, Decimal.NULL), policyAttribute.getValueSet(policy, null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueSetIProductComponentCalendarIValidationContext() throws Exception {
        ConstPolicy policy = new ConstPolicy();
        PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(policy);
        PolicyAttribute policyAttribute = policyCmptType.getAttribute(ConstPolicy.PROPERTY_ATTR);

        policyAttribute.getValueSet(policy.getProductComponent(), effectiveDate);
    }

    @Test
    public void testCreateOverwritingAttributeFor() throws Exception {
        ConstPolicy policy = new ConstPolicy();
        PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(policy);
        PolicyAttribute policyAttribute = policyCmptType.getAttribute(ConstPolicy.PROPERTY_ATTR);

        @IpsPolicyCmptType(name = "SubPolicy")
        class SubPolicy extends ConstPolicy {

        }
        Attribute overwritingAttribute = policyAttribute
                .createOverwritingAttributeFor(IpsModel.getPolicyCmptType(SubPolicy.class));

        assertTrue(overwritingAttribute instanceof ConstantPolicyAttribute);
    }

    @IpsPolicyCmptType(name = "ConstPolicy")
    @IpsAttributes({ "attr", "nullAttr", "nullObjectAttr" })
    @IpsConfiguredBy(ConstProduct.class)
    private static class ConstPolicy implements IConfigurableModelObject {

        public static final String PROPERTY_ATTR = "attr";

        public static final String PROPERTY_NULL_ATTR = "nullAttr";

        public static final String PROPERTY_NULL_OBJECT_ATTR = "nullObjectAttr";

        @IpsAttribute(name = "attr", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public static final int ATTR = 1;

        @IpsAttribute(name = "nullAttr", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public static final Integer NULL_ATTR = null;

        @IpsAttribute(name = "nullObjectAttr", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public static final Decimal NULL_OBJECT_ATTR = Decimal.NULL;

        private ConstProduct constProduct = new ConstProduct();

        @Override
        public void initialize() {
        }

        @Override
        public IProductComponent getProductComponent() {
            return constProduct;
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            constProduct = (ConstProduct)productComponent;
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return effectiveDate;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

    }

    @IpsProductCmptType(name = "ConstProduct")
    @IpsConfigures(ConstPolicy.class)
    private static class ConstProduct extends ProductComponent {

        public ConstProduct() {
            super(repository, "c1", "c", "1");
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

        @Override
        public ConstPolicy createPolicyComponent() {
            return null;
        }
    }
}
