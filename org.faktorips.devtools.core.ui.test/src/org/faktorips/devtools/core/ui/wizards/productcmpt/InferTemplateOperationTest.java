/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Function;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.template.PropertyValueHistograms;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.util.Histogram;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InferTemplateOperationTest {

    private static final IValueHolder<?> SINGLE_VALUE = mock(SingleValueHolder.class);

    private static final IValueSet VALUE_SET = mock(IValueSet.class);

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

    @InjectMocks
    private InferTemplateOperation inferTemplateOperation;

    private List<IPropertyValue> attributeValues;

    private List<IPropertyValue> configElements;

    private List<IPropertyValue> templateTableUsages;

    private List<IPropertyValue> templateFormulas;

    private List<IPropertyValue> ruleConfigs;

    private List<IPropertyValue> propertyValues;

    @Before
    public void setUp() {
        when(productDirtySrcFile.isDirty()).thenReturn(true);
        when(templateGeneration.getIpsSrcFile()).thenReturn(templateSrcFile);
        attributeValues = mockPropertyValueInTemplates(IAttributeValue.class);
        propertyValues = mockHistograms(IAttributeValue.class, attributeValues);
        configElements = mockPropertyValueInTemplates(IConfigElement.class);
        propertyValues.addAll(mockHistograms(IConfigElement.class, configElements));
        templateTableUsages = mockPropertyValueInTemplates(ITableContentUsage.class);
        propertyValues.addAll(mockHistograms(ITableContentUsage.class, templateTableUsages));
        templateFormulas = mockPropertyValueInTemplates(IFormula.class);
        propertyValues.addAll(mockHistograms(IFormula.class, templateFormulas));
        ruleConfigs = mockPropertyValueInTemplates(IValidationRuleConfig.class);
        propertyValues.addAll(mockHistograms(IValidationRuleConfig.class, ruleConfigs));
    }

    private List<IPropertyValue> mockPropertyValueInTemplates(Class<? extends IPropertyValue> propValueClass) {
        List<IPropertyValue> propertyValues = mockPropertyValues(propValueClass, templateSrcFile);
        doReturn(propertyValues).when(templateGeneration).getPropertyValuesIncludingProductCmpt(propValueClass);
        return propertyValues;
    }

    private List<IPropertyValue> mockPropertyValues(Class<? extends IPropertyValue> propValueClass, IIpsSrcFile srcFile) {
        List<IPropertyValue> propertyValues = new ArrayList<IPropertyValue>();
        for (int i = 0; i < 3; i++) {
            String name = propValueClass.getSimpleName() + i;
            IPropertyValue propertyValue = mock(IPropertyValue.class, withSettings().extraInterfaces(propValueClass)
                    .name(name));
            when(propertyValue.getPropertyName()).thenReturn(name);
            when(propertyValue.getIpsSrcFile()).thenReturn(srcFile);
            propertyValues.add(propertyValue);
        }
        return propertyValues;
    }

    private List<IPropertyValue> mockHistograms(Class<? extends IPropertyValue> propValueClass,
            List<IPropertyValue> templateValues) {
        // values for first product
        List<IPropertyValue> mockPropertyValues = mockPropertyValues(propValueClass, productSrcFile);
        // values for second product
        mockPropertyValues.addAll(mockPropertyValues(propValueClass, productDirtySrcFile));
        for (IPropertyValue templateValue : templateValues) {
            Function<IPropertyValue, Object> elementToValueFunction = getValueFunction(propValueClass);
            Histogram<Object, IPropertyValue> histogram = new Histogram<Object, IPropertyValue>(elementToValueFunction,
                    histrogramValues(mockPropertyValues, templateValue.getPropertyName()));
            when(histograms.get(templateValue.getPropertyName())).thenReturn(histogram);
        }
        return mockPropertyValues;
    }

    private List<IPropertyValue> histrogramValues(List<IPropertyValue> mockPropertyValues, String propertyName) {
        ArrayList<IPropertyValue> result = new ArrayList<IPropertyValue>();
        for (IPropertyValue propertyValue : mockPropertyValues) {
            if (propertyValue.getPropertyName().equals(propertyName)) {
                result.add(propertyValue);
            }
        }
        return result;
    }

    @Test
    public void testUpdatePropertyValues_SrcFileSaved() throws Exception {
        inferTemplateOperation.updatePropertyValues();

        verify(templateSrcFile).save(anyBoolean(), any(IProgressMonitor.class));
        verify(productSrcFile).save(anyBoolean(), any(IProgressMonitor.class));
        verify(productDirtySrcFile, atLeastOnce()).isDirty();
        verifyNoMoreInteractions(productDirtySrcFile);
    }

    @Test
    public void testUpdatePropertyValues_TemplateValueUpdate() throws Exception {
        inferTemplateOperation.updatePropertyValues();

        verify((IAttributeValue)attributeValues.get(0)).setValueHolder(SINGLE_VALUE);
        verify(attributeValues.get(1)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        verify(attributeValues.get(2)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        verify((IConfigElement)configElements.get(0)).setValueSet(VALUE_SET);
        verify(configElements.get(1)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        verify(configElements.get(2)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        verify((ITableContentUsage)templateTableUsages.get(0)).setTableContentName("ITableContentUsage0Value");
        verify(templateTableUsages.get(1)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        verify(templateTableUsages.get(2)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        verify((IFormula)templateFormulas.get(0)).setExpression("IFormula0Value");
        verify(templateFormulas.get(1)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        verify(templateFormulas.get(2)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        verify((IValidationRuleConfig)ruleConfigs.get(0)).setActive(true);
        verify(ruleConfigs.get(1)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        verify(ruleConfigs.get(2)).setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

    }

    @Test
    public void testUpdatePropertyValues_InheritedUpdate() throws Exception {
        inferTemplateOperation.updatePropertyValues();

        for (IPropertyValue propertyValue : propertyValues) {
            if (propertyValue.getPropertyName().endsWith("0")) {
                verify(propertyValue).setTemplateValueStatus(TemplateValueStatus.INHERITED);
            } else {
                verify(propertyValue, atLeastOnce()).getPropertyName();
                verifyZeroInteractions(propertyValue);
            }
        }
    }

    private Function<IPropertyValue, Object> getValueFunction(Class<? extends IPropertyValue> type) {
        if (type.equals(IAttributeValue.class)) {
            return new Function<IPropertyValue, Object>() {

                @Override
                public Object apply(IPropertyValue propertyValue) {
                    String propertyName = propertyValue.getPropertyName();
                    // all first properties have the same value
                    // all other properties have unique values
                    if (propertyName.endsWith("0")) {
                        return SINGLE_VALUE;
                    } else {
                        return mock(SingleValueHolder.class);
                    }
                }
            };
        } else if (type.equals(IConfigElement.class)) {
            return new Function<IPropertyValue, Object>() {

                @Override
                public Object apply(IPropertyValue propertyValue) {
                    String propertyName = propertyValue.getPropertyName();
                    // all first properties have the same value
                    // all other properties have unique values
                    if (propertyName.endsWith("0")) {
                        return VALUE_SET;
                    } else {
                        return mock(IValueSet.class);
                    }
                }
            };

        } else if (type.equals(IValidationRuleConfig.class)) {
            return new Function<IPropertyValue, Object>() {

                @Override
                public Object apply(IPropertyValue propertyValue) {
                    String propertyName = propertyValue.getPropertyName();
                    // all first properties have the same value
                    // all other properties have unique values
                    if (propertyName.endsWith("0")) {
                        return true;
                    } else {
                        return null;
                    }
                }
            };

        } else {
            return new Function<IPropertyValue, Object>() {

                @Override
                public Object apply(IPropertyValue propertyValue) {
                    String propertyName = propertyValue.getPropertyName();
                    // all first properties have the same value
                    // all other properties have unique values
                    if (propertyName.endsWith("0")) {
                        return propertyName + "Value";
                    } else {
                        return UUID.randomUUID().toString();
                    }
                }
            };

        }
    }

}
