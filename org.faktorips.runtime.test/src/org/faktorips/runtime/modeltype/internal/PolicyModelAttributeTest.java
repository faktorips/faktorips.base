package org.faktorips.runtime.modeltype.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
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
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.ValueSetType;
import org.faktorips.valueset.DefaultRange;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PolicyModelAttributeTest {

    @Mock
    private IRuntimeRepository repository;

    private final Calendar effectiveDate = new GregorianCalendar(1999, 1, 1);

    // @Test
    // public void testGetValueSet() {
    // ConfVertrag vertrag = new ConfVertrag();
    // IModelType typeModel = Models.getModelType(ConfVertrag.class);
    //
    // PolicyModelAttribute attribute = (PolicyModelAttribute)typeModel.getAttribute("attr1");
    // ValueSet<?> valueSet = attribute.getValueSet(vertrag, null);
    //
    // assertTrue(valueSet instanceof UnrestrictedValueSet);
    // }
    //
    // @Test
    // public void testGetValueSet_notConfigured() {
    // Vertrag vertrag = new Vertrag();
    // IModelType typeModel = Models.getModelType(Vertrag.class);
    //
    // PolicyModelAttribute attribute = (PolicyModelAttribute)typeModel.getAttribute("attr1");
    // ValueSet<?> valueSet = attribute.getValueSet(vertrag, null);
    //
    // assertTrue(valueSet instanceof DefaultRange);
    // }

    @Test
    public void testGetDefaultValue() {
        IModelType typeModel = Models.getModelType(ConfVertrag.class);

        PolicyModelAttribute attribute = (PolicyModelAttribute)typeModel.getAttribute("attr1");
        Object defaultValue = attribute.getDefaultValue(new Produkt(), null);

        assertEquals("foobar", defaultValue);
    }

    @Test
    public void testGetDefaultValue_changingOverTimeWithCalendar() {
        Produkt source = new Produkt();
        ProduktGen gen = new ProduktGen();
        when(repository.getProductComponentGeneration("id", effectiveDate)).thenReturn(gen);
        IModelType typeModel = Models.getModelType(ConfVertrag.class);

        PolicyModelAttribute attribute = (PolicyModelAttribute)typeModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(source, effectiveDate);

        assertEquals("blub", defaultValue);
    }

    @Test
    public void testGetDefaultValue_changingOverTime() {
        Produkt source = new Produkt();
        ProduktGen gen = new ProduktGen();
        when(repository.getLatestProductComponentGeneration(source)).thenReturn(gen);
        IModelType typeModel = Models.getModelType(ConfVertrag.class);

        PolicyModelAttribute attribute = (PolicyModelAttribute)typeModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(source, null);

        assertEquals("blub", defaultValue);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDefaultValue_notConfigured() {
        IModelType typeModel = Models.getModelType(Vertrag.class);
        PolicyModelAttribute attribute = (PolicyModelAttribute)typeModel.getAttribute("attr1");

        attribute.getDefaultValue(new Produkt(), null);
    }

    @Test
    public void testGetDefaultValue_modelObject() {
        ProduktGen gen = new ProduktGen();
        ConfVertrag vertrag = new ConfVertrag();
        vertrag.effectiveFrom = Calendar.getInstance();
        when(repository.getProductComponentGeneration("id", vertrag.effectiveFrom)).thenReturn(gen);
        IModelType typeModel = Models.getModelType(ConfVertrag.class);

        PolicyModelAttribute attribute = (PolicyModelAttribute)typeModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(vertrag);

        assertEquals("blub", defaultValue);
    }

    @Test
    public void testGetDefaultValue_modelObject_noEffectiveFrom() {
        ProduktGen gen = new ProduktGen();
        ConfVertrag vertrag = new ConfVertrag();
        when(repository.getLatestProductComponentGeneration(vertrag.getProductComponent())).thenReturn(gen);
        IModelType typeModel = Models.getModelType(ConfVertrag.class);

        PolicyModelAttribute attribute = (PolicyModelAttribute)typeModel.getAttribute("attrChangingOverTime");
        Object defaultValue = attribute.getDefaultValue(vertrag);

        assertEquals("blub", defaultValue);
    }

    @Test
    public void testIsChangingOverTime() {
        IModelType typeModel = Models.getModelType(ConfVertrag.class);
        PolicyModelAttribute attribute = (PolicyModelAttribute)typeModel.getAttribute("attr1");

        assertFalse(attribute.isChangingOverTime());
    }

    @Test
    public void testIsChangingOverTime_true() {
        IModelType typeModel = Models.getModelType(ConfVertrag.class);
        PolicyModelAttribute attribute = (PolicyModelAttribute)typeModel.getAttribute("attrChangingOverTime");

        assertTrue(attribute.isChangingOverTime());
    }

    @Test(expected = IllegalStateException.class)
    public void testIsChangingOverTime_noAnnotation() {
        IModelType typeModel = Models.getModelType(DummyVertrag.class);
        PolicyModelAttribute attribute = (PolicyModelAttribute)typeModel.getAttribute("attrChangingOverTime");

        assertTrue(attribute.isChangingOverTime());
    }

    @IpsPolicyCmptType(name = "Vertragxyz")
    @IpsConfiguredBy(Produkt.class)
    @IpsAttributes({ "attr1", "attrChangingOverTime" })
    private class ConfVertrag implements IConfigurableModelObject {

        private final Produkt produkt;

        private String attr1;
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

        @IpsAttribute(name = "attrChangingOverTime", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
        @IpsConfiguredAttribute(changingOverTime = true)
        public String getAttrChangingOverTime() {
            return attrChangingOverTime;
        }

        @IpsAttributeSetter("attrChangingOverTime")
        public void setAttrChangingOverTime(String value) {
            attrChangingOverTime = value;
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
            return new UnrestrictedValueSet<String>();
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

}
