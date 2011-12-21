/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class NewProdutCmptValidator {

    public static final String MSGCODE_PREFIX = "NEW_PRODUCT_CMPT_WIZARD-"; //$NON-NLS-1$

    public static final String MSG_INVALID_PROJECT = MSGCODE_PREFIX + "invalidProject"; //$NON-NLS-1$

    public static final String MSG_INVALID_BASE_TYPE = MSGCODE_PREFIX + "invalidBaseType"; //$NON-NLS-1$

    public static final String MSG_INVALID_SELECTED_TYPE = MSGCODE_PREFIX + "invalidSelectedType"; //$NON-NLS-1$

    public static final String MSG_EMPTY_KIND_ID = MSGCODE_PREFIX + "emptyKindId"; //$NON-NLS-1$

    public static final String MSG_INVALID_KIND_ID = MSGCODE_PREFIX + "invalidKindId"; //$NON-NLS-1$

    public static final String MSG_EMPTY_VERSION_ID = MSGCODE_PREFIX + "emptyVersionId"; //$NON-NLS-1$

    public static final String MSG_INVALID_VERSION_ID = MSGCODE_PREFIX + "invalidVersionId"; //$NON-NLS-1$

    public static final String MSG_INVALID_PACKAGE_ROOT = MSGCODE_PREFIX + "invalidPackageRoot"; //$NON-NLS-1$

    public static final String MSG_INVALID_PACKAGE = MSGCODE_PREFIX + "invalidPackage"; //$NON-NLS-1$

    public static final String MSG_SRC_FILE_EXISTS = MSGCODE_PREFIX + "sourceFileExists"; //$NON-NLS-1$

    public static final String MSG_INVALID_FULL_NAME = MSGCODE_PREFIX + "invalidFullName"; //$NON-NLS-1$

    public static final String MSG_INVALID_ADD_TO_GENERATION = MSGCODE_PREFIX + "addToGeneration"; //$NON-NLS-1$

    private final NewProductCmptPMO pmo;

    public NewProdutCmptValidator(NewProductCmptPMO pmo) {
        this.pmo = pmo;
    }

    public MessageList validateTypeSelection() {
        MessageList result = new MessageList();

        if (pmo.getIpsProject() == null || !pmo.getIpsProject().isProductDefinitionProject()) {
            result.add(new Message(MSG_INVALID_PROJECT, Messages.NewProdutCmptValidator_msg_invalidProject,
                    Message.ERROR, pmo, NewProductCmptPMO.PROPERTY_IPS_PROJECT));
        }

        if (pmo.getSelectedBaseType() == null) {
            result.add(new Message(MSG_INVALID_BASE_TYPE, Messages.NewProdutCmptValidator_msg_invalidBaseType,
                    Message.ERROR, pmo, NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE));
        }

        return result;
    }

    public MessageList validateProductCmptPage() {
        IChangesOverTimeNamingConvention convention = IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention();
        MessageList result = new MessageList();
        if ((result = validateTypeSelection()).containsErrorMsg()) {
            // validation only makes sense if there are no error on type selection page.
            return result;
        }

        if (pmo.getSelectedType() == null) {
            result.add(new Message(MSG_INVALID_SELECTED_TYPE, Messages.NewProdutCmptValidator_msg_invalidSelectedType,
                    Message.ERROR, pmo, NewProductCmptPMO.PROPERTY_SELECTED_TYPE));
        }

        if (StringUtils.isEmpty(pmo.getKindId())) {
            result.add(new Message(MSG_EMPTY_KIND_ID, Messages.NewProdutCmptValidator_msg_emptyKindId, Message.ERROR,
                    pmo, NewProductCmptPMO.PROPERTY_KIND_ID));
        }
        if (pmo.isNeedVersionId() && StringUtils.isEmpty(pmo.getVersionId())) {
            result.add(new Message(MSG_EMPTY_VERSION_ID, NLS.bind(Messages.NewProdutCmptValidator_msg_emptyVersionId,
                    convention.getVersionConceptNameSingular()), Message.ERROR, pmo,
                    NewProductCmptPMO.PROPERTY_VERSION_ID));
        }
        if (pmo.getKindId() != null && (!pmo.isNeedVersionId() || pmo.getVersionId() != null)) {
            IProductCmptNamingStrategy productCmptNamingStrategy = pmo.getIpsProject().getProductCmptNamingStrategy();
            try {
                if (!pmo.getKindId().equals(productCmptNamingStrategy.getKindId(pmo.getFullName()))) {
                    result.add(new Message(MSG_INVALID_KIND_ID, Messages.NewProdutCmptValidator_msg_invalidKindId,
                            Message.ERROR, pmo, NewProductCmptPMO.PROPERTY_KIND_ID));
                }
                if (pmo.isNeedVersionId()
                        && !pmo.getVersionId().equals(productCmptNamingStrategy.getVersionId(pmo.getFullName()))) {
                    result.add(new Message(MSG_INVALID_VERSION_ID, NLS.bind(
                            Messages.NewProdutCmptValidator_msg_invalidVersionId,
                            convention.getVersionConceptNameSingular()), Message.ERROR, pmo,
                            NewProductCmptPMO.PROPERTY_VERSION_ID));
                }
            } catch (IllegalArgumentException e) {
                result.add(new Message(MSG_INVALID_FULL_NAME, Messages.NewProdutCmptValidator_msg_invalidFullName,
                        Message.ERROR, pmo, NewProductCmptPMO.PROPERTY_VERSION_ID, NewProductCmptPMO.PROPERTY_KIND_ID));
            }
        }

        if (result.isEmpty()) {
            result.add(additionalValidateProductCmptName());
            result.add(validateAddToGeneration());
        }

        return result;
    }

    MessageList additionalValidateProductCmptName() {
        MessageList result = new MessageList();
        IIpsProjectNamingConventions namingConventions = pmo.getIpsProject().getNamingConventions();
        try {
            result.add(namingConventions.validateUnqualifiedIpsObjectName(IpsObjectType.PRODUCT_CMPT, pmo.getFullName()));
            IIpsSrcFile file = pmo.getIpsProject().findIpsSrcFile(IpsObjectType.PRODUCT_CMPT, pmo.getQualifiedName());
            if (file != null) {
                result.add(new Message(MSG_SRC_FILE_EXISTS, Messages.NewProdutCmptValidator_msg_srcFileExists,
                        Message.ERROR));
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return result;
    }

    public MessageList validateFolderAndPackage() {
        MessageList result = new MessageList();
        if ((result = validateTypeSelection()).containsErrorMsg()) {
            // validation only makes sense if there are no error on type selection page.
            return result;
        }

        if (pmo.getPackageRoot() == null) {
            result.add(new Message(MSG_INVALID_PACKAGE_ROOT, Messages.NewProdutCmptValidator_msg_invalidPackageRoot,
                    Message.ERROR, pmo, NewProductCmptPMO.PROPERTY_PACKAGE_ROOT));
        }

        if (pmo.getIpsPackage() == null || !pmo.getIpsPackage().getRoot().equals(pmo.getPackageRoot())) {
            result.add(new Message(MSG_INVALID_PACKAGE, Messages.NewProdutCmptValidator_msg_invalidPackage,
                    Message.ERROR, pmo, NewProductCmptPMO.PROPERTY_IPS_PACKAGE));
        }

        return result;
    }

    public MessageList validateAll() {
        MessageList result = validateTypeSelection();
        result.add(validateProductCmptPage());
        result.add(validateFolderAndPackage());
        result.add(validateAddToGeneration());
        return result;
    }

    public MessageList validateAddToGeneration() {
        MessageList messageList = new MessageList();
        messageList.add(validateAddToType());
        IProductCmptGeneration generation = pmo.getAddToProductCmptGeneration();
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
        IpsPreferences ipsPreferences = IpsPlugin.getDefault().getIpsPreferences();
        if (generation.isValidFromInPast() && !ipsPreferences.canEditRecentGeneration()) {
            messageList.add(new Message(MSG_INVALID_ADD_TO_GENERATION, NLS.bind(
                    Messages.NewProdutCmptValidator_msg_invalidAddGenerationInPast, generation.getProductCmpt()
                            .getName()), Message.WARNING));
        }
        return messageList;
    }

    MessageList validateAddToType() {
        MessageList result = new MessageList();
        if (pmo.getAddToProductCmptGeneration() != null && pmo.getAddToProductCmptGeneration() != null) {
            IProductCmptTypeAssociation addToAssociation = pmo.getAddToAssociation();
            try {
                IProductCmptType targetProductCmptType = addToAssociation
                        .findTargetProductCmptType(pmo.getIpsProject());
                if (!pmo.getSelectedType().isSubtypeOrSameType(targetProductCmptType, pmo.getIpsProject())) {
                    result.add(new Message(MSG_INVALID_SELECTED_TYPE, NLS.bind(
                            Messages.NewProdutCmptValidator_msg_invalidTypeAddTo, addToAssociation.getName(), pmo
                                    .getAddToProductCmptGeneration().getProductCmpt().getName()), Message.WARNING));
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return result;
    }

}
