/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * Validates the links of a {@link IProductCmptLinkContainer} against their corresponding
 * associations in the product component type hierarchy. Ensures that
 * <ul>
 * <li>the number of links for a single association:</li>
 * <ul>
 * <li>
 * satisfies the minimum cardinality, and</li>
 * <li>
 * does not exceed the maximum cardinality defined by the type association.</li>
 * </ul>
 * <li>no duplicate targets exist. That is a product component is never linked (or used) more than
 * once by this container.</li> </ul>
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
        try {
            this.list = new MessageList();
            start(type);
            parentList.add(list);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    protected boolean visit(IProductCmptType currentType) throws CoreException {
        List<IAssociation> associations = currentType.getAssociations();
        for (IAssociation association : associations) {
            if (association.isDerivedUnion()) {
                continue;
            }
            List<IProductCmptLink> relations = linkContainer.getLinksAsList(association.getTargetRoleSingular());
            addMessageIfAssociationHasValidationMessages(association, list);
            addMessageIfLessLinksThanMinCard(association, relations, list);
            addMessageIfMoreLinksThanMaxCard(association, relations, list);
            addMessageIfDuplicateTargetPresent(association, relations, list);
        }

        return true;
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
                    String associationLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(association);
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
            String associationLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(association);
            Object[] params = { new Integer(relations.size()), "" + maxCardinality, associationLabel }; //$NON-NLS-1$
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
        if (minCardinality > relations.size()) {
            addBelowMinCardinalityErrorMessage(association, relations, messageList, minCardinality);
        }
    }

    protected void addBelowMinCardinalityErrorMessage(IAssociation association,
            List<IProductCmptLink> relations,
            MessageList messageList,
            int minCardinality) {
        String associationLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(association);
        Object[] params = { new Integer(relations.size()), associationLabel, new Integer(minCardinality) };
        String msg = NLS.bind(Messages.ProductCmptGeneration_msgNotEnoughRelations, params);
        ObjectProperty prop1 = new ObjectProperty(this, null);
        ObjectProperty prop2 = new ObjectProperty(association.getTargetRoleSingular(), null);
        messageList.add(new Message(IProductCmptLinkContainer.MSGCODE_NOT_ENOUGH_RELATIONS, msg, Message.ERROR,
                new ObjectProperty[] { prop1, prop2 }));
    }

    protected void addMessageIfAssociationHasValidationMessages(IAssociation association, MessageList messageList) {
        try {
            // get all messages for the relation types and add them
            MessageList relMessages = association.validate(ipsProject);
            if (!relMessages.isEmpty()) {
                messageList.add(relMessages, new ObjectProperty(association.getTargetRoleSingular(), null), true);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

}