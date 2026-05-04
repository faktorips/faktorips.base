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

import static org.faktorips.abstracttest.matcher.ValueSetMatchers.contains;
import static org.faktorips.abstracttest.matcher.ValueSetMatchers.empty;
import static org.faktorips.testsupport.IpsMatchers.hasInvalidObject;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.hasMessageThat;
import static org.faktorips.testsupport.IpsMatchers.hasSeverity;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.osgi.util.NLS;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.internal.ValueSetNullIncompatibleValidator;
import org.faktorips.devtools.model.internal.valueset.DelegatingValueSet;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
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
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IStringLengthValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
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
        productCmpt.getIpsSrcFile().save(null);
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
    }

    @Test
    public void testGetAllowedValueSetTypes() {
        // case 1: attribute not found
        attribute.setName("unknown");
        List<ValueSetType> types = configuredValueSet.getAllowedValueSetTypes(ipsProject);
        assertThat(types, hasSize(1));
        assertThat(types, contains(configuredValueSet.getValueSet().getValueSetType()));

        // case 2: attribute found, value set type is unrestricted, data type is Integer
        // => all types should be available
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        a1.setName("attribute");
        a1.setDatatype("Integer");
        a1.setValueSetConfiguredByProduct(true);
        a1.setValueSetType(ValueSetType.UNRESTRICTED);
        types = configuredValueSet.getAllowedValueSetTypes(ipsProject);
        assertThat(types, contains(ValueSetType.UNRESTRICTED, ValueSetType.RANGE, ValueSetType.ENUM));

        // case 3: as before, but with datatype String
        // => only unrestricted and enum should be available
        a1.setDatatype("String");
        types = configuredValueSet.getAllowedValueSetTypes(ipsProject);
        assertThat(types, contains(ValueSetType.UNRESTRICTED, ValueSetType.ENUM));

        // case 4a: Integer with RANGE value set
        // => should allow only RANGE
        a1.setDatatype("Integer");
        a1.setValueSetType(ValueSetType.RANGE);
        types = configuredValueSet.getAllowedValueSetTypes(ipsProject);
        assertThat(types, contains(ValueSetType.RANGE));

        // case 4b: Integer with RANGE value set in project with unified value set method names
        // => should allow both RANGE and ENUM
        ((TestIpsArtefactBuilderSet)ipsProject.getIpsArtefactBuilderSet()).setUsesUnifiedValueSets(true);
        types = configuredValueSet.getAllowedValueSetTypes(ipsProject);
        assertThat(types, contains(ValueSetType.RANGE, ValueSetType.ENUM));

        // case 5: attribute is derived, but still product relevant
        a1.setValueSetType(ValueSetType.DERIVED);
        types = configuredValueSet.getAllowedValueSetTypes(ipsProject);
        assertThat(types, contains(ValueSetType.UNRESTRICTED, ValueSetType.RANGE, ValueSetType.ENUM));

        // case 6: String with STRINGLENGTH value set
        // => should allow both STRINGLENGTH and ENUM
        a1.setDatatype("String");
        a1.setValueSetType(ValueSetType.STRINGLENGTH);
        types = configuredValueSet.getAllowedValueSetTypes(ipsProject);
        assertThat(types, contains(ValueSetType.STRINGLENGTH, ValueSetType.ENUM));
    }

    @Test
    public void testValidate_UnknownAttribute() {
        attribute.setName("unknown");
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));

        attribute.setName("attribute");

        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));
    }

    @Test
    public void testValidate_UnknownDatatypeValue() {
        attribute.setAttributeType(AttributeType.CHANGEABLE);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfigElement.MSGCODE_UNKNOWN_DATATYPE));
        assertThat(ml.getMessageByCode(IConfiguredDefault.MSGCODE_UNKNOWN_DATATYPE), hasSeverity(Severity.WARNING));

        attribute.setDatatype("Decimal");

        policyCmptType.getIpsSrcFile().save(null);

        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfigElement.MSGCODE_UNKNOWN_DATATYPE));
    }

    @Test
    public void testValidate_InvalidValueset() {
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        attribute.setDatatype("Decimal");
        attribute.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)attribute.getValueSet();
        valueSet.setLowerBound("a");
        valueSet.setUpperBound("b");

        configuredValueSet.setValueSetCopy(valueSet);

        policyCmptType.getIpsSrcFile().save(null);
        productCmpt.getIpsSrcFile().save(null);

        MessageList ml = configuredValueSet.validate(configuredValueSet.getIpsProject());

        // no test for specific message codes because the codes are under control
        // of the value set.
        assertThat(ml.size(), is(not(0)));

        valueSet = (IRangeValueSet)configuredValueSet.getValueSet();
        valueSet.setLowerBound("0");
        valueSet.setUpperBound("100");

        valueSet = (IRangeValueSet)attribute.getValueSet();
        valueSet.setLowerBound("0");
        valueSet.setUpperBound("100");

        policyCmptType.getIpsSrcFile().save(null);
        productCmpt.getIpsSrcFile().save(null);

        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, isEmpty());
    }

    @Test
    public void testValidate_InvalidDatatype() throws Exception {
        try (var testIpsModelExtensions = TestIpsModelExtensions.get()) {
            InvalidDatatype datatype = new InvalidDatatype();
            if (!Abstractions.isEclipseRunning()) {
                // for eclipse this datatype is defined in the fragment.xml
                testIpsModelExtensions.addPredefinedDatatype(datatype);
            }
            attribute.setDatatype(datatype.getQualifiedName());

            Datatype[] vds = ipsProject.findDatatypes(true, false);
            ArrayList<Datatype> vdlist = new ArrayList<>();
            vdlist.addAll(Arrays.asList(vds));
            vdlist.add(datatype);

            IIpsProjectProperties properties = ipsProject.getProperties();
            properties.setPredefinedDatatypesUsed(vdlist.toArray(new ValueDatatype[vdlist.size()]));
            ipsProject.setProperties(properties);

            MessageList ml = configuredValueSet.validate(configuredValueSet.getIpsProject());
            assertThat(ml, hasMessageCode(IConfigElement.MSGCODE_INVALID_DATATYPE));
        }
    }

    @Test
    public void testValidate_MandatoryValueSetIsEmpty() {
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setDatatype("Integer");
        attribute.getValueSet().setContainsNull(false);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_MANDATORY_VALUESET_IS_EMPTY));
    }

    @Test
    public void testValidate_MandatoryEnumValueSetIsEmptyInModel() {
        attribute.setDatatype("Integer");
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setRelevanceConfiguredByProduct(true);
        attribute.changeValueSetType(ValueSetType.ENUM);
        attribute.getValueSet().setContainsNull(false);

        configuredValueSet.changeValueSetType(ValueSetType.ENUM);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, isEmpty());
    }

    @Test
    public void testValidate_MandatoryRangeValueSetIsEmptyInModel() {
        attribute.setDatatype("Integer");
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setRelevanceConfiguredByProduct(true);
        attribute.changeValueSetType(ValueSetType.RANGE);
        attribute.getValueSet().setContainsNull(false);
        RangeValueSet valueSet = (RangeValueSet)attribute.getValueSet();
        valueSet.setLowerBound("0");
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        RangeValueSet configuredSet = (RangeValueSet)configuredValueSet.getValueSet();
        configuredSet.setEmpty(true);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_MANDATORY_VALUESET_IS_EMPTY));
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_MANDATORY_VALUESET_MUST_BE_MANDATORY));
    }

    @Test
    public void testValidate_MandatoryValueSetIsAbstractInModel() {
        attribute.setDatatype("Integer");
        attribute.changeValueSetType(ValueSetType.ENUM);
        attribute.getValueSet().setContainsNull(false);
        attribute.getValueSet().setAbstract(true);
        IEnumValueSet modelValueSet = (IEnumValueSet)attribute.getValueSet();
        modelValueSet.addValue("1");
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, isEmpty());
    }

    @Test
    public void testValidate_MandatoryValueSetIsAbstractInModelAndEmptyInProduct_RelevanceOnly() {
        attribute.setDatatype("Integer");
        attribute.changeValueSetType(ValueSetType.ENUM);
        attribute.getValueSet().setContainsNull(false);
        attribute.getValueSet().setAbstract(true);
        attribute.setRelevanceConfiguredByProduct(true);
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_MANDATORY_VALUESET_MUST_BE_MANDATORY_RELEVANCE_ONLY));
    }

    @Test
    public void testValidate_MandatoryValueSetIsAbstractInModelAndEmptyInProduct_ValueSetOnly() {
        attribute.setDatatype("Integer");
        attribute.changeValueSetType(ValueSetType.ENUM);
        attribute.getValueSet().setContainsNull(false);
        attribute.getValueSet().setAbstract(true);
        attribute.setValueSetConfiguredByProduct(true);
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_MANDATORY_VALUESET_IS_EMPTY));
    }

    @Test
    public void testValidate_MandatoryValueSetIsAbstractInModelAndEmptyInProduct() {
        attribute.setDatatype("Integer");
        attribute.changeValueSetType(ValueSetType.ENUM);
        attribute.getValueSet().setContainsNull(false);
        attribute.getValueSet().setAbstract(true);
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setRelevanceConfiguredByProduct(true);
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_MANDATORY_VALUESET_IS_EMPTY));
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_MANDATORY_VALUESET_MUST_BE_MANDATORY));
    }

    @Test
    public void testValidate_MandatoryValueSetIsEmptyInModel_PrimitiveAttribute() {
        attribute.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        attribute.changeValueSetType(ValueSetType.ENUM);
        attribute.setValueSetConfiguredByProduct(true);
        attribute.getValueSet().setContainsNull(false);
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, isEmpty());
    }

    @Test
    public void testValidate_ValueSetTypeMismatch() {
        setUpRangeIntegerAttr();

        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        ((TestIpsArtefactBuilderSet)ipsProject.getIpsArtefactBuilderSet()).setUsesUnifiedValueSets(true);
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        attribute.changeValueSetType(ValueSetType.UNRESTRICTED);
        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        configuredValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);
        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        attribute.setValueSetType(ValueSetType.ENUM);
        configuredValueSet.setValueSetType(ValueSetType.RANGE);
        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));
    }

    @Test
    public void testValidate_ValueSetTypeMismatch_RangeValueSet_ContainsOpenBoundProperties() {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("typeMismatchOpenBoundsTest");
        attr.setValueSetType(ValueSetType.ENUM);
        attr.setDatatype("Integer");

        configuredValueSet = productCmpt.newPropertyValue(attr, IConfiguredValueSet.class);
        configuredValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet productRange = (IRangeValueSet)configuredValueSet.getValueSet();
        productRange.setLowerBound("10");
        productRange.setUpperBound("20");

        MessageList ml = configuredValueSet.validate(ipsProject);

        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_LOWERBOUND)));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_UPPERBOUND)));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_STEP)));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_LOWERBOUND_OPEN)));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_UPPERBOUND_OPEN)));
    }

    @Test
    public void testValidate_ValueSetTypeMismatchForInheritedValues_changeParentValueSet() {
        setUpRangeIntegerAttr();
        IConfiguredValueSet templateValueSet = setUpRangeIntegerTemplate(attribute);
        setUpInheritRangeValueSet();

        assertThat(configuredValueSet.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(configuredValueSet.validate(ipsProject),
                lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        templateValueSet.changeValueSetType(ValueSetType.ENUM);
        assertThat(configuredValueSet.validate(ipsProject),
                lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        ((TestIpsArtefactBuilderSet)ipsProject.getIpsArtefactBuilderSet()).setUsesUnifiedValueSets(true);
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        assertThat(configuredValueSet.validate(ipsProject),
                lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        attribute.changeValueSetType(ValueSetType.UNRESTRICTED);
        assertThat(configuredValueSet.validate(ipsProject),
                lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));

        attribute.changeValueSetType(ValueSetType.ENUM);
        templateValueSet.changeValueSetType(ValueSetType.RANGE);
        assertThat(configuredValueSet.validate(ipsProject),
                hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH));
    }

    @Test
    public void testValidate_ValueSetTypeMismatchForInheritedValues() {
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

    private IConfiguredValueSet setUpRangeIntegerTemplate(IPolicyCmptTypeAttribute attr) {
        IConfiguredValueSet templateValueSet = createTemplateValueSet(attr);
        templateValueSet.changeValueSetType(ValueSetType.RANGE);
        return templateValueSet;
    }

    @Test
    public void testValidate_ValueSetTypeMismatchForUndefinedValues() {
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
        attribute.getValueSet().setContainsNull(true);
        attribute.setDatatype("Integer");
    }

    @Test
    public void testValidate_ValueSetNotASubset() {
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

        policyCmptType.getIpsSrcFile().save(null);
        productCmpt.getIpsSrcFile().save(null);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        valueSet.setUpperBound("20");
        policyCmptType.getIpsSrcFile().save(null);

        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, isEmpty());

        // check lower unbound values
        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        valueSet.setLowerBound("10");
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound("10");
        valueSet2.setUpperBound(null);
        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        // check upper unbound values
        valueSet.setLowerBound(null);
        valueSet.setUpperBound("10");
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));

        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound("10");
        ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    @Test
    public void testValidate_ValueSetNotASubset_ModelLowerOpen_ProductIncludesLower() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model (10..20], product [10..20] -> error: product includes 10 but model excludes it
        modelRange.setLowerBoundOpen(true);
        productRange.setLowerBoundOpen(false);
        productRange.setUpperBoundOpen(false);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_LOWERBOUND)));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_LOWERBOUND_OPEN)));
    }

    @Test
    public void testValidate_ValueSetNotASubset_SameOpenBounds_NoError() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model (10..20), product (10..20) -> no error: same open bounds
        modelRange.setLowerBoundOpen(true);
        modelRange.setUpperBoundOpen(true);
        productRange.setLowerBoundOpen(true);
        productRange.setUpperBoundOpen(true);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    @Test
    public void testValidate_ValueSetNotASubset_ProductMoreRestricted_NoError() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model [10..20], product (10..20) -> no error: product is more restricted
        modelRange.setLowerBoundOpen(false);
        modelRange.setUpperBoundOpen(false);
        productRange.setLowerBoundOpen(true);
        productRange.setUpperBoundOpen(true);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    @Test
    public void testValidate_ValueSetNotASubset_ModelBothOpen_ProductIncludesLower() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model (10..20), product [10..20) -> error: product includes 10 but model excludes it
        modelRange.setLowerBoundOpen(true);
        modelRange.setUpperBoundOpen(true);
        productRange.setLowerBoundOpen(false);
        productRange.setUpperBoundOpen(true);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_LOWERBOUND)));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_LOWERBOUND_OPEN)));
    }

    @Test
    public void testValidate_ValueSetNotASubset_BothClosed_NoError() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // baseline: model [10..20], product [10..20] -> no error: both closed, same bounds
        modelRange.setLowerBoundOpen(false);
        modelRange.setUpperBoundOpen(false);
        productRange.setLowerBoundOpen(false);
        productRange.setUpperBoundOpen(false);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    @Test
    public void testValidate_ValueSetNotASubset_ModelUpperOpen_ProductIncludesUpper() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model [10..20), product [10..20] -> error: product includes 20 but model excludes it
        modelRange.setLowerBoundOpen(false);
        modelRange.setUpperBoundOpen(true);
        productRange.setLowerBoundOpen(false);
        productRange.setUpperBoundOpen(false);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_UPPERBOUND)));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_UPPERBOUND_OPEN)));
    }

    @Test
    public void testValidate_ValueSetNotASubset_ModelUpperOpen_ProductNarrowerUpper_NoError() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model [10..20), product [10..19] -> no error: product upper is within open model bound
        productRange.setUpperBound("19");
        modelRange.setLowerBoundOpen(false);
        modelRange.setUpperBoundOpen(true);
        productRange.setLowerBoundOpen(false);
        productRange.setUpperBoundOpen(false);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    @Test
    public void testValidate_ValueSetNotASubset_UnboundedModelLower_NoError() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model (*..20], product [10..20] -> no error: model lower is unbounded
        modelRange.setLowerBound(null);
        modelRange.setLowerBoundOpen(true);
        modelRange.setUpperBoundOpen(false);
        productRange.setLowerBound("10");
        productRange.setLowerBoundOpen(false);
        productRange.setUpperBoundOpen(false);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    @Test
    public void testValidate_ValueSetNotASubset_UnboundedModelUpper_NoError() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model [10..*), product [10..20] -> no error: model upper is unbounded
        modelRange.setLowerBound("10");
        modelRange.setLowerBoundOpen(false);
        modelRange.setUpperBound(null);
        modelRange.setUpperBoundOpen(true);
        productRange.setUpperBound("20");
        productRange.setUpperBoundOpen(false);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    @Test
    public void testValidate_ValueSetNotASubset_ModelWider_MixedOpenness_NoError() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model (5..25), product [10..20] -> no error: model is wider with open bounds
        modelRange.setLowerBound("5");
        modelRange.setUpperBound("25");
        modelRange.setLowerBoundOpen(true);
        modelRange.setUpperBoundOpen(true);
        productRange.setLowerBound("10");
        productRange.setUpperBound("20");
        productRange.setLowerBoundOpen(false);
        productRange.setUpperBoundOpen(false);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    @Test
    public void testValidate_ValueSetNotASubset_ProductExceedsModelLower_MixedOpenness() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model (10..20), product [5..20) -> error: product lower exceeds open model lower bound
        modelRange.setLowerBound("10");
        modelRange.setUpperBound("20");
        modelRange.setLowerBoundOpen(true);
        modelRange.setUpperBoundOpen(true);
        productRange.setLowerBound("5");
        productRange.setUpperBound("20");
        productRange.setLowerBoundOpen(false);
        productRange.setUpperBoundOpen(true);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_LOWERBOUND)));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_LOWERBOUND_OPEN)));
    }

    @Test
    public void testValidate_ValueSetNotASubset_StepWithOpenBounds_NoError() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model (10..20] step=2, product (10..18] step=2 -> no error
        modelRange.setLowerBound("10");
        modelRange.setUpperBound("20");
        modelRange.setStep("2");
        modelRange.setLowerBoundOpen(true);
        modelRange.setUpperBoundOpen(false);
        productRange.setLowerBound("10");
        productRange.setUpperBound("18");
        productRange.setStep("2");
        productRange.setLowerBoundOpen(true);
        productRange.setUpperBoundOpen(false);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    @Test
    public void testValidate_ValueSetNotASubset_StepWithOpenBounds_ProductClosedLower() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model (10..20] step=2, product [10..18] step=2 -> error: product includes 10 but model excludes it
        modelRange.setLowerBound("10");
        modelRange.setUpperBound("20");
        modelRange.setStep("2");
        modelRange.setLowerBoundOpen(true);
        modelRange.setUpperBoundOpen(false);
        productRange.setLowerBound("10");
        productRange.setUpperBound("18");
        productRange.setStep("2");
        productRange.setLowerBoundOpen(false);
        productRange.setUpperBoundOpen(false);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_LOWERBOUND)));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_LOWERBOUND_OPEN)));
    }

    @Test
    public void testValidate_ValueSetNotASubset_SameOpenUpperBounds_NoError() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model [10..20), product [10..20) -> no error: same open upper bounds
        modelRange.setLowerBoundOpen(false);
        modelRange.setUpperBoundOpen(true);
        productRange.setLowerBoundOpen(false);
        productRange.setUpperBoundOpen(true);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    @Test
    public void testValidate_ValueSetNotASubset_ProductUnboundedLower() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model [10..20], product (*..20] -> error: product lower is unbounded
        modelRange.setLowerBound("10");
        modelRange.setUpperBound("20");
        modelRange.setLowerBoundOpen(false);
        modelRange.setUpperBoundOpen(false);
        productRange.setLowerBound(null);
        productRange.setUpperBound("20");
        productRange.setLowerBoundOpen(true);
        productRange.setUpperBoundOpen(false);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_LOWERBOUND)));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_LOWERBOUND_OPEN)));
    }

    @Test
    public void testValidate_ValueSetNotASubset_ProductUnboundedUpper() {
        IRangeValueSet[] ranges = setUpIntervalBoundTypesTest();
        IRangeValueSet modelRange = ranges[0];
        IRangeValueSet productRange = ranges[1];

        // model [10..20], product [10..*) -> error: product upper is unbounded
        modelRange.setUpperBound("20");
        productRange.setLowerBound("10");
        productRange.setLowerBoundOpen(false);
        productRange.setUpperBound(null);
        productRange.setUpperBoundOpen(true);
        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_UPPERBOUND)));
        assertThat(ml, hasMessageThat(hasInvalidObject(productRange, IRangeValueSet.PROPERTY_UPPERBOUND_OPEN)));
    }

    private IRangeValueSet[] setUpIntervalBoundTypesTest() {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("openBoundsTest");
        attr.setValueSetType(ValueSetType.RANGE);
        attr.setDatatype("Integer");
        IRangeValueSet modelRange = (IRangeValueSet)attr.getValueSet();
        modelRange.setLowerBound("10");
        modelRange.setUpperBound("20");

        configuredValueSet = productCmpt.newPropertyValue(attr, IConfiguredValueSet.class);
        configuredValueSet.setValueSetCopy(modelRange);
        IRangeValueSet productRange = (IRangeValueSet)configuredValueSet.getValueSet();

        policyCmptType.getIpsSrcFile().save(null);
        productCmpt.getIpsSrcFile().save(null);

        return new IRangeValueSet[] { modelRange, productRange };
    }

    @Test
    public void testValidate_ValueSetNotASubset_NoSubsetErrorForInvalidProductRange() {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("stepTest");
        attr.setValueSetType(ValueSetType.RANGE);
        attr.setDatatype("Integer");
        IRangeValueSet modelRange = (IRangeValueSet)attr.getValueSet();
        modelRange.setLowerBound("0");
        modelRange.setUpperBound("110");
        modelRange.setStep("2");

        configuredValueSet = productCmpt.newPropertyValue(attr, IConfiguredValueSet.class);
        configuredValueSet.setValueSetCopy(modelRange);
        IRangeValueSet productRange = (IRangeValueSet)configuredValueSet.getValueSet();
        productRange.setStep("4");

        policyCmptType.getIpsSrcFile().save(null);
        productCmpt.getIpsSrcFile().save(null);

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml, hasMessageCode(IRangeValueSet.MSGCODE_STEP_RANGE_MISMATCH));
        assertThat(ml, lacksMessageCode(IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET));
    }

    @Test
    public void testValidate_EnumValueSetForStringLengthValueSet() {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");
        attr.setValueSetType(ValueSetType.STRINGLENGTH);
        attr.setDatatype("String");
        IStringLengthValueSet valueSet = (IStringLengthValueSet)attr.getValueSet();
        valueSet.setMaximumLength("12");

        configuredValueSet = productCmpt.newPropertyValue(attr, IConfiguredValueSet.class);
        configuredValueSet.setValueSetCopy(valueSet);
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        IEnumValueSet enumValueSet = (IEnumValueSet)configuredValueSet.getValueSet();
        enumValueSet.addValue("short enough");

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml.isEmpty(), is(true));
    }

    @Test
    public void testValidate_EnumValueSetForStringLengthValueSet_ValueTooLong() {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("valueTest");
        attr.setValueSetType(ValueSetType.STRINGLENGTH);
        attr.setDatatype("String");
        IStringLengthValueSet valueSet = (IStringLengthValueSet)attr.getValueSet();
        valueSet.setMaximumLength("12");

        configuredValueSet = productCmpt.newPropertyValue(attr, IConfiguredValueSet.class);
        configuredValueSet.setValueSetCopy(valueSet);
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        IEnumValueSet enumValueSet = (IEnumValueSet)configuredValueSet.getValueSet();
        enumValueSet.addValue("this value is longer than the string length allows");

        MessageList ml = configuredValueSet.validate(ipsProject);
        assertThat(ml.containsErrorMsg(), is(true));
        assertThat(ml.getMessageByCode(IConfiguredValueSet.MSGCODE_STRING_TOO_LONG), is(not(nullValue())));
    }

    @Test
    public void testGetValueSet_InheritedValue() {
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
    public void testGetValueSet_UndefinedValue() {
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
        assertThat(configuredValueSet.getId(), is("1"));
        IRangeValueSet range = (IRangeValueSet)configuredValueSet.getValueSet();
        assertThat(range.getLowerBound(), is("22"));
        assertThat(range.getUpperBound(), is("33"));
        assertThat(range.getStep(), is("4"));
    }

    @Test
    public void testInitFromXml_InheritedValuesAreRead() {
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
        assertThat(((IRangeValueSet)newCfgValueSet.getValueSet()).getLowerBound(), is("22"));
        assertThat(((IRangeValueSet)newCfgValueSet.getValueSet()).getUpperBound(), is("33"));
        assertThat(((IRangeValueSet)newCfgValueSet.getValueSet()).getStep(), is("4"));

        configuredValueSet.setValueSetType(ValueSetType.ENUM);
        IEnumValueSet enumValueSet = (IEnumValueSet)configuredValueSet.getValueSet();
        enumValueSet.addValue("one");
        enumValueSet.addValue("two");
        enumValueSet.addValue("three");
        enumValueSet.addValue("four");

        xmlElement = toXml(configuredValueSet);
        assertThat(((IEnumValueSet)configuredValueSet.getValueSet()).getValues(),
                arrayContaining("one", "two", "three", "four"));
    }

    private Element toXml(IConfiguredValueSet configuredValueSet) {
        return configuredValueSet.toXml(getTestDocument());
    }

    @Test
    public void testToXmlDocument_ShouldPersistInheritedTemplateValues() throws XPathException {

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
    public void testValidateThis_nullIncompatible() {
        attribute.setDatatype(ValueDatatype.INTEGER.getQualifiedName());
        attribute.setValueSetType(ValueSetType.UNRESTRICTED);
        attribute.getValueSet().setContainsNull(false);

        configuredValueSet.setValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.getValueSet().setAbstract(false);
        configuredValueSet.getValueSet().setContainsNull(true);

        MessageList messages = configuredValueSet.validate(ipsProject);
        assertThat(messages, hasMessageCode(ValueSetNullIncompatibleValidator.MSGCODE_INCOMPATIBLE_VALUESET));
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

    private IConfiguredValueSet createTemplateValueSet(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGen = template.getProductCmptGeneration(0);
        IConfiguredValueSet templateValueSet = templateGen.newPropertyValue(policyCmptTypeAttribute,
                IConfiguredValueSet.class);
        productCmpt.setTemplate(template.getQualifiedName());
        return templateValueSet;
    }

    @Test
    public void testFindPcTypeAttribute() {
        IPolicyCmptType policyCmptSupertype = newPolicyCmptType(ipsProject, "SuperPolicy");
        policyCmptType.setSupertype(policyCmptSupertype.getQualifiedName());

        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        a1.setName("a1");
        IPolicyCmptTypeAttribute a2 = policyCmptSupertype.newPolicyCmptTypeAttribute();
        a2.setName("a2");

        generation = productCmpt.getProductCmptGeneration(0);
        IConfiguredDefault defaultValue = generation.newPropertyValue(a1, IConfiguredDefault.class);
        assertThat(defaultValue.findPcTypeAttribute(ipsProject), is(a1));
    }

    @Test
    public void testGetCaption() {
        attribute.setLabelValue(Locale.US, "Attribute Label");

        assertThat(configuredValueSet.getCaption(Locale.US),
                is(NLS.bind(Messages.ConfiguredValueSet_caption, "", "Attribute Label")));

        attribute.setValueSetType(ValueSetType.DERIVED);

        assertThat(configuredValueSet.getCaption(Locale.US),
                is(NLS.bind(Messages.ConfiguredValueSet_caption, "/ ", "Attribute Label")));
    }

    @Test
    public void testGetLastResortCaption() {
        assertThat(configuredValueSet.getLastResortCaption(),
                is(NLS.bind(Messages.ConfiguredValueSet_caption, "", "attribute")));

        attribute.setValueSetType(ValueSetType.DERIVED);

        assertThat(configuredValueSet.getCaption(null),
                is(NLS.bind(Messages.ConfiguredValueSet_caption, "/ ", "attribute")));
    }

    @Test
    public void testConvertValueSetToEnumType_Range() {
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);

        IEnumValueSet enumValueSet = configuredValueSet.convertValueSetToEnumType();

        assertThat(configuredValueSet.getValueSet(), is(enumValueSet));
        assertThat(enumValueSet, is(empty()));
    }

    @Test
    public void testConvertValueSetToEnumType_Unrestricted() {
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);

        IEnumValueSet enumValueSet = configuredValueSet.convertValueSetToEnumType();

        assertThat(configuredValueSet.getValueSet(), is(enumValueSet));
        assertThat(enumValueSet, is(empty()));
    }

    @Test
    public void testConvertValueSetToEnumType_Unrestricted_ModelBoolean() {
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        attribute.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        attribute.setValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);

        IEnumValueSet enumValueSet = configuredValueSet.convertValueSetToEnumType();

        assertThat(configuredValueSet.getValueSet(), is(enumValueSet));
        assertThat(enumValueSet, is(not(empty())));
        assertThat(enumValueSet, contains("true", "false", null));
        assertThat(enumValueSet.isContainsNull(), is(true));
    }

    @Test
    public void testConvertValueSetToEnumType_Unrestricted_ModelPrimitiveBoolean() {
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        attribute.setDatatype(Datatype.PRIMITIVE_BOOLEAN.getQualifiedName());
        attribute.setValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);

        IEnumValueSet enumValueSet = configuredValueSet.convertValueSetToEnumType();

        assertThat(configuredValueSet.getValueSet(), is(enumValueSet));
        assertThat(enumValueSet, is(not(empty())));
        assertThat(enumValueSet, contains("true", "false"));
        assertThat(enumValueSet.isContainsNull(), is(false));
    }

    @Test
    public void testConvertValueSetToEnumType_Derived() {
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.changeValueSetType(ValueSetType.DERIVED);

        IEnumValueSet enumValueSet = configuredValueSet.convertValueSetToEnumType();

        assertThat(configuredValueSet.getValueSet(), is(enumValueSet));
        assertThat(enumValueSet, is(empty()));
    }

    @Test
    public void testConvertValueSetToEnumType_Derived_ModelEnum() {
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setValueSetType(ValueSetType.ENUM);
        ((IEnumValueSet)attribute.getValueSet()).addValues(Arrays.asList("1", "2"));
        configuredValueSet.changeValueSetType(ValueSetType.DERIVED);

        IEnumValueSet enumValueSet = configuredValueSet.convertValueSetToEnumType();

        assertThat(configuredValueSet.getValueSet(), is(enumValueSet));
        assertThat(enumValueSet, is(not(empty())));
        assertThat(enumValueSet, contains("1", "2"));
    }

    @Test
    public void testValidateEnumAgainstRange_WithInteger() {
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet modelRangeValueSet = (IRangeValueSet)attribute.getValueSet();
        modelRangeValueSet.setLowerBound("0");
        modelRangeValueSet.setUpperBound("100");

        configuredValueSet.setValueSetType(ValueSetType.ENUM);
        IEnumValueSet enumValueSet = (IEnumValueSet)configuredValueSet.getValueSet();

        // Values within range
        enumValueSet.addValue("0");
        enumValueSet.addValue("59");
        enumValueSet.addValue("100");
        MessageList list = configuredValueSet.validate(ipsProject);
        assertThat(list, isEmpty());

        // Value outside range
        enumValueSet.addValue("107");
        list = configuredValueSet.validate(ipsProject);
        assertThat(list, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUE_NOT_IN_RANGE));

        // Invalid number
        enumValueSet.addValue("IamNotNumber");
        list = configuredValueSet.validate(ipsProject);
        assertThat(list, hasMessageCode(IConfiguredValueSet.MSGCODE_INVALID_NUMBER_FORMAT));

        // Empty enum with non-null range
        configuredValueSet.setValueSetType(ValueSetType.ENUM);
        enumValueSet = (IEnumValueSet)configuredValueSet.getValueSet();
        modelRangeValueSet.setContainsNull(false);
        list = configuredValueSet.validate(ipsProject);
        assertThat(list, hasMessageCode(IConfiguredValueSet.MSGCODE_MANDATORY_VALUESET_IS_EMPTY));

        // Null value within non-null range
        enumValueSet.addValue(null);
        list = configuredValueSet.validate(ipsProject);
        assertThat(list, hasMessageCode(IConfiguredValueSet.MSGCODE_NULL_NOT_ALLOWED));

        // Null value with null-allowing range
        modelRangeValueSet.setContainsNull(true);
        list = configuredValueSet.validate(ipsProject);
        assertThat(list, isEmpty());
    }

    @Test
    public void testValidateEnumAgainstRange_WithDecimal() {
        attribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        attribute.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet modelRangeValueSet = (IRangeValueSet)attribute.getValueSet();
        modelRangeValueSet.setLowerBound("0.0");
        modelRangeValueSet.setUpperBound("100.0");

        configuredValueSet.setValueSetType(ValueSetType.ENUM);
        IEnumValueSet enumValueSet = (IEnumValueSet)configuredValueSet.getValueSet();

        // Values within range
        enumValueSet.addValue("0.0");
        enumValueSet.addValue("51.5");
        enumValueSet.addValue("100.0");
        MessageList list = configuredValueSet.validate(ipsProject);
        assertThat(list, isEmpty());

        // Value outside range
        enumValueSet.addValue("100.1");
        list = configuredValueSet.validate(ipsProject);
        assertThat(list, hasMessageCode(IConfiguredValueSet.MSGCODE_VALUE_NOT_IN_RANGE));

        // Invalid decimal
        enumValueSet.addValue("ImNotADecimal");
        list = configuredValueSet.validate(ipsProject);
        assertThat(list, hasMessageCode(IConfiguredValueSet.MSGCODE_INVALID_NUMBER_FORMAT));

        // Empty enum with non-null range
        configuredValueSet.setValueSetType(ValueSetType.ENUM);
        enumValueSet = (IEnumValueSet)configuredValueSet.getValueSet();
        modelRangeValueSet.setContainsNull(false);
        list = configuredValueSet.validate(ipsProject);
        assertThat(list, hasMessageCode(IConfiguredValueSet.MSGCODE_MANDATORY_VALUESET_IS_EMPTY));

        // Null value with non-null range
        enumValueSet.addValue(null);
        list = configuredValueSet.validate(ipsProject);
        assertThat(list, hasMessageCode(IConfiguredValueSet.MSGCODE_NULL_NOT_ALLOWED));

        // Null value with null-allowing range
        modelRangeValueSet.setContainsNull(true);
        list = configuredValueSet.validate(ipsProject);
        assertThat(list, isEmpty());
    }
}
