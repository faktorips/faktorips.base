/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConfiguredDefaultTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IPolicyCmptTypeAttribute attribute;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IConfiguredDefault configuredDefaultValue;

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

        configuredDefaultValue = generation.newPropertyValue(attribute, IConfiguredDefault.class);
        productCmpt.getIpsSrcFile().save(true, null);
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
    }

    @Test
    public void testValidate_UnknownAttribute() throws CoreException {
        attribute.setName("otherName");
        MessageList ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));

        attribute.setName("attribute");
        ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));
    }

    @Test
    public void testValidate_UnknownDatatype() throws CoreException {
        configuredDefaultValue.setValue("1");

        attribute.setAttributeType(AttributeType.CHANGEABLE);

        // no data type given
        MessageList ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredDefault.MSGCODE_UNKNOWN_DATATYPE));

        // unknown data type
        attribute.setDatatype("HopefullyNoExistingDatatype");
        policyCmptType.getIpsSrcFile().save(true, null);
        ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredDefault.MSGCODE_UNKNOWN_DATATYPE));

        // valid data type
        attribute.setDatatype("Decimal");
        policyCmptType.getIpsSrcFile().save(true, null);
        ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredDefault.MSGCODE_UNKNOWN_DATATYPE));
    }

    @Test
    public void testValidate_ValueNotParsable() throws CoreException {
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        attribute.setDatatype("Money");

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        // value cannot be parsed -> expect error message
        configuredDefaultValue.setValue("1");
        MessageList ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        // make value parsable -> expect no error message
        attribute.setDatatype("Decimal");
        policyCmptType.getIpsSrcFile().save(true, null);
        ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml,
                lacksMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
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

        configuredDefaultValue.setValue("a");
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        attribute.setDatatype(enumType.getQualifiedName());

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        MessageList ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml,
                lacksMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        configuredDefaultValue.setValue("b");
        ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
    }

    @Test
    public void testValidate_InvalidDatatype() throws Exception {
        InvalidDatatype datatype = new InvalidDatatype();
        attribute.setDatatype(datatype.getQualifiedName());

        Datatype[] vds = ipsProject.findDatatypes(true, false);
        ArrayList<Datatype> vdlist = new ArrayList<>();
        vdlist.addAll(Arrays.asList(vds));
        vdlist.add(datatype);

        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed(vdlist.toArray(new ValueDatatype[vdlist.size()]));
        ipsProject.setProperties(properties);

        MessageList ml = configuredDefaultValue.validate(configuredDefaultValue.getIpsProject());
        assertThat(ml, hasMessageCode(IConfigElement.MSGCODE_INVALID_DATATYPE));
    }

    @Test
    public void testValidate_ValueNotInValueset() throws CoreException {
        attribute.setAttributeType(AttributeType.CONSTANT);
        attribute.setValueSetType(ValueSetType.RANGE);
        attribute.setDatatype("Decimal");
        IRangeValueSet valueSetAttr = (IRangeValueSet)attribute.getValueSet();
        valueSetAttr.setLowerBound(null);
        valueSetAttr.setUpperBound(null);

        configuredDefaultValue.setValue("1");
        IConfiguredValueSet configuredValueSet = generation.newPropertyValue(attribute, IConfiguredValueSet.class);
        configuredValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)configuredValueSet.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");

        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);

        MessageList ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredDefault.MSGCODE_VALUE_NOT_IN_VALUESET));

        configuredDefaultValue.setValue("15");

        ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredDefault.MSGCODE_VALUE_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_ValueNotInValuesetForInheritedValues() throws CoreException {
        attribute.setValueSetType(ValueSetType.RANGE);
        attribute.setDatatype("Decimal");

        IConfiguredValueSet configuredValueSet = generation.newPropertyValue(attribute, IConfiguredValueSet.class);

        configuredValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)configuredValueSet.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");

        IConfiguredDefault templateDefaultValue = createDefaultValueAndValueSetInNewTemplate(attribute);
        templateDefaultValue.setValue("1");

        configuredDefaultValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(configuredDefaultValue.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(configuredDefaultValue.getValue(), is("1"));

        MessageList ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredDefault.MSGCODE_VALUE_NOT_IN_VALUESET));

        templateDefaultValue.setValue("15");
        ml = configuredDefaultValue.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredDefault.MSGCODE_VALUE_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_ValueNotInValuesetForUndefinedValues() throws CoreException {
        attribute.setValueSetType(ValueSetType.RANGE);
        attribute.setDatatype("Decimal");

        IConfiguredDefault templateDefaultValue = createDefaultValueAndValueSetInNewTemplate(attribute);
        IConfiguredValueSet templateValueSet = getCorrespondingTemplateValueSet(templateDefaultValue);

        // First set some invalid value set in template's fields...
        templateValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)templateValueSet.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");
        templateDefaultValue.setValue("1");
        assertThat(templateDefaultValue.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
        assertThat(templateValueSet.getValueSet(), is((IValueSet)valueSet));

        // ...then make sure template forgets about its invalid value set if it is undefined
        templateDefaultValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        assertThat(templateDefaultValue.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
        assertThat(templateDefaultValue.getValue(), is(""));

        assertThat(templateDefaultValue.validate(ipsProject),
                lacksMessageCode(IConfiguredDefault.MSGCODE_VALUE_NOT_IN_VALUESET));
    }

    @Test
    public void testSetValue() {
        configuredDefaultValue.setValue("newValue");
        assertThat(configuredDefaultValue.getValue(), is("newValue"));
        assertThat(configuredDefaultValue.getIpsSrcFile().isDirty(), is(true));
    }

    @Test
    public void testGetValue_DefinedValue() {
        configuredDefaultValue.setValue("newValue");
        assertThat(configuredDefaultValue.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
        assertThat(configuredDefaultValue.getValue(), is("newValue"));
    }

    @Test
    public void testGetValue_InheritedValue() throws CoreException {
        IConfiguredDefault templateDefaultValue = createDefaultValueInNewTemplate(attribute);

        templateDefaultValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        templateDefaultValue.setValue("value");

        templateDefaultValue.setValue("templateValue");

        assertThat(templateDefaultValue.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(templateDefaultValue.getValue(), is("templateValue"));
    }

    @Test
    public void testInitFromXml() {
        Document doc = getTestDocument();
        configuredDefaultValue.initFromXml(doc.getDocumentElement());
        assertThat(configuredDefaultValue.getId(), is("42"));
        assertThat(configuredDefaultValue.getValue(), is("10"));
    }

    @Test
    public void testGetValue_UndefinedValue() throws CoreException {
        IConfiguredDefault templateDefaultValue = createDefaultValueInNewTemplate();
        templateDefaultValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        templateDefaultValue.setValue("templateValue");

        assertThat(templateDefaultValue.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
        assertThat(templateDefaultValue.getValue(), is(""));
    }

    @Test
    public void testInitFromXml_InheritedValuesAreRead() throws CoreException {
        attribute.setValueSetType(ValueSetType.RANGE);
        attribute.setDatatype("Decimal");

        IConfiguredDefault templateConfiguredDefault = createDefaultValueInNewTemplate(attribute);

        // Set value 10 in template
        templateConfiguredDefault.setValue("10");

        // Set value 100 in config element
        configuredDefaultValue.setValue("100");

        // Let config element inherit from template and thus use template's value/value set
        configuredDefaultValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        // Precondition: inheriting from template should work
        assertThat(configuredDefaultValue.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(configuredDefaultValue.getValue(), is("10"));

        Element xmlElement = toXml(configuredDefaultValue);

        // Do not use template anymore. Values read from XML should now be the defined values
        productCmpt.setTemplate("");
        IConfiguredDefault configuredDefaultFromXml = generation.newPropertyValue(attribute, IConfiguredDefault.class);
        configuredDefaultFromXml.initFromXml(xmlElement);

        assertThat(configuredDefaultFromXml.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
        assertThat(configuredDefaultFromXml.getValue(), is("10"));
    }

    @Test
    public void testToXmlDocument() {
        configuredDefaultValue.setValue("value");
        Element xmlElement = toXml(configuredDefaultValue);

        IConfiguredDefault cfgDefaultFromXml = generation.newPropertyValue(attribute, IConfiguredDefault.class);
        cfgDefaultFromXml.initFromXml(xmlElement);
        assertThat(cfgDefaultFromXml.getValue(), is("value"));

        configuredDefaultValue.setValue(null);
        xmlElement = toXml(configuredDefaultValue);
        cfgDefaultFromXml.initFromXml(xmlElement);

        assertThat(cfgDefaultFromXml.getValue(), equalTo(null));
    }

    private Element toXml(IConfiguredDefault configuredDefaultValue) {
        return configuredDefaultValue.toXml(getTestDocument());
    }

    @Test
    public void testToXmlDocument_ShouldPersistInheritedTemplateValues() throws CoreException {
        attribute.setValueSetType(ValueSetType.RANGE);
        attribute.setDatatype("Decimal");

        IConfiguredDefault templateDefaultValue = createDefaultValueInNewTemplate(attribute);

        // Set value 10 in template
        templateDefaultValue.setValue("10");

        // Set value 100 in config element
        configuredDefaultValue.setValue("100");

        // Let config element inherit from template and thus use template's value/value set
        configuredDefaultValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        // Precondition: inheriting from template should work
        assertThat(configuredDefaultValue.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(configuredDefaultValue.getValue(), is("10"));

        // Assert that template values are persisted, not the values set in the config element
        Element xmlElement = toXml(configuredDefaultValue);
        assertThat(xmlElement.getTextContent(), is("10"));
    }

    @Test
    public void testSetTemplateValueStatus() throws Exception {
        IConfiguredDefault templateDefaultValue = createDefaultValueInNewTemplate(attribute);
        templateDefaultValue.setValue("10");
        configuredDefaultValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(configuredDefaultValue.getValue(), is("10"));

        configuredDefaultValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        assertThat(configuredDefaultValue.getValue(), is("10"));
    }

    private IConfiguredDefault createDefaultValueInNewTemplate() throws CoreException {
        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGen = template.getProductCmptGeneration(0);
        IConfiguredDefault templateConfigElement = templateGen.newPropertyValue(attribute, IConfiguredDefault.class);
        productCmpt.setTemplate(template.getQualifiedName());
        return templateConfigElement;
    }

    private IConfiguredDefault createDefaultValueInNewTemplate(IPolicyCmptTypeAttribute policyCmptTypeAttribute)
            throws CoreException {
        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGen = template.getProductCmptGeneration(0);
        IConfiguredDefault templateConfigElement = templateGen.newPropertyValue(policyCmptTypeAttribute,
                IConfiguredDefault.class);
        productCmpt.setTemplate(template.getQualifiedName());
        IConfiguredDefault templateConfiguredDefault = templateConfigElement;
        return templateConfiguredDefault;
    }

    private IConfiguredDefault createDefaultValueAndValueSetInNewTemplate(
            IPolicyCmptTypeAttribute policyCmptTypeAttribute) throws CoreException {
        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGen = template.getProductCmptGeneration(0);
        IConfiguredDefault templateConfiguredDefault = templateGen.newPropertyValue(policyCmptTypeAttribute,
                IConfiguredDefault.class);
        templateGen.newPropertyValue(policyCmptTypeAttribute, IConfiguredValueSet.class);
        productCmpt.setTemplate(template.getQualifiedName());
        return templateConfiguredDefault;
    }

    private IConfiguredValueSet getCorrespondingTemplateValueSet(IConfiguredDefault defaultValue) {
        return defaultValue.getPropertyValueContainer().getPropertyValue(defaultValue.getPolicyCmptTypeAttribute(),
                IConfiguredValueSet.class);
    }

    @Test
    public void testGetCaption() throws CoreException {
        attribute.setLabelValue(Locale.US, "Attribute Label");

        assertThat(NLS.bind(Messages.ConfiguredDefault_caption, "Attribute Label"),
                is(configuredDefaultValue.getCaption(Locale.US)));
    }

    @Test
    public void testGetLastResortCaption() {
        assertThat(NLS.bind(Messages.ConfiguredDefault_caption, "attribute"),
                is(configuredDefaultValue.getLastResortCaption()));
    }

}
