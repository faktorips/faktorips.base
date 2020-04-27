package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Set;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ValidationContext;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAllowedValuesSetter;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.model.annotation.IpsDefaultValueSetter;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.valueset.DefaultRange;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;
import org.junit.Test;

public class DefaultPolicyAttributeTest {

    private final InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();

    private final GregorianCalendar effectiveDate = new GregorianCalendar(1999, 1, 1);

    @Test
    public void testGetValue() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(Policy.class);
        PolicyAttribute constant = modelType.getAttribute("const");
        PolicyAttribute attr1 = modelType.getAttribute("attr1");
        PolicyAttribute attr2 = modelType.getAttribute("overriddenAttr");
        PolicyCmptType subModelType = IpsModel.getPolicyCmptType(SubPolicy.class);
        PolicyAttribute subConstant = subModelType.getAttribute("const");
        PolicyAttribute subAttr1 = subModelType.getAttribute("attr1");
        PolicyAttribute subAttr2 = subModelType.getAttribute("overriddenAttr");
        Policy modelObject = new Policy();

        modelObject.attr1 = 123;

        assertEquals("const", constant.getValue(modelObject));
        assertEquals(123, attr1.getValue(modelObject));
        assertEquals(null, attr2.getValue(modelObject));

        SubPolicy subPolicy = new SubPolicy();
        assertEquals("const", constant.getValue(subPolicy));
        assertEquals(42, attr1.getValue(subPolicy));
        assertEquals(new Date(0), attr2.getValue(subPolicy));
        assertEquals("const", subConstant.getValue(subPolicy));
        assertEquals(42, subAttr1.getValue(subPolicy));
        assertEquals(new Date(0), subAttr2.getValue(subPolicy));
    }

    @Test
    public void testSetValue() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(Policy.class);
        PolicyAttribute attr1 = modelType.getAttribute("attr1");
        PolicyCmptType subModelType = IpsModel.getPolicyCmptType(SubPolicy.class);
        PolicyAttribute subAttr1 = subModelType.getAttribute("attr1");
        Policy modelObject = new Policy();

        attr1.setValue(modelObject, 412);

        assertEquals(412, modelObject.attr1);

        subAttr1.setValue(modelObject, 567);

        assertEquals(567, modelObject.attr1);
    }

    @Test
    public void testSetValue_OnSubclass() {
        PolicyCmptType subModelType = IpsModel.getPolicyCmptType(SubPolicy.class);
        PolicyAttribute subAttr1 = subModelType.getAttribute("attr1");
        SubPolicy subPolicy = new SubPolicy();
        assertEquals(42, subPolicy.getAttr1());

        subAttr1.setValue(subPolicy, 43);

        assertEquals(43, subPolicy.getAttr1());
    }

    @Test
    public void testSetValue_Overridden_WithoutSetter() {
        PolicyCmptType subModelType = IpsModel.getPolicyCmptType(SubPolicy.class);
        PolicyAttribute overriddenAttr = subModelType.getAttribute("getterOverriddenAttr");

        SubPolicy subPolicy = new SubPolicy();
        overriddenAttr.setValue(subPolicy, true);

        assertTrue(subPolicy.isGetterOverriddenAttr());
    }

    @Test
    public void testSetValue_Overridden_SetterOverridden() {
        PolicyCmptType subModelType = IpsModel.getPolicyCmptType(SubPolicy.class);
        PolicyAttribute subAttr2 = subModelType.getAttribute("overriddenAttr");
        SubPolicy subPolicy = new SubPolicy();
        assertEquals(new Date(0), subPolicy.overriddenAttr);

        subAttr2.setValue(subPolicy, new Date(1));

        assertEquals(new Date(1), subPolicy.overriddenAttr);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValue_CannotModifyConstant() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(Policy.class);
        PolicyAttribute constant = modelType.getAttribute("const");
        PolicyCmptType subModelType = IpsModel.getPolicyCmptType(SubPolicy.class);
        PolicyAttribute subConstant = subModelType.getAttribute("const");
        Policy modelObject = new Policy();

        constant.setValue(modelObject, "asd");
        subConstant.setValue(modelObject, "asd");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValue_CannotDerivedAttribute() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(Policy.class);
        PolicyAttribute attr2 = modelType.getAttribute("attr2");
        Policy modelObject = new Policy();

        attr2.setValue(modelObject, "asd");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValue_CannotDerivedAttributeWithOverwritten() {
        PolicyCmptType subModelType = IpsModel.getPolicyCmptType(SubPolicy.class);
        PolicyAttribute subAttr2 = subModelType.getAttribute("attr2");
        Policy modelObject = new Policy();

        subAttr2.setValue(modelObject, "asd");
    }

    @Test
    public void testGetValueSet_ProductComponent() {
        Produkt source = new Produkt();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        ValueSet<?> valueSet = attribute.getValueSet(source, null, null);

        assertTrue(valueSet instanceof UnrestrictedValueSet);
    }

    @Test
    public void testGetValueSet_Product_ChangingOverTime() {
        Produkt source = new Produkt();
        ProduktGen produktGen = new ProduktGen(source);
        repository.putProductCmptGeneration(produktGen);

        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        ValueSet<?> valueSet = attribute.getValueSet(source, null, null);

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<String>(false, null, "foo", "bar"), valueSet);
    }

    @Test
    public void testGetValueSet_Product_ChangingOverTimeWithCalendar() {
        Produkt source = new Produkt();
        ProduktGen gen = new ProduktGen(source);
        gen.setValidFrom(DateTime.createDateOnly(effectiveDate));
        repository.putProductCmptGeneration(gen);

        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        ValueSet<?> valueSet = attribute.getValueSet(source, effectiveDate, null);

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<String>(false, null, "foo", "bar"), valueSet);
    }

    @Test
    public void testGetValueSet_Product_ChangingOverTime_Handwritten() {
        Produkt source = new Produkt();
        ProduktGen produktGen = new ProduktGen(source);
        repository.putProductCmptGeneration(produktGen);

        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithValueSetWithoutValidationContext");
        ValueSet<?> valueSet = attribute.getValueSet(source, effectiveDate, new ValidationContext());

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<String>(false, null, "lorem", "ipsum"), valueSet);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValueSet_Product_ChangingOverTime_TooManyArgs() {
        Produkt source = new Produkt();
        ProduktGen produktGen = new ProduktGen(source);
        repository.putProductCmptGeneration(produktGen);

        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithValueSetWithTooManyArgs");
        attribute.getValueSet(source, effectiveDate, new ValidationContext());
    }

    @Test
    public void testGetValueSet_ModelObject_Handwritten() {
        Policy policy = new Policy();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Policy.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithValueSetWithoutValidationContext");
        ValueSet<?> valueSet = attribute.getValueSet(policy, new ValidationContext());

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<String>(false, null, "lorem", "ipsum"), valueSet);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValueSet_ModelObject_TooManyArgs() {
        Policy policy = new Policy();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Policy.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithValueSetWithTooManyArgs");
        attribute.getValueSet(policy, new ValidationContext());
    }

    @Test
    public void testGetValueSet_ModelObject() {
        ConfVertrag vertrag = new ConfVertrag();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, new ValidationContext());

        assertTrue(valueSet instanceof UnrestrictedValueSet);
    }

    @Test
    public void testGetValueSet_ModelObject_UnrestrictedNotConfigured() {
        Policy policy = new Policy();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Policy.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithNull");
        ValueSet<?> valueSet = attribute.getValueSet(policy, new ValidationContext());

        assertTrue(valueSet instanceof UnrestrictedValueSet);
        assertTrue(valueSet.containsNull());
    }

    @Test
    public void testGetValueSet_ModelObject_UnrestrictedWithoutNull() {
        Policy policy = new Policy();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Policy.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithoutNull");
        ValueSet<?> valueSet = attribute.getValueSet(policy, new ValidationContext());

        assertTrue(valueSet instanceof UnrestrictedValueSet);
        assertFalse(valueSet.containsNull());
    }

    @Test
    public void testGetValueSet_ModelObject_UnrestrictedPrimitive() {
        Policy policy = new Policy();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Policy.class);

        PolicyAttribute attribute = policyModel.getAttribute("primitiveUnrestrictedAttr");
        ValueSet<?> valueSet = attribute.getValueSet(policy, new ValidationContext());

        assertTrue(valueSet instanceof UnrestrictedValueSet);
        assertFalse(valueSet.containsNull());
    }

    @Test
    public void testGetValueSet_ModelObject_ChangingOverTime() {
        ProduktGen gen = new ProduktGen();
        repository.putProductCmptGeneration(gen);
        ConfVertrag vertrag = new ConfVertrag();
        vertrag.effectiveFrom = Calendar.getInstance();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, null);

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<String>(false, null, "foo", "bar"), valueSet);
    }

    @Test
    public void testGetValueSet_ModelObject_ChangingOverTime_NoEffectiveDate() {
        ConfVertrag vertrag = new ConfVertrag();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, null);

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<String>(false, null, "foo", "bar"), valueSet);
    }

    @Test
    public void testGetValueSet_ModelObject_NotChangingOverTime_Handwritten() {
        ConfVertrag vertrag = new ConfVertrag();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithValueSetWithoutValidationContext");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, new ValidationContext());

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<String>(false, null, "lorem", "ipsum"), valueSet);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValueSet_ModelObject_NotChangingOverTime_TooManyArgs() {
        ConfVertrag vertrag = new ConfVertrag();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithValueSetWithTooManyArgs");
        attribute.getValueSet(vertrag, new ValidationContext());
    }

    @Test
    public void testGetValueSet_ModelObject_NotConfigured() {
        Vertrag vertrag = new Vertrag();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Vertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, null);

        assertTrue(valueSet instanceof DefaultRange);
        assertEquals(new DefaultRange<String>("A", "Z"), valueSet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueSet_Failing() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");

        attribute.getValueSet(new FailingProdukt(), null);
    }

    @Test
    public void testGetValueSet_ModelObject_NotConfiguredOnConfigurablePolicy() {
        ConfVertrag vertrag = new ConfVertrag();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attr2");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, null);

        assertTrue(valueSet instanceof UnrestrictedValueSet);
        assertFalse(valueSet.containsNull());
    }

    @Test
    public void testGetDefaultValue() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        Object defaultValue = attribute.getDefaultValue(new Produkt(), null);

        assertEquals("foobar", defaultValue);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDefaultValue_NotProductRelevant() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attr2");
        attribute.getDefaultValue(new Produkt(), null);
    }

    @Test
    public void testGetDefaultValue_ChangingOverTimeWithCalendar() {
        Produkt source = new Produkt();
        ProduktGen gen = new ProduktGen();
        gen.setValidFrom(DateTime.createDateOnly(effectiveDate));
        repository.putProductCmptGeneration(gen);

        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(source, effectiveDate);

        assertEquals("blub", defaultValue);
    }

    @Test
    public void testGetDefaultValue_ChangingOverTime() {
        Produkt source = new Produkt();
        ProduktGen gen = new ProduktGen(source);
        repository.putProductCmptGeneration(gen);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(source, null);

        assertEquals("blub", defaultValue);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDefaultValue_NotConfigured() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Vertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");

        attribute.getDefaultValue(new Produkt(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDefaultValue_Failing() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");

        attribute.getDefaultValue(new FailingProdukt(), null);
    }

    @Test
    public void testGetDefaultValue_ModelObject() {
        GregorianCalendar effectiveFrom = new GregorianCalendar();

        ProduktGen gen = new ProduktGen();
        gen.setValidFrom(DateTime.createDateOnly(effectiveFrom));
        repository.putProductCmptGeneration(gen);

        ConfVertrag vertrag = new ConfVertrag();
        vertrag.effectiveFrom = effectiveFrom;
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(vertrag);

        assertEquals("blub", defaultValue);
    }

    @Test
    public void testGetDefaultValue_ModelObject_NoEffectiveFrom() {
        ProduktGen gen = new ProduktGen();
        repository.putProductCmptGeneration(gen);
        ConfVertrag vertrag = new ConfVertrag();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(vertrag);

        assertEquals("blub", defaultValue);
    }

    @Test
    public void testSetDefaultValue() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        Produkt produkt = new Produkt();

        attribute.setDefaultValue(produkt, null, "new");

        assertEquals("new", produkt.getDefaultValueAttr1());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetDefaultValue_NotProductRelevant() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr2");

        attribute.setDefaultValue(new Produkt(), null, "not product relevant");
    }

    @Test
    public void testSetDefaultValue_ChangingOverTimeWithCalendar() {
        Produkt produkt = new Produkt();
        ProduktGen gen = new ProduktGen();
        gen.setValidFrom(DateTime.createDateOnly(effectiveDate));
        repository.putProductCmptGeneration(gen);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");

        attribute.setDefaultValue(produkt, effectiveDate, "new");

        assertEquals("new", gen.getDefaultValueAttrChangingOverTime());
    }

    @Test
    public void testSetDefaultValue_ChangingOverTime() {
        Produkt produkt = new Produkt();
        ProduktGen gen = new ProduktGen(produkt);
        repository.putProductCmptGeneration(gen);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");

        attribute.setDefaultValue(produkt, null, "new");

        assertEquals("new", gen.getDefaultValueAttrChangingOverTime());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetDefaultValue_NotConfigured() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Vertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");

        attribute.setDefaultValue(new Produkt(), null, "new");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultValue_Failing() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");

        attribute.setDefaultValue(new FailingProdukt(), null, "new");
    }

    @Test
    public void testSetDefaultValue_ModelObject() {
        GregorianCalendar effectiveFrom = new GregorianCalendar();
        ProduktGen gen = new ProduktGen();
        gen.setValidFrom(DateTime.createDateOnly(effectiveFrom));
        repository.putProductCmptGeneration(gen);
        ConfVertrag vertrag = new ConfVertrag();
        vertrag.effectiveFrom = effectiveFrom;
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");

        attribute.setDefaultValue(vertrag, "new");

        assertEquals("new", gen.getDefaultValueAttrChangingOverTime());
    }

    @Test
    public void testSetDefaultValue_ModelObject_NoEffectiveFrom() {
        ProduktGen gen = new ProduktGen();
        repository.putProductCmptGeneration(gen);
        ConfVertrag vertrag = new ConfVertrag();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");

        attribute.setDefaultValue(vertrag, "new");

        assertEquals("new", gen.getDefaultValueAttrChangingOverTime());
    }

    @Test
    public void testSetValueSet() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        Produkt produkt = new Produkt();
        OrderedValueSet<String> valueSet = new OrderedValueSet<String>(false, null, "A", "B", "C");

        attribute.setValueSet(produkt, null, valueSet);

        assertEquals(valueSet, produkt.getSetOfAllowedValuesForAttr1(null));
    }

    @Test
    public void testSetValueSet_WithWrongGenericType() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        Produkt produkt = new Produkt();
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, 1, 2, 3);

        attribute.setValueSet(produkt, null, valueSet);

        assertEquals(valueSet, produkt.getSetOfAllowedValuesForAttr1(null));
        // yes, this works.
        assertThat(produkt.getSetOfAllowedValuesForAttr1(null).contains("A"), is(false));
        try {
            ValueSet<String> setOfAllowedValuesForAttr1 = produkt.getSetOfAllowedValuesForAttr1(null);
            Set<String> values = setOfAllowedValuesForAttr1.getValues(true);
            String firstValue = values.iterator().next();
            fail("expected a " + ClassCastException.class.getSimpleName()
                    + " when casting Integer 1 to a String, but we got \""
                    + firstValue + "\"");
        } catch (ClassCastException e) {
            // Generics are fun
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testSetValueSet_NotProductRelevant() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr2");

        attribute.setValueSet(new Produkt(), null, new UnrestrictedValueSet<String>());
    }

    @Test
    public void testSetValueSet_ChangingOverTimeWithCalendar() {
        Produkt produkt = new Produkt();
        ProduktGen gen = new ProduktGen();
        gen.setValidFrom(DateTime.createDateOnly(effectiveDate));
        repository.putProductCmptGeneration(gen);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        OrderedValueSet<String> valueSet = new OrderedValueSet<String>(false, null, "A", "B", "C");

        attribute.setValueSet(produkt, effectiveDate, valueSet);

        assertEquals(valueSet, gen.getSetOfAllowedValuesForAttrChangingOverTime(null));
    }

    @Test
    public void testSetValueSet_ChangingOverTime() {
        Produkt produkt = new Produkt();
        ProduktGen gen = new ProduktGen(produkt);
        repository.putProductCmptGeneration(gen);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        OrderedValueSet<String> valueSet = new OrderedValueSet<String>(false, null, "A", "B", "C");

        attribute.setValueSet(produkt, null, valueSet);

        assertEquals(valueSet, gen.getSetOfAllowedValuesForAttrChangingOverTime(null));
    }

    @Test(expected = IllegalStateException.class)
    public void testSetValueSet_NotConfigured() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Vertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");

        attribute.setValueSet(new Produkt(), null, new UnrestrictedValueSet<String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValueSet_Failing() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");

        attribute.setValueSet(new FailingProdukt(), null, new UnrestrictedValueSet<String>());
    }

    @Test
    public void testSetValueSet_ModelObject() {
        GregorianCalendar effectiveFrom = new GregorianCalendar();
        ProduktGen gen = new ProduktGen();
        gen.setValidFrom(DateTime.createDateOnly(effectiveFrom));
        repository.putProductCmptGeneration(gen);
        ConfVertrag vertrag = new ConfVertrag();
        vertrag.effectiveFrom = effectiveFrom;
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        OrderedValueSet<String> valueSet = new OrderedValueSet<String>(false, null, "A", "B", "C");

        attribute.setValueSet(vertrag, valueSet);

        assertEquals(valueSet, gen.getSetOfAllowedValuesForAttrChangingOverTime(null));
    }

    @Test
    public void testSetValueSet_ModelObject_NoEffectiveFrom() {
        ProduktGen gen = new ProduktGen();
        repository.putProductCmptGeneration(gen);
        ConfVertrag vertrag = new ConfVertrag();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        OrderedValueSet<String> valueSet = new OrderedValueSet<String>(false, null, "A", "B", "C");

        attribute.setValueSet(vertrag, valueSet);

        assertEquals(valueSet, gen.getSetOfAllowedValuesForAttrChangingOverTime(null));
    }

    @Test
    public void testIsChangingOverTime() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");

        assertFalse(attribute.isChangingOverTime());
    }

    @Test
    public void testIsChangingOverTime_True() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");

        assertTrue(attribute.isChangingOverTime());
    }

    @Test
    public void testIsChangingOverTime_NoAnnotation() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(DummyVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");

        assertFalse(attribute.isChangingOverTime());
    }

    @Test
    public void testGetLabel_OverwrittenAttribute() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(Policy.class);
        PolicyCmptType subModelType = IpsModel.getPolicyCmptType(SubPolicy.class);
        PolicyAttribute attribute = modelType.getAttribute("attr1");
        PolicyAttribute overwritingAttribute = subModelType.getAttribute("attr1");

        assertEquals("MeinAttribut (original)", attribute.getLabel(Locale.GERMAN));
        assertEquals("MeinAttribut (überschrieben)", overwritingAttribute.getLabel(Locale.GERMAN));
    }

    @IpsPolicyCmptType(name = "Vertragxyz")
    @IpsConfiguredBy(Produkt.class)
    @IpsAttributes({ "attr1", "attr2", "attrChangingOverTime", "attrWithValueSetWithoutValidationContext",
            "attrWithValueSetWithTooManyArgs" })
    private class ConfVertrag implements IConfigurableModelObject {

        private Produkt produkt;

        private String attr1;
        private String attr2;
        private String attrChangingOverTime;
        private String attrWithValueSetWithoutValidationContext;
        private String attrWithValueSetWithTooManyArgs;
        private Calendar effectiveFrom;

        public ConfVertrag() {
            produkt = new Produkt();
        }

        @IpsAttribute(name = "attr1", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        @IpsConfiguredAttribute(changingOverTime = false)
        public String getAttr1() {
            return attr1;
        }

        @IpsAttributeSetter("attr1")
        public void setAttr1(String value) {
            attr1 = value;
        }

        @IpsAllowedValues("attr1")
        public ValueSet<String> getSetOfAllowedValuesForAttr1(IValidationContext context) {
            return produkt.getSetOfAllowedValuesForAttr1(context);
        }

        @IpsAttribute(name = "attr2", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public String getAttr2() {
            return attr2;
        }

        @IpsAttributeSetter("attr2")
        public void setAttr2(String value) {
            attr2 = value;
        }

        /**
         * @param context unused
         */
        @IpsAllowedValues("attr2")
        public ValueSet<String> getSetOfAllowedValuesForAttr2(IValidationContext context) {
            return new UnrestrictedValueSet<String>(false);
        }

        @IpsAttribute(name = "attrChangingOverTime", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        @IpsConfiguredAttribute(changingOverTime = true)
        public String getAttrChangingOverTime() {
            return attrChangingOverTime;
        }

        @IpsAttributeSetter("attrChangingOverTime")
        public void setAttrChangingOverTime(String value) {
            attrChangingOverTime = value;
        }

        /**
         * @param context unused
         */
        @IpsAllowedValues("attrChangingOverTime")
        public ValueSet<String> getSetOfAllowedValuesForAttrChangingOverTime(IValidationContext context) {
            return new OrderedValueSet<String>(false, null, "foo", "bar");
        }

        @IpsAttribute(name = "attrWithValueSetWithoutValidationContext", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        public String getAttrWithValueSetWithoutValidationContext() {
            return attrWithValueSetWithoutValidationContext;
        }

        @IpsAttributeSetter("attrWithValueSetWithoutValidationContext")
        public void setAttrWithValueSetWithoutValidationContext(String attrWithValueSetWithoutValidationContext) {
            this.attrWithValueSetWithoutValidationContext = attrWithValueSetWithoutValidationContext;
        }

        @IpsAllowedValues("attrWithValueSetWithoutValidationContext")
        public ValueSet<String> getHandwrittenAllowedValuesForAttrWithValueSetWithoutValidationContext() {
            return produkt.getHandwrittenAllowedValuesForAttrWithValueSetWithoutValidationContext();
        }

        @IpsAttribute(name = "attrWithValueSetWithTooManyArgs", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        public String getAttrWithValueSetWithTooManyArgs() {
            return attrWithValueSetWithTooManyArgs;
        }

        @IpsAttributeSetter("attrWithValueSetWithTooManyArgs")
        public void setAttrWithValueSetWithTooManyArgs(String attrWithValueSetWithTooManyArgs) {
            this.attrWithValueSetWithTooManyArgs = attrWithValueSetWithTooManyArgs;
        }

        @SuppressWarnings("unused")
        @IpsAllowedValues("attrWithValueSetWithTooManyArgs")
        public ValueSet<String> getAllowedValuesForAttrWithValueSetWithTooManyArgs(IValidationContext context,
                boolean includeNull) {
            return produkt.getAllowedValuesForAttrWithValueSetWithTooManyArgs(context, includeNull);
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

        @Override
        public IProductComponent getProductComponent() {
            return produkt;
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return effectiveFrom;
        }

        @Override
        public void initialize() {
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            produkt = (Produkt)productComponent;
        }
    }

    @IpsPolicyCmptType(name = "VertragDummy")
    @IpsAttributes("attrChangingOverTime")
    public class DummyVertrag implements IModelObject {

        @IpsAttribute(name = "attrChangingOverTime", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public String getAttrChangingOverTime() {
            return null;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
    }

    @IpsProductCmptType(name = "ProductXYZ")
    @IpsConfigures(ConfVertrag.class)
    @IpsChangingOverTime(ProduktGen.class)
    private class Produkt extends ProductComponent {

        private String defaultValueAttr1 = "foobar";
        private ValueSet<String> allowedValuesForAttr1 = new UnrestrictedValueSet<String>();

        public Produkt() {
            super(repository, "id", "kindId", "versionId");
        }

        @IpsDefaultValue("attr1")
        public String getDefaultValueAttr1() {
            return defaultValueAttr1;
        }

        @IpsDefaultValueSetter("attr1")
        public void setDefaultValueAttr1(String defaultValueAttr1) {
            this.defaultValueAttr1 = defaultValueAttr1;
        }

        /**
         * @param context validation context
         */
        @IpsAllowedValues("attr1")
        public ValueSet<String> getSetOfAllowedValuesForAttr1(IValidationContext context) {
            return allowedValuesForAttr1;
        }

        @IpsAllowedValuesSetter("attr1")
        public void setSetOfAllowedValuesForAttr1(ValueSet<String> valueSet) {
            allowedValuesForAttr1 = valueSet;
        }

        @SuppressWarnings("unused")
        @IpsAllowedValues("attrWithValueSetWithTooManyArgs")
        public ValueSet<String> getAllowedValuesForAttrWithValueSetWithTooManyArgs(IValidationContext context,
                boolean includeNull) {
            fail("this message should never be called as it has too many args");
            return null;
        }

        @IpsAllowedValues("attrWithValueSetWithoutValidationContext")
        public ValueSet<String> getHandwrittenAllowedValuesForAttrWithValueSetWithoutValidationContext() {
            return new OrderedValueSet<String>(false, null, "lorem", "ipsum");
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }
    }

    @IpsProductCmptType(name = "ProductXYZ")
    @IpsConfigures(ConfVertrag.class)
    @IpsChangingOverTime(ProduktGen.class)
    private class FailingProdukt extends ProductComponent {

        public FailingProdukt() {
            super(repository, "id", "kindId", "versionId");
        }

        @IpsDefaultValue("attr1")
        public String getDefaultValueAttr1() throws IllegalAccessException {
            throw new IllegalAccessException("forbidden");
        }

        @IpsDefaultValueSetter("attr1")
        public void setDefaultValueAttr1(@SuppressWarnings("unused") String defaultValueAttr1)
                throws IllegalAccessException {
            throw new IllegalAccessException("forbidden");
        }

        /**
         * @param context validation context
         */
        @IpsAllowedValues("attr1")
        public ValueSet<String> getSetOfAllowedValuesForAttr1(IValidationContext context)
                throws IllegalAccessException {
            throw new IllegalAccessException("forbidden");
        }

        @IpsAllowedValuesSetter("attr1")
        public void setSetOfAllowedValuesForAttr1(@SuppressWarnings("unused") ValueSet<String> valueSet)
                throws IllegalAccessException {
            throw new IllegalAccessException("forbidden");
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }
    }

    private class ProduktGen extends ProductComponentGeneration {

        private String defaultValueAttrChangingOverTime = "blub";
        private ValueSet<String> allowedValuesForAttrChangingOverTime = new OrderedValueSet<String>(false, null,
                "foo", "bar");

        public ProduktGen() {
            this(new Produkt());
        }

        public ProduktGen(Produkt produkt) {
            super(produkt);
        }

        @IpsDefaultValue("attrChangingOverTime")
        public String getDefaultValueAttrChangingOverTime() {
            return defaultValueAttrChangingOverTime;
        }

        @IpsDefaultValueSetter("attrChangingOverTime")
        public void setDefaultValueAttrChangingOverTime(String defaultValueAttrChangingOverTime) {
            this.defaultValueAttrChangingOverTime = defaultValueAttrChangingOverTime;
        }

        /**
         * @param context validation context
         */
        @IpsAllowedValues("attrChangingOverTime")
        public ValueSet<String> getSetOfAllowedValuesForAttrChangingOverTime(IValidationContext context) {
            return allowedValuesForAttrChangingOverTime;
        }

        @IpsAllowedValuesSetter("attrChangingOverTime")
        public void setSetOfAllowedValuesForAttrChangingOverTime(ValueSet<String> valueSet) {
            allowedValuesForAttrChangingOverTime = valueSet;
        }
    }

    @IpsPolicyCmptType(name = "VertragABC")
    @IpsAttributes("attr1")
    private class Vertrag implements IModelObject {

        private String attr1;

        @IpsAttribute(name = "attr1", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
        public String getAttr1() {
            return attr1;
        }

        @IpsAttributeSetter("attr1")
        public void setAttr1(String value) {
            attr1 = value;
        }

        /**
         * @param context validation context
         */
        @IpsAllowedValues("attr1")
        public ValueSet<String> getSetOfAllowedValuesForAttr1(IValidationContext context) {
            return new DefaultRange<String>("A", "Z");
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsAttributes({ "const", "attr1", "primitiveUnrestrictedAttr", "attrWithNull", "attrWithoutNull", "overriddenAttr",
            "attrWithValueSetWithoutValidationContext", "attrWithValueSetWithTooManyArgs", "getterOverriddenAttr" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private static class Policy implements IModelObject {

        @IpsAttribute(name = "const", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public final String CONSTANT = "const";

        private int attr1;
        private int primitiveUnrestrictedAttr;
        private Integer attrWithNull;
        private Integer attrWithoutNull;
        private String attrWithValueSetWithoutValidationContext;
        private String attrWithValueSetWithTooManyArgs;
        private boolean getterOverriddenAttr;

        @IpsAttribute(name = "attr1", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        @IpsConfiguredAttribute(changingOverTime = true)
        public int getAttr1() {
            return attr1;
        }

        @IpsAttributeSetter("attr1")
        public void setAttr1(int i) {
            attr1 = i;
        }

        @IpsAttribute(name = "primitiveUnrestrictedAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public int getPrimitiveUnrestrictedAttr() {
            return primitiveUnrestrictedAttr;
        }

        @IpsAttributeSetter("primitiveUnrestrictedAttr")
        public void setPrimitiveUnrestrictedAttr(int primitiveUnrestrictedAttr) {
            this.primitiveUnrestrictedAttr = primitiveUnrestrictedAttr;
        }

        @IpsAttribute(name = "attrWithNull", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public Integer getAttrWithNull() {
            return attrWithNull;
        }

        @IpsAttributeSetter("attrWithNull")
        public void setAttrWithNull(Integer attrWithNull) {
            this.attrWithNull = attrWithNull;
        }

        @IpsAttribute(name = "attrWithoutNull", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public Integer getAttrWithoutNull() {
            return attrWithoutNull;
        }

        @IpsAttributeSetter("attrWithoutNull")
        public void setAttrWithoutNull(Integer attrWithoutNull) {
            this.attrWithoutNull = attrWithoutNull;
        }

        @IpsAllowedValues("attrWithoutNull")
        public ValueSet<Integer> getSetOfAllowedValuesForAttrWithoutNull(
                @SuppressWarnings("unused") IValidationContext context) {
            return new UnrestrictedValueSet<Integer>(false);
        }

        @IpsAttribute(name = "attrWithValueSetWithoutValidationContext", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        public String getAttrWithValueSetWithoutValidationContext() {
            return attrWithValueSetWithoutValidationContext;
        }

        @IpsAttributeSetter("attrWithValueSetWithoutValidationContext")
        public void setAttrWithValueSetWithoutValidationContext(String attrWithValueSetWithoutValidationContext) {
            this.attrWithValueSetWithoutValidationContext = attrWithValueSetWithoutValidationContext;
        }

        @IpsAllowedValues("attrWithValueSetWithoutValidationContext")
        public ValueSet<String> getHandwrittenAllowedValuesForAttrWithValueSetWithoutValidationContext() {
            return new OrderedValueSet<String>(false, null, "lorem", "ipsum");
        }

        @IpsAttribute(name = "attrWithValueSetWithTooManyArgs", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        public String getAttrWithValueSetWithTooManyArgs() {
            return attrWithValueSetWithTooManyArgs;
        }

        @IpsAttributeSetter("attrWithValueSetWithTooManyArgs")
        public void setAttrWithValueSetWithTooManyArgs(String attrWithValueSetWithTooManyArgs) {
            this.attrWithValueSetWithTooManyArgs = attrWithValueSetWithTooManyArgs;
        }

        @SuppressWarnings("unused")
        @IpsAllowedValues("attrWithValueSetWithTooManyArgs")
        public ValueSet<String> getAllowedValuesForAttrWithValueSetWithTooManyArgs(IValidationContext context,
                boolean includeNull) {
            fail("this message should never be called as it has too many args");
            return null;
        }

        @IpsAttribute(name = "overriddenAttr", kind = AttributeKind.DERIVED_ON_THE_FLY, valueSetKind = ValueSetKind.Range)
        public Date getOverriddenAttr() {
            return null;
        }

        @IpsAttribute(name = "getterOverriddenAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public boolean isGetterOverriddenAttr() {
            return getterOverriddenAttr;
        }

        @IpsAttributeSetter("getterOverriddenAttr")
        public void setGetterOverriddenAttr(boolean getterOverriddenAttr) {
            this.getterOverriddenAttr = getterOverriddenAttr;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
    }

    @IpsPolicyCmptType(name = "MySubPolicy")
    @IpsAttributes({ "const", "attr1", "overriddenAttr", "getterOverriddenAttr" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private static class SubPolicy extends Policy {

        private Date overriddenAttr = new Date(0);

        public SubPolicy() {
            setAttr1(42);
        }

        @Override
        @IpsAttribute(name = "overriddenAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
        public Date getOverriddenAttr() {
            return overriddenAttr;
        }

        @IpsAttributeSetter("overriddenAttr")
        public void setOverriddenAttr(Date d) {
            overriddenAttr = d;
        }

        @Override
        @IpsAttribute(name = "getterOverriddenAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        public boolean isGetterOverriddenAttr() {
            return super.isGetterOverriddenAttr();
        }
    }

}
