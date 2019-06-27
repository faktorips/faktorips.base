/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.beans.PropertyChangeEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.internal.model.productcmpt.template.TemplateValueFinder;
import org.faktorips.devtools.core.internal.model.productcmpt.template.TemplateValueSettings;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.HierarchyVisitor;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.util.NullSafeComparableComparator;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.functional.BiConsumer;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ProductCmptLink extends AtomicIpsObjectPart implements IProductCmptLink {

    private static final Cardinality DEFAULT_CARDINALITY = new Cardinality(0, 1, 0);

    /** the name of the association this link is an instance of */
    private String association = ""; //$NON-NLS-1$

    private String target = ""; //$NON-NLS-1$

    private Cardinality cardinality = DEFAULT_CARDINALITY;

    private final TemplateValueSettings templateValueSettings;

    public ProductCmptLink(IProductCmptLinkContainer parent, String id) {
        super(parent, id);
        this.templateValueSettings = new TemplateValueSettings(this);
    }

    @Override
    public IProductCmpt getProductCmpt() {
        return getProductCmptLinkContainer().getProductCmpt();
    }

    /**
     * @deprecated use {@link #getProductCmptLinkContainer()} instead
     */
    @Override
    @Deprecated
    public IProductCmptGeneration getProductCmptGeneration() {
        if (getProductCmptLinkContainer() instanceof IProductCmptGeneration) {
            return (IProductCmptGeneration)getParent();
        }
        return null;
    }

    @Override
    public IProductCmptLinkContainer getProductCmptLinkContainer() {
        return (IProductCmptLinkContainer)getParent();
    }

    @Override
    public IProductCmptLinkContainer getTemplatedValueContainer() {
        return getProductCmptLinkContainer();
    }

    @Override
    public String getName() {
        return target;
    }

    @Override
    public String getAssociation() {
        return association;
    }

    @Override
    public IProductCmptTypeAssociation findAssociation(IIpsProject ipsProject) throws CoreException {
        IProductCmptType productCmptType = getProductCmpt().findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return null;
        }
        return (IProductCmptTypeAssociation)productCmptType.findAssociation(association, ipsProject);
    }

    @Override
    public void setAssociation(String association) {
        String oldAsso = this.association;
        this.association = association;
        valueChanged(oldAsso, association);
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public IProductCmpt findTarget(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findProductCmpt(target);
    }

    @Override
    public void setTarget(String newTarget) {
        String oldTarget = target;
        target = newTarget;
        valueChanged(oldTarget, target);
    }

    @Override
    public Cardinality getCardinality() {
        if (getTemplateValueStatus() == TemplateValueStatus.INHERITED) {
            return findTemplateCardinality();
        }
        return cardinality;
    }

    private Cardinality findTemplateCardinality() {
        IProductCmptLink templateLink = findTemplateProperty(getIpsProject());
        if (templateLink == null) {
            // Template should have a link but does not. Use the "last known" cardinality
            // as a more or less helpful fallback while some validation hopefully addresses
            // the missing link in the template...
            return cardinality;
        }
        return templateLink.getCardinality();
    }

    @Override
    public void setCardinality(Cardinality cardinality) {
        Cardinality oldValue = this.cardinality;
        this.cardinality = cardinality;
        valueChanged(oldValue, cardinality, PROPERTY_CARDINALITY);
    }

    @Override
    public int getMinCardinality() {
        return getCardinality().getMin();
    }

    @Override
    public void setMinCardinality(int newValue) {
        int oldValue = getMinCardinality();
        cardinality = getCardinality().withMin(newValue);
        valueChanged(oldValue, newValue);
    }

    @Override
    public int getDefaultCardinality() {
        return getCardinality().getDefault();
    }

    @Override
    public void setDefaultCardinality(int newValue) {
        int oldValue = getDefaultCardinality();
        cardinality = getCardinality().withDefault(newValue);
        valueChanged(oldValue, newValue);
    }

    @Override
    public int getMaxCardinality() {
        return getCardinality().getMax();
    }

    @Override
    public void setMaxCardinality(int newValue) {
        int oldValue = getMaxCardinality();
        cardinality = getCardinality().withMax(newValue);
        valueChanged(oldValue, newValue);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (isDeleted()) {
            return;
        }
        super.validateThis(list, ipsProject);
        ValidationUtils.checkIpsObjectReference(target, IpsObjectType.PRODUCT_CMPT, "target", this, PROPERTY_TARGET, //$NON-NLS-1$
                MSGCODE_UNKNWON_TARGET, list);

        IProductCmptTypeAssociation associationObj = findAssociation(ipsProject);
        if (associationObj == null) {
            String typeLabel = getProductCmpt().getProductCmptType();
            IProductCmptType productCmptType = getProductCmpt().findProductCmptType(ipsProject);
            if (productCmptType != null) {
                typeLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(productCmptType);
            }
            String text = NLS.bind(Messages.ProductCmptRelation_msgNoRelationDefined, association, typeLabel);
            list.add(new Message(MSGCODE_UNKNWON_ASSOCIATION, text, Message.ERROR, this, PROPERTY_ASSOCIATION));
        } else {
            validateCardinalityForMatchingAssociation(list, ipsProject, associationObj);

            IProductCmpt targetObj = findTarget(ipsProject);
            if (!willBeValid(targetObj, associationObj, ipsProject)) {
                String associationLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(associationObj);
                String msg = NLS.bind(Messages.ProductCmptRelation_msgInvalidTarget, target, associationLabel);
                list.add(new Message(MSGCODE_INVALID_TARGET, msg, Message.ERROR, this, PROPERTY_TARGET));
            }

            validateChangingOverTimeProperty(list, associationObj);
        }
        list.add(templateValueSettings.validate(this, ipsProject));
    }

    protected void validateCardinalityForMatchingAssociation(MessageList list,
            IIpsProject ipsProject,
            IProductCmptTypeAssociation associationObj) {
        IPolicyCmptTypeAssociation polAssociation = associationObj.findMatchingPolicyCmptTypeAssociation(ipsProject);
        if (polAssociation != null) {
            validateCardinality(list, polAssociation);
        }
    }

    private void validateChangingOverTimeProperty(MessageList list, IProductCmptTypeAssociation associationObj) {
        if (!getProductCmptLinkContainer().isContainerFor(associationObj)) {
            String associationLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(associationObj);
            String msg;
            if (associationObj.isChangingOverTime()) {
                msg = NLS.bind(Messages.ProductCmptLink_msgChaningOverTimeMismatch_partOfComponent, associationLabel,
                        getName());
            } else {
                msg = NLS.bind(Messages.ProductCmptLink_msgChaningOverTimeMismatch_partOfGeneration,
                        new Object[] { associationLabel, getName(), IpsPlugin.getDefault().getIpsPreferences()
                                .getChangesOverTimeNamingConvention().getGenerationConceptNameSingular(true) });
            }
            ObjectProperty prop1 = new ObjectProperty(this, PROPERTY_ASSOCIATION);
            ObjectProperty prop2 = new ObjectProperty(associationObj.getTargetRoleSingular(), null);
            list.add(new Message(MSGCODE_CHANGING_OVER_TIME_MISMATCH, msg, Message.ERROR, prop1, prop2));
        }
    }

    private void validateCardinality(MessageList list, IPolicyCmptTypeAssociation associationObj) {
        MessageList cardinalityValidation = getCardinality().validate(this);
        list.add(cardinalityValidation);
        if (!cardinalityValidation.containsErrorMsg()) {
            if (associationObj.isQualified()) {
                if (getMaxCardinality() > associationObj.getMaxCardinality()) {
                    String text = NLS.bind(Messages.ProductCmptLink_msgMaxCardinalityExceedsModelMaxQualified,
                            this.getMaxCardinality(), associationObj.getMaxCardinality());
                    list.add(new Message(MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX, text, Message.ERROR, this,
                            PROPERTY_MAX_CARDINALITY));
                }
            } else {
                validateTotalMax(list, associationObj);
                if (!getProductCmptLinkContainer().isProductTemplate()) {
                    validateTotalMin(list, associationObj);
                }
            }
        }
    }

    /**
     * FIPS-106: this.maxCardinality + ForAllOtherOfSameAssociation(Sum(other.minCardinality)) <=
     * policyCmptAssociation.maxCardinality
     */
    private void validateTotalMax(MessageList list, IPolicyCmptTypeAssociation associationObj) {
        int maxType = associationObj.getMaxCardinality();
        if (maxType != IProductCmptTypeAssociation.CARDINALITY_MANY) {
            int sumMinCardinality = this.getMaxCardinality();
            List<IProductCmptLink> links = getProductCmptLinkContainer().getLinksAsList(getAssociation());
            if (sumMinCardinality < Cardinality.CARDINALITY_MANY) {
                for (IProductCmptLink productCmptLink : links) {
                    if (!equals(productCmptLink)) {
                        sumMinCardinality += productCmptLink.getMinCardinality();
                    }
                }
            }
            if (sumMinCardinality > maxType) {
                String text = NLS.bind(Messages.ProductCmptLink_msgMaxCardinalityExceedsModelMax,
                        this.getMaxCardinality(), Integer.toString(maxType));
                list.add(new Message(MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX, text, Message.ERROR, this,
                        PROPERTY_MAX_CARDINALITY));
            }
        }
    }

    /**
     * FIPS-106: this.minCardinality + ForAllOtherOfSameAssociation(Sum(other.maxCardinality)) <=
     * policyCmptAssociation.minCardinality
     */
    private void validateTotalMin(MessageList list, IPolicyCmptTypeAssociation associationObj) {
        int minType = associationObj.getMinCardinality();
        int sumMaxCardinality = this.getMinCardinality();
        List<IProductCmptLink> links = getProductCmptLinkContainer().getLinksAsList(getAssociation());
        for (IProductCmptLink productCmptLink : links) {
            if (!equals(productCmptLink)) {
                if (productCmptLink.getMaxCardinality() == Cardinality.CARDINALITY_MANY) {
                    sumMaxCardinality = Cardinality.CARDINALITY_MANY;
                    break;
                }
                sumMaxCardinality += productCmptLink.getMaxCardinality();
            }
        }
        if (sumMaxCardinality < minType) {
            addTotalMinMessage(list, minType);
        }
    }

    private void addTotalMinMessage(MessageList list, int minType) {
        String text = NLS.bind(Messages.ProductCmptLink_msgMinCardinalityExceedsModelMin, this.getMinCardinality(),
                Integer.toString(minType));
        ObjectProperty property = new ObjectProperty(this, PROPERTY_MIN_CARDINALITY);
        list.newError(MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN, text, property);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        association = element.getAttribute(PROPERTY_ASSOCIATION);
        target = element.getAttribute(PROPERTY_TARGET);
        int minCardinality;
        int maxCardinality;
        int defaultCardinality;
        try {
            minCardinality = Integer.parseInt(element.getAttribute(PROPERTY_MIN_CARDINALITY));
        } catch (NumberFormatException e) {
            minCardinality = 0;
        }
        try {
            defaultCardinality = Integer.parseInt(element.getAttribute(PROPERTY_DEFAULT_CARDINALITY));
        } catch (NumberFormatException e) {
            defaultCardinality = minCardinality;
        }
        String max = element.getAttribute(PROPERTY_MAX_CARDINALITY);
        if ("*".equals(max)) { //$NON-NLS-1$
            maxCardinality = Cardinality.CARDINALITY_MANY;
        } else {
            try {
                maxCardinality = Integer.parseInt(max);
            } catch (NumberFormatException e) {
                maxCardinality = 0;
            }
        }
        if (minCardinality == 0 && maxCardinality == 0 && defaultCardinality == 0) {
            cardinality = Cardinality.UNDEFINED;
        } else {
            cardinality = new Cardinality(minCardinality, maxCardinality, defaultCardinality);
        }
        templateValueSettings.initPropertiesFromXml(element);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Cardinality card = getCardinality();
        element.setAttribute(PROPERTY_ASSOCIATION, association);
        element.setAttribute(PROPERTY_TARGET, target);
        element.setAttribute(PROPERTY_MIN_CARDINALITY, Integer.toString(card.getMin()));
        element.setAttribute(PROPERTY_DEFAULT_CARDINALITY, Integer.toString(card.getDefault()));

        if (card.isToMany()) {
            element.setAttribute(PROPERTY_MAX_CARDINALITY, "*"); //$NON-NLS-1$
        } else {
            element.setAttribute(PROPERTY_MAX_CARDINALITY, Integer.toString(card.getMax()));
        }
        templateValueSettings.propertiesToXml(element);
    }

    @Override
    public boolean constrainsPolicyCmptTypeAssociation(IIpsProject ipsProject) {
        if (isDeleted()) {
            return false;
        }
        try {
            IProductCmptTypeAssociation assoc = findAssociation(ipsProject);
            if (assoc == null) {
                return false;
            }
            IPolicyCmptTypeAssociation matchingPolicyCmptTypeAssociation = assoc
                    .findMatchingPolicyCmptTypeAssociation(ipsProject);
            return matchingPolicyCmptTypeAssociation != null && matchingPolicyCmptTypeAssociation.isConfigurable();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

    }

    @Override
    public boolean isMandatory() {
        return getMinCardinality() == 1 && getMaxCardinality() == 1;
    }

    @Override
    public boolean isOptional() {
        return getMinCardinality() == 0 && getMaxCardinality() == 1;
    }

    @Override
    public boolean is1ToMany() {
        return getMaxCardinality() > 1;
    }

    @Override
    public String getCaption(Locale locale) throws CoreException {
        ArgumentCheck.notNull(locale);

        String caption = null;
        IAssociation assoc = findAssociation(getIpsProject());
        if (assoc != null) {
            caption = assoc.getLabelValue(locale);
        }
        return caption;
    }

    @Override
    public String getPluralCaption(Locale locale) throws CoreException {
        ArgumentCheck.notNull(locale);

        String pluralCaption = null;
        IAssociation assoc = findAssociation(getIpsProject());
        if (assoc != null) {
            pluralCaption = assoc.getPluralLabelValue(locale);
        }
        return pluralCaption;
    }

    @Override
    public String getLastResortCaption() {
        return StringUtils.capitalize(association);
    }

    @Override
    public String getLastResortPluralCaption() {
        return StringUtils.capitalize(association);
    }

    @Override
    public boolean isLinkOfAssociation(IAssociation association, IIpsProject ipsProject) throws CoreException {
        DerivedUnionVisitor hierarchyVisitor = new DerivedUnionVisitor(association, ipsProject);
        hierarchyVisitor.start(findAssociation(ipsProject));
        return hierarchyVisitor.found;
    }

    /**
     * @param target The product component that will be used as target for the new relation.
     * @param ipsProject The ips project which ips object path is used.
     * @return <code>true</code> if it is possible to create a valid relation with the given
     *         parameters at this time, <code>false</code> otherwise.
     * 
     * @throws CoreException if an error occurs during supertype-evaluation
     */
    private boolean willBeValid(IProductCmpt target, IAssociation association, IIpsProject ipsProject)
            throws CoreException {

        if (target == null || association == null) {
            return false;
        }
        IProductCmptType actualTargetType = target.findProductCmptType(ipsProject);
        if (actualTargetType == null) {
            return false;
        }
        return actualTargetType.isSubtypeOrSameType(association.findTarget(ipsProject), ipsProject);
    }

    @Override
    public void setTemplateValueStatus(TemplateValueStatus newStatus) {
        TemplateValueStatus oldValue = templateValueSettings.getStatus();
        if (oldValue == newStatus) {
            return;
        }
        if (newStatus == TemplateValueStatus.DEFINED) {
            // safe the current cardinality from template
            cardinality = Optional.fromNullable(findTemplateCardinality()).or(DEFAULT_CARDINALITY);
        } else if (newStatus == TemplateValueStatus.UNDEFINED) {
            cardinality = Cardinality.UNDEFINED;
        }
        templateValueSettings.setStatus(newStatus);
        objectHasChanged(new PropertyChangeEvent(this, PROPERTY_TEMPLATE_VALUE_STATUS, oldValue, newStatus));
    }

    @Override
    public TemplateValueStatus getTemplateValueStatus() {
        return templateValueSettings.getStatus();
    }

    @Override
    public void switchTemplateValueStatus() {
        setTemplateValueStatus(getTemplateValueStatus().getNextStatus(this));
    }

    @Override
    public IProductCmptLink findTemplateProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.findTemplateValue(this, IProductCmptLink.class);
    }

    @Override
    public boolean hasTemplateForProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.hasTemplateForValue(this, IProductCmptLink.class);
    }

    /**
     * {@inheritDoc}
     * <p>
     * As individual links may be added and removed for an association instead of being "the"
     * configuration for it, a link is only considered to be part of the hierarchy if the
     * {@link ProductCmptType} configured by the template (if any) contains the association this
     * link configures.
     */
    @Override
    public boolean isPartOfTemplateHierarchy() {
        return getTemplatedValueContainer().isPartOfTemplateHierarchy()
                && (getTemplatedValueContainer().isProductTemplate() || isAssociationConfiguredInTemplate());
    }

    public boolean isAssociationConfiguredInTemplate() {
        IProductCmptLinkContainer templatedValueContainer = getTemplatedValueContainer();
        if (templatedValueContainer.isUsingTemplate()) {
            IProductCmptLinkContainer template = templatedValueContainer.findTemplate(getIpsProject());
            if (template != null) {
                IProductCmpt templateCmpt = template.getProductCmpt();
                IProductCmptType templateProductCmptType = templateCmpt.findProductCmptType(getIpsProject());
                return templateProductCmptType.findAssociation(association, getIpsProject()) != null;
            }
        }
        return false;
    }

    @Override
    public void delete() {
        if (findTemplateProperty(getIpsProject()) != null) {
            setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        } else {
            super.delete();
        }
    }

    @Override
    public Comparator<Object> getValueComparator() {
        return new NullSafeComparableComparator<Object>();
    }

    @Override
    public Function<IProductCmptLink, Object> getValueGetter() {
        return new Function<IProductCmptLink, Object>() {

            @SuppressFBWarnings
            @Override
            public Object apply(IProductCmptLink input) {
                return input.getCardinality();
            }
        };
    }

    @Override
    public BiConsumer<IProductCmptLink, Object> getValueSetter() {
        return new BiConsumer<IProductCmptLink, Object>() {
            @Override
            public void accept(IProductCmptLink t, Object u) {
                ArgumentCheck.isInstanceOf(u, Cardinality.class);
                t.setCardinality((Cardinality)u);
            }
        };
    }

    @Override
    public LinkIdentifier getIdentifier() {
        return new LinkIdentifier(this);
    }

    @Override
    public boolean isConcreteValue() {
        return getTemplateValueStatus() == TemplateValueStatus.DEFINED
                || getTemplateValueStatus() == TemplateValueStatus.UNDEFINED;
    }

    @Override
    public boolean isConfiguringPolicyAssociation() {
        try {
            IProductCmptTypeAssociation productAsssociation = findAssociation(getIpsProject());
            return productAsssociation != null
                    && productAsssociation.findMatchingPolicyCmptTypeAssociation(getIpsProject()) != null;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private static class DerivedUnionVisitor extends HierarchyVisitor<IAssociation> {

        private final IAssociation association;

        private boolean found;

        /**
         * @param association The association that should be found by this visitor
         * @param ipsProject The project for searching the associations
         */
        public DerivedUnionVisitor(IAssociation association, IIpsProject ipsProject) {
            super(ipsProject);
            this.association = association;
        }

        @Override
        protected IAssociation findSupertype(IAssociation currentAssociation, IIpsProject ipsProject) {
            try {
                return currentAssociation.findSubsettedDerivedUnion(ipsProject);
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

        @Override
        protected boolean visit(IAssociation currentAssociation) {
            if (currentAssociation.equals(association)) {
                found = true;
                return false;
            }
            return true;
        }

    }

}
