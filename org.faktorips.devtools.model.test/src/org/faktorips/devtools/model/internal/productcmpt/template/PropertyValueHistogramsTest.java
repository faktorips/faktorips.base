/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.template;

import static org.faktorips.devtools.model.productcmpt.PropertyValueType.ATTRIBUTE_VALUE;
import static org.faktorips.devtools.model.productcmpt.PropertyValueType.CONFIGURED_DEFAULT;
import static org.faktorips.devtools.model.productcmpt.PropertyValueType.CONFIGURED_VALUESET;
import static org.faktorips.devtools.model.productcmpt.PropertyValueType.FORMULA;
import static org.faktorips.devtools.model.productcmpt.PropertyValueType.TABLE_CONTENT_USAGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.util.Histogram;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValue.PropertyValueIdentifier;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.values.Decimal;
import org.junit.Before;
import org.junit.Test;

public class PropertyValueHistogramsTest extends AbstractIpsPluginTest {

    private static final List<PropertyValueIdentifier> CMPT_PROPERTIES = Arrays.asList(p("prodAttr", ATTRIBUTE_VALUE),
            p("polAttr", CONFIGURED_DEFAULT), p("polAttr", CONFIGURED_VALUESET), p("form", FORMULA),
            p("tab", TABLE_CONTENT_USAGE));
    private static final List<PropertyValueIdentifier> GEN_PROPERTIES = Arrays.asList(p("prodAttr2", ATTRIBUTE_VALUE),
            p("polAttr2", CONFIGURED_DEFAULT), p("polAttr2", CONFIGURED_VALUESET), p("form2", FORMULA),
            p("tab2", TABLE_CONTENT_USAGE));
    private IIpsProject ipsProject;
    private PolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmptTypeAttribute productCmptTypeAttribute;
    private IPolicyCmptTypeAttribute policyCmptTypeAttribute;
    private ITableStructureUsage tableStructureUsage;
    private IProductCmptTypeMethod formulaSignature;
    private IProductCmptTypeAttribute productCmptTypeAttribute2;
    private IPolicyCmptTypeAttribute policyCmptTypeAttribute2;
    private ITableStructureUsage tableStructureUsage2;
    private IProductCmptTypeMethod formulaSignature2;

    private static PropertyValueIdentifier p(String name, PropertyValueType type) {
        return new PropertyValueIdentifier(name, type);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "V", "P");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmptTypeAttribute = productCmptType.newProductCmptTypeAttribute("prodAttr");
        productCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute("polAttr");
        policyCmptTypeAttribute.setValueSetConfiguredByProduct(true);
        policyCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        tableStructureUsage = productCmptType.newTableStructureUsage();
        tableStructureUsage.setRoleName("tab");
        formulaSignature = productCmptType.newFormulaSignature("form");
    }

    @Test
    public void testCreateForSingleValue() throws Exception {
        List<IProductCmpt> list = Arrays.asList(createProductCmpt("P"));

        PropertyValueHistograms valueHistograms = PropertyValueHistograms.createFor(list);

        for (PropertyValueIdentifier identifier : CMPT_PROPERTIES) {
            checkDistributions(valueHistograms, identifier, 1, Decimal.valueOf(1), list.get(0));
        }
    }

    private void checkDistributions(PropertyValueHistograms valueHistograms,
            PropertyValueIdentifier identifier,
            int expectedAbsoluteDistribution,
            Decimal expectedRelativeDistribution,
            IPropertyValueContainer propertyValueContainer) {
        Histogram<Object, IPropertyValue> histogram = valueHistograms.get(identifier);
        String propertyName = identifier.getPropertyName();
        IPropertyValue propertyValue = identifier.getValueFrom(propertyValueContainer);
        Object value = identifier.getType().getValueGetter().apply(propertyValue);
        assertThat("absolute distribution for " + propertyName, histogram.getAbsoluteDistribution().get(value),
                is(expectedAbsoluteDistribution));
        assertThat("relative distribution for " + propertyName, histogram.getRelativeDistribution().get(value),
                is(expectedRelativeDistribution));
    }

    @Test
    public void testCreateFor() throws Exception {
        List<IProductCmpt> list = Arrays.asList(createProductCmpt("P1"), createProductCmpt("P2"),
                createProductCmpt("P3"));

        PropertyValueHistograms valueHistograms = PropertyValueHistograms.createFor(list);

        for (PropertyValueIdentifier identifier : CMPT_PROPERTIES) {
            checkDistributions(valueHistograms, identifier, 3, Decimal.valueOf(1), list.get(0));
        }
    }

    @Test
    public void testCreateFor_Generation() throws Exception {
        productCmptTypeAttribute2 = productCmptType.newProductCmptTypeAttribute("prodAttr2");
        productCmptTypeAttribute2.setDatatype(Datatype.STRING.getQualifiedName());
        productCmptTypeAttribute2.setChangingOverTime(true);
        policyCmptTypeAttribute2 = policyCmptType.newPolicyCmptTypeAttribute("polAttr2");
        policyCmptTypeAttribute2.setValueSetConfiguredByProduct(true);
        policyCmptTypeAttribute2.setDatatype(Datatype.STRING.getQualifiedName());
        policyCmptTypeAttribute2.setChangingOverTime(true);
        tableStructureUsage2 = productCmptType.newTableStructureUsage();
        tableStructureUsage2.setRoleName("tab2");
        tableStructureUsage2.setChangingOverTime(true);
        formulaSignature2 = productCmptType.newFormulaSignature("form2");
        formulaSignature2.setChangingOverTime(true);
        List<IProductCmpt> list = Arrays.asList(createProductCmptWithGenerationProperties("PG"));

        PropertyValueHistograms valueHistograms = PropertyValueHistograms.createFor(list);

        for (PropertyValueIdentifier identifier : CMPT_PROPERTIES) {
            checkDistributions(valueHistograms, identifier, 1, Decimal.valueOf(1), list.get(0));
        }
        for (PropertyValueIdentifier identifier : GEN_PROPERTIES) {
            checkDistributions(valueHistograms, identifier, 1, Decimal.valueOf(1), list.get(0)
                    .getLatestProductCmptGeneration());
        }
    }

    private IProductCmpt createProductCmpt(String name) throws CoreRuntimeException {
        IProductCmpt productCmpt = newProductCmpt(productCmptType, name);
        IAttributeValue attributeValue = productCmpt.newPropertyValue(productCmptTypeAttribute, IAttributeValue.class);
        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, "polAttrValue"));
        IConfiguredDefault configuredDefault = productCmpt.newPropertyValue(policyCmptTypeAttribute,
                IConfiguredDefault.class);
        configuredDefault.setValue("prodAttrDefaultValue");
        IConfiguredValueSet configuredValueSet = productCmpt.newPropertyValue(policyCmptTypeAttribute,
                IConfiguredValueSet.class);
        configuredValueSet.setValueSet(new UnrestrictedValueSet(configuredValueSet, UUID.randomUUID().toString()));
        IFormula formula = productCmpt.newPropertyValue(formulaSignature, IFormula.class);
        formula.setExpression("formulaExpression");
        ITableContentUsage tableContentUsage = productCmpt.newPropertyValue(tableStructureUsage,
                ITableContentUsage.class);
        tableContentUsage.setTableContentName("tableContentName");
        return productCmpt;
    }

    private IProductCmpt createProductCmptWithGenerationProperties(String name) throws CoreRuntimeException {
        IProductCmpt productCmpt = createProductCmpt(name);
        IProductCmptGeneration productCmptGeneration = productCmpt.getLatestProductCmptGeneration();
        IAttributeValue attributeValue = productCmptGeneration.newPropertyValue(productCmptTypeAttribute2,
                IAttributeValue.class);
        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, "polAttrValue"));
        IConfiguredDefault configuredDefault = productCmptGeneration.newPropertyValue(policyCmptTypeAttribute2,
                IConfiguredDefault.class);
        configuredDefault.setValue("prodAttrDefaultValue");
        IConfiguredValueSet configuredValueSet = productCmptGeneration.newPropertyValue(policyCmptTypeAttribute2,
                IConfiguredValueSet.class);
        configuredValueSet.setValueSet(new UnrestrictedValueSet(configuredValueSet, UUID.randomUUID().toString()));
        IFormula formula = productCmptGeneration.newPropertyValue(formulaSignature2, IFormula.class);
        formula.setExpression("formulaExpression");
        ITableContentUsage tableContentUsage = productCmptGeneration.newPropertyValue(tableStructureUsage2,
                ITableContentUsage.class);
        tableContentUsage.setTableContentName("tableContentName");
        return productCmpt;
    }
}
