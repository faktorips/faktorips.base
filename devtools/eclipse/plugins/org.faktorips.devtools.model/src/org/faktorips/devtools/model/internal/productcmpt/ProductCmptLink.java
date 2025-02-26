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

import java.beans.PropertyChangeEvent;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.HierarchyVisitor;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.internal.productcmpt.template.TemplateValueFinder;
import org.faktorips.devtools.model.internal.productcmpt.template.TemplateValueSettings;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.util.NullSafeComparableComparator;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductCmptLink extends AtomicIpsObjectPart implements IProductCmptLink {

    private static final Cardinality DEFAULT_CARDINALITY = new Cardinality(0, 1, 0);

    /** the name of the association this link is an instance of */
    private String association = ""; //$NON-NLS-1$

    private String target = ""; //$NON-NLS-1$

    private String targetRuntimeId = ""; //$NON-NLS-1$

    private Cardinality cardinality = DEFAULT_CARDINALITY;

    private final TemplateValueSettings templateValueSettings;

    public ProductCmptLink(IProductCmptLinkContainer parent, String id) {
        super(parent, id);
        templateValueSettings = new TemplateValueSettings(this);
    }

    @Override
    public IProductCmpt getProductCmpt() {
        return getProductCmptLinkContainer().getProductCmpt();
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
    public IProductCmptTypeAssociation findAssociation(IIpsProject ipsProject) {
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
    public IProductCmpt findTarget(IIpsProject ipsProject) {
        return ipsProject.findProductCmpt(target);
    }

    @Override
    public void setTarget(String newTarget) {
        String oldTarget = target;
        target = newTarget;
        valueChanged(oldTarget, target);
        IProductCmpt targetProductCmpt = findTarget(getIpsProject());
        if (targetProductCmpt != null) {
            setTargetRuntimeId(targetProductCmpt.getRuntimeId());
        } else {
            setTargetRuntimeId(null);
        }
    }

    @Override
    public String getTargetRuntimeId() {
        return targetRuntimeId;
    }

    @Override
    public void setTargetRuntimeId(String newTargetRuntimeId) {
        String oldTargetRuntimeId = targetRuntimeId;
        targetRuntimeId = newTargetRuntimeId;
        valueChanged(oldTargetRuntimeId, targetRuntimeId);
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
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
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
                typeLabel = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(productCmptType);
            }
            String text = MessageFormat.format(Messages.ProductCmptRelation_msgNoRelationDefined, association,
                    typeLabel);
            list.add(new Message(MSGCODE_UNKNWON_ASSOCIATION, text, Message.ERROR, this, PROPERTY_ASSOCIATION));
        } else {
            validateCardinalityForMatchingAssociation(list, ipsProject, associationObj);

            IProductCmpt targetObj = findTarget(ipsProject);
            if (!willBeValid(targetObj, associationObj, ipsProject)) {
                String associationLabel = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(associationObj);
                String msg = MessageFormat.format(Messages.ProductCmptRelation_msgInvalidTarget, target,
                        associationLabel);
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
            String associationLabel = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(associationObj);
            String msg;
            if (associationObj.isChangingOverTime()) {
                msg = MessageFormat.format(Messages.ProductCmptLink_msgChaningOverTimeMismatch_partOfComponent,
                        associationLabel,
                        getName());
            } else {
                msg = MessageFormat.format(Messages.ProductCmptLink_msgChaningOverTimeMismatch_partOfGeneration,
                        associationLabel, getName(), IIpsModelExtensions.get().getModelPreferences()
                                .getChangesOverTimeNamingConvention().getGenerationConceptNameSingular(true));
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
                    String text = MessageFormat.format(
                            Messages.ProductCmptLink_msgMaxCardinalityExceedsModelMaxQualified,
                            getMaxCardinality(), associationObj.getMaxCardinality());
                    list.add(new Message(MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX, text, Message.ERROR, this,
                            PROPERTY_MAX_CARDINALITY));
                }
            }
        }
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
        targetRuntimeId = element.getAttribute(PROPERTY_TARGET_RUNTIME_ID);
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
        element.setAttribute(PROPERTY_TARGET_RUNTIME_ID, targetRuntimeId);
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
        IProductCmptTypeAssociation assoc = findAssociation(ipsProject);
        if (assoc == null) {
            return false;
        }
        IPolicyCmptTypeAssociation matchingPolicyCmptTypeAssociation = assoc
                .findMatchingPolicyCmptTypeAssociation(ipsProject);
        return matchingPolicyCmptTypeAssociation != null && matchingPolicyCmptTypeAssociation.isConfigurable();

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
    public String getCaption(Locale locale) {
        ArgumentCheck.notNull(locale);

        String caption = null;
        IAssociation assoc = findAssociation(getIpsProject());
        if (assoc != null) {
            caption = assoc.getLabelValue(locale);
        }
        return caption;
    }

    @Override
    public String getPluralCaption(Locale locale) {
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
    public boolean isLinkOfAssociation(IAssociation association, IIpsProject ipsProject) {
        DerivedUnionVisitor hierarchyVisitor = new DerivedUnionVisitor(association, ipsProject);
        hierarchyVisitor.start(findAssociation(ipsProject));
        return hierarchyVisitor.found;
    }

    /**
     * @param target The product component that will be used as target for the new relation.
     * @param ipsProject The ips project which ips object path is used.
     * @return <code>true</code> if it is possible to create a valid relation with the given
     *             parameters at this time, <code>false</code> otherwise.
     *
     * @throws IpsException if an error occurs during supertype-evaluation
     */
    private boolean willBeValid(IProductCmpt target, IAssociation association, IIpsProject ipsProject) {
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
            cardinality = Optional.ofNullable(findTemplateCardinality()).orElse(DEFAULT_CARDINALITY);
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
        return new NullSafeComparableComparator<>();
    }

    @Override
    public Function<IProductCmptLink, Object> getValueGetter() {
        return IProductCmptLink::getCardinality;
    }

    @Override
    public Function<IProductCmptLink, Object> getInternalValueGetter() {
        return o -> {
            if (o instanceof ProductCmptLink productCmptLink) {
                return productCmptLink.cardinality;
            } else {
                throw new IllegalArgumentException("Illegal parameter " + o);
            }
        };
    }

    @Override
    public BiConsumer<IProductCmptLink, Object> getValueSetter() {
        return (productCmptLink, obj) -> {
            ArgumentCheck.isInstanceOf(obj, Cardinality.class);
            productCmptLink.setCardinality((Cardinality)obj);
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
        IProductCmptTypeAssociation productAsssociation = findAssociation(getIpsProject());
        return productAsssociation != null
                && productAsssociation.findMatchingPolicyCmptTypeAssociation(getIpsProject()) != null;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(association);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj)
                && obj instanceof ProductCmptLink otherLink
                && Objects.equals(getAssociation(), otherLink.getAssociation());
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
            return currentAssociation.findSubsettedDerivedUnion(ipsProject);
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
