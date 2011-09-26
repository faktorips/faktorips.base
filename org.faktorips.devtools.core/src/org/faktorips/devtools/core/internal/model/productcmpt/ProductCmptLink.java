/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductCmptLink extends AtomicIpsObjectPart implements IProductCmptLink {

    /**
     * @param target The product component that will be used as target for the new relation.
     * @param ipsProject The ips project which ips object path is used.
     * @return <code>true</code> if it is possible to create a valid relation with the given
     *         parameters at this time, <code>false</code> otherwise.
     * 
     * @throws CoreException if an error occurs during supertype-evaluation
     */
    public static boolean willBeValid(IProductCmpt target, IAssociation association, IIpsProject ipsProject)
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

    /** the name of the association this link is an instance of */
    private String association = ""; //$NON-NLS-1$

    private String target = ""; //$NON-NLS-1$

    private int minCardinality = 0;

    private int defaultCardinality = minCardinality;

    private int maxCardinality = 1;

    public ProductCmptLink(IProductCmptGeneration generation, String id) {
        super(generation, id);
    }

    public ProductCmptLink() {
        super();
    }

    @Override
    public IProductCmpt getProductCmpt() {
        return (IProductCmpt)getParent().getParent();
    }

    @Override
    public IProductCmptGeneration getProductCmptGeneration() {
        return (IProductCmptGeneration)getParent();
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
    public int getMinCardinality() {
        return minCardinality;
    }

    @Override
    public void setMinCardinality(int newValue) {
        int oldValue = minCardinality;
        minCardinality = newValue;
        valueChanged(oldValue, newValue);

    }

    @Override
    public int getDefaultCardinality() {
        return defaultCardinality;
    }

    @Override
    public void setDefaultCardinality(int newValue) {
        int oldValue = defaultCardinality;
        defaultCardinality = newValue;
        valueChanged(oldValue, newValue);
    }

    @Override
    public int getMaxCardinality() {
        return maxCardinality;
    }

    @Override
    public void setMaxCardinality(int newValue) {
        int oldValue = maxCardinality;
        maxCardinality = newValue;
        valueChanged(oldValue, newValue);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (isDeleted()) {
            return;
        }
        super.validateThis(list, ipsProject);
        ValidationUtils.checkIpsObjectReference(target, IpsObjectType.PRODUCT_CMPT,
                "target", this, PROPERTY_TARGET, MSGCODE_UNKNWON_TARGET, list); //$NON-NLS-1$

        IProductCmptTypeAssociation associationObj = findAssociation(ipsProject);
        if (associationObj == null) {
            String typeLabel = getProductCmpt().getProductCmptType();
            IProductCmptType productCmptType = getProductCmpt().findProductCmptType(ipsProject);
            if (productCmptType != null) {
                typeLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(productCmptType);
            }
            String text = NLS.bind(Messages.ProductCmptRelation_msgNoRelationDefined, association, typeLabel);
            list.add(new Message(MSGCODE_UNKNWON_ASSOCIATION, text, Message.ERROR, this, PROPERTY_ASSOCIATION));
            return;
        }
        IPolicyCmptTypeAssociation polAssociation = associationObj.findMatchingPolicyCmptTypeAssociation(ipsProject);
        if (polAssociation != null) {
            validateCardinality(list, polAssociation);
        }

        IProductCmpt targetObj = findTarget(ipsProject);
        if (!willBeValid(targetObj, associationObj, ipsProject)) {
            String associationLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(associationObj);
            String msg = NLS.bind(Messages.ProductCmptRelation_msgInvalidTarget, target, associationLabel);
            list.add(new Message(MSGCODE_INVALID_TARGET, msg, Message.ERROR, this, PROPERTY_TARGET));
        }
    }

    private void validateCardinality(MessageList list, IPolicyCmptTypeAssociation associationObj) {
        if (maxCardinality < 1) {
            String text = Messages.ProductCmptRelation_msgMaxCardinalityIsLessThan1;
            list.add(new Message(MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1, text, Message.ERROR, this,
                    PROPERTY_MAX_CARDINALITY));
        } else {
            if (minCardinality > maxCardinality) {
                String text = Messages.ProductCmptRelation_msgMaxCardinalityIsLessThanMin;
                list.add(new Message(MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN, text, Message.ERROR, this, new String[] {
                        PROPERTY_MIN_CARDINALITY, PROPERTY_MAX_CARDINALITY }));
            }
            // For qulified associations the implicit
            if (associationObj.isQualified()) {
                if (getMaxCardinality() > associationObj.getMaxCardinality()) {
                    String text = NLS.bind(Messages.ProductCmptLink_msgMaxCardinalityExceedsModelMaxQualified,
                            this.getMaxCardinality(), associationObj.getMaxCardinality());
                    list.add(new Message(MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX, text, Message.ERROR, this,
                            PROPERTY_MAX_CARDINALITY));
                }
            } else {
                // MTB#515
                // this.maxCardinality + ForAllOtherOfSameAssociation(Sum(other.minCardinality)) <=
                // policyCmptAssociation.maxCardinality
                int maxType = associationObj.getMaxCardinality();
                if (maxType != IProductCmptTypeAssociation.CARDINALITY_MANY) {
                    int sumMinCardinality = this.getMaxCardinality();
                    IProductCmptLink[] links = getProductCmptGeneration().getLinks(getAssociation());
                    if (sumMinCardinality < IProductCmptLink.CARDINALITY_MANY) {
                        for (IProductCmptLink productCmptLink : links) {
                            if (!productCmptLink.equals(this)) {
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
                // MTB#515
                // this.minCardinality + ForAllOtherOfSameAssociation(Sum(other.maxCardinality)) <=
                // policyCmptAssociation.minCardinality
                int minType = associationObj.getMinCardinality();
                int sumMaxCardinality = this.getMinCardinality();
                IProductCmptLink[] links = getProductCmptGeneration().getLinks(getAssociation());
                for (IProductCmptLink productCmptLink : links) {
                    if (!productCmptLink.equals(this)) {
                        if (productCmptLink.getMaxCardinality() == IProductCmptLink.CARDINALITY_MANY) {
                            sumMaxCardinality = IProductCmptLink.CARDINALITY_MANY;
                            break;
                        }
                        sumMaxCardinality += productCmptLink.getMaxCardinality();
                    }
                }
                if (sumMaxCardinality < minType) {
                    String text = NLS.bind(Messages.ProductCmptLink_msgMinCardinalityExceedsModelMin,
                            this.getMinCardinality(), Integer.toString(minType));
                    list.add(new Message(MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN, text, Message.ERROR, this,
                            PROPERTY_MIN_CARDINALITY));
                }
            }
            if (defaultCardinality > maxCardinality || minCardinality > defaultCardinality
                    || defaultCardinality == Integer.MAX_VALUE) {
                String text = NLS.bind(Messages.ProductCmptLink_msgDefaultCardinalityOutOfRange, Integer
                        .toString(minCardinality),
                        maxCardinality == IAssociation.CARDINALITY_MANY ? "*" : Integer.toString(maxCardinality) //$NON-NLS-1$ 
                        );
                list.add(new Message(MSGCODE_DEFAULT_CARDINALITY_OUT_OF_RANGE, text, Message.ERROR, this,
                        PROPERTY_DEFAULT_CARDINALITY));
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
        if (max.equals("*")) { //$NON-NLS-1$
            maxCardinality = CARDINALITY_MANY;
        } else {
            try {
                maxCardinality = Integer.parseInt(max);
            } catch (NumberFormatException e) {
                maxCardinality = 0;
            }
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_ASSOCIATION, association);
        element.setAttribute(PROPERTY_TARGET, target);
        element.setAttribute(PROPERTY_MIN_CARDINALITY, Integer.toString(minCardinality));
        element.setAttribute(PROPERTY_DEFAULT_CARDINALITY, Integer.toString(defaultCardinality));

        if (maxCardinality == CARDINALITY_MANY) {
            element.setAttribute(PROPERTY_MAX_CARDINALITY, "*"); //$NON-NLS-1$
        } else {
            element.setAttribute(PROPERTY_MAX_CARDINALITY, Integer.toString(maxCardinality));
        }
    }

    @Override
    public boolean constrainsPolicyCmptTypeAssociation(IIpsProject ipsProject) throws CoreException {
        if (isDeleted()) {
            return false;
        }
        IProductCmptTypeAssociation association = findAssociation(ipsProject);
        if (association == null) {
            return false;
        }
        IPolicyCmptTypeAssociation matchingPolicyCmptTypeAssociation = association
                .findMatchingPolicyCmptTypeAssociation(ipsProject);
        return matchingPolicyCmptTypeAssociation != null && matchingPolicyCmptTypeAssociation.isConfigured();

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
        IAssociation association = findAssociation(getIpsProject());
        if (association != null) {
            caption = association.getLabelValue(locale);
        }
        return caption;
    }

    @Override
    public String getPluralCaption(Locale locale) throws CoreException {
        ArgumentCheck.notNull(locale);

        String pluralCaption = null;
        IAssociation association = findAssociation(getIpsProject());
        if (association != null) {
            pluralCaption = association.getPluralLabelValue(locale);
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

}
