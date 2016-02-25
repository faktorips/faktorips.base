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
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.productcmpt.Cardinality;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.internal.model.productcmpt.template.ProductCmptLinkHistograms;
import org.faktorips.devtools.core.internal.model.productcmpt.template.PropertyValueHistograms;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink.LinkIdentifier;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.util.Histogram;
import org.faktorips.devtools.core.util.Histogram.BestValue;
import org.faktorips.util.functional.BiConsumer;
import org.faktorips.values.Decimal;

public class InferTemplateProcessor implements IWorkspaceRunnable {

    private static final Decimal RELATIVE_LINK_THRESHOLD = Decimal.valueOf(1.0);
    private static final Decimal RELATIVE_PROPERTY_VALUE_THRESHOLD = Decimal.valueOf(0.8);

    /**
     * Histograms for property values. The name of the property is the string key for the map of
     * histograms.
     */
    private final PropertyValueHistograms propertyValueHistograms;

    /** Histograms for links in the product component. */
    private final ProductCmptLinkHistograms productCmptLinkHistograms;

    /** Histograms for links in the generation. */
    private final ProductCmptLinkHistograms generationLinkHistograms;

    private IProductCmptGeneration templateGeneration;

    private final Set<IIpsSrcFile> srcFilesToSave = new HashSet<IIpsSrcFile>();

    private final List<IProductCmpt> productCmpts;

    private IProgressMonitor monitor;

    /**
     * @deprecated use {@link #InferTemplateProcessor(IProductCmptGeneration, List)}
     */
    @Deprecated
    public InferTemplateProcessor(IProductCmptGeneration templateGeneration, List<IProductCmpt> productCmpts,
            PropertyValueHistograms propertyValueHistograms) {
        this.templateGeneration = templateGeneration;
        this.productCmpts = productCmpts;
        this.propertyValueHistograms = propertyValueHistograms;
        this.productCmptLinkHistograms = ProductCmptLinkHistograms.createFor(productCmpts);
        this.generationLinkHistograms = ProductCmptLinkHistograms.createFor(Lists.transform(productCmpts,
                latestGeneration()));
    }

    public InferTemplateProcessor(IProductCmptGeneration templateGeneration, List<IProductCmpt> productCmpts) {
        this.templateGeneration = templateGeneration;
        this.productCmpts = productCmpts;
        this.propertyValueHistograms = PropertyValueHistograms.createFor(productCmpts);
        this.productCmptLinkHistograms = ProductCmptLinkHistograms.createFor(productCmpts);
        this.generationLinkHistograms = ProductCmptLinkHistograms.createFor(Lists.transform(productCmpts,
                latestGeneration()));
    }

    private Function<IProductCmpt, IProductCmptGeneration> latestGeneration() {
        return new Function<IProductCmpt, IProductCmptGeneration>() {

            @Override
            public IProductCmptGeneration apply(IProductCmpt p) {
                if (p == null) {
                    return null;
                }
                return p.getLatestProductCmptGeneration();
            }
        };
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
     * relative distribution of at least {@link #RELATIVE_PROPERTY_VALUE_THRESHOLD}.
     */
    private void inferTemplate() {
        monitor.beginTask(Messages.InferTemplateOperation_progress_inferringTemplate, propertyValueHistograms.size()
                + productCmptLinkHistograms.size() + generationLinkHistograms.size() + (productCmpts.size() * 2));
        try {
            srcFileChanged(templateGeneration.getIpsSrcFile());
            updateProductCmpts();
            for (PropertyValueType type : PropertyValueType.values()) {
                updatePropertyValues(type.getInterfaceClass(), type.getValueSetter());
            }
            updateLinks();
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
     * at least {@link #RELATIVE_PROPERTY_VALUE_THRESHOLD} are set. The value of the property value
     * is set with the given setter.
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
        Histogram<Object, IPropertyValue> histogram = propertyValueHistograms.get(propertyValue.getPropertyName());
        BestValue<Object> bestValue = histogram.getBestValue(RELATIVE_PROPERTY_VALUE_THRESHOLD);
        return bestValue;
    }

    private void updateOriginPropertyValues(String propertyName, Object value) {
        Histogram<Object, IPropertyValue> histogram = propertyValueHistograms.get(propertyName);
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

    private void updateLinks() {
        updateLinks(productCmptLinkHistograms, templateGeneration.getProductCmpt());
        updateLinks(generationLinkHistograms, templateGeneration);
    }

    private void updateLinks(ProductCmptLinkHistograms histograms, IProductCmptLinkContainer templateContainer) {
        for (Entry<LinkIdentifier, Histogram<Cardinality, IProductCmptLink>> e : histograms.getEntries()) {
            Histogram<Cardinality, IProductCmptLink> histogram = e.getValue();
            BestValue<Cardinality> cardinality = histogram.getBestValue(RELATIVE_LINK_THRESHOLD);
            // Only consider links that occur in all product components with the same cardinality
            if (histogram.countElements() == productCmpts.size() && cardinality.isPresent()) {
                addTemplateLink(templateContainer, e.getKey(), cardinality.getValue());
                setLinksInherited(histogram.getElements(cardinality.getValue()));
            }
            monitor.worked(1);
        }

    }

    /**
     * Adds a new link with the given cardinality to the given template (generation of product
     * component). The link's association and target are read from the given link identifier-
     */
    private void addTemplateLink(IProductCmptLinkContainer container,
            LinkIdentifier linkIdentifier,
            Cardinality cardinality) {
        IProductCmptLink newLink = container.newLink(linkIdentifier.getAssociation());
        newLink.setTarget(linkIdentifier.getTarget());
        newLink.setCardinality(cardinality);
        newLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);
    }

    /** Set template value status of the given links to {@link TemplateValueStatus#INHERITED}. */
    private void setLinksInherited(Set<IProductCmptLink> links) {
        for (IProductCmptLink link : links) {
            link.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        }
    }
}
