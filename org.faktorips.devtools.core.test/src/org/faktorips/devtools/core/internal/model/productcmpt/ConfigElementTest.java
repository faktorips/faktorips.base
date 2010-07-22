/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConfigElementTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IConfigElement configElement;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);
        configElement = generation.newConfigElement();
        productCmpt.getIpsSrcFile().save(true, null);
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
    }

    public void testGetAllowedValueSetTypes() throws CoreException {

        // case 1: attribute not found
        configElement.setPolicyCmptTypeAttribute("a1");
        List<ValueSetType> types = configElement.getAllowedValueSetTypes(ipsProject);
        assertEquals(1, types.size());
        assertEquals(configElement.getValueSet().getValueSetType(), types.get(0));

        // case 2: attribute found, value set type is unrestricted, datatype is Integer
        // => all types should be available
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        a1.setName("a1");
        a1.setDatatype("Integer");
        a1.setProductRelevant(true);
        a1.setValueSetType(ValueSetType.UNRESTRICTED);
        types = configElement.getAllowedValueSetTypes(ipsProject);
        assertEquals(3, types.size());
        assertTrue(types.contains(ValueSetType.ENUM));
        assertTrue(types.contains(ValueSetType.RANGE));
        assertTrue(types.contains(ValueSetType.UNRESTRICTED));

        // case 3: as before, but with datatype String
        // => only unrestricted and enum should be available
        a1.setDatatype("String");
        types = configElement.getAllowedValueSetTypes(ipsProject);
        assertEquals(2, types.size());
        assertTrue(types.contains(ValueSetType.ENUM));
        assertTrue(types.contains(ValueSetType.UNRESTRICTED));

        // case 4: as before, but with datatype String
        // => only unrestricted and enum should be available
        a1.setDatatype("Integer");
        a1.setValueSetType(ValueSetType.RANGE);
        types = configElement.getAllowedValueSetTypes(ipsProject);
        assertEquals(1, types.size());
        assertTrue(types.contains(ValueSetType.RANGE));
    }

    public void testFindPcTypeAttribute() throws CoreException {
        IPolicyCmptType policyCmptSupertype = newPolicyCmptType(ipsProject, "SuperPolicy");
        policyCmptType.setSupertype(policyCmptSupertype.getQualifiedName());

        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        a1.setName("a1");
        IPolicyCmptTypeAttribute a2 = policyCmptSupertype.newPolicyCmptTypeAttribute();
        a2.setName("a2");

        generation = productCmpt.getProductCmptGeneration(0);
        IConfigElement ce = generation.newConfigElement();
        ce.setPolicyCmptTypeAttribute("a1");
        assertEquals(a1, ce.findPcTypeAttribute(ipsProject));
        ce.setPolicyCmptTypeAttribute("a2");
        assertEquals(a2, ce.findPcTypeAttribute(ipsProject));
        ce.setPolicyCmptTypeAttribute("unkown");
        assertNull(ce.findPcTypeAttribute(ipsProject));
    }

    public void testValidate_UnknownAttribute() throws CoreException {
        configElement.setPolicyCmptTypeAttribute("a");
        MessageList ml = configElement.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));

        policyCmptType.newPolicyCmptTypeAttribute().setName("a");
        policyCmptType.getIpsSrcFile().save(true, null);

        ml = configElement.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));
    }

    public void testValidate_UnknownDatatypeValue() throws CoreException {
        IConfigElement ce = generation.newConfigElement();
        ce.setValue("1");
        ce.setPolicyCmptTypeAttribute("valueTest");
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");
        attr.setAttributeType(AttributeType.CHANGEABLE);

        MessageList ml = ce.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_VALUE));

        attr.setDatatype("Decimal");

        policyCmptType.getIpsSrcFile().save(true, null);

        ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_VALUE));
    }

    public void testValidate_ValueNotParsable() throws CoreException {
        IConfigElement ce = generation.newConfigElement();
        ce.setValue("1");
        ce.setPolicyCmptTypeAttribute("valueTest");
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");
        attr.setAttributeType(AttributeType.CHANGEABLE);
        attr.setDatatype("Money");

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        MessageList ml = ce.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_PARSABLE));

        attr.setDatatype("Decimal");
        policyCmptType.getIpsSrcFile().save(true, null);

        ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_PARSABLE));
    }

    public void testValidateParsableEnumTypeDatatype() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setContainingValues(true);
        enumType.newEnumLiteralNameAttribute();

        IEnumAttribute id = enumType.newEnumAttribute();
        id.setName("id");
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setUnique(true);
        id.setIdentifier(true);

        IEnumAttribute name = enumType.newEnumAttribute();
        name.setName("name");
        name.setDatatype(Datatype.STRING.getQualifiedName());
        name.setUnique(true);
        name.setUsedAsNameInFaktorIpsUi(true);

        IEnumValue enumValue = enumType.newEnumValue();
        List<IEnumAttributeValue> values = enumValue.getEnumAttributeValues();
        values.get(0).setValue("AN");
        values.get(1).setValue("a");
        values.get(2).setValue("an");

        IConfigElement ce = generation.newConfigElement();
        ce.setValue("a");
        ce.setPolicyCmptTypeAttribute("valueTest");
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");
        attr.setAttributeType(AttributeType.CHANGEABLE);
        attr.setDatatype(enumType.getQualifiedName());

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        MessageList ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_PARSABLE));

        ce.setValue("b");
        ml = ce.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_PARSABLE));
    }

    public void testValidate_InvalidValueset() throws CoreException {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");
        attr.setAttributeType(AttributeType.CHANGEABLE);
        attr.setDatatype("Decimal");
        attr.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)attr.getValueSet();
        valueSet.setLowerBound("a");
        valueSet.setUpperBound("b");

        IConfigElement ce = generation.newConfigElement();
        ce.setValue("1");
        ce.setPolicyCmptTypeAttribute("valueTest");
        ce.setValueSetCopy(valueSet);

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        MessageList ml = ce.validate(ce.getIpsProject());

        // no test for specific message codes because the codes are under controll
        // of the value set.
        assertTrue(ml.getNoOfMessages() > 0);

        valueSet = (IRangeValueSet)ce.getValueSet();
        valueSet.setLowerBound("0");
        valueSet.setUpperBound("100");

        valueSet = (IRangeValueSet)attr.getValueSet();
        valueSet.setLowerBound("0");
        valueSet.setUpperBound("100");

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        ml = ce.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());
    }

    public void testValidate_InvalidDatatype() throws Exception {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("test");
        InvalidDatatype datatype = new InvalidDatatype();
        attr.setDatatype(datatype.getQualifiedName());

        Datatype[] vds = ipsProject.findDatatypes(true, false);
        ArrayList<Datatype> vdlist = new ArrayList<Datatype>();
        vdlist.addAll(Arrays.asList(vds));
        vdlist.add(datatype);

        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed(vdlist.toArray(new ValueDatatype[vdlist.size()]));
        ipsProject.setProperties(properties);

        InvalidDatatypeHelper idh = new InvalidDatatypeHelper();
        idh.setDatatype(datatype);
        ((IpsModel)ipsProject.getIpsModel()).addDatatypeHelper(idh);

        IConfigElement ce = generation.newConfigElement();
        ce.setPolicyCmptTypeAttribute("test");
        MessageList ml = ce.validate(ce.getIpsProject());
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_INVALID_DATATYPE));
    }

    public void testValidate_ValueNotInValueset() throws CoreException {
        IConfigElement ce = generation.newConfigElement();
        ce.setValue("1");
        ce.setPolicyCmptTypeAttribute("valueTest");
        ce.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)ce.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");

        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");

        attr.setAttributeType(AttributeType.CONSTANT);
        attr.setValueSetType(ValueSetType.RANGE);
        attr.setDatatype("Decimal");
        IRangeValueSet valueSetAttr = (IRangeValueSet)attr.getValueSet();
        valueSetAttr.setLowerBound(null);
        valueSetAttr.setUpperBound(null);

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        MessageList ml = ce.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET));

        ce.setValue("15");

        ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET));
    }

    public void testValidate_ValueSetTypeMismatch() throws CoreException {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("a1");
        attr.setValueSetType(ValueSetType.RANGE);
        attr.setDatatype("Integer");

        IConfigElement ce = generation.newConfigElement();
        ce.setValue("12");
        ce.setPolicyCmptTypeAttribute("a1");
        ce.changeValueSetType(ValueSetType.RANGE);

        MessageList ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH));

        ce.changeValueSetType(ValueSetType.ENUM);
        ml = ce.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH));

        attr.changeValueSetType(ValueSetType.UNRESTRICTED);
        ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH));
        ce.changeValueSetType(ValueSetType.RANGE);
        ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH));
        ce.changeValueSetType(ValueSetType.UNRESTRICTED);
        ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH));
    }

    public void testValidate_ValueSetNotASubset() throws CoreException {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");
        attr.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)attr.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("15");
        attr.setDatatype("Decimal");

        IConfigElement ce = generation.newConfigElement();
        ce.setValue("12");
        ce.setPolicyCmptTypeAttribute("valueTest");
        ce.setValueSetCopy(valueSet);
        IRangeValueSet valueSet2 = (IRangeValueSet)ce.getValueSet();
        valueSet2.setUpperBound("20");

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        MessageList ml = ce.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        valueSet.setUpperBound("20");
        policyCmptType.getIpsSrcFile().save(true, null);

        ml = ce.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());

        // check lower unbound values
        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        valueSet.setLowerBound("10");
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = ce.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound("10");
        valueSet2.setUpperBound(null);
        ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        // check upper unbound values
        valueSet.setLowerBound(null);
        valueSet.setUpperBound("10");
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = ce.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound("10");
        ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    public void testSetValue() {
        configElement.setValue("newValue");
        assertEquals("newValue", configElement.getValue());
        assertTrue(configElement.getIpsSrcFile().isDirty());
    }

    public void testInitFromXml() {
        Document doc = getTestDocument();
        configElement.initFromXml(doc.getDocumentElement());
        assertEquals("42", configElement.getId());
        assertEquals("sumInsured", configElement.getPolicyCmptTypeAttribute());
        assertEquals("10", configElement.getValue());
        IRangeValueSet range = (IRangeValueSet)configElement.getValueSet();
        assertEquals("22", range.getLowerBound());
        assertEquals("33", range.getUpperBound());
        assertEquals("4", range.getStep());
    }

    public void testToXmlDocument() {
        IConfigElement cfgElement = generation.newConfigElement();
        cfgElement.setValue("value");
        cfgElement.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)cfgElement.getValueSet();
        valueSet.setLowerBound("22");
        valueSet.setUpperBound("33");
        valueSet.setStep("4");
        Element xmlElement = cfgElement.toXml(getTestDocument());

        IConfigElement newCfgElement = generation.newConfigElement();
        newCfgElement.initFromXml(xmlElement);
        assertEquals("value", newCfgElement.getValue());
        assertEquals("22", ((IRangeValueSet)newCfgElement.getValueSet()).getLowerBound());
        assertEquals("33", ((IRangeValueSet)newCfgElement.getValueSet()).getUpperBound());
        assertEquals("4", ((IRangeValueSet)newCfgElement.getValueSet()).getStep());

        cfgElement.setValueSetType(ValueSetType.ENUM);
        EnumValueSet enumValueSet = (EnumValueSet)cfgElement.getValueSet();
        enumValueSet.addValue("one");
        enumValueSet.addValue("two");
        enumValueSet.addValue("three");
        enumValueSet.addValue("four");

        xmlElement = cfgElement.toXml(getTestDocument());
        assertEquals(4, ((IEnumValueSet)cfgElement.getValueSet()).getValues().length);
        assertEquals("one", ((IEnumValueSet)cfgElement.getValueSet()).getValues()[0]);
        assertEquals("two", ((IEnumValueSet)cfgElement.getValueSet()).getValues()[1]);
        assertEquals("three", ((IEnumValueSet)cfgElement.getValueSet()).getValues()[2]);
        assertEquals("four", ((IEnumValueSet)cfgElement.getValueSet()).getValues()[3]);

        cfgElement.setValue(null);
        xmlElement = cfgElement.toXml(getTestDocument());
        newCfgElement.initFromXml(xmlElement);

        assertNull(newCfgElement.getValue());
    }

    /**
     * Tests for the correct type of exception to be thrown - no part of any type could ever be
     * created.
     */
    public void testNewPart() {
        try {
            configElement.newPart(PolicyCmptTypeAttribute.class);
            fail();
        } catch (IllegalArgumentException e) {
            // nothing to do :-)
        }
    }

    private class InvalidDatatype implements ValueDatatype {

        @Override
        public ValueDatatype getWrapperType() {
            return null;
        }

        @Override
        public boolean isEnum() {
            return false;
        }

        @Override
        public boolean isParsable(String value) {
            return true;
        }

        @Override
        public String getName() {
            return getQualifiedName();
        }

        @Override
        public String getQualifiedName() {
            return "InvalidDatatype";
        }

        @Override
        public String getDefaultValue() {
            return null;
        }

        @Override
        public boolean isVoid() {
            return false;
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public boolean isAbstract() {
            return false;
        }

        @Override
        public boolean isValueDatatype() {
            return true;
        }

        @Override
        public String getJavaClassName() {
            return null;
        }

        @Override
        public MessageList checkReadyToUse() {
            MessageList ml = new MessageList();

            ml.add(new Message("", "", Message.ERROR));

            return ml;
        }

        @Override
        public int compareTo(Datatype o) {
            return -1;
        }

        @Override
        public boolean hasNullObject() {
            return false;
        }

        @Override
        public boolean isNull(String value) {
            return false;
        }

        @Override
        public boolean supportsCompare() {
            return false;
        }

        @Override
        public int compare(String valueA, String valueB) throws UnsupportedOperationException {
            return 0;
        }

        @Override
        public boolean areValuesEqual(String valueA, String valueB) {
            return false;
        }

        @Override
        public boolean isMutable() {
            return true;
        }

        @Override
        public boolean isImmutable() {
            return false;
        }

    }

    private class InvalidDatatypeHelper extends AbstractDatatypeHelper {

        @Override
        protected JavaCodeFragment valueOfExpression(String expression) {
            return null;
        }

        @Override
        public JavaCodeFragment nullExpression() {
            return null;
        }

        @Override
        public JavaCodeFragment newInstance(String value) {
            return null;
        }

    }
}
