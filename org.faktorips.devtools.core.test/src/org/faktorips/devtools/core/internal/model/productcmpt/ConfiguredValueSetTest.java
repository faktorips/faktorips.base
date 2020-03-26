/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ValueSetNullIncompatibleValidator;
import org.faktorips.devtools.core.internal.model.valueset.DelegatingValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConfiguredValueSetTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IPolicyCmptTypeAttribute attribute;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IConfiguredValueSet configuredValueSet;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("attribute");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);
        configuredValueSet = generation.newPropertyValue(attribute, IConfiguredValueSet.class);
        productCmpt.getIpsSrcFile().save(true, null);
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
    }

    @Test
    public void testGetAllowedValueSetTypes() throws CoreException {
        // case 1: attribute not found
        attribute.setName("unknown");
        List<ValueSetType> types = configuredValueSet.getAllowedValueSetTypes(ipsProject);
        assertEquals(1, types.size());
        assertEquals(configuredValueSet.getValueSet().getValueSetType(), types.get(0));

        // case 2: attribute found, value set type is unrestricted, data type is Integer
        // => all types should be available
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        a1.setName("attribute");
        a1.setDatatype("Integer");
        a1.setValueSetConfiguredByProduct(true);
        a1.setValueSetType(ValueSetType.UNRESTRICTED);
        types = configuredValueSet.getAllowedValueSetTypes(ipsProject);
        assertEquals(3, types.size());
        assertTrue(types.contains(ValueSetType.ENUM));
        assertTrue(types.contains(ValueSetType.RANGE));
        assertTrue(types.contains(ValueSetType.UNRESTRICTED));

        // case 3: as before, but with datatype String
        // => only unrestricted and enum should be available
        a1.setDatatype("String");
        types = configuredValueSet.getAllowedValueSetTypes(ipsProject);
        assertEquals(3, types.size());
        assertTrue(types.contains(ValueSetType.ENUM));
        assertTrue(types.contains(ValueSetType.UNRESTRICTED));
        assertTrue(types.contains(ValueSetType.STRINGLENGTH));

        // case 4: as before, but with datatype String
        // => only unrestricted and enum should be available
        a1.setDatatype("Integer");
        a1.setValueSetType(ValueSetType.RANGE);
        types = configuredValueSet.getAllowedValueSetTypes(ipsProject);
        assertEquals(1, types.size());
        assertTrue(types.contains(ValueSetType.RANGE));

        // case 5: attribute is not product relevant because the value set is derived
        // => there should not be a configuredValueSet, but if the model was changed and differences
        // ignored, it might still exist
        a1.setValueSetType(ValueSetType.DERIVED);
        types = configuredValueSet.getAllowedValueSetTypes(ipsProject);
        assertEquals(1, types.size());
        assertTrue(types.contains(ValueSetType.UNRESTRICTED));
    }

    @Test
    public void testValidate_UnknownAttribute() throws CoreException {
        attribute.setName("unknown");
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));

        attribute.setName("attribute");

        ml = configuredValueSet.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));
    }

    @Test
    public void testValidate_UnknownDatatypeValue() throws CoreException {
        attribute.setAttributeType(AttributeType.CHANGEABLE);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNOWN_DATATYPE));

        attribute.setDatatype("Decimal");

        policyCmptType.getIpsSrcFile().save(true, null);

        ml = configuredValueSet.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNOWN_DATATYPE));
    }

    @Test
    public void testValidate_InvalidValueset() throws CoreException {
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        attribute.setDatatype("Decimal");
        attribute.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)attribute.getValueSet();
        valueSet.setLowerBound("a");
        valueSet.setUpperBound("b");

        configuredValueSet.setValueSetCopy(valueSet);

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        MessageList ml = configuredValueSet.validate(configuredValueSet.getIpsProject());

        // no test for specific message codes because the codes are under control
        // of the value set.
        assertTrue(ml.size() > 0);

        valueSet = (IRangeValueSet)configuredValueSet.getValueSet();
        valueSet.setLowerBound("0");
        valueSet.setUpperBound("100");

        valueSet = (IRangeValueSet)attribute.getValueSet();
        valueSet.setLowerBound("0");
        valueSet.setUpperBound("100");

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        ml = configuredValueSet.validate(ipsProject);
        assertEquals(0, ml.size());
    }

    @Test
    public void testValidate_InvalidDatatype() throws Exception {
        InvalidDatatype datatype = new InvalidDatatype();
        attribute.setDatatype(datatype.getQualifiedName());

        Datatype[] vds = ipsProject.findDatatypes(true, false);
        ArrayList<Datatype> vdlist = new ArrayList<Datatype>();
        vdlist.addAll(Arrays.asList(vds));
        vdlist.add(datatype);

        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed(vdlist.toArray(new ValueDatatype[vdlist.size()]));
        ipsProject.setProperties(properties);

        MessageList ml = configuredValueSet.validate(configuredValueSet.getIpsProject());
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_INVALID_DATATYPE));
    }

    @Test
    public void testValidate_ValueSetTypeMismatch() throws CoreException {
        setUpRangeIntegerAttr();

        configuredValueSet.changeValueSetType(ValueSetType.RANGE);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        ml = configuredValueSet.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        attribute.changeValueSetType(ValueSetType.UNRESTRICTED);
        ml = configuredValueSet.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        ml = configuredValueSet.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));
        configuredValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);
        ml = configuredValueSet.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));
    }

    @Test
    public void testValidate_ValueSetTypeMismatchForInheritedValues_changeParentValueSet() throws CoreException {
        setUpRangeIntegerAttr();
        IConfiguredValueSet templateValueSet = setUpRangeIntegerTemplate(attribute);
        setUpInheritRangeValueSet();

        assertThat(configuredValueSet.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(configuredValueSet.validate(ipsProject),
                lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        templateValueSet.changeValueSetType(ValueSetType.ENUM);
        // expect messages, as enum value set is empty and thus does not contain the value 12
        assertThat(configuredValueSet.validate(ipsProject),
                hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        attribute.changeValueSetType(ValueSetType.UNRESTRICTED);
        assertThat(configuredValueSet.validate(ipsProject),
                lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

    }

    @Test
    public void testValidate_ValueSetTypeMismatchForInheritedValues() throws CoreException {
        setUpRangeIntegerAttr();

        IConfiguredValueSet templateValueSet = setUpRangeIntegerTemplate(attribute);
        setUpInheritRangeValueSet();

        assertThat(configuredValueSet.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(configuredValueSet.validate(ipsProject),
                lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        attribute.changeValueSetType(ValueSetType.UNRESTRICTED);
        // more general value set not validated, because defined as inherited
        assertThat(configuredValueSet.validate(ipsProject),
                lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        templateValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);
        assertThat(configuredValueSet.validate(ipsProject),
                lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));
    }

    private void setUpInheritRangeValueSet() {
        configuredValueSet.setTemplateValueStatus(TemplateValueStatus.INHERITED);
    }

    private IConfiguredValueSet setUpRangeIntegerTemplate(IPolicyCmptTypeAttribute attr) throws CoreException {
        IConfiguredValueSet templateValueSet = createTemplateValueSet(attr);
        templateValueSet.changeValueSetType(ValueSetType.RANGE);
        return templateValueSet;
    }

    @Test
    public void testValidate_ValueSetTypeMismatchForUndefinedValues() throws CoreException {
        setUpRangeIntegerAttr();

        IConfiguredValueSet templateValueSet = createTemplateValueSet(attribute);
        templateValueSet.changeValueSetType(ValueSetType.ENUM);
        templateValueSet.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        assertThat(templateValueSet.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
        assertThat(templateValueSet.getValueSet(), is(instanceOf(UnrestrictedValueSet.class)));
        assertThat(templateValueSet.validate(ipsProject),
                lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));
    }

    private void setUpRangeIntegerAttr() {
        attribute.setValueSetType(ValueSetType.RANGE);
        attribute.setDatatype("Integer");
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

        configuredValueSet = productCmpt.newPropertyValue(attr, IConfiguredValueSet.class);
        configuredValueSet.setValueSetCopy(valueSet);
        IRangeValueSet valueSet2 = (IRangeValueSet)configuredValueSet.getValueSet();
        valueSet2.setUpperBound("20");

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        valueSet.setUpperBound("20");
        policyCmptType.getIpsSrcFile().save(true, null);

        ml = configuredValueSet.validate(ipsProject);
        assertEquals(0, ml.size());

        // check lower unbound values
        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = configuredValueSet.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        valueSet.setLowerBound("10");
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = configuredValueSet.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound("10");
        valueSet2.setUpperBound(null);
        ml = configuredValueSet.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        // check upper unbound values
        valueSet.setLowerBound(null);
        valueSet.setUpperBound("10");
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = configuredValueSet.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound("10");
        ml = configuredValueSet.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    @Test
    public void testGetValueSet_InheritedValue() throws CoreException {
        attribute.setValueSetType(ValueSetType.RANGE);

        IConfiguredValueSet templateConfiguredValueSet = createTemplateValueSet(attribute);
        templateConfiguredValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)templateConfiguredValueSet.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");

        configuredValueSet.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(configuredValueSet.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IRangeValueSet.class)));

        assertThat(((IRangeValueSet)configuredValueSet.getValueSet()).getLowerBound(), is("10"));
        assertThat(((IRangeValueSet)configuredValueSet.getValueSet()).getUpperBound(), is("20"));
    }

    @Test
    public void testGetValueSet_DefinedValue() {
        attribute.setValueSetType(ValueSetType.RANGE);

        configuredValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)configuredValueSet.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");

        assertThat(configuredValueSet.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IRangeValueSet.class)));

        assertThat(((IRangeValueSet)configuredValueSet.getValueSet()).getLowerBound(), is("10"));
        assertThat(((IRangeValueSet)configuredValueSet.getValueSet()).getUpperBound(), is("20"));

    }

    @Test
    public void testGetValueSet_UndefinedValue() throws CoreException {
        attribute.setValueSetType(ValueSetType.RANGE);

        IConfiguredValueSet templateConfiguredValueSet = createTemplateValueSet(attribute);
        templateConfiguredValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)templateConfiguredValueSet.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");

        templateConfiguredValueSet.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        assertThat(templateConfiguredValueSet.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
        assertThat(templateConfiguredValueSet.getValueSet(), is(not((IValueSet)valueSet)));
        assertThat(templateConfiguredValueSet.getValueSet(), is(instanceOf(UnrestrictedValueSet.class)));

    }

    @Test
    public void testInitFromXml() {
        Document doc = getTestDocument();
        configuredValueSet.initFromXml(doc.getDocumentElement());
        assertEquals("1", configuredValueSet.getId());
        IRangeValueSet range = (IRangeValueSet)configuredValueSet.getValueSet();
        assertEquals("22", range.getLowerBound());
        assertEquals("33", range.getUpperBound());
        assertEquals("4", range.getStep());
    }

    @Test
    public void testInitFromXml_InheritedValuesAreRead() throws CoreException {
        attribute.setValueSetType(ValueSetType.RANGE);
        attribute.setDatatype("Decimal");

        IConfiguredValueSet templateValueSet = createTemplateValueSet(attribute);

        // Set Range [10 ... 20 / 1] and value 10 in template
        templateValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet templateConfigElementRange = (IRangeValueSet)templateValueSet.getValueSet();
        templateConfigElementRange.setLowerBound("10");
        templateConfigElementRange.setUpperBound("20");
        templateConfigElementRange.setStep("1");

        // Set Range [100 ... 200 / 10] and value 100 in config element
        configuredValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet configElementRange = (IRangeValueSet)configuredValueSet.getValueSet();
        configElementRange.setLowerBound("100");
        configElementRange.setUpperBound("200");
        configElementRange.setStep("10");

        // Let config element inherit from template and thus use template's value/value set
        configuredValueSet.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        // Precondition: inheriting from template should work
        assertThat(configuredValueSet.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));

        Element xmlElement = toXml(configuredValueSet);

        // Do not use template anymore. Values read from XML should now be the defined values
        productCmpt.setTemplate("");
        IConfiguredValueSet newConfigValueSet = generation.newPropertyValue(attribute, IConfiguredValueSet.class);
        newConfigValueSet.initFromXml(xmlElement);

        assertThat(newConfigValueSet.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
        assertThat(newConfigValueSet.getValueSet(), is(instanceOf(IRangeValueSet.class)));
        IRangeValueSet newRange = (IRangeValueSet)newConfigValueSet.getValueSet();
        assertThat(newRange.getLowerBound(), is("10"));
        assertThat(newRange.getUpperBound(), is("20"));
        assertThat(newRange.getStep(), is("1"));
    }

    @Test
    public void testToXmlDocument() {
        configuredValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)configuredValueSet.getValueSet();
        valueSet.setLowerBound("22");
        valueSet.setUpperBound("33");
        valueSet.setStep("4");
        Element xmlElement = toXml(configuredValueSet);

        IConfiguredValueSet newCfgValueSet = generation.newPropertyValue(attribute, IConfiguredValueSet.class);
        newCfgValueSet.initFromXml(xmlElement);
        assertEquals("22", ((IRangeValueSet)newCfgValueSet.getValueSet()).getLowerBound());
        assertEquals("33", ((IRangeValueSet)newCfgValueSet.getValueSet()).getUpperBound());
        assertEquals("4", ((IRangeValueSet)newCfgValueSet.getValueSet()).getStep());

        configuredValueSet.setValueSetType(ValueSetType.ENUM);
        IEnumValueSet enumValueSet = (IEnumValueSet)configuredValueSet.getValueSet();
        enumValueSet.addValue("one");
        enumValueSet.addValue("two");
        enumValueSet.addValue("three");
        enumValueSet.addValue("four");

        xmlElement = toXml(configuredValueSet);
        assertEquals(4, ((IEnumValueSet)configuredValueSet.getValueSet()).getValues().length);
        assertEquals("one", ((IEnumValueSet)configuredValueSet.getValueSet()).getValues()[0]);
        assertEquals("two", ((IEnumValueSet)configuredValueSet.getValueSet()).getValues()[1]);
        assertEquals("three", ((IEnumValueSet)configuredValueSet.getValueSet()).getValues()[2]);
        assertEquals("four", ((IEnumValueSet)configuredValueSet.getValueSet()).getValues()[3]);
    }

    private Element toXml(IConfiguredValueSet configuredValueSet) {
        return configuredValueSet.toXml(getTestDocument());
    }

    @Test
    public void testToXmlDocument_ShouldPersistInheritedTemplateValues() throws XPathException, CoreException {

        attribute.setValueSetType(ValueSetType.RANGE);
        attribute.setDatatype("Decimal");

        IConfiguredValueSet templateConfiguredValueSet = createTemplateValueSet(attribute);

        // Set Range [10 ... 20 / 1] in template
        templateConfiguredValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet templateValueSet = (IRangeValueSet)templateConfiguredValueSet.getValueSet();
        templateValueSet.setLowerBound("10");
        templateValueSet.setUpperBound("20");
        templateValueSet.setStep("1");

        // Set Range [100 ... 200 / 10] in config element
        configuredValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet configElementValueSet = (IRangeValueSet)configuredValueSet.getValueSet();
        configElementValueSet.setLowerBound("100");
        configElementValueSet.setUpperBound("200");
        configElementValueSet.setStep("10");

        // Let config element inherit from template and thus use template's value/value set
        configuredValueSet.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        // Precondition: inheriting from template should work
        assertThat(configuredValueSet.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));

        Element xmlElement = toXml(configuredValueSet);
        XPath xpath = XPathFactory.newInstance().newXPath();
        assertThat(xpath.evaluate("ValueSet/Range/LowerBound", xmlElement), is("10"));
        assertThat(xpath.evaluate("ValueSet/Range/UpperBound", xmlElement), is("20"));
        assertThat(xpath.evaluate("ValueSet/Range/Step", xmlElement), is("1"));
    }

    @Test
    public void testValidateThis_nullIncompatible() throws CoreException {
        attribute.setDatatype(ValueDatatype.INTEGER.getQualifiedName());
        attribute.setValueSetType(ValueSetType.UNRESTRICTED);
        attribute.getValueSet().setContainsNull(false);

        configuredValueSet.setValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.getValueSet().setAbstract(false);
        configuredValueSet.getValueSet().setContainsNull(true);

        MessageList messages = configuredValueSet.validate(ipsProject);
        assertNotNull(messages.getMessageByCode(ValueSetNullIncompatibleValidator.MSGCODE_INCOMPATIBLE_VALUESET));
    }

    @Test
    public void testSetTemplateValueStatus() throws Exception {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute("attrName");
        IConfiguredValueSet templateValueSet = createTemplateValueSet(attr);

        templateValueSet.setValueSet(new UnrestrictedValueSet(templateValueSet, "123"));

        configuredValueSet = productCmpt.getProductCmptGeneration(0).newPropertyValue(attr, IConfiguredValueSet.class);
        configuredValueSet.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(configuredValueSet.getValueSet(), instanceOf(DelegatingValueSet.class));

        configuredValueSet.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        assertThat(configuredValueSet.getValueSet(), instanceOf(UnrestrictedValueSet.class));
    }

    private IConfiguredValueSet createTemplateValueSet(IPolicyCmptTypeAttribute policyCmptTypeAttribute)
            throws CoreException {
        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGen = template.getProductCmptGeneration(0);
        IConfiguredValueSet templateValueSet = templateGen.newPropertyValue(policyCmptTypeAttribute,
                IConfiguredValueSet.class);
        productCmpt.setTemplate(template.getQualifiedName());
        return templateValueSet;
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
        IConfiguredDefault defaultValue = generation.newPropertyValue(a1, IConfiguredDefault.class);
        assertEquals(a1, defaultValue.findPcTypeAttribute(ipsProject));
    }

    @Test
    public void testGetCaption() throws CoreException {
        attribute.setLabelValue(Locale.US, "Attribute Label");

        assertEquals(NLS.bind(Messages.ConfiguredValueSet_caption, "Attribute Label"),
                configuredValueSet.getCaption(Locale.US));
    }

    @Test
    public void testGetLastResortCaption() {
        assertEquals(NLS.bind(Messages.ConfiguredValueSet_caption, "attribute"),
                configuredValueSet.getLastResortCaption());
    }

}
