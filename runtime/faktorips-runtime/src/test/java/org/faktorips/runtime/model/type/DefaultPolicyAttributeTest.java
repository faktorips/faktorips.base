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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.MessageLists;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.ValidationContext;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.ProductConfiguration;
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
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.faktorips.valueset.DecimalRange;
import org.faktorips.valueset.DefaultRange;
import org.faktorips.valueset.IntegerRange;
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

    @Test(expected = IllegalArgumentException.class)
    public void testSetValue_OnTheFly() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(Policy.class);
        PolicyAttribute onTheFly = modelType.getAttribute("onTheFly");
        Policy modelObject = new Policy();

        onTheFly.setValue(modelObject, "asd");
    }

    @Test
    public void testGetValueSet_ProductComponent() {
        Produkt source = new Produkt(repository);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        ValueSet<?> valueSet = attribute.getValueSet(source, null, null);

        assertTrue(valueSet instanceof UnrestrictedValueSet);
    }

    @Test
    public void testGetValueSet_Product_ChangingOverTime() {
        Produkt source = new Produkt(repository);
        ProduktGen produktGen = new ProduktGen(source);
        repository.putProductCmptGeneration(produktGen);

        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        ValueSet<?> valueSet = attribute.getValueSet(source, null, null);

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<>(false, null, "foo", "bar"), valueSet);
    }

    @Test
    public void testGetValueSet_Product_ChangingOverTimeWithCalendar() {
        Produkt source = new Produkt(repository);
        ProduktGen gen = new ProduktGen(source);
        gen.setValidFrom(DateTime.createDateOnly(effectiveDate));
        repository.putProductCmptGeneration(gen);

        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        ValueSet<?> valueSet = attribute.getValueSet(source, effectiveDate, null);

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<>(false, null, "foo", "bar"), valueSet);
    }

    @Test
    public void testGetValueSet_Product_ChangingOverTime_Handwritten() {
        Produkt source = new Produkt(repository);
        ProduktGen produktGen = new ProduktGen(source);
        repository.putProductCmptGeneration(produktGen);

        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithValueSetWithoutValidationContext");
        ValueSet<?> valueSet = attribute.getValueSet(source, effectiveDate, new ValidationContext());

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<>(false, null, "lorem", "ipsum"), valueSet);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValueSet_Product_ChangingOverTime_TooManyArgs() {
        Produkt source = new Produkt(repository);
        ProduktGen produktGen = new ProduktGen(source);
        repository.putProductCmptGeneration(produktGen);

        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithValueSetWithTooManyArgs");
        attribute.getValueSet(source, effectiveDate, new ValidationContext());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetValueSet_Product_ExtensibleEnum() {
        TestExtensibleEnum testExtensibleEnum = new TestExtensibleEnum(2, "a3ID", "a3Name");
        repository.putEnumValues(TestExtensibleEnum.class, List.of(testExtensibleEnum));
        Produkt source = new Produkt(repository);
        ProduktGen produktGen = new ProduktGen(source);
        repository.putProductCmptGeneration(produktGen);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrExtensibleEnum");
        ValueSet<?> valueSet = attribute.getValueSet(source, effectiveDate, new ValidationContext());

        assertTrue(valueSet instanceof OrderedValueSet);
        assertTrue(((ValueSet<TestExtensibleEnum>)valueSet).contains(TestExtensibleEnum.ENUM1));
        assertTrue(((ValueSet<TestExtensibleEnum>)valueSet).contains(TestExtensibleEnum.ENUM2));
        assertTrue(((ValueSet<TestExtensibleEnum>)valueSet).contains(testExtensibleEnum));
        assertTrue(valueSet.containsNull());
    }

    @Test
    public void testGetValueSetFromModel() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Policy.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        ValueSet<?> valueSet = attribute.getValueSetFromModel();
        assertEquals(Policy.MAX_ALLOWED_VALUES_FOR_ATTR1, valueSet);
    }

    @Test
    public void testGetValueSetFromModel_NoField() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Policy.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrBoolean");
        ValueSet<?> valueSet = attribute.getValueSetFromModel();
        assertEquals(new UnrestrictedValueSet<>(), valueSet);
    }

    @Test
    public void testGetValueSet_ModelObject_Handwritten() {
        Policy policy = new Policy();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Policy.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithValueSetWithoutValidationContext");
        ValueSet<?> valueSet = attribute.getValueSet(policy, new ValidationContext());

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<>(false, null, "lorem", "ipsum"), valueSet);
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
        ConfVertrag vertrag = new ConfVertrag(repository);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, new ValidationContext());

        assertTrue(valueSet instanceof UnrestrictedValueSet);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetValueSet_ModelObject_Boolean() {
        Policy policy = new Policy();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Policy.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrBoolean");
        ValueSet<?> valueSet = attribute.getValueSet(policy, new ValidationContext());

        assertTrue(valueSet instanceof OrderedValueSet);
        assertTrue(((ValueSet<Boolean>)valueSet).contains(Boolean.TRUE));
        assertTrue(((ValueSet<Boolean>)valueSet).contains(Boolean.FALSE));
        assertTrue(valueSet.containsNull());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetValueSet_ModelObject_PrimitiveBoolean() {
        Policy policy = new Policy();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Policy.class);

        PolicyAttribute attribute = policyModel.getAttribute("primitiveBooleanAttr");
        ValueSet<?> valueSet = attribute.getValueSet(policy, new ValidationContext());

        assertTrue(valueSet instanceof OrderedValueSet);
        assertTrue(((ValueSet<Boolean>)valueSet).contains(Boolean.TRUE));
        assertTrue(((ValueSet<Boolean>)valueSet).contains(Boolean.FALSE));
        assertFalse(valueSet.containsNull());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetValueSet_ModelObject_Enum() {
        Policy policy = new Policy();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Policy.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrEnum");
        ValueSet<?> valueSet = attribute.getValueSet(policy, new ValidationContext());

        assertTrue(valueSet instanceof OrderedValueSet);
        assertTrue(((ValueSet<TestEnum>)valueSet).contains(TestEnum.TestEnum1));
        assertTrue(((ValueSet<TestEnum>)valueSet).contains(TestEnum.TestEnum2));
        assertTrue(((ValueSet<TestEnum>)valueSet).contains(TestEnum.TestEnum3));
        assertTrue(valueSet.containsNull());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetValueSet_ModelObject_ExtensibleEnum() {
        TestExtensibleEnum testExtensibleEnum = new TestExtensibleEnum(2, "a3ID", "a3Name");
        repository.putEnumValues(TestExtensibleEnum.class, List.of(testExtensibleEnum));
        ConfVertrag vertrag = new ConfVertrag(repository);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrExtensibleEnum");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, new ValidationContext());

        assertTrue(valueSet instanceof OrderedValueSet);
        assertTrue(((ValueSet<TestExtensibleEnum>)valueSet).contains(TestExtensibleEnum.ENUM1));
        assertTrue(((ValueSet<TestExtensibleEnum>)valueSet).contains(TestExtensibleEnum.ENUM2));
        assertTrue(((ValueSet<TestExtensibleEnum>)valueSet).contains(testExtensibleEnum));
        assertTrue(valueSet.containsNull());
    }

    @Test
    public void testGetValueSet_ModelObject_ExtensibleEnum_NoProduct() {
        TestExtensibleEnum testExtensibleEnum = new TestExtensibleEnum(2, "a3ID", "a3Name");
        repository.putEnumValues(TestExtensibleEnum.class, List.of(testExtensibleEnum));
        ConfVertrag vertrag = new ConfVertrag((Produkt)null);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrExtensibleEnum");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, new ValidationContext());

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
    public void testGetValueSet_ModelObject_UnrestrictedNotConfigured() {
        Policy policy = new Policy();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Policy.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithNull");
        ValueSet<?> valueSet = attribute.getValueSet(policy, new ValidationContext());

        assertTrue(valueSet instanceof UnrestrictedValueSet);
        assertTrue(valueSet.containsNull());
    }

    @Test
    public void testGetValueSet_ModelObject_ChangingOverTime() {
        ProduktGen gen = new ProduktGen(repository);
        repository.putProductCmptGeneration(gen);
        ConfVertrag vertrag = new ConfVertrag(repository);
        vertrag.effectiveFrom = Calendar.getInstance();
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, null);

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<>(false, null, "foo", "bar"), valueSet);
    }

    @Test
    public void testGetValueSet_ModelObject_ChangingOverTime_NoEffectiveDate() {
        ConfVertrag vertrag = new ConfVertrag(repository);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, null);

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<>(false, null, "foo", "bar"), valueSet);
    }

    @Test
    public void testGetValueSet_ModelObject_NotChangingOverTime_Handwritten() {
        ConfVertrag vertrag = new ConfVertrag(repository);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrWithValueSetWithoutValidationContext");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, new ValidationContext());

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<>(false, null, "lorem", "ipsum"), valueSet);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValueSet_ModelObject_NotChangingOverTime_TooManyArgs() {
        ConfVertrag vertrag = new ConfVertrag(repository);
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
        assertEquals(new DefaultRange<>("A", "Z"), valueSet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueSet_Failing() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");

        attribute.getValueSet(new FailingProdukt(), null);
    }

    @Test
    public void testGetValueSet_ModelObject_NotConfiguredOnConfigurablePolicy() {
        ConfVertrag vertrag = new ConfVertrag(repository);
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
        Object defaultValue = attribute.getDefaultValue(new Produkt(repository), null);

        assertEquals("foobar", defaultValue);
    }

    @Test
    public void testGetDefaultValue_ModelObject_NotProductRelevant() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attr2");
        Object defaultValue = attribute.getDefaultValue(new ConfVertrag(repository));

        assertEquals(ConfVertrag.DEFAULT_VALUE_FOR_ATTR2, defaultValue);
    }

    @Test
    public void testGetDefaultValue_ModelObject_NotProductRelevant_CamelCaseName() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrExtensibleEnum");
        Object defaultValue = attribute.getDefaultValue(new ConfVertrag(repository));

        assertEquals(TestExtensibleEnum.ENUM2, defaultValue);
    }

    public void testGetDefaultValue_ModelObject_MissingConstant() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(DummyVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        assertThat(attribute.getDefaultValue(new DummyVertrag()), is(nullValue()));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDefaultValue_NotProductRelevant() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attr2");
        attribute.getDefaultValue(new Produkt(repository), null);
    }

    @Test
    public void testGetDefaultValue_ChangingOverTimeWithCalendar() {
        Produkt source = new Produkt(repository);
        ProduktGen gen = new ProduktGen(repository);
        gen.setValidFrom(DateTime.createDateOnly(effectiveDate));
        repository.putProductCmptGeneration(gen);

        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(source, effectiveDate);

        assertEquals("blub", defaultValue);
    }

    @Test
    public void testGetDefaultValue_ChangingOverTime() {
        Produkt source = new Produkt(repository);
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

        attribute.getDefaultValue(new Produkt(repository), null);
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

        ProduktGen gen = new ProduktGen(repository);
        gen.setValidFrom(DateTime.createDateOnly(effectiveFrom));
        repository.putProductCmptGeneration(gen);

        ConfVertrag vertrag = new ConfVertrag(repository);
        vertrag.effectiveFrom = effectiveFrom;
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(vertrag);

        assertEquals("blub", defaultValue);
    }

    @Test
    public void testGetDefaultValue_ModelObject_NoEffectiveFrom() {
        ProduktGen gen = new ProduktGen(repository);
        repository.putProductCmptGeneration(gen);
        ConfVertrag vertrag = new ConfVertrag(repository);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);

        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(vertrag);

        assertEquals("blub", defaultValue);
    }

    @Test
    public void testSetDefaultValue() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        Produkt produkt = new Produkt(repository);

        attribute.setDefaultValue(produkt, null, "new");

        assertEquals("new", produkt.getDefaultValueAttr1());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetDefaultValue_NotProductRelevant() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr2");

        attribute.setDefaultValue(new Produkt(repository), null, "not product relevant");
    }

    @Test
    public void testSetDefaultValue_ChangingOverTimeWithCalendar() {
        Produkt produkt = new Produkt(repository);
        ProduktGen gen = new ProduktGen(repository);
        gen.setValidFrom(DateTime.createDateOnly(effectiveDate));
        repository.putProductCmptGeneration(gen);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");

        attribute.setDefaultValue(produkt, effectiveDate, "new");

        assertEquals("new", gen.getDefaultValueAttrChangingOverTime());
    }

    @Test
    public void testSetDefaultValue_ChangingOverTime() {
        Produkt produkt = new Produkt(repository);
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

        attribute.setDefaultValue(new Produkt(repository), null, "new");
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
        ProduktGen gen = new ProduktGen(repository);
        gen.setValidFrom(DateTime.createDateOnly(effectiveFrom));
        repository.putProductCmptGeneration(gen);
        ConfVertrag vertrag = new ConfVertrag(repository);
        vertrag.effectiveFrom = effectiveFrom;
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");

        attribute.setDefaultValue(vertrag, "new");

        assertEquals("new", gen.getDefaultValueAttrChangingOverTime());
    }

    @Test
    public void testSetDefaultValue_ModelObject_NoEffectiveFrom() {
        ProduktGen gen = new ProduktGen(repository);
        repository.putProductCmptGeneration(gen);
        ConfVertrag vertrag = new ConfVertrag(repository);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");

        attribute.setDefaultValue(vertrag, "new");

        assertEquals("new", gen.getDefaultValueAttrChangingOverTime());
    }

    @Test
    public void testSetValueSet() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        Produkt produkt = new Produkt(repository);
        OrderedValueSet<String> valueSet = new OrderedValueSet<>(false, null, "A", "B", "C");

        attribute.setValueSet(produkt, null, valueSet);

        assertEquals(valueSet, produkt.getSetOfAllowedValuesForAttr1(null));
    }

    @Test
    public void testSetValueSet_WithWrongGenericType() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");
        Produkt produkt = new Produkt(repository);
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<>(false, null, 1, 2, 3);

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

        attribute.setValueSet(new Produkt(repository), null, new UnrestrictedValueSet<>());
    }

    @Test
    public void testSetValueSet_ChangingOverTimeWithCalendar() {
        Produkt produkt = new Produkt(repository);
        ProduktGen gen = new ProduktGen(repository);
        gen.setValidFrom(DateTime.createDateOnly(effectiveDate));
        repository.putProductCmptGeneration(gen);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        OrderedValueSet<String> valueSet = new OrderedValueSet<>(false, null, "A", "B", "C");

        attribute.setValueSet(produkt, effectiveDate, valueSet);

        assertEquals(valueSet, gen.getSetOfAllowedValuesForAttrChangingOverTime(null));
    }

    @Test
    public void testSetValueSet_ChangingOverTime() {
        Produkt produkt = new Produkt(repository);
        ProduktGen gen = new ProduktGen(produkt);
        repository.putProductCmptGeneration(gen);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        OrderedValueSet<String> valueSet = new OrderedValueSet<>(false, null, "A", "B", "C");

        attribute.setValueSet(produkt, null, valueSet);

        assertEquals(valueSet, gen.getSetOfAllowedValuesForAttrChangingOverTime(null));
    }

    @Test(expected = IllegalStateException.class)
    public void testSetValueSet_NotConfigured() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(Vertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");

        attribute.setValueSet(new Produkt(repository), null, new UnrestrictedValueSet<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValueSet_Failing() {
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attr1");

        attribute.setValueSet(new FailingProdukt(), null, new UnrestrictedValueSet<>());
    }

    @Test
    public void testSetValueSet_ModelObject() {
        GregorianCalendar effectiveFrom = new GregorianCalendar();
        ProduktGen gen = new ProduktGen(repository);
        gen.setValidFrom(DateTime.createDateOnly(effectiveFrom));
        repository.putProductCmptGeneration(gen);
        ConfVertrag vertrag = new ConfVertrag(repository);
        vertrag.effectiveFrom = effectiveFrom;
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        OrderedValueSet<String> valueSet = new OrderedValueSet<>(false, null, "A", "B", "C");

        attribute.setValueSet(vertrag, valueSet);

        assertEquals(valueSet, gen.getSetOfAllowedValuesForAttrChangingOverTime(null));
    }

    @Test
    public void testSetValueSet_ModelObject_NoEffectiveFrom() {
        ProduktGen gen = new ProduktGen(repository);
        repository.putProductCmptGeneration(gen);
        ConfVertrag vertrag = new ConfVertrag(repository);
        PolicyCmptType policyModel = IpsModel.getPolicyCmptType(ConfVertrag.class);
        PolicyAttribute attribute = policyModel.getAttribute("attrChangingOverTime");
        OrderedValueSet<String> valueSet = new OrderedValueSet<>(false, null, "A", "B", "C");

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

    @Test
    public void testIsDeprecated() throws Exception {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        assertThat(modelType.getAttribute("attr1").isDeprecated(), is(false));
        assertThat(modelType.getAttribute("deprecatedAttribute").isDeprecated(), is(true));
    }

    @Test
    public void testGetDeprecation() throws Exception {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        assertThat(modelType.getAttribute("attr1").getDeprecation().isPresent(), is(false));
        Optional<Deprecation> deprecation = modelType.getAttribute("deprecatedAttribute").getDeprecation();
        assertThat(deprecation.isPresent(), is(true));
        assertThat(deprecation.get().getSinceVersion().isPresent(), is(false));
        assertThat(deprecation.get().isMarkedForRemoval(), is(false));
    }

    @Test(expected = NullPointerException.class)
    public void testValidate_NoMessageList() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType.getAttribute("attr1");
        Produkt product = new Produkt(repository);

        defaultPolicyAttribute.validate(null, new ValidationContext(), product, null);
    }

    @Test(expected = NullPointerException.class)
    public void testValidate_NoContext() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType.getAttribute("attr1");
        Produkt product = new Produkt(repository);
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validate(messageList, null, product, null);
    }

    @Test(expected = NullPointerException.class)
    public void testValidate_NoProduct() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType.getAttribute("attr1");
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validate(messageList, new ValidationContext(), null, null);
    }

    @Test
    public void testValidate_OK() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType.getAttribute("attr1");
        Produkt product = new Produkt(repository);
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validate(messageList, new ValidationContext(), product, null);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testValidate_DefaultValue() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType.getAttribute("attr1");
        Produkt product = new Produkt(repository);
        product.defaultValueAttr1 = "foo";
        product.allowedValuesForAttr1 = new OrderedValueSet<>(false, null, "bar");
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validate(messageList, new ValidationContext(Locale.ENGLISH), product, null);

        assertThat(messageList.isEmpty(), is(false));
        assertThat(messageList.getMessageByCode(DefaultPolicyAttribute.MSGCODE_DEFAULT_VALUE_NOT_IN_VALUE_SET),
                is(not(nullValue())));
    }

    @Test
    public void testValidate_ValueSet() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfiguredPolicy.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType
                .getAttribute(ConfiguredPolicy.PROPERTY_INTEGERATTRIBUTE);
        ConfiguringProduct product = new ConfiguringProduct(repository, "t1", "t", "1");
        product.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(-1000, 1000));
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validate(messageList, new ValidationContext(Locale.ENGLISH), product, null);

        assertThat(messageList.isEmpty(), is(false));
        assertThat(messageList.getMessageByCode(DefaultPolicyAttribute.MSGCODE_VALUE_SET_NOT_IN_VALUE_SET),
                is(not(nullValue())));
    }

    @Test
    public void testValidateValueSetNotEmptyIfMandatory_OK() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType
                .getAttribute("orderedValueSet");
        Produkt product = new Produkt(repository);
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validateValueSetNotEmptyIfMandatory(messageList, new ValidationContext(), product, null);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testValidateValueSetNotEmptyIfMandatory_ValueSetIsMandatoryAndEmpty() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType
                .getAttribute("orderedValueSet");
        Produkt product = new Produkt(repository);
        product.setAllowedValuesForOrderedValueSet(OrderedValueSet.of());
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validateValueSetNotEmptyIfMandatory(messageList, new ValidationContext(),
                product, null);

        assertThat(messageList.isEmpty(), is(false));
        Message message = messageList.getMessage(0);
        assertThat(message.getCode(), is(DefaultPolicyAttribute.MSGCODE_MANDATORY_VALUESET_IS_EMPTY));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        ObjectProperty objectProperty = message.getInvalidObjectProperties().get(0);
        assertThat(objectProperty.getObject(), is(defaultPolicyAttribute));
        assertThat(objectProperty.getProperty(), is(DefaultPolicyAttribute.PROPERTY_VALUE_SET));
    }

    @Test
    public void testValidateDefaultValue_OK() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType.getAttribute("attr1");
        Produkt product = new Produkt(repository);
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validateDefaultValue(messageList, new ValidationContext(), product, null);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testValidateDefaultValue_DefaultValueNotAllowedInValueSet() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType.getAttribute("attr1");
        Produkt product = new Produkt(repository);
        product.defaultValueAttr1 = "foo";
        product.allowedValuesForAttr1 = new OrderedValueSet<>(false, null, "bar");
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validateDefaultValue(messageList, new ValidationContext(Locale.ENGLISH), product, null);

        assertThat(messageList.isEmpty(), is(false));
        Message message = messageList.getMessage(0);
        assertThat(message.getCode(), is(DefaultPolicyAttribute.MSGCODE_DEFAULT_VALUE_NOT_IN_VALUE_SET));
        assertThat(message.getText(), containsString("default value"));
        assertThat(message.getText(), containsString("attribute"));
        assertThat(message.getText(), containsString("the first attribute"));
        assertThat(message.getText(), containsString("foo"));
        assertThat(message.getText(), containsString("value set"));
        assertThat(message.getText(), containsString("bar"));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        ObjectProperty objectProperty = message.getInvalidObjectProperties().get(0);
        assertThat(objectProperty.getObject(), is(defaultPolicyAttribute));
        assertThat(objectProperty.getProperty(), is(DefaultPolicyAttribute.PROPERTY_DEFAULT_VALUE));
    }

    @Test
    public void testValidateDefaultValue_DefaultValueNotAllowedInValueSet_LocaleDE() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType.getAttribute("attr1");
        Produkt product = new Produkt(repository);
        product.defaultValueAttr1 = "foo";
        product.allowedValuesForAttr1 = new OrderedValueSet<>(false, null, "bar");
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validateDefaultValue(messageList, new ValidationContext(Locale.GERMAN), product, null);

        assertThat(messageList.isEmpty(), is(false));
        Message message = messageList.getMessage(0);
        assertThat(message.getCode(), is(DefaultPolicyAttribute.MSGCODE_DEFAULT_VALUE_NOT_IN_VALUE_SET));
        assertThat(message.getText(), containsString("Vorbelegung"));
        assertThat(message.getText(), containsString("Attribut"));
        assertThat(message.getText(), containsString("Das erste Attribut"));
        assertThat(message.getText(), containsString("foo"));
        assertThat(message.getText(), containsString("Wertemenge"));
        assertThat(message.getText(), containsString("bar"));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        ObjectProperty objectProperty = message.getInvalidObjectProperties().get(0);
        assertThat(objectProperty.getObject(), is(defaultPolicyAttribute));
        assertThat(objectProperty.getProperty(), is(DefaultPolicyAttribute.PROPERTY_DEFAULT_VALUE));
    }

    @Test
    public void testValidateDefaultValue_NullIsOk() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType.getAttribute("attr1");
        Produkt product = new Produkt(repository);
        product.defaultValueAttr1 = null;
        product.allowedValuesForAttr1 = new OrderedValueSet<>(false, null, "bar");
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validateDefaultValue(messageList, new ValidationContext(), product, null);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testValidateDefaultValue_DecimalNullIsOk() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType.getAttribute("decAttr");
        Produkt product = new Produkt(repository);
        product.defaultValueDecAttr = Decimal.NULL;
        product.allowedValuesForDecAttr = DecimalRange.valueOf(Decimal.ZERO, Decimal.valueOf(100),
                Decimal.valueOf(1, 3), false);
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validateDefaultValue(messageList, new ValidationContext(), product, null);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testValidateDefaultValue_MoneyNullIsOk() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfVertrag.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType.getAttribute("money");
        Produkt product = new Produkt(repository);
        product.defaultValueMoney = Money.NULL;
        product.allowedValuesForMoney = new OrderedValueSet<>(false, Money.NULL, Money.euro(12, 50));
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validateDefaultValue(messageList, new ValidationContext(), product, null);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testValidateValueSet_OK() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfiguredPolicy.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType
                .getAttribute(ConfiguredPolicy.PROPERTY_INTEGERATTRIBUTE);
        ConfiguringProduct product = new ConfiguringProduct(repository, "t1", "t", "1");
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validateValueSet(messageList, new ValidationContext(), product, null);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testValidateValueSet_ValueSetNotInValueSet() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfiguredPolicy.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType
                .getAttribute(ConfiguredPolicy.PROPERTY_INTEGERATTRIBUTE);
        ConfiguringProduct product = new ConfiguringProduct(repository, "t1", "t", "1");
        product.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(-1000, 1000));
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validateValueSet(messageList, new ValidationContext(Locale.ENGLISH), product, null);

        assertThat(messageList.isEmpty(), is(false));
        Message message = messageList.getMessage(0);
        assertThat(message.getCode(), is(DefaultPolicyAttribute.MSGCODE_VALUE_SET_NOT_IN_VALUE_SET));
        assertThat(message.getText(), containsString("value set"));
        assertThat(message.getText(), containsString("attribute"));
        assertThat(message.getText(), containsString("Integer Attribute"));
        assertThat(message.getText(), containsString("-1000-1000"));
        assertThat(message.getText(), containsString("value set"));
        assertThat(message.getText(), containsString("0-100"));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        ObjectProperty objectProperty = message.getInvalidObjectProperties().get(0);
        assertThat(objectProperty.getObject(), is(defaultPolicyAttribute));
        assertThat(objectProperty.getProperty(), is(DefaultPolicyAttribute.PROPERTY_VALUE_SET));
    }

    @Test
    public void testValidateValueSet_DefaultValueNotAllowedInValueSet_LocaleDE() {
        PolicyCmptType modelType = IpsModel.getPolicyCmptType(ConfiguredPolicy.class);
        DefaultPolicyAttribute defaultPolicyAttribute = (DefaultPolicyAttribute)modelType
                .getAttribute(ConfiguredPolicy.PROPERTY_INTEGERATTRIBUTE);
        ConfiguringProduct product = new ConfiguringProduct(repository, "t1", "t", "1");
        product.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(-1000, 1000));
        MessageList messageList = MessageLists.emptyMessageList();

        defaultPolicyAttribute.validateValueSet(messageList, new ValidationContext(Locale.GERMAN), product, null);

        assertThat(messageList.isEmpty(), is(false));
        Message message = messageList.getMessage(0);
        assertThat(message.getCode(), is(DefaultPolicyAttribute.MSGCODE_VALUE_SET_NOT_IN_VALUE_SET));
        assertThat(message.getText(), containsString("Wertemenge"));
        assertThat(message.getText(), containsString("Attribut"));
        assertThat(message.getText(), containsString("Ganzzahliges Attribut"));
        assertThat(message.getText(), containsString("-1000-1000"));
        assertThat(message.getText(), containsString("Wertemenge"));
        assertThat(message.getText(), containsString("0-100"));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        ObjectProperty objectProperty = message.getInvalidObjectProperties().get(0);
        assertThat(objectProperty.getObject(), is(defaultPolicyAttribute));
        assertThat(objectProperty.getProperty(), is(DefaultPolicyAttribute.PROPERTY_VALUE_SET));
    }

    @IpsPolicyCmptType(name = "Vertragxyz")
    @IpsConfiguredBy(Produkt.class)
    @IpsAttributes({ "attr1", "attr2", "attrChangingOverTime", "attrWithValueSetWithoutValidationContext",
            "attrWithValueSetWithTooManyArgs", "attrExtensibleEnum", "attrExtensibleEnumConfigured",
            "deprecatedAttribute", "decAttr", "money", "orderedValueSet" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.DefaultPolicyAttributeTest", defaultLocale = "de")
    private static class ConfVertrag implements IConfigurableModelObject {

        @IpsDefaultValue("attr2")
        public static final String DEFAULT_VALUE_FOR_ATTR2 = "foo";
        @IpsDefaultValue("attrExtensibleEnum")
        public static final TestExtensibleEnum DEFAULT_VALUE_FOR_ATTR_EXTENSIBLE_ENUM = TestExtensibleEnum.ENUM2;
        @IpsAllowedValues("orderedValueSet")
        public static final OrderedValueSet<String> MAX_ALLOWED_VALUES_FOR_ORDERED_VALUE_SET = new OrderedValueSet<>(
                false,
                null, "a", "b", "c");
        @IpsDefaultValue("orderedValueSet")
        public static final String DEFAULT_VALUE_FOR_ORDERED_VALUE_SET = null;

        private Produkt produkt;

        private String attr1;
        private String attr2 = DEFAULT_VALUE_FOR_ATTR2;
        private String attrChangingOverTime;
        private String attrWithValueSetWithoutValidationContext;
        private String attrWithValueSetWithTooManyArgs;
        private TestExtensibleEnum attrExtensibleEnum = DEFAULT_VALUE_FOR_ATTR_EXTENSIBLE_ENUM;
        private TestExtensibleEnum attrExtensibleEnumConfigured;
        private Calendar effectiveFrom;
        private Decimal decAttr;
        private Money money;
        private String orderedValueSet = DEFAULT_VALUE_FOR_ORDERED_VALUE_SET;

        public ConfVertrag(IRuntimeRepository repository) {
            this(new Produkt(repository));
        }

        public ConfVertrag(Produkt produkt) {
            this.produkt = produkt;
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
            return new UnrestrictedValueSet<>(false);
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
            return new OrderedValueSet<>(false, null, "foo", "bar");
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

        @IpsAllowedValues("attrWithValueSetWithTooManyArgs")
        public ValueSet<String> getAllowedValuesForAttrWithValueSetWithTooManyArgs(IValidationContext context,
                boolean includeNull) {
            return produkt.getAllowedValuesForAttrWithValueSetWithTooManyArgs(context, includeNull);
        }

        @IpsAttribute(name = "attrExtensibleEnum", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        public TestExtensibleEnum getAttrExtensibleEnum() {
            return attrExtensibleEnum;
        }

        @IpsAttributeSetter("attrExtensibleEnum")
        public void setAttrExtensibleEnum(TestExtensibleEnum attrExtensibleEnum) {
            this.attrExtensibleEnum = attrExtensibleEnum;
        }

        @IpsAttribute(name = "attrExtensibleEnumConfigured", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        @IpsConfiguredAttribute(changingOverTime = false)
        public TestExtensibleEnum getAttrExtensibleEnumConfigured() {
            return attrExtensibleEnumConfigured;
        }

        @IpsAttributeSetter("attrExtensibleEnumConfigured")
        public void setAttrExtensibleEnumConfigured(TestExtensibleEnum attrExtensibleEnumConfigured) {
            this.attrExtensibleEnumConfigured = attrExtensibleEnumConfigured;
        }

        @IpsAttribute(name = "decAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        @IpsConfiguredAttribute(changingOverTime = false)
        public Decimal getDecAttr() {
            return decAttr;
        }

        @IpsAttributeSetter("decAttr")
        public void setDecAttr(Decimal value) {
            decAttr = value;
        }

        @IpsAttribute(name = "money", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        @IpsConfiguredAttribute(changingOverTime = false)
        public Money getMoney() {
            return money;
        }

        @IpsAttributeSetter("money")
        public void setMoney(Money value) {
            money = value;
        }

        @IpsAllowedValues("money")
        public ValueSet<Money> getSetOfAllowedValuesForMoney(IValidationContext context) {
            return produkt.getSetOfAllowedValuesForMoney(context);
        }

        @IpsAllowedValues("decAttr")
        public ValueSet<Decimal> getSetOfAllowedValuesForDecAttr(IValidationContext context) {
            return produkt.getSetOfAllowedValuesForDecAttr(context);
        }

        @IpsAllowedValues("orderedValueSet")
        public ValueSet<String> getAllowedValuesForOrderedValueSet() {
            return produkt.getAllowedValuesForOrderedValueSet();
        }

        @IpsAttribute(name = "orderedValueSet", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        @IpsConfiguredAttribute(changingOverTime = false)
        public String getOrderedValueSet() {
            return orderedValueSet;
        }

        @IpsAttributeSetter("orderedValueSet")
        public void setOrderedValueSet(String newValue) {
            orderedValueSet = newValue;
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
            // nothing to do
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            produkt = (Produkt)productComponent;
        }

        @IpsAttribute(name = "deprecatedAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        @Deprecated
        public int getDeprecatedAttribute() {
            return -1;
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
    private static class Produkt extends ProductComponent {

        private String defaultValueAttr1 = "foobar";
        private ValueSet<String> allowedValuesForAttr1 = new UnrestrictedValueSet<>();
        private ValueSet<Decimal> allowedValuesForDecAttr = new UnrestrictedValueSet<>();
        private ValueSet<Money> allowedValuesForMoney = new UnrestrictedValueSet<>();
        private Decimal defaultValueDecAttr;
        private Money defaultValueMoney;
        private String defaultValueOrderedValueSet = null;
        private OrderedValueSet<String> allowedValuesForOrderedValueSet = ConfVertrag.MAX_ALLOWED_VALUES_FOR_ORDERED_VALUE_SET;

        public Produkt(IRuntimeRepository repository) {
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
            return new OrderedValueSet<>(false, null, "lorem", "ipsum");
        }

        @IpsDefaultValue("decAttr")
        public Decimal getDefaultValueDecAttr() {
            return defaultValueDecAttr;
        }

        @IpsDefaultValueSetter("decAttr")
        public void setDefaultValueDecAttr(Decimal defaultValueDecAttr) {
            this.defaultValueDecAttr = defaultValueDecAttr;
        }

        /**
         * @param context validation context
         */
        @IpsAllowedValues("decAttr")
        public ValueSet<Decimal> getSetOfAllowedValuesForDecAttr(IValidationContext context) {
            return allowedValuesForDecAttr;
        }

        @IpsAllowedValuesSetter("decAttr")
        public void setSetOfAllowedValuesForDecAttr(ValueSet<Decimal> valueSet) {
            allowedValuesForDecAttr = valueSet;
        }

        @IpsDefaultValue("money")
        public Money getDefaultValueMoney() {
            return defaultValueMoney;
        }

        @IpsDefaultValueSetter("money")
        public void setDefaultValueMoney(Money defaultValueMoney) {
            this.defaultValueMoney = defaultValueMoney;
        }

        /**
         * @param context validation context
         */
        @IpsAllowedValues("money")
        public ValueSet<Money> getSetOfAllowedValuesForMoney(IValidationContext context) {
            return allowedValuesForMoney;
        }

        @IpsAllowedValuesSetter("money")
        public void setSetOfAllowedValuesForMoney(ValueSet<Money> valueSet) {
            allowedValuesForMoney = valueSet;
        }

        @IpsDefaultValue("orderedValueSet")
        public String getDefaultValueOrderedValueSet() {
            return defaultValueOrderedValueSet;
        }

        @IpsDefaultValueSetter("orderedValueSet")
        public void setDefaultValueOrderedValueSet(String defaultValueOrderedValueSet) {
            if (getRepository() != null && !getRepository().isModifiable()) {
                throw new IllegalRepositoryModificationException();
            }
            this.defaultValueOrderedValueSet = defaultValueOrderedValueSet;
        }

        @IpsAllowedValues("orderedValueSet")
        public ValueSet<String> getAllowedValuesForOrderedValueSet() {
            return allowedValuesForOrderedValueSet;
        }

        @IpsAllowedValuesSetter("orderedValueSet")
        public void setAllowedValuesForOrderedValueSet(ValueSet<String> allowedValuesForOrderedValueSet) {
            if (getRepository() != null && !getRepository().isModifiable()) {
                throw new IllegalRepositoryModificationException();
            }
            this.allowedValuesForOrderedValueSet = (OrderedValueSet<String>)allowedValuesForOrderedValueSet;
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

    private static class ProduktGen extends ProductComponentGeneration {

        private String defaultValueAttrChangingOverTime = "blub";
        private ValueSet<String> allowedValuesForAttrChangingOverTime = new OrderedValueSet<>(false, null,
                "foo", "bar");

        public ProduktGen(IRuntimeRepository repository) {
            this(new Produkt(repository));
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
            return new DefaultRange<>("A", "Z");
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsAttributes({ "const", "attr1", "primitiveUnrestrictedAttr", "attrWithNull", "attrWithoutNull", "overriddenAttr",
            "attrWithValueSetWithoutValidationContext", "attrWithValueSetWithTooManyArgs", "getterOverriddenAttr",
            "attrBoolean", "primitiveBooleanAttr", "attrEnum", "onTheFly" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private static class Policy implements IModelObject {

        @IpsAttribute(name = "const", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public final String CONSTANT = "const";

        @IpsAllowedValues("attr1")
        public static final ValueSet<Integer> MAX_ALLOWED_VALUES_FOR_ATTR1 = new UnrestrictedValueSet<>(
                false);

        private int attr1;
        private int primitiveUnrestrictedAttr;
        private boolean primitiveBooleanAttr;
        private Boolean attrBoolean;
        private TestEnum attrEnum;
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
            return new UnrestrictedValueSet<>(false);
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
            return new OrderedValueSet<>(false, null, "lorem", "ipsum");
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

        @IpsAttribute(name = "primitiveBooleanAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public boolean isPrimitiveBooleanAttr() {
            return primitiveBooleanAttr;
        }

        @IpsAttributeSetter("primitiveBooleanAttr")
        public void setPrimitiveBooleanAttr(boolean primitiveBooleanAttr) {
            this.primitiveBooleanAttr = primitiveBooleanAttr;
        }

        @IpsAttribute(name = "attrBoolean", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public Boolean getAttrBoolean() {
            return attrBoolean;
        }

        @IpsAttributeSetter("attrBoolean")
        public void setAttrBoolean(Boolean attrBoolean) {
            this.attrBoolean = attrBoolean;
        }

        @IpsAttribute(name = "attrEnum", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        public TestEnum getAttrEnum() {
            return attrEnum;
        }

        @IpsAttributeSetter("attrEnum")
        public void setAttrEnum(TestEnum attrEnum) {
            this.attrEnum = attrEnum;
        }

        public static final OrderedValueSet<String> MAX_ALLOWED_VALUES_FOR_ON_THE_FLY = new OrderedValueSet<>(false,
                null);

        @IpsAllowedValues("onTheFly")
        public ValueSet<String> getAllowedValuesForOnTheFly() {
            return MAX_ALLOWED_VALUES_FOR_ON_THE_FLY;
        }

        @IpsAttribute(name = "onTheFly", kind = AttributeKind.DERIVED_ON_THE_FLY, valueSetKind = ValueSetKind.Enum)
        public String getOnTheFly() {
            // begin-user-code
            return "SOMEVAL";
            // end-user-code
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

    private enum TestEnum {
        TestEnum1,
        TestEnum2,
        TestEnum3;
    }

    @IpsPolicyCmptType(name = "ConfiguredPolicy")
    @IpsAttributes({ "integerAttribute" })
    @IpsConfiguredBy(ConfiguringProduct.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.DefaultPolicyAttributeTest", defaultLocale = "de")
    private static class ConfiguredPolicy extends AbstractModelObject implements IConfigurableModelObject {

        public static final String PROPERTY_INTEGERATTRIBUTE = "integerAttribute";

        @IpsAllowedValues("integerAttribute")
        public static final IntegerRange MAX_ALLOWED_RANGE_FOR_INTEGER_ATTRIBUTE = IntegerRange
                .valueOf(Integer.valueOf("0"), Integer.valueOf(100), Integer.valueOf(5), false);

        @IpsDefaultValue("integerAttribute")
        public static final Integer DEFAULT_VALUE_FOR_INTEGER_ATTRIBUTE = null;

        private Integer integerAttribute = DEFAULT_VALUE_FOR_INTEGER_ATTRIBUTE;

        private ProductConfiguration productConfiguration;

        public ConfiguredPolicy(ConfiguringProduct productCmpt) {
            super();
            productConfiguration = new ProductConfiguration(productCmpt);
        }

        @IpsAllowedValues("integerAttribute")
        public ValueSet<Integer> getAllowedValuesForIntegerAttribute() {
            return getConfiguringProduct().getAllowedValuesForIntegerAttribute();
        }

        @IpsAttribute(name = "integerAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
        @IpsConfiguredAttribute(changingOverTime = false)
        public Integer getIntegerAttribute() {
            return integerAttribute;
        }

        @IpsAttributeSetter("integerAttribute")
        public void setIntegerAttribute(Integer newValue) {
            integerAttribute = newValue;
        }

        @Override
        public void initialize() {
            if (getConfiguringProduct() != null) {
                setIntegerAttribute(getConfiguringProduct().getDefaultValueIntegerAttribute());
            }
        }

        public ConfiguringProduct getConfiguringProduct() {
            return (ConfiguringProduct)getProductComponent();
        }

        @Override
        public IProductComponent getProductComponent() {
            return productConfiguration.getProductComponent();
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            productConfiguration.setProductComponent(productComponent);
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return null;
        }

    }

    @IpsProductCmptType(name = "ConfiguringProduct")
    @IpsConfigures(ConfiguredPolicy.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.DefaultPolicyAttributeTest", defaultLocale = "de")
    private static class ConfiguringProduct extends ProductComponent {

        private Integer defaultValueIntegerAttribute = null;

        private IntegerRange rangeForIntegerAttribute = ConfiguredPolicy.MAX_ALLOWED_RANGE_FOR_INTEGER_ATTRIBUTE;

        public ConfiguringProduct(IRuntimeRepository repository, String id, String kindId, String versionId) {
            super(repository, id, kindId, versionId);
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

        @IpsDefaultValue("integerAttribute")
        public Integer getDefaultValueIntegerAttribute() {
            return defaultValueIntegerAttribute;
        }

        @IpsDefaultValueSetter("integerAttribute")
        public void setDefaultValueIntegerAttribute(Integer defaultValueIntegerAttribute) {
            if (getRepository() != null && !getRepository().isModifiable()) {
                throw new IllegalRepositoryModificationException();
            }
            this.defaultValueIntegerAttribute = defaultValueIntegerAttribute;
        }

        @IpsAllowedValues("integerAttribute")
        public ValueSet<Integer> getAllowedValuesForIntegerAttribute() {
            return rangeForIntegerAttribute;
        }

        @IpsAllowedValuesSetter("integerAttribute")
        public void setAllowedValuesForIntegerAttribute(ValueSet<Integer> rangeForIntegerAttribute) {
            if (getRepository() != null && !getRepository().isModifiable()) {
                throw new IllegalRepositoryModificationException();
            }
            this.rangeForIntegerAttribute = (IntegerRange)rangeForIntegerAttribute;
        }

        public ConfiguredPolicy createConfiguredPolicy() {
            ConfiguredPolicy policy = new ConfiguredPolicy(this);
            policy.initialize();
            return policy;
        }

        @Override
        public ConfiguredPolicy createPolicyComponent() {
            return createConfiguredPolicy();
        }

    }

}
