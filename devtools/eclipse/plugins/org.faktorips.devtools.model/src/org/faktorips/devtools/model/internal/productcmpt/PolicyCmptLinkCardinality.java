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
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.internal.productcmpt.template.TemplateValueFinder;
import org.faktorips.devtools.model.internal.productcmpt.template.TemplateValueSettings;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IPolicyCmptLinkCardinality;
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

public class PolicyCmptLinkCardinality extends AtomicIpsObjectPart implements IPolicyCmptLinkCardinality {

    private final TemplateValueSettings templateValueSettings;
    private String policyAssociationName;
    private Cardinality cardinality;

    public PolicyCmptLinkCardinality(IProductCmptLinkContainer parent, String id) {
        super(parent, id);
        templateValueSettings = new TemplateValueSettings(this);
        cardinality = new Cardinality(0, Cardinality.CARDINALITY_MANY, 0);
    }

    public PolicyCmptLinkCardinality(IProductCmptLinkContainer parent, String id, String policyAssociationName) {
        super(parent, id);
        this.policyAssociationName = policyAssociationName;
        templateValueSettings = new TemplateValueSettings(this);
        cardinality = findModelCardinality();
    }

    @Override
    public void setTemplateValueStatus(TemplateValueStatus newStatus) {
        TemplateValueStatus oldValue = templateValueSettings.getStatus();
        if (oldValue == newStatus) {
            return;
        }
        if (newStatus == TemplateValueStatus.DEFINED) {
            // safe the current cardinality from template
            cardinality = Optional.ofNullable(findTemplateCardinality()).orElse(findModelCardinality());
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
    public boolean hasTemplateForProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.hasTemplateForValue(this, IPolicyCmptLinkCardinality.class);
    }

    @Override
    public boolean isPartOfTemplateHierarchy() {
        return getTemplatedValueContainer().isPartOfTemplateHierarchy()
                && (getTemplatedValueContainer().isProductTemplate() || isAssociationConfiguredInTemplate());
    }

    public boolean isAssociationConfiguredInTemplate() {
        var templatedValueContainer = getTemplatedValueContainer();
        if (templatedValueContainer.isUsingTemplate()) {
            var template = templatedValueContainer.findTemplate(getIpsProject());
            if (template != null) {
                var templateCmpt = template.getProductCmpt();
                var templateProductCmptType = templateCmpt.findProductCmptType(getIpsProject());
                var templatePolicyCmptType = templateProductCmptType.findPolicyCmptType(getIpsProject());
                return templatePolicyCmptType.findAssociation(policyAssociationName, getIpsProject()) != null;
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
    public IProductCmptLinkContainer getTemplatedValueContainer() {
        return getProductCmptLinkContainer();
    }

    @Override
    public Comparator<Object> getValueComparator() {
        return new NullSafeComparableComparator<>();
    }

    @Override
    public BiConsumer<IPolicyCmptLinkCardinality, Object> getValueSetter() {
        return (policyLinkCardinality, obj) -> {
            ArgumentCheck.isInstanceOf(obj, Cardinality.class);
            policyLinkCardinality.setCardinality((Cardinality)obj);
        };
    }

    @Override
    public PolicyCmptLinkIdentifier getIdentifier() {
        return new PolicyCmptLinkIdentifier(this);
    }

    @Override
    public boolean isConcreteValue() {
        return getTemplateValueStatus() == TemplateValueStatus.DEFINED
                || getTemplateValueStatus() == TemplateValueStatus.UNDEFINED;
    }

    @Override
    public IProductCmptLinkContainer getProductCmptLinkContainer() {
        return (IProductCmptLinkContainer)getParent();
    }

    @Override
    public String getAssociation() {
        return policyAssociationName;
    }

    @Override
    public void setAssociation(String policyAssociationName) {
        String oldAsso = this.policyAssociationName;
        this.policyAssociationName = policyAssociationName;
        valueChanged(oldAsso, policyAssociationName);
        setCardinality(findModelCardinality());
    }

    @Override
    public IPolicyCmptTypeAssociation findAssociation(IIpsProject ipsProject) throws IpsException {
        var productCmptType = getProductCmptLinkContainer().findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return null;
        }
        var policyCmptType = productCmptType.findPolicyCmptType(ipsProject);
        if (policyCmptType == null) {
            return null;
        }
        return (IPolicyCmptTypeAssociation)policyCmptType.findAssociation(policyAssociationName, ipsProject);
    }

    @Override
    public Cardinality getCardinality() {
        if (getTemplateValueStatus() == TemplateValueStatus.INHERITED) {
            return findTemplateCardinality();
        }
        return cardinality;
    }

    private Cardinality findModelCardinality() {
        var association = findAssociation(getIpsProject());
        return association == null
                ? Cardinality.UNDEFINED
                : new Cardinality(association.getMinCardinality(), association.getMaxCardinality(), 0);
    }

    private Cardinality findTemplateCardinality() {
        IPolicyCmptLinkCardinality templateLinkCardinality = findTemplateProperty(getIpsProject());
        if (templateLinkCardinality == null) {
            // Template should have a cardinality but does not. Use the "last known" cardinality
            // as a more or less helpful fallback while some validation hopefully addresses
            // the missing link in the template...
            return cardinality;
        }
        return templateLinkCardinality.getCardinality();
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
        return StringUtils.capitalize(policyAssociationName);
    }

    @Override
    public String getLastResortPluralCaption() {
        return StringUtils.capitalize(policyAssociationName);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        if (isDeleted()) {
            return;
        }
        super.validateThis(list, ipsProject);

        IPolicyCmptTypeAssociation associationObj = findAssociation(ipsProject);
        if (associationObj == null) {
            String typeLabel = getProductCmptLinkContainer().getProductCmptType();
            IProductCmptType productCmptType = getProductCmptLinkContainer().findProductCmptType(ipsProject);
            if (productCmptType != null) {
                typeLabel = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(productCmptType);
            }
            String text = MessageFormat.format(Messages.ProductCmptRelation_msgNoRelationDefined, policyAssociationName,
                    typeLabel);
            list.add(new Message(MSGCODE_UNKNOWN_ASSOCIATION, text, Message.ERROR, this, PROPERTY_ASSOCIATION));
        } else {
            validateCardinalityForMatchingAssociation(list, associationObj);

            validateChangingOverTimeProperty(list, associationObj, ipsProject);
        }
        list.add(templateValueSettings.validate(this, ipsProject));
    }

    protected void validateCardinalityForMatchingAssociation(MessageList list,
            IPolicyCmptTypeAssociation polAssociation) {
        if (polAssociation != null) {
            validateCardinality(list, polAssociation);
        }
    }

    private void validateChangingOverTimeProperty(MessageList list,
            IPolicyCmptTypeAssociation polAssociation,
            IIpsProject ipsProject) {
        IProductCmptTypeAssociation associationObj = polAssociation.findMatchingProductCmptTypeAssociation(ipsProject);
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
    public IPolicyCmptLinkCardinality findTemplateProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.findTemplateValue(this, IPolicyCmptLinkCardinality.class);
    }

    @Override
    public Function<IPolicyCmptLinkCardinality, Object> getValueGetter() {
        return IPolicyCmptLinkCardinality::getCardinality;
    }

    @Override
    public Function<IPolicyCmptLinkCardinality, Object> getInternalValueGetter() {
        return o -> {
            if (o instanceof PolicyCmptLinkCardinality policyLinkCardinality) {
                return policyLinkCardinality.cardinality;
            } else {
                throw new IllegalArgumentException("Illegal parameter " + o);
            }
        };
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        policyAssociationName = element.getAttribute(PROPERTY_ASSOCIATION);
        int minCardinality;
        int maxCardinality;
        try {
            minCardinality = Integer.parseInt(element.getAttribute(PROPERTY_MIN_CARDINALITY));
        } catch (NumberFormatException e) {
            minCardinality = 0;
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
        if (minCardinality == 0 && maxCardinality == 0) {
            cardinality = Cardinality.UNDEFINED;
        } else {
            cardinality = new Cardinality(minCardinality, maxCardinality, 0);
        }
        templateValueSettings.initPropertiesFromXml(element);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Cardinality card = getCardinality();
        element.setAttribute(PROPERTY_ASSOCIATION, policyAssociationName);
        element.setAttribute(PROPERTY_MIN_CARDINALITY, Integer.toString(card.getMin()));
        if (card.isToMany()) {
            element.setAttribute(PROPERTY_MAX_CARDINALITY, "*"); //$NON-NLS-1$
        } else {
            element.setAttribute(PROPERTY_MAX_CARDINALITY, Integer.toString(card.getMax()));
        }
        templateValueSettings.propertiesToXml(element);
    }

}
