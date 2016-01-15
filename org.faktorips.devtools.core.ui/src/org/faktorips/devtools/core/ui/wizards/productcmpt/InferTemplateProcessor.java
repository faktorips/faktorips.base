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

import com.google.common.base.Preconditions;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.productcmpt.template.PropertyValueHistograms;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.util.Histogram;
import org.faktorips.devtools.core.util.Histogram.BestValue;
import org.faktorips.values.Decimal;

public class InferTemplateProcessor implements IWorkspaceRunnable {

    private static final Decimal RELATIVE_THRESHOLD = Decimal.valueOf(0.8);

    /**
     * The histograms of the values based on some product components. The string key is the name of
     * the property.
     */
    private final PropertyValueHistograms histograms;

    private IProductCmptGeneration templateGeneration;

    private final Set<IIpsSrcFile> srcFilesToSave = new HashSet<IIpsSrcFile>();

    private final List<IProductCmpt> productCmpts;

    private IProgressMonitor monitor;

    public InferTemplateProcessor(IProductCmptGeneration templateGeneration, List<IProductCmpt> productCmpts,
            PropertyValueHistograms histograms) {
        this.templateGeneration = templateGeneration;
        this.productCmpts = productCmpts;
        this.histograms = histograms;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        if (monitor == null) {
            this.monitor = new NullProgressMonitor();
        } else {
            this.monitor = monitor;
        }
        inferTemplate();
    }

    /**
     * Sets the values in the given template generation (and the corresponding template/product
     * generation) for which a histogram is found and for which the value in that histogram has a
     * relative distribution of at least {@link #RELATIVE_THRESHOLD}.
     */
    private void inferTemplate() {
        monitor.beginTask(Messages.InferTemplateOperation_progress_inferringTemplate,
                histograms.size() + (productCmpts.size() * 2));
        try {
            srcFileChanged(templateGeneration.getIpsSrcFile());
            updateProductCmpts();
            updatePropertyValues(IAttributeValue.class, IValueHolder.class, attributeValueHolderSetter());
            updatePropertyValues(ITableContentUsage.class, String.class, tableContentUsageSetter());
            updatePropertyValues(IFormula.class, String.class, formulaExpressionSetter());
            updatePropertyValues(IValidationRuleConfig.class, Boolean.class, validationRuleConfigActiveSetter());
            updatePropertyValues(IConfigElement.class, IValueSet.class, configElementValueSetSetter());
            save();
        } finally {
            monitor.done();
        }
    }

    protected IpsModel getIpsModel() {
        IIpsModel ipsModel = templateGeneration.getIpsModel();
        if (ipsModel instanceof IpsModel) {
            return (IpsModel)ipsModel;
        } else {
            return null;
        }
    }

    private void updateProductCmpts() {
        for (IProductCmpt productCmpt : productCmpts) {
            srcFileChanged(productCmpt.getIpsSrcFile());
            productCmpt.setTemplate(getTemplateName());
            monitor.worked(1);
        }
    }

    private String getTemplateName() {
        return templateGeneration.getProductCmpt().getQualifiedName();
    }

    private void save() {
        SubProgressMonitor saveMonitor = new SubProgressMonitor(monitor, productCmpts.size());
        saveMonitor.beginTask(Messages.InferTemplateOperation_progress_save, srcFilesToSave.size() + 1);
        try {
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
            BestValue<?> bestValue = getBestValueFor(propertyValue);
            if (bestValue.isPresent()) {
                V value = getValueAndCastTo(bestValue, valueClass);
                setter.set(value, propertyValue);
                updateOriginPropertyValues(propertyValue.getPropertyName(), value);
            } else {
                propertyValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
            }
            monitor.worked(1);
        }
    }

    /**
     * @param bestValue the best value to retrieve the value from
     * @param valueClass the value class to cast the value to
     * @return the casted value. May be <code>null</code>.
     * @throws IllegalStateException if the value is non-null and not instance of the value class.
     */
    public <T> T getValueAndCastTo(BestValue<?> bestValue, Class<T> valueClass) {
        Object rawValue = bestValue.getValue();
        Preconditions.checkState(rawValue == null || valueClass.isInstance(rawValue));
        T castedValue = valueClass.cast(rawValue);
        return castedValue;
    }

    /**
     * Returns the best value from the histograms with the given name. Never returns
     * <code>null</code>. Instead returns a {@link BestValue} object representing a missing value
     * instead.
     */
    private <P extends IPropertyValue> BestValue<Object> getBestValueFor(P propertyValue) {
        Histogram<Object, IPropertyValue> histogram = histograms.get(propertyValue.getPropertyName());
        BestValue<Object> bestValue = histogram.getBestValueExceeding(RELATIVE_THRESHOLD);
        return bestValue;
    }

    private void updateOriginPropertyValues(String propertyName, Object value) {
        Histogram<Object, IPropertyValue> histogram = histograms.get(propertyName);
        Set<IPropertyValue> elements = histogram.getElements(value);
        for (IPropertyValue propertyValue : elements) {
            srcFileChanged(propertyValue.getIpsSrcFile());
            propertyValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        }
    }

    private void srcFileChanged(IIpsSrcFile ipsSrcFile) {
        if (!ipsSrcFile.isDirty()) {
            srcFilesToSave.add(ipsSrcFile);
        }
    }

    /** Setter to set a config element's value set. */
    private Setter<IConfigElement, IValueSet> configElementValueSetSetter() {
        return new Setter<IConfigElement, IValueSet>() {

            @Override
            public void set(IValueSet valueSet, IConfigElement c) {
                c.setValueSet(valueSet.copy(c, c.getIpsModel().getNextPartId(c)));
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
                a.setValueHolder(valueHolder.copy(a));
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
