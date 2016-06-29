package org.faktorips.runtime.modeltype.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ValidationContext;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.ValueSetType;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.faktorips.runtime.modeltype.IPolicyAttributeModel;
import org.faktorips.valueset.DefaultRange;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PolicyAttributeModelTest {

    @Mock
    private IRuntimeRepository repository;

    private final Calendar effectiveDate = new GregorianCalendar(1999, 1, 1);

    @Test
    public void testGetValue() {
        IPolicyModel modelType = Models.getPolicyModel(Policy.class);
        IPolicyAttributeModel constant = modelType.getAttribute("const");
        IPolicyAttributeModel attr1 = modelType.getAttribute("attr1");
        IPolicyAttributeModel attr2 = modelType.getAttribute("attr2");
        IPolicyModel subModelType = Models.getPolicyModel(SubPolicy.class);
        IPolicyAttributeModel subConstant = subModelType.getAttribute("const");
        IPolicyAttributeModel subAttr1 = subModelType.getAttribute("attr1");
        IPolicyAttributeModel subAttr2 = subModelType.getAttribute("attr2");
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
        IPolicyModel modelType = Models.getPolicyModel(Policy.class);
        IPolicyAttributeModel attr1 = modelType.getAttribute("attr1");
        IPolicyModel subModelType = Models.getPolicyModel(SubPolicy.class);
        IPolicyAttributeModel subAttr1 = subModelType.getAttribute("attr1");
        Policy modelObject = new Policy();

        attr1.setValue(modelObject, 412);

        assertEquals(412, modelObject.attr1);

        subAttr1.setValue(modelObject, 567);

        assertEquals(567, modelObject.attr1);
    }

    @Test
    public void testSetValue_Overwritten() {
        IPolicyModel subModelType = Models.getPolicyModel(SubPolicy.class);
        IPolicyAttributeModel subAttr2 = subModelType.getAttribute("attr2");
        SubPolicy subPolicy = new SubPolicy();

        subAttr2.setValue(subPolicy, new Date(1));

        assertEquals(new Date(1), subPolicy.attr2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValue_CannotModifyConstant() {
        IPolicyModel modelType = Models.getPolicyModel(Policy.class);
        IPolicyAttributeModel constant = modelType.getAttribute("const");
        IPolicyModel subModelType = Models.getPolicyModel(SubPolicy.class);
        IPolicyAttributeModel subConstant = subModelType.getAttribute("const");
        Policy modelObject = new Policy();

        constant.setValue(modelObject, "asd");
        subConstant.setValue(modelObject, "asd");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValue_CannotDerivedAttribute() {
        IPolicyModel modelType = Models.getPolicyModel(Policy.class);
        IPolicyAttributeModel attr2 = modelType.getAttribute("attr2");
        Policy modelObject = new Policy();

        attr2.setValue(modelObject, "asd");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValue_CannotDerivedAttributeWithOverwritten() {
        IPolicyModel subModelType = Models.getPolicyModel(SubPolicy.class);
        IPolicyAttributeModel subAttr2 = subModelType.getAttribute("attr2");
        Policy modelObject = new Policy();

        subAttr2.setValue(modelObject, "asd");
    }

    @Test
    public void testGetValueSet_productComponent() {
        Produkt source = new Produkt();
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attr1");
        ValueSet<?> valueSet = attribute.getValueSet(source, null, null);

        assertTrue(valueSet instanceof UnrestrictedValueSet);
    }

    @Test
    public void testGetValueSet_changingOverTime() {
        Produkt source = new Produkt();
        when(repository.getLatestProductComponentGeneration(source)).thenReturn(new ProduktGen());
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attrChangingOverTime");
        ValueSet<?> valueSet = attribute.getValueSet(source, null, null);

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<String>(false, null, "foo", "bar"), valueSet);
    }

    @Test
    public void testGetValueSet_changingOverTimeWithCalendar() {
        Produkt source = new Produkt();
        ProduktGen gen = new ProduktGen();
        when(repository.getProductComponentGeneration("id", effectiveDate)).thenReturn(gen);
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attrChangingOverTime");
        ValueSet<?> valueSet = attribute.getValueSet(source, effectiveDate, null);

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<String>(false, null, "foo", "bar"), valueSet);
    }

    @Test
    public void testGetValueSet_modelObject() {
        ConfVertrag vertrag = new ConfVertrag();
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attr1");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, new ValidationContext());

        assertTrue(valueSet instanceof UnrestrictedValueSet);
    }

    @Test
    public void testGetValueSet_modelObjectChangingOverTime() {
        ProduktGen gen = new ProduktGen();
        ConfVertrag vertrag = new ConfVertrag();
        vertrag.effectiveFrom = Calendar.getInstance();
        when(repository.getProductComponentGeneration("id", vertrag.effectiveFrom)).thenReturn(gen);
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attrChangingOverTime");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, null);

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<String>(false, null, "foo", "bar"), valueSet);
    }

    @Test
    public void testGetValueSet_modelObjectChangingOverTime_noEffectiveDate() {
        ConfVertrag vertrag = new ConfVertrag();
        when(repository.getLatestProductComponentGeneration(vertrag.getProductComponent()))
        .thenReturn(new ProduktGen());
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attrChangingOverTime");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, null);

        assertTrue(valueSet instanceof OrderedValueSet);
        assertEquals(new OrderedValueSet<String>(false, null, "foo", "bar"), valueSet);
    }

    @Test
    public void testGetValueSet_notConfigured() {
        Vertrag vertrag = new Vertrag();
        IPolicyModel policyModel = Models.getPolicyModel(Vertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attr1");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, null);

        assertTrue(valueSet instanceof DefaultRange);
        assertEquals(new DefaultRange<String>("A", "Z"), valueSet);
    }

    @Test
    public void testGetValueSet_notConfiguredOnConfigurablePolicy() {
        ConfVertrag vertrag = new ConfVertrag();
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attr2");
        ValueSet<?> valueSet = attribute.getValueSet(vertrag, null);

        assertTrue(valueSet instanceof UnrestrictedValueSet);
        assertFalse(valueSet.containsNull());
    }

    @Test
    public void testGetDefaultValue() {
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attr1");
        Object defaultValue = attribute.getDefaultValue(new Produkt(), null);

        assertEquals("foobar", defaultValue);
    }

    @Test
    public void testGetDefaultValue_changingOverTimeWithCalendar() {
        Produkt source = new Produkt();
        ProduktGen gen = new ProduktGen();
        when(repository.getProductComponentGeneration("id", effectiveDate)).thenReturn(gen);
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(source, effectiveDate);

        assertEquals("blub", defaultValue);
    }

    @Test
    public void testGetDefaultValue_changingOverTime() {
        Produkt source = new Produkt();
        ProduktGen gen = new ProduktGen();
        when(repository.getLatestProductComponentGeneration(source)).thenReturn(gen);
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(source, null);

        assertEquals("blub", defaultValue);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDefaultValue_notConfigured() {
        IPolicyModel policyModel = Models.getPolicyModel(Vertrag.class);
        IPolicyAttributeModel attribute = policyModel.getAttribute("attr1");

        attribute.getDefaultValue(new Produkt(), null);
    }

    @Test
    public void testGetDefaultValue_modelObject() {
        ProduktGen gen = new ProduktGen();
        ConfVertrag vertrag = new ConfVertrag();
        vertrag.effectiveFrom = Calendar.getInstance();
        when(repository.getProductComponentGeneration("id", vertrag.effectiveFrom)).thenReturn(gen);
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(vertrag);

        assertEquals("blub", defaultValue);
    }

    @Test
    public void testGetDefaultValue_modelObject_noEffectiveFrom() {
        ProduktGen gen = new ProduktGen();
        ConfVertrag vertrag = new ConfVertrag();
        when(repository.getLatestProductComponentGeneration(vertrag.getProductComponent())).thenReturn(gen);
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);

        IPolicyAttributeModel attribute = policyModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(vertrag);

        assertEquals("blub", defaultValue);
    }

    @Test
    public void testIsChangingOverTime() {
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);
        IPolicyAttributeModel attribute = policyModel.getAttribute("attr1");

        assertFalse(attribute.isChangingOverTime());
    }

    @Test
    public void testIsChangingOverTime_true() {
        IPolicyModel policyModel = Models.getPolicyModel(ConfVertrag.class);
        IPolicyAttributeModel attribute = policyModel.getAttribute("attrChangingOverTime");

        assertTrue(attribute.isChangingOverTime());
    }

    @Test
    public void testIsChangingOverTime_noAnnotation() {
        IPolicyModel policyModel = Models.getPolicyModel(DummyVertrag.class);
        IPolicyAttributeModel attribute = policyModel.getAttribute("attrChangingOverTime");

        assertFalse(attribute.isChangingOverTime());
    }

    @Test
    public void testGetLabel_overwrittenAttribute() {
        IModelType modelType = Models.getModelType(Policy.class);
        IModelType subModelType = Models.getModelType(SubPolicy.class);
        IModelTypeAttribute attribute = modelType.getAttribute("attr1");
        IModelTypeAttribute overwritingAttribute = subModelType.getAttribute("attr1");

        assertEquals("MeinAttribut (original)", attribute.getLabel(Locale.GERMAN));
        assertEquals("MeinAttribut (überschrieben)", overwritingAttribute.getLabel(Locale.GERMAN));
    }

    @IpsPolicyCmptType(name = "Vertragxyz")
    @IpsConfiguredBy(Produkt.class)
    @IpsAttributes({ "attr1", "attr2", "attrChangingOverTime" })
    private class ConfVertrag implements IConfigurableModelObject {

        private final Produkt produkt;

        private String attr1;
        private String attr2;
        private String attrChangingOverTime;
        private Calendar effectiveFrom;

        public ConfVertrag() {
            produkt = new Produkt();
        }

        @IpsAttribute(name = "attr1", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
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

        @IpsAttribute(name = "attr2", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
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

        @IpsAttribute(name = "attrChangingOverTime", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
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
    }

    @IpsPolicyCmptType(name = "VertragDummy")
    @IpsAttributes("attrChangingOverTime")
    public class DummyVertrag implements IModelObject {

        @IpsAttribute(name = "attrChangingOverTime", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
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

        public Produkt() {
            super(repository, "id", "kindId", "versionId");
        }

        @IpsDefaultValue("attr1")
        public String getDefaultValueAttr1() {
            return "foobar";
        }

        /**
         * @param context validation context
         */
        @IpsAllowedValues("attr1")
        public ValueSet<String> getSetOfAllowedValuesForAttr1(IValidationContext context) {
            return new UnrestrictedValueSet<String>();
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

    private class ProduktGen extends ProductComponentGeneration {

        public ProduktGen() {
            super(new Produkt());
        }

        @IpsDefaultValue("attrChangingOverTime")
        public String getDefaultValueAttrChangingOverTime() {
            return "blub";
        }

        /**
         * @param context validation context
         */
        @IpsAllowedValues("attrChangingOverTime")
        public ValueSet<String> getSetOfAllowedValuesForAttrChangingOverTime(IValidationContext context) {
            return new OrderedValueSet<String>(false, null, "foo", "bar");
        }

    }

    @IpsPolicyCmptType(name = "VertragABC")
    @IpsAttributes("attr1")
    private class Vertrag implements IModelObject {

        private String attr1;

        @IpsAttribute(name = "attr1", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.Range)
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

        public SubPolicy() {
            setAttr1(42);
        }

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
