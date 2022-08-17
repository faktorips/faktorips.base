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

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionPMO;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionValidator;
import org.faktorips.devtools.model.internal.ipsobject.DeprecationValidation;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

public class NewProductCmptValidator extends NewProductDefinitionValidator {

    public static final String MSG_INVALID_PROJECT = MSGCODE_PREFIX + "invalidProject"; //$NON-NLS-1$

    public static final String MSG_INVALID_BASE_TYPE = MSGCODE_PREFIX + "invalidBaseType"; //$NON-NLS-1$

    public static final String MSG_INVALID_SELECTED_TYPE = MSGCODE_PREFIX + "invalidSelectedType"; //$NON-NLS-1$

    public static final String MSG_EMPTY_KIND_ID = MSGCODE_PREFIX + "emptyKindId"; //$NON-NLS-1$

    public static final String MSG_INVALID_KIND_ID = MSGCODE_PREFIX + "invalidKindId"; //$NON-NLS-1$

    public static final String MSG_EMPTY_VERSION_ID = MSGCODE_PREFIX + "emptyVersionId"; //$NON-NLS-1$

    public static final String MSG_INVALID_VERSION_ID = MSGCODE_PREFIX + "invalidVersionId"; //$NON-NLS-1$

    public static final String MSG_INVALID_FULL_NAME = MSGCODE_PREFIX + "invalidFullName"; //$NON-NLS-1$

    public static final String MSG_INVALID_ADD_TO_GENERATION = MSGCODE_PREFIX + "addToGeneration"; //$NON-NLS-1$

    public NewProductCmptValidator(NewProductCmptPMO pmo) {
        super(pmo);
    }

    @Override
    public NewProductCmptPMO getPmo() {
        return (NewProductCmptPMO)super.getPmo();
    }

    public MessageList validateTypeSelection() {
        MessageList result = new MessageList();

        if (getPmo().getIpsProject() == null) {
            result.add(new Message(MSG_INVALID_PROJECT, Messages.NewProdutCmptValidator_msg_invalidProject,
                    Message.ERROR, getPmo(), NewProductCmptPMO.PROPERTY_IPS_PROJECT));
        }

        if (getPmo().getSelectedBaseType() == null) {
            result.add(new Message(MSG_INVALID_BASE_TYPE, Messages.NewProdutCmptValidator_msg_invalidBaseType,
                    Message.ERROR, getPmo(), NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE));
        }

        return result;
    }

    public MessageList validateProductCmptPage() {
        MessageList result = validateTypeSelection();
        if (result.containsErrorMsg()) {
            // validation only makes sense if there are no error on type selection page.
            return result;
        }

        if (getPmo().getSelectedType() == null) {
            result.add(new Message(MSG_INVALID_SELECTED_TYPE, Messages.NewProdutCmptValidator_msg_invalidSelectedType,
                    Message.ERROR, getPmo(), NewProductCmptPMO.PROPERTY_SELECTED_TYPE));
        } else {
            DeprecationValidation.validateProductCmptTypeIsNotDeprecated(null, getPmo().getQualifiedName(),
                    getPmo().getSelectedType(), getPmo().getIpsProject(), result);
        }

        if (StringUtils.isEmpty(getPmo().getKindId())) {
            result.add(new Message(MSG_EMPTY_KIND_ID, Messages.NewProdutCmptValidator_msg_emptyKindId, Message.ERROR,
                    getPmo(), NewProductCmptPMO.PROPERTY_KIND_ID));
        }

        validateNamingConvention(result);

        if (getPmo().getEffectiveDate() == null) {
            result.newError(MSG_INVALID_EFFECTIVE_DATE, Messages.NewProdutCmptValidator_msg_invalidEffectiveDate,
                    getPmo(), NewProductDefinitionPMO.PROPERTY_EFFECTIVE_DATE);
        }

        if (result.isEmpty()) {
            result.add(validateIpsObjectName(NewProductCmptPMO.PROPERTY_KIND_ID));
            result.add(validateAddToGeneration());
        }

        return result;
    }

    private void validateNamingConvention(MessageList result) {
        final IChangesOverTimeNamingConvention convention = getChangeOverTimeNamingConvention();
        if (getPmo().isNeedVersionId() && StringUtils.isEmpty(getPmo().getVersionId())) {
            result.add(new Message(MSG_EMPTY_VERSION_ID, NLS.bind(Messages.NewProdutCmptValidator_msg_emptyVersionId,
                    convention.getVersionConceptNameSingular()), Message.ERROR, getPmo(),
                    NewProductCmptPMO.PROPERTY_VERSION_ID));
        }
        if (getPmo().getKindId() != null && (!getPmo().isNeedVersionId() || getPmo().getVersionId() != null)) {
            final IProductCmptNamingStrategy productCmptNamingStrategy = getPmo().getIpsProject()
                    .getProductCmptNamingStrategy();
            try {
                if (!getPmo().getKindId().equals(productCmptNamingStrategy.getKindId(getPmo().getName()))) {
                    result.add(new Message(MSG_INVALID_KIND_ID, Messages.NewProdutCmptValidator_msg_invalidKindId,
                            Message.ERROR, getPmo(), NewProductCmptPMO.PROPERTY_KIND_ID));
                }
                if (getPmo().isNeedVersionId()
                        && !getPmo().getVersionId()
                                .equals(productCmptNamingStrategy.getVersionId(getPmo().getName()))) {
                    result.add(new Message(MSG_INVALID_VERSION_ID, NLS.bind(
                            Messages.NewProdutCmptValidator_msg_invalidVersionId,
                            convention.getVersionConceptNameSingular()), Message.ERROR, getPmo(),
                            NewProductCmptPMO.PROPERTY_VERSION_ID));
                }
            } catch (IllegalArgumentException e) {
                result.add(new Message(MSG_INVALID_FULL_NAME, Messages.NewProdutCmptValidator_msg_invalidFullName,
                        Message.ERROR, getPmo(), NewProductCmptPMO.PROPERTY_VERSION_ID,
                        NewProductCmptPMO.PROPERTY_KIND_ID));
            }
        }
    }

    private IChangesOverTimeNamingConvention getChangeOverTimeNamingConvention() {
        return IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention();
    }

    @Override
    public MessageList validateAllPages() {
        MessageList result = validateTypeSelection();
        result.add(validateProductCmptPage());
        result.add(validateFolderAndPackage());
        result.add(validateAddToGeneration());
        return result;
    }

    public MessageList validateAddToGeneration() {
        MessageList messageList = new MessageList();
        messageList.add(validateAddToType());
        IProductCmptGeneration generation = getPmo().getAddToProductCmptGeneration();
        if (generation == null) {
            return messageList;
        }
        IIpsSrcFile ipsSrcFile = generation.getIpsSrcFile();
        if (!IpsUIPlugin.isEditable(ipsSrcFile)) {
            messageList
                    .add(new Message(MSG_INVALID_ADD_TO_GENERATION, NLS.bind(
                            Messages.NewProdutCmptValidator_msg_invalidAddToGeneration, ipsSrcFile.getName()),
                            Message.WARNING));
        }
        return messageList;
    }

    MessageList validateAddToType() {
        MessageList result = new MessageList();
        if (getPmo().getAddToProductCmptGeneration() != null) {
            IProductCmptTypeAssociation addToAssociation = getPmo().getAddToAssociation();
            IProductCmptType targetProductCmptType = addToAssociation.findTargetProductCmptType(getPmo()
                    .getIpsProject());
            if (getPmo().getSelectedType() == null
                    || !getPmo().getSelectedType().isSubtypeOrSameType(targetProductCmptType,
                            getPmo().getIpsProject())) {
                result.add(new Message(MSG_INVALID_SELECTED_TYPE, NLS.bind(
                        Messages.NewProdutCmptValidator_msg_invalidTypeAddTo, addToAssociation.getName(), getPmo()
                                .getAddToProductCmptGeneration().getProductCmpt().getName()),
                        Message.WARNING));
            }
        }
        return result;
    }

    @Override
    protected MessageList validateBeforeFolderAndPacket() {
        return validateTypeSelection();
    }

}
