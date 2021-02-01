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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * Validates the links of a {@link IProductCmptLinkContainer} against their corresponding
 * associations in the product component type hierarchy. Ensures that
 * <ul>
 * <li>the number of links for a single association:</li>
 * <ul>
 * <li>satisfies the minimum cardinality, and</li>
 * <li>does not exceed the maximum cardinality defined by the type association.</li>
 * </ul>
 * <li>no duplicate targets exist. That is a product component is never linked (or used) more than
 * once by this container.</li>
 * <li>a link has an effective date that is before or equal to the effective date of the referencing
 * product component link container.</li>
 * </ul>
 * 
 */
public class ProductCmptLinkContainerValidator extends TypeHierarchyVisitor<IProductCmptType> {

    private MessageList list;
    private final IProductCmptLinkContainer linkContainer;

    public ProductCmptLinkContainerValidator(IIpsProject ipsProject, IProductCmptLinkContainer linkContainer) {
        super(ipsProject);
        this.linkContainer = linkContainer;
        this.list = new MessageList();
    }

    public void startAndAddMessagesToList(IProductCmptType type, MessageList parentList) {
        this.list = new MessageList();
        start(type);
        parentList.add(list);
    }

    @Override
    protected boolean visit(IProductCmptType currentType) {
        List<IProductCmptTypeAssociation> associations = currentType.getProductCmptTypeAssociations();
        for (IProductCmptTypeAssociation association : associations) {
            if (association.isDerivedUnion()) {
                continue;
            }
            if (!linkContainer.isContainerFor(association)) {
                continue;
            }
            validateAssociation(association);
        }

        return true;
    }

    protected void validateAssociation(IAssociation association) {
        List<IProductCmptLink> relations = linkContainer.getLinksAsList(association.getTargetRoleSingular());
        addMessageIfAssociationHasValidationMessages(association, list);
        addMessageIfLessLinksThanMinCard(association, relations, list);
        addMessageIfMoreLinksThanMaxCard(association, relations, list);
        addMessageIfDuplicateTargetPresent(association, relations, list);

        IIpsProjectProperties props = getIpsProject().getReadOnlyProperties();
        if (props.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled()) {
            addMessageIfTargetNotValidOnValidFromDate(association, relations, list);
        }

        if (association instanceof IProductCmptTypeAssociation) {
            IProductCmptTypeAssociation productAssociation = (IProductCmptTypeAssociation)association;
            IPolicyCmptTypeAssociation policyCmptTypeAssociation = productAssociation
                    .findMatchingPolicyCmptTypeAssociation(getIpsProject());
            validateCardinality(policyCmptTypeAssociation, relations, list);
        }
    }

    private void validateCardinality(IPolicyCmptTypeAssociation policyCmptTypeAssociation,
            List<IProductCmptLink> relations,
            MessageList list) {
        if (policyCmptTypeAssociation != null && !policyCmptTypeAssociation.isQualified()) {
            validateTotalMax(relations, list, policyCmptTypeAssociation);
            if (!linkContainer.isProductTemplate()) {
                validateTotalMin(relations, list, policyCmptTypeAssociation);
            }
        }
    }

    private void validateTotalMax(List<IProductCmptLink> relations,
            MessageList list,
            IPolicyCmptTypeAssociation associationObj) {
        int maxType = associationObj.getMaxCardinality();
        if (maxType != IProductCmptTypeAssociation.CARDINALITY_MANY) {
            int sumMinCardinality = relations.stream().mapToInt(IProductCmptLink::getMinCardinality).sum();
            for (IProductCmptLink productCmptLink : relations) {
                int sumCardinality;
                if (productCmptLink.getMaxCardinality() < Cardinality.CARDINALITY_MANY) {
                    sumCardinality = sumMinCardinality;
                    sumCardinality += productCmptLink.getMaxCardinality();
                    sumCardinality -= productCmptLink.getMinCardinality();
                } else {
                    sumCardinality = Cardinality.CARDINALITY_MANY;
                }
                if (sumCardinality > maxType) {
                    String text = NLS.bind(Messages.ProductCmptLink_msgMaxCardinalityExceedsModelMax,
                            productCmptLink.getMaxCardinality(), Integer.toString(maxType));
                    list.add(
                            new Message(IProductCmptLink.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX, text, Message.ERROR,
                                    productCmptLink,
                                    IProductCmptLink.PROPERTY_MAX_CARDINALITY));
                }
            }
        }

    }

    private void validateTotalMin(List<IProductCmptLink> relations,
            MessageList list,
            IPolicyCmptTypeAssociation associationObj) {
        int minType = associationObj.getMinCardinality();
        for (IProductCmptLink productCmptLink : relations) {
            int sumMaxCardinality = productCmptLink.getMinCardinality();
            for (IProductCmptLink link : relations) {
                if (!productCmptLink.equals(link)) {
                    if (link.getMaxCardinality() == Cardinality.CARDINALITY_MANY) {
                        sumMaxCardinality = Cardinality.CARDINALITY_MANY;
                        break;
                    }
                    sumMaxCardinality += link.getMaxCardinality();
                }
            }
            if (sumMaxCardinality < minType) {
                addTotalMinMessage(list, minType, productCmptLink);
            }
        }
    }

    private void addTotalMinMessage(MessageList list, int minType, IProductCmptLink link) {
        String text = NLS.bind(Messages.ProductCmptLink_msgMinCardinalityExceedsModelMin, link.getMinCardinality(),
                Integer.toString(minType));
        list.newError(IProductCmptLink.MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN, text, link,
                IProductCmptLink.PROPERTY_MIN_CARDINALITY);
    }

    protected void addMessageIfDuplicateTargetPresent(IAssociation association,
            List<IProductCmptLink> relations,
            MessageList messageList) {
        Set<String> targets = new HashSet<String>();
        String msg = null;
        for (IProductCmptLink relation : relations) {
            String target = relation.getTarget();
            if (!targets.add(target)) {
                if (msg == null) {
                    String associationLabel = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(association);
                    msg = NLS.bind(Messages.ProductCmptGeneration_msgDuplicateTarget, associationLabel, target);
                }
                messageList.add(new Message(IProductCmptLinkContainer.MSGCODE_DUPLICATE_RELATION_TARGET, msg,
                        Message.ERROR, association.getTargetRoleSingular()));
            }
        }
    }

    protected void addMessageIfMoreLinksThanMaxCard(IAssociation association,
            List<IProductCmptLink> relations,
            MessageList messageList) {
        int maxCardinality = association.getMaxCardinality();
        if (maxCardinality < relations.size()) {
            String associationLabel = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(association);
            Object[] params = { Integer.valueOf(relations.size()), "" + maxCardinality, associationLabel }; //$NON-NLS-1$
            String msg = NLS.bind(Messages.ProductCmptGeneration_msgTooManyRelations, params);
            ObjectProperty prop1 = new ObjectProperty(this, null);
            ObjectProperty prop2 = new ObjectProperty(association.getTargetRoleSingular(), null);
            messageList.add(new Message(IProductCmptLinkContainer.MSGCODE_TOO_MANY_RELATIONS, msg, Message.ERROR,
                    new ObjectProperty[] { prop1, prop2 }));
        }
    }

    protected void addMessageIfLessLinksThanMinCard(IAssociation association,
            List<IProductCmptLink> relations,
            MessageList messageList) {
        int minCardinality = association.getMinCardinality();
        if (!linkContainer.isProductTemplate() && minCardinality > relations.size()) {
            addBelowMinCardinalityErrorMessage(association, relations, messageList, minCardinality);
        }
    }

    protected void addBelowMinCardinalityErrorMessage(IAssociation association,
            List<IProductCmptLink> relations,
            MessageList messageList,
            int minCardinality) {
        String associationLabel = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(association);
        Object[] params = { Integer.valueOf(relations.size()), associationLabel, Integer.valueOf(minCardinality) };
        String msg = NLS.bind(Messages.ProductCmptGeneration_msgNotEnoughRelations, params);
        ObjectProperty prop1 = new ObjectProperty(this, null);
        ObjectProperty prop2 = new ObjectProperty(association.getTargetRoleSingular(), null);
        messageList.add(new Message(IProductCmptLinkContainer.MSGCODE_NOT_ENOUGH_RELATIONS, msg, Message.ERROR,
                new ObjectProperty[] { prop1, prop2 }));
    }

    protected void addMessageIfAssociationHasValidationMessages(IAssociation association, MessageList messageList) {
        MessageList relMessages = getErrorMessagesFor(association);
        if (!relMessages.isEmpty()) {
            messageList.add(relMessages, new ObjectProperty(association.getTargetRoleSingular(), null), true);
        }
    }

    protected void addMessageIfTargetNotValidOnValidFromDate(IAssociation association,
            List<IProductCmptLink> links,
            MessageList msgList) {

        for (IProductCmptLink link : links) {
            // associations of type association will be excluded from this constraint. If the type
            // of the association
            // cannot be determined then the link will not be evaluated
            if (association == null || association.isAssoziation()) {
                continue;
            }
            IProductCmpt productCmpt;
            try {
                productCmpt = link.findTarget(linkContainer.getIpsProject());
                if (productCmpt != null) {
                    if (linkContainer.getValidFrom() != null
                            && productCmpt.getGenerationEffectiveOn(linkContainer.getValidFrom()) == null) {
                        String dateString = IIpsModelExtensions.get().getModelPreferences().getDateFormat()
                                .format(linkContainer.getValidFrom().getTime());
                        String generationName = IIpsModelExtensions.get().getModelPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNameSingular();
                        String text = NLS.bind(
                                Messages.ProductCmptGeneration_msgNoGenerationInLinkedTargetForEffectiveDate,
                                new Object[] { productCmpt.getQualifiedName(), generationName, dateString });
                        msgList.add(new Message(IProductCmptLinkContainer.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE,
                                text, Message.ERROR, link));
                    }
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    private MessageList getErrorMessagesFor(IAssociation association) {
        try {
            MessageList errorList = association.validate(getIpsProject());
            errorList = errorList.getMessages(Message.ERROR);
            return errorList;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

}