/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.template.PropertyValueHistograms;
import org.faktorips.devtools.model.internal.util.Histogram;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValue.PropertyValueIdentifier;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.values.Decimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InferTemplateProcessorMockTest {

    private static final String TEMPLATE_NAME = "templateName";

    @Mock
    private IValueHolder<?> singleValue;

    @Mock
    private IValueHolder<?> singleValueCopy;

    @Mock
    private IValueSet valueSet;

    @Mock
    private IValueSet valueSetCopy;

    private String defaultValue = "10";

    @Mock
    private PropertyValueHistograms histograms;

    @Mock
    private IProgressMonitor monitor;

    @Mock
    private IProductCmptGeneration templateGeneration;

    @Mock
    private IIpsSrcFile templateSrcFile;

    @Mock
    private IIpsSrcFile productSrcFile;

    @Mock
    private IIpsSrcFile productDirtySrcFile;

    @Mock
    private IProductCmpt templateProduct;

    @Mock
    private IProductCmpt productCmpt1;

    @Mock
    private IProductCmpt productCmpt2;

    @Mock
    private IProductCmptGeneration gen1;

    @Mock
    private IProductCmptGeneration gen2;

    @Mock
    IIpsProject ipsProject;

    @Mock
    IIpsProjectProperties ipsProjectProperties;

    private List<IProductCmpt> productCmpts;

    private InferTemplateProcessor inferTemplateProcessor;

    private List<IPropertyValue> attributeValues;

    private List<IPropertyValue> configuredDefaults;

    private List<IPropertyValue> configuredValueSets;

    private List<IPropertyValue> templateTableUsages;

    private List<IPropertyValue> templateFormulas;

    private List<IPropertyValue> ruleConfigs;

    private List<IPropertyValue> propertyValues;

    @Before
    @SuppressWarnings("deprecation")
    public void setUp() {

        when(templateGeneration.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.getProperties()).thenReturn(ipsProjectProperties);
        when(ipsProjectProperties.getInferredTemplateLinkThreshold()).thenReturn(Decimal.valueOf(1));
        when(ipsProjectProperties.getInferredTemplatePropertyValueThreshold()).thenReturn(Decimal.valueOf(8, 1));

        when(productCmpt1.getLatestProductCmptGeneration()).thenReturn(gen1);
        when(productCmpt2.getLatestProductCmptGeneration()).thenReturn(gen2);

        productCmpts = Arrays.asList(productCmpt1, productCmpt2);
        when(productCmpt1.getIpsSrcFile()).thenReturn(productSrcFile);
        inferTemplateProcessor = new InferTemplateProcessor(templateGeneration, productCmpts, histograms);

        when(templateGeneration.getProductCmpt()).thenReturn(templateProduct);
        when(templateProduct.getQualifiedName()).thenReturn(TEMPLATE_NAME);

        when(productCmpt2.getIpsSrcFile()).thenReturn(productDirtySrcFile);
        when(productDirtySrcFile.isDirty()).thenReturn(true);
        when(templateGeneration.getIpsSrcFile()).thenReturn(templateSrcFile);

        attributeValues = mockPropertyValueInTemplates(IAttributeValue.class);
        propertyValues = mockHistograms(IAttributeValue.class, attributeValues);

        configuredDefaults = mockPropertyValueInTemplates(IConfiguredDefault.class);
        propertyValues.addAll(mockHistograms(IConfiguredDefault.class, configuredDefaults));

        configuredValueSets = mockPropertyValueInTemplates(IConfiguredValueSet.class);
        setUpConfiguredValueSets();
        propertyValues.addAll(mockHistograms(IConfiguredValueSet.class, configuredValueSets));

        templateTableUsages = mockPropertyValueInTemplates(ITableContentUsage.class);
        propertyValues.addAll(mockHistograms(ITableContentUsage.class, templateTableUsages));

        templateFormulas = mockPropertyValueInTemplates(IFormula.class);
        propertyValues.addAll(mockHistograms(IFormula.class, templateFormulas));

        ruleConfigs = mockPropertyValueInTemplates(IValidationRuleConfig.class);
        propertyValues.addAll(mockHistograms(IValidationRuleConfig.class, ruleConfigs));

        doReturn(singleValueCopy).when(singleValue).copy(any(IAttributeValue.class));
    }

    private void setUpConfiguredValueSets() {
        IIpsModel ipsModel = mock(IIpsModel.class);
        for (IPropertyValue propertyValue : configuredValueSets) {
            when(ipsModel.getNextPartId(propertyValue)).thenReturn(UUID.randomUUID().toString());
            when(propertyValue.getIpsModel()).thenReturn(ipsModel);
            when(valueSet.copy(eq((IConfiguredValueSet)propertyValue), anyString())).thenReturn(valueSetCopy);
        }
    }

    private List<IPropertyValue> mockPropertyValueInTemplates(Class<? extends IPropertyValue> propValueClass) {
        List<IPropertyValue> propertyValues = mockPropertyValues(templateSrcFile, propValueClass);
        doReturn(propertyValues).when(templateGeneration).getPropertyValuesIncludingProductCmpt(propValueClass);
        return propertyValues;
    }

    private List<IPropertyValue> mockPropertyValues(IIpsSrcFile srcFile,
            Class<? extends IPropertyValue> propValueClass) {
        List<IPropertyValue> propertyValues = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String name = propValueClass.getSimpleName() + i;
            IPropertyValue propertyValue = mock(IPropertyValue.class,
                    withSettings().extraInterfaces(propValueClass).name(name));
            when(propertyValue.getPropertyName()).thenReturn(name);
            when(propertyValue.getIdentifier()).thenReturn(
                    new PropertyValueIdentifier(name, PropertyValueType.getTypeForValueClass(propValueClass)));
            when(propertyValue.getIpsSrcFile()).thenReturn(srcFile);
            propertyValues.add(propertyValue);
        }
        return propertyValues;
    }

    private List<IPropertyValue> mockHistograms(Class<? extends IPropertyValue> propValueClass,
            List<IPropertyValue> templateValues) {
        // values for first product
        List<IPropertyValue> mockPropertyValues = mockPropertyValues(productSrcFile, propValueClass);
        // values for second product
        mockPropertyValues.addAll(mockPropertyValues(productDirtySrcFile, propValueClass));
        for (IPropertyValue templateValue : templateValues) {
            Function<IPropertyValue, Object> elementToValueFunction = getValueFunction(propValueClass);
            Histogram<Object, IPropertyValue> histogram = new Histogram<>(elementToValueFunction,
                    histrogramValues(mockPropertyValues, templateValue.getPropertyName()));
            when(histograms.get(templateValue.getIdentifier())).thenReturn(histogram);
        }
        return mockPropertyValues;
    }

    private List<IPropertyValue> histrogramValues(List<IPropertyValue> mockPropertyValues, String propertyName) {
        ArrayList<IPropertyValue> result = new ArrayList<>();
        for (IPropertyValue propertyValue : mockPropertyValues) {
            if (propertyValue.getPropertyName().equals(propertyName)) {
                result.add(propertyValue);
            }
        }
        return result;
    }

    @Test
    public void testInferTemplate_SrcFileSaved() throws Exception {
        inferTemplateProcessor.run(monitor);

        verify(templateSrcFile).save(anyBoolean(), any(IProgressMonitor.class));
        verify(productSrcFile).save(anyBoolean(), any(IProgressMonitor.class));
        verify(productDirtySrcFile, atLeastOnce()).isDirty();
        verifyNoMoreInteractions(productDirtySrcFile);
    }

    @Test
    public void testInferTemplate_TemplateValueUpdate() throws Exception {
        inferTemplateProcessor.run(monitor);

        verify((IAttributeValue)attributeValues.get(0)).setValueHolder(singleValueCopy);
        verify(attributeValues.get(1)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        verify(attributeValues.get(2)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        verify((IConfiguredDefault)configuredDefaults.get(0)).setValue(defaultValue);
        verify(configuredDefaults.get(1)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        verify(configuredDefaults.get(2)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        verify((IConfiguredValueSet)configuredValueSets.get(0)).setValueSet(valueSetCopy);
        verify(configuredValueSets.get(1)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        verify(configuredValueSets.get(2)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        verify((ITableContentUsage)templateTableUsages.get(0)).setTableContentName("ITableContentUsage0Value");
        verify(templateTableUsages.get(1)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        verify(templateTableUsages.get(2)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        verify((IFormula)templateFormulas.get(0)).setExpression("IFormula0Value");
        verify(templateFormulas.get(1)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        verify(templateFormulas.get(2)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        /*
         * VRules are either activated or deactivated, there is no null-case.
         */
        verify((IValidationRuleConfig)ruleConfigs.get(0)).setActive(true);
        verify((IValidationRuleConfig)ruleConfigs.get(1)).setActive(false);
        verify((IValidationRuleConfig)ruleConfigs.get(2)).setActive(false);
    }

    @Test
    public void testInferTemplate_InheritedUpdate() throws Exception {
        inferTemplateProcessor.run(monitor);

        for (IPropertyValue propertyValue : propertyValues) {
            if (propertyValue.getPropertyName().endsWith("0") || propertyValue instanceof IValidationRuleConfig) {
                verify(propertyValue).setTemplateValueStatus(TemplateValueStatus.INHERITED);
            } else {
                verify(propertyValue, atLeastOnce()).getPropertyName();
                verifyNoMoreInteractions(propertyValue);
            }
        }
    }

    @Test
    public void testInferTemplate_UpdateProductCmpts() throws Exception {
        inferTemplateProcessor.run(monitor);

        verify(productCmpt1).setTemplate(TEMPLATE_NAME);
        verify(productCmpt2).setTemplate(TEMPLATE_NAME);
    }

    @SuppressWarnings("unchecked")
    private Function<IPropertyValue, Object> getValueFunction(Class<? extends IPropertyValue> type) {
        if (type.equals(IAttributeValue.class)) {
            return propertyValue -> {
                String propertyName = propertyValue.getPropertyName();
                // all first properties have the same value
                // all other properties have unique values
                if (propertyName.endsWith("0")) {
                    return singleValue;
                } else {
                    SingleValueHolder valueMock = mock(SingleValueHolder.class);
                    when(valueMock.compareTo(any(IValueHolder.class))).thenReturn(1);
                    return valueMock;
                }
            };
        } else if (type.equals(IConfiguredValueSet.class)) {
            return propertyValue -> {
                String propertyName = propertyValue.getPropertyName();
                // all first properties have the same value
                // all other properties have unique values
                if (propertyName.endsWith("0")) {
                    return valueSet;
                } else {
                    IValueSet mockValueSet = mock(IValueSet.class);
                    when(mockValueSet.compareTo(mockValueSet)).thenReturn(0);
                    when(mockValueSet.compareTo(any(IValueSet.class))).thenReturn(-1);
                    return mockValueSet;
                }
            };
        } else if (type.equals(IConfiguredDefault.class))

        {
            return propertyValue -> {
                String propertyName = propertyValue.getPropertyName();
                // all first properties have the same value
                // all other properties have unique values
                if (propertyName.endsWith("0")) {
                    return defaultValue;
                } else {
                    return UUID.randomUUID().toString();
                }
            };

        } else if (type.equals(IValidationRuleConfig.class))

        {
            return propertyValue -> {
                String propertyName = propertyValue.getPropertyName();
                // all first properties have the same value
                // all other properties have unique values
                if (propertyName.endsWith("0")) {
                    return true;
                } else {
                    return false;
                }
            };

        } else

        {
            return propertyValue -> {
                String propertyName = propertyValue.getPropertyName();
                // all first properties have the same value
                // all other properties have unique values
                if (propertyName.endsWith("0")) {
                    return propertyName + "Value";
                } else {
                    return UUID.randomUUID().toString();
                }
            };
        }
    }
}
