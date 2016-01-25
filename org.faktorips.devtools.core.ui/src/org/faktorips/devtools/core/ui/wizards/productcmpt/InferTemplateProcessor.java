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
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.util.Histogram;
import org.faktorips.devtools.core.util.Histogram.BestValue;
import org.faktorips.util.functional.BiConsumer;
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
            for (PropertyValueType type : PropertyValueType.values()) {
                updatePropertyValues(type.getInterfaceClass(), type.getValueSetter());
            }
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
    private void updatePropertyValues(Class<? extends IPropertyValue> propertyValueClass,
            BiConsumer<IPropertyValue, Object> setter) {
        List<? extends IPropertyValue> propertyValues = templateGeneration
                .getPropertyValuesIncludingProductCmpt(propertyValueClass);
        for (IPropertyValue propertyValue : propertyValues) {
            BestValue<?> bestValue = getBestValueFor(propertyValue);
            if (bestValue.isPresent()) {
                Object value = bestValue.getValue();
                setter.accept(propertyValue, value);
                updateOriginPropertyValues(propertyValue.getPropertyName(), value);
            } else {
                propertyValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
            }
            monitor.worked(1);
        }
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

}
