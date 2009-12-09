/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
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
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductCmptLink extends AtomicIpsObjectPart implements IProductCmptLink {

    /**
     * @param target The product component that will be used as target for the new relation.
     * @param relationType The type of the new relation.
     * @param ipsProject The ips project which ips object path is used.
     * @return <code>true</code> if it is possible to create a valid relation with the given
     *         parameters at this time, <code>false</code> otherwise.
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

    // the name of the association this link is an instance of
    private String association = ""; //$NON-NLS-1$

    private String target = ""; //$NON-NLS-1$

    private int minCardinality = 0;

    private int maxCardinality = 1;

    public ProductCmptLink(IProductCmptGeneration generation, int id) {
        super(generation, id);
    }

    public ProductCmptLink() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmpt getProductCmpt() {
        return (IProductCmpt)getParent().getParent();
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptGeneration getProductCmptGeneration() {
        return (IProductCmptGeneration)getParent();
    }

    @Override
    public String getName() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("ProductCmptLink.gif"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getAssociation() {
        return association;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeAssociation findAssociation(IIpsProject ipsProject) throws CoreException {
        IProductCmptType productCmptType = getProductCmpt().findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return null;
        }
        return (IProductCmptTypeAssociation)productCmptType.findAssociation(association, ipsProject);
    }

    void setProductCmptTypeRelation(String newRelation) {
        association = newRelation;
    }

    /**
     * {@inheritDoc}
     */
    public String getTarget() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmpt findTarget(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findProductCmpt(target);
    }

    /**
     * {@inheritDoc}
     */
    public void setTarget(String newTarget) {
        String oldTarget = target;
        target = newTarget;
        valueChanged(oldTarget, target);
    }

    /**
     * {@inheritDoc}
     */
    public int getMinCardinality() {
        return minCardinality;
    }

    /**
     * {@inheritDoc}
     */
    public void setMinCardinality(int newValue) {
        int oldValue = minCardinality;
        minCardinality = newValue;
        valueChanged(oldValue, newValue);

    }

    /**
     * {@inheritDoc}
     */
    public int getMaxCardinality() {
        return maxCardinality;
    }

    /**
     * {@inheritDoc}
     */
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
            String text = NLS.bind(Messages.ProductCmptRelation_msgNoRelationDefined, association, getProductCmpt()
                    .getProductCmptType());
            list.add(new Message(MSGCODE_UNKNWON_ASSOCIATION, text, Message.ERROR, this, PROPERTY_ASSOCIATION));
            return;
        }
        IPolicyCmptTypeAssociation polAssociation = associationObj.findMatchingPolicyCmptTypeAssociation(ipsProject);
        if (polAssociation != null) {
            validateCardinality(list, polAssociation);
        }

        IProductCmpt targetObj = findTarget(ipsProject);
        if (!willBeValid(targetObj, associationObj, ipsProject)) {
            String msg = NLS.bind(Messages.ProductCmptRelation_msgInvalidTarget, target, associationObj
                    .getTargetRoleSingular());
            list.add(new Message(MSGCODE_INVALID_TARGET, msg, Message.ERROR, this, PROPERTY_TARGET));
        }

    }

    private void validateCardinality(MessageList list, IPolicyCmptTypeAssociation associationObj) {
        if (maxCardinality == 0) {
            String text = Messages.ProductCmptRelation_msgMaxCardinalityIsLessThan1;
            list.add(new Message(MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1, text, Message.ERROR, this,
                    PROPERTY_MAX_CARDINALITY));
        } else if (maxCardinality != -1) {
            if (minCardinality > maxCardinality) {
                String text = Messages.ProductCmptRelation_msgMaxCardinalityIsLessThanMin;
                list.add(new Message(MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN, text, Message.ERROR, this, new String[] {
                        PROPERTY_MIN_CARDINALITY, PROPERTY_MAX_CARDINALITY }));
            }
            if (associationObj.getMaxCardinality() != IProductCmptTypeAssociation.CARDINALITY_MANY) {
                int maxType = associationObj.getMaxCardinality();
                if (maxCardinality > maxType) {
                    String text = NLS.bind(Messages.ProductCmptRelation_msgMaxCardinalityExceedsModelMax, ""
                            + (maxCardinality == IAssociation.CARDINALITY_MANY ? "*" : "" + maxCardinality),
                            "" + associationObj.getMaxCardinality()); //$NON-NLS-1$ 
                    list.add(new Message(MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX, text, Message.ERROR, this,
                            PROPERTY_MAX_CARDINALITY));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        association = element.getAttribute(PROPERTY_ASSOCIATION);
        target = element.getAttribute(PROPERTY_TARGET);
        try {
            minCardinality = Integer.parseInt(element.getAttribute(PROPERTY_MIN_CARDINALITY));
        } catch (NumberFormatException e) {
            minCardinality = 0;
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
        element.setAttribute(PROPERTY_MIN_CARDINALITY, "" + minCardinality); //$NON-NLS-1$

        if (maxCardinality == CARDINALITY_MANY) {
            element.setAttribute(PROPERTY_MAX_CARDINALITY, "*"); //$NON-NLS-1$
        } else {
            element.setAttribute(PROPERTY_MAX_CARDINALITY, "" + maxCardinality); //$NON-NLS-1$
        }
    }

    public boolean isMandatory() {
        return getMinCardinality() == 1 && getMaxCardinality() == 1;
    }

    public boolean isOptional() {
        return getMinCardinality() == 0 && getMaxCardinality() == 1;
    }

    public RenameRefactoring getRenameRefactoring() {
        return null;
    }

    public boolean isRenameRefactoringSupported() {
        return false;
    }

}
