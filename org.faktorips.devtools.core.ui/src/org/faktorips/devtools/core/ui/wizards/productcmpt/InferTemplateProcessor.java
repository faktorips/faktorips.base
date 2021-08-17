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

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.productcmpt.template.ProductCmptLinkHistograms;
import org.faktorips.devtools.model.internal.productcmpt.template.PropertyValueHistograms;
import org.faktorips.devtools.model.internal.util.Histogram;
import org.faktorips.devtools.model.internal.util.Histogram.BestValue;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink.LinkIdentifier;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueIdentifier;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;

public class InferTemplateProcessor implements IWorkspaceRunnable {

    private static final Function<IProductCmpt, IProductCmptGeneration> LATEST_GENERATION = productCmpt -> productCmpt == null
            ? null
            : productCmpt.getLatestProductCmptGeneration();

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

    private final Set<IIpsSrcFile> srcFilesToSave = new HashSet<>();

    private final List<IProductCmpt> productCmpts;

    private IProgressMonitor monitor;

    /**
     * @deprecated use {@link #InferTemplateProcessor(IProductCmptGeneration, List)}
     */
    @Deprecated
    InferTemplateProcessor(IProductCmptGeneration templateGeneration, List<IProductCmpt> productCmpts,
            PropertyValueHistograms propertyValueHistograms) {
        this.templateGeneration = templateGeneration;
        this.productCmpts = productCmpts;
        this.propertyValueHistograms = propertyValueHistograms;
        this.productCmptLinkHistograms = ProductCmptLinkHistograms.createFor(productCmpts);
        this.generationLinkHistograms = ProductCmptLinkHistograms
                .createFor(productCmpts.stream().map(LATEST_GENERATION).collect(Collectors.toList()));
    }

    public InferTemplateProcessor(IProductCmptGeneration templateGeneration, List<IProductCmpt> productCmpts) {
        this.templateGeneration = templateGeneration;
        this.productCmpts = productCmpts;
        this.propertyValueHistograms = PropertyValueHistograms.createFor(productCmpts);
        this.productCmptLinkHistograms = ProductCmptLinkHistograms.createFor(productCmpts);
        this.generationLinkHistograms = ProductCmptLinkHistograms
                .createFor(productCmpts.stream().map(LATEST_GENERATION).collect(Collectors.toList()));
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
     * relative distribution of at least
     * {@link IIpsProjectProperties#getInferredTemplateLinkThreshold()}.
     */
    private void inferTemplate() {
        monitor.beginTask(Messages.InferTemplateOperation_progress_inferringTemplate, propertyValueHistograms.size()
                + productCmptLinkHistograms.size() + generationLinkHistograms.size() + (productCmpts.size() * 2));
        try {
            preSrcFileChanged(templateGeneration.getIpsSrcFile());
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
            preSrcFileChanged(productCmpt.getIpsSrcFile());
            productCmpt.setTemplate(getTemplateName());
            monitor.worked(1);
        }
    }

    private String getTemplateName() {
        return templateGeneration.getProductCmpt().getQualifiedName();
    }

    @SuppressWarnings("deprecation")
    private void save() {
        org.eclipse.core.runtime.SubProgressMonitor saveMonitor = new org.eclipse.core.runtime.SubProgressMonitor(
                monitor, productCmpts.size());
        saveMonitor.beginTask(Messages.InferTemplateOperation_progress_save, srcFilesToSave.size() + 1);
        try {
            for (IIpsSrcFile ipsSrcFile : srcFilesToSave) {
                ipsSrcFile.save(false, new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1));
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Sets the values of the given class to the property values of the given class in the given
     * template generation (and the corresponding product component). Only property values for which
     * a histogram is found and for which the value in that histogram has a relative distribution of
     * at least {@link IIpsProjectProperties#getInferredTemplatePropertyValueThreshold()} are set.
     * The value of the property value is set with the given setter.
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
                updateOriginPropertyValues(propertyValue.getIdentifier(), value);
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
        Histogram<Object, IPropertyValue> histogram = propertyValueHistograms.get(propertyValue.getIdentifier());
        BestValue<Object> bestValue = histogram
                .getBestValue(getIpsProjectProperties().getInferredTemplatePropertyValueThreshold());
        return bestValue;
    }

    private IIpsProjectProperties getIpsProjectProperties() {
        return templateGeneration.getIpsProject().getProperties();
    }

    private void updateOriginPropertyValues(ITemplatedValueIdentifier identifier, Object value) {
        Histogram<Object, IPropertyValue> histogram = propertyValueHistograms.get(identifier);
        Set<IPropertyValue> elements = histogram.getElements(value);
        for (IPropertyValue propertyValue : elements) {
            preSrcFileChanged(propertyValue.getIpsSrcFile());
            propertyValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        }
    }

    private void preSrcFileChanged(IIpsSrcFile ipsSrcFile) {
        if (!ipsSrcFile.isDirty()) {
            srcFilesToSave.add(ipsSrcFile);
        }
    }

    private void updateLinks() {
        updateLinks(productCmptLinkHistograms, templateGeneration.getProductCmpt());
        updateLinks(generationLinkHistograms, templateGeneration);
    }

    private void updateLinks(ProductCmptLinkHistograms histograms, IProductCmptLinkContainer templateContainer) {
        for (Entry<LinkIdentifier, Histogram<Cardinality, IProductCmptLinkContainer>> e : histograms.getEntries()) {
            Histogram<Cardinality, IProductCmptLinkContainer> histogram = e.getValue();
            BestValue<Cardinality> bestCardinality = histogram
                    .getBestValue(getIpsProjectProperties().getInferredTemplateLinkThreshold());
            if (bestCardinality.isPresent()) {
                addTemplateLink(templateContainer, e.getKey(), bestCardinality.getValue());
                updateLinkTemplateValueStates(e.getKey(), templateContainer.isChangingOverTimeContainer(),
                        bestCardinality.getValue());
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

    /**
     * Sets the template value states for all product components or their (latest) generations for
     * the given link identifier.
     * <p>
     * If the component/generation has a link for the given identifier, the link's template value
     * status is set to {@link TemplateValueStatus#INHERITED} or {@link TemplateValueStatus#DEFINED}
     * depending on whether or not the link's cardinality is the same as the given template
     * cardinality.
     * <p>
     * If the component/generation does not have a link for the identifier, a new link is created
     * and its template values status is set to {@link TemplateValueStatus#UNDEFINED}.
     * 
     * @param linkIdentifier the link identifier to use
     * @param isLinkFromGeneration whether the link exists on the (latest) product component
     *            generation or on the component itself
     * @param templateCardinality the cardinality the link has in the inferred template
     */
    private void updateLinkTemplateValueStates(LinkIdentifier linkIdentifier,
            boolean isLinkFromGeneration,
            Cardinality templateCardinality) {
        for (IProductCmpt productCmpt : productCmpts) {
            IProductCmptLinkContainer container = isLinkFromGeneration ? LATEST_GENERATION.apply(productCmpt)
                    : productCmpt;
            IProductCmptLink link = linkIdentifier.getValueFrom(container);
            if (link == null) {
                IProductCmptLink newLink = container.newLink(linkIdentifier.getAssociation());
                newLink.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
                newLink.setTarget(linkIdentifier.getTarget());
                newLink.setCardinality(templateCardinality);
            } else {
                if (templateCardinality.equals(link.getCardinality())) {
                    link.setTemplateValueStatus(TemplateValueStatus.INHERITED);
                } else {
                    link.setTemplateValueStatus(TemplateValueStatus.DEFINED);
                }
            }
        }
    }
}
