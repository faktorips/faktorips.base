/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.faktorips.abstracttest.matcher.Matchers.hasMessageCode;
import static org.faktorips.abstracttest.matcher.Matchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ValueSetNullIncompatibleValidator;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.TemplateValueStatus;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
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
    @Before
    public void setUp() throws Exception {
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

    @Test
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

    @Test
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

    @Test
    public void testValidate_UnknownAttribute() throws CoreException {
        configElement.setPolicyCmptTypeAttribute("a");
        MessageList ml = configElement.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));

        policyCmptType.newPolicyCmptTypeAttribute().setName("a");
        policyCmptType.getIpsSrcFile().save(true, null);

        ml = configElement.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));
    }

    @Test
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

    @Test
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

    @Test
    public void testValidateParsableEnumTypeDatatype() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setExtensible(false);
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
        values.get(0).setValue(ValueFactory.createStringValue("AN"));
        values.get(1).setValue(ValueFactory.createStringValue("a"));
        values.get(2).setValue(ValueFactory.createStringValue("an"));

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

    @Test
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
        assertTrue(ml.size() > 0);

        valueSet = (IRangeValueSet)ce.getValueSet();
        valueSet.setLowerBound("0");
        valueSet.setUpperBound("100");

        valueSet = (IRangeValueSet)attr.getValueSet();
        valueSet.setLowerBound("0");
        valueSet.setUpperBound("100");

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        ml = ce.validate(ipsProject);
        assertEquals(0, ml.size());
    }

    @Test
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

    @Test
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

    @Test
    public void testValidate_ValueNotInValuesetForInheritedValues() throws CoreException {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");
        attr.setValueSetType(ValueSetType.RANGE);
        attr.setDatatype("Decimal");

        IConfigElement templateConfigElement = createTemplateConfigElement(attr);

        templateConfigElement.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)templateConfigElement.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");
        templateConfigElement.setValue("1");

        configElement.setPolicyCmptTypeAttribute(attr.getName());
        configElement.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(configElement.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(configElement.getValue(), is("1"));

        MessageList ml = configElement.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET));

        templateConfigElement.setValue("15");
        ml = configElement.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_ValueNotInValuesetForUndefinedValues() throws CoreException {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");
        attr.setValueSetType(ValueSetType.RANGE);
        attr.setDatatype("Decimal");

        IConfigElement template = createTemplateConfigElement(attr);

        // First set some invalid value set in template's fields...
        template.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)template.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");
        template.setValue("1");
        assertThat(template.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
        assertThat(template.getValueSet(), is((IValueSet)valueSet));

        // ...then make sure template forgets about its invalid value set if it is undefined
        template.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        assertThat(template.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
        assertThat(template.getValue(), is(""));
        assertThat(template.getValueSet(), is(instanceOf(UnrestrictedValueSet.class)));

        assertThat(template.validate(ipsProject), lacksMessageCode(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_ValueSetTypeMismatch() throws CoreException {
        IPolicyCmptTypeAttribute attr = setUpRangeIntegerAttr();

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

    @Test
    public void testValidate_ValueSetTypeMismatchForInheritedValues_changeParentValueSet() throws CoreException {
        IPolicyCmptTypeAttribute attr = setUpRangeIntegerAttr();
        IConfigElement templateConfigElement = setUpRangeIntegerTemplate(attr);
        setUpInheritRangeValueSet(attr);

        assertThat(configElement.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(configElement.validate(ipsProject), lacksMessageCode(IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH));

        templateConfigElement.changeValueSetType(ValueSetType.ENUM);
        // expect messages, as enum value set is empty and thus does not contain the value 12
        assertThat(configElement.validate(ipsProject), hasMessageCode(IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH));

        attr.changeValueSetType(ValueSetType.UNRESTRICTED);
        assertThat(configElement.validate(ipsProject), lacksMessageCode(IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH));

    }

    @Test
    public void testValidate_ValueSetTypeMismatchForInheritedValues() throws CoreException {
        IPolicyCmptTypeAttribute attr = setUpRangeIntegerAttr();
        IConfigElement templateConfigElement = setUpRangeIntegerTemplate(attr);
        setUpInheritRangeValueSet(attr);

        assertThat(configElement.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(configElement.validate(ipsProject), lacksMessageCode(IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH));

        attr.changeValueSetType(ValueSetType.UNRESTRICTED);
        // more general value set not validated, because defined as inherited
        assertThat(configElement.validate(ipsProject), lacksMessageCode(IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH));

        templateConfigElement.changeValueSetType(ValueSetType.UNRESTRICTED);
        assertThat(configElement.validate(ipsProject), lacksMessageCode(IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH));
    }

    private void setUpInheritRangeValueSet(IPolicyCmptTypeAttribute attr) {
        configElement.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        configElement.setPolicyCmptTypeAttribute(attr.getName());
    }

    private IConfigElement setUpRangeIntegerTemplate(IPolicyCmptTypeAttribute attr) throws CoreException {
        IConfigElement templateConfigElement = createTemplateConfigElement(attr);
        templateConfigElement.setValue("12");
        templateConfigElement.changeValueSetType(ValueSetType.RANGE);
        return templateConfigElement;
    }

    private IPolicyCmptTypeAttribute setUpRangeIntegerAttr() {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("a1");
        attr.setValueSetType(ValueSetType.RANGE);
        attr.setDatatype("Integer");
        return attr;
    }

    @Test
    public void testValidate_ValueSetTypeMismatchForUndefinedValues() throws CoreException {
        IPolicyCmptTypeAttribute attr = setUpRangeIntegerAttr();

        IConfigElement template = createTemplateConfigElement(attr);
        template.setValue("12");
        template.changeValueSetType(ValueSetType.ENUM);
        template.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        assertThat(template.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
        assertThat(template.getValueSet(), is(instanceOf(UnrestrictedValueSet.class)));
        assertThat(template.validate(ipsProject), lacksMessageCode(IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH));
    }

    @Test
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
        assertEquals(0, ml.size());

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

    @Test
    public void testSetValue() {
        configElement.setValue("newValue");
        assertEquals("newValue", configElement.getValue());
        assertTrue(configElement.getIpsSrcFile().isDirty());
    }

    @Test
    public void testGetValue_DefinedValue() {
        configElement.setValue("newValue");
        assertThat(configElement.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
        assertThat(configElement.getValue(), is("newValue"));
    }

    @Test
    public void testGetValue_InheritedValue() throws CoreException {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute("attribute");
        IConfigElement templateConfigElement = createTemplateConfigElement(attribute);

        configElement.setPolicyCmptTypeAttribute(attribute.getName());
        configElement.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        configElement.setValue("value");

        templateConfigElement.setValue("templateValue");

        assertThat(configElement.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(configElement.getValue(), is("templateValue"));

    }

    @Test
    public void testGetValue_UndefinedValue() throws CoreException {
        IConfigElement templateConfigElement = createTemplateConfigElement();
        templateConfigElement.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        templateConfigElement.setValue("templateValue");

        assertThat(templateConfigElement.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
        assertThat(templateConfigElement.getValue(), is(""));

    }

    @Test
    public void testGetValueSet_InheritedValue() throws CoreException {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");
        attr.setValueSetType(ValueSetType.RANGE);

        IConfigElement templateConfigElement = createTemplateConfigElement(attr);
        templateConfigElement.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)templateConfigElement.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");

        configElement.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        configElement.setPolicyCmptTypeAttribute(attr.getName());

        assertThat(configElement.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(configElement.getValueSet(), is(instanceOf(IRangeValueSet.class)));

        assertThat(((IRangeValueSet)configElement.getValueSet()).getLowerBound(), is("10"));
        assertThat(((IRangeValueSet)configElement.getValueSet()).getUpperBound(), is("20"));
    }

    @Test
    public void testGetValueSet_DefinedValue() {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");
        attr.setValueSetType(ValueSetType.RANGE);

        configElement.setPolicyCmptTypeAttribute(attr.getName());
        configElement.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)configElement.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");

        assertThat(configElement.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
        assertThat(configElement.getValueSet(), is(instanceOf(IRangeValueSet.class)));

        assertThat(((IRangeValueSet)configElement.getValueSet()).getLowerBound(), is("10"));
        assertThat(((IRangeValueSet)configElement.getValueSet()).getUpperBound(), is("20"));

    }

    @Test
    public void testGetValueSet_UndefinedValue() throws CoreException {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");
        attr.setValueSetType(ValueSetType.RANGE);

        IConfigElement templateConfigElement = createTemplateConfigElement(attr);
        templateConfigElement.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)templateConfigElement.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");

        templateConfigElement.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        assertThat(templateConfigElement.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
        assertThat(templateConfigElement.getValueSet(), is(not((IValueSet)valueSet)));
        assertThat(templateConfigElement.getValueSet(), is(instanceOf(UnrestrictedValueSet.class)));

    }

    @Test
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

    @Test
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
        IEnumValueSet enumValueSet = (IEnumValueSet)cfgElement.getValueSet();
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

    @Test
    public void testGetCaption() throws CoreException {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("attribute");
        configElement.setPolicyCmptTypeAttribute("attribute");

        ILabel label = attribute.getLabel(Locale.US);
        label.setValue("TheCaption");
        assertEquals("TheCaption", configElement.getCaption(Locale.US));
    }

    @Test
    public void testGetCaptionNotExistent() throws CoreException {
        assertNull(configElement.getCaption(Locale.TAIWAN));
    }

    @Test
    public void testGetCaptionNullPointer() throws CoreException {
        try {
            configElement.getCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetLastResortCaption() {
        configElement.setPolicyCmptTypeAttribute("attribute");
        assertEquals(StringUtils.capitalize(configElement.getPolicyCmptTypeAttribute()),
                configElement.getLastResortCaption());
    }

    @Test
    public void testValidateThis_nullIncompatible() throws CoreException {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(ValueDatatype.INTEGER.getQualifiedName());
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.getValueSet().setContainsNull(false);

        configElement.setPolicyCmptTypeAttribute("attr");
        configElement.setValueSetType(ValueSetType.UNRESTRICTED);
        configElement.getValueSet().setAbstract(false);
        configElement.getValueSet().setContainsNull(true);

        MessageList messages = configElement.validate(ipsProject);
        assertNotNull(messages.getMessageByCode(ValueSetNullIncompatibleValidator.MSGCODE_INCOMPATIBLE_VALUESET));
    }

    private IConfigElement createTemplateConfigElement() throws CoreException {
        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGen = template.getProductCmptGeneration(0);
        IConfigElement templateConfigElement = templateGen.newConfigElement();
        productCmpt.setTemplate(template.getQualifiedName());
        return templateConfigElement;
    }

    private IConfigElement createTemplateConfigElement(IPolicyCmptTypeAttribute policyCmotTypeAttribute)
            throws CoreException {
        IConfigElement templateConfigElement = createTemplateConfigElement();
        templateConfigElement.setPolicyCmptTypeAttribute(policyCmotTypeAttribute.getName());
        return templateConfigElement;
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

        @Override
        public Object getValue(String value) {
            throw new RuntimeException("not supported");
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
