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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import com.google.common.base.Preconditions;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
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
import org.faktorips.values.Decimal;

public class InferTemplateOperation {

    private static final Decimal RELATIVE_THRESHOLD = Decimal.valueOf(1);

    private static final int SAVE_MONITOR_PART = 10;

    /**
     * The histograms of the values based on some product components. The string key is the name of
     * the property.
     */
    private final PropertyValueHistograms histograms;

    private IProductCmptGeneration templateGeneration;

    private final IProgressMonitor monitor;

    private final Set<IIpsSrcFile> srcFilesToSave = new HashSet<IIpsSrcFile>();

    public InferTemplateOperation(IProductCmptGeneration templateGeneration, PropertyValueHistograms histograms,
            IProgressMonitor monitor) {
        this.templateGeneration = templateGeneration;
        this.histograms = histograms;
        if (monitor == null) {
            this.monitor = new NullProgressMonitor();
        } else {
            this.monitor = monitor;
        }
    }

    /**
     * Sets the values in the given template generation (and the corresponding template/product
     * generation) for which a histogram is found and for which the value in that histogram has a
     * relative distribution of at least {@link #RELATIVE_THRESHOLD}.
     */
    public void updatePropertyValues() {
        monitor.beginTask(Messages.InferTemplateOperation_progress_inferringTemplate, histograms.size()
                + SAVE_MONITOR_PART);
        updatePropertyValues(IAttributeValue.class, IValueHolder.class, attributeValueHolderSetter());
        updatePropertyValues(ITableContentUsage.class, String.class, tableContentUsageSetter());
        updatePropertyValues(IFormula.class, String.class, formulaExpressionSetter());
        updatePropertyValues(IValidationRuleConfig.class, Boolean.class, validationRuleConfigActiveSetter());
        updatePropertyValues(IConfigElement.class, IValueSet.class, configElementValueSetSetter());
        save();
        monitor.done();
    }

    private void save() {
        SubProgressMonitor saveMonitor = new SubProgressMonitor(monitor, SAVE_MONITOR_PART);
        saveMonitor.beginTask(Messages.InferTemplateOperation_progress_save, srcFilesToSave.size() + 1);
        try {
            templateGeneration.getIpsSrcFile().save(false, monitor);
            for (IIpsSrcFile ipsSrcFile : srcFilesToSave) {
                ipsSrcFile.save(false, new SubProgressMonitor(monitor, 1));
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Sets the values of the given class to the property values of the given class in the given
     * template generation (and the corresponding product component). Only property values for which
     * a histogram is found and for which the value in that histogram has a relative distribution of
     * at least {@link #RELATIVE_THRESHOLD} are set. The value of the property value is set with the
     * given setter.
     */
    private <P extends IPropertyValue, V> void updatePropertyValues(Class<P> propertyValueClass,
            Class<V> valueClass,
            Setter<P, V> setter) {
        List<P> propertyValues = templateGeneration.getPropertyValuesIncludingProductCmpt(propertyValueClass);
        for (P propertyValue : propertyValues) {
            Object bestValue = getBestValue(propertyValue.getPropertyName());
            if (bestValue == null) {
                propertyValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
            } else {
                Preconditions.checkState(valueClass.isInstance(bestValue));
                V value = valueClass.cast(bestValue);
                setter.set(value, propertyValue);
                updateOriginPropertyValues(propertyValue.getPropertyName(), value);
            }
            monitor.worked(1);
        }
    }

    /**
     * Returns the value from the histograms with the given name if it has a relative distribution
     * of at least {@link #RELATIVE_THRESHOLD}. If there is no histogram for the given name or the
     * best value in the histogram does not occur often enough {@code null} is returned.
     */
    private Object getBestValue(String name) {
        Histogram<Object, IPropertyValue> histogram = histograms.get(name);
        if (histogram == null || histogram.isEmtpy()) {
            return null;
        }
        SortedMap<Object, Decimal> relativeDistribution = histogram.getRelativeDistribution();
        Object candidateValue = relativeDistribution.firstKey();
        if (relativeDistribution.get(candidateValue).greaterThanOrEqual(RELATIVE_THRESHOLD)) {
            return candidateValue;
        }
        return null;
    }

    private void updateOriginPropertyValues(String propertyName, Object value) {
        Histogram<Object, IPropertyValue> histogram = histograms.get(propertyName);
        Set<IPropertyValue> elements = histogram.getElements(value);
        for (IPropertyValue propertyValue : elements) {
            addSrcFileToSave(propertyValue.getIpsSrcFile());
            propertyValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        }
    }

    private void addSrcFileToSave(IIpsSrcFile ipsSrcFile) {
        if (!ipsSrcFile.isDirty()) {
            srcFilesToSave.add(ipsSrcFile);
        }
    }

    /** Setter to set a config element's value set. */
    private Setter<IConfigElement, IValueSet> configElementValueSetSetter() {
        return new Setter<IConfigElement, IValueSet>() {

            @Override
            public void set(IValueSet valueSet, IConfigElement c) {
                c.setValueSet(valueSet);
            }

        };
    }

    /** Setter to set a formulas expression. */
    private Setter<IFormula, String> formulaExpressionSetter() {
        return new Setter<IFormula, String>() {
            @Override
            public void set(String expression, IFormula f) {
                f.setExpression(expression);
            }

        };
    }

    /** Setter to set an attribute value's value holder. */
    @SuppressWarnings("rawtypes")
    private Setter<IAttributeValue, IValueHolder> attributeValueHolderSetter() {
        return new Setter<IAttributeValue, IValueHolder>() {
            @Override
            public void set(IValueHolder valueHolder, IAttributeValue a) {
                a.setValueHolder(valueHolder);
            }
        };
    }

    /** Setter to set a validation rule configs active flag. */
    private Setter<IValidationRuleConfig, Boolean> validationRuleConfigActiveSetter() {
        return new Setter<IValidationRuleConfig, Boolean>() {
            @Override
            public void set(Boolean active, IValidationRuleConfig v) {
                v.setActive(BooleanUtils.isTrue(active));
            }
        };
    }

    /** Setter to set an table content usages table content name. */
    private Setter<ITableContentUsage, String> tableContentUsageSetter() {
        return new Setter<ITableContentUsage, String>() {

            @Override
            public void set(String tableContentName, ITableContentUsage table) {
                table.setTableContentName(tableContentName);
            }

        };
    }

    /**
     * Interface for objects used to set a value of type T in an object of type P.
     * 
     * @param <P> the type of object to set a value in
     * @param <T> the type of value to set
     */
    private static interface Setter<P, T> {
        void set(T value, P object);
    }
}
