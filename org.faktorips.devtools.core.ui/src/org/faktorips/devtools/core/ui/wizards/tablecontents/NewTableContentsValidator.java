/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionValidator;
import org.faktorips.devtools.model.internal.ipsobject.DeprecationValidation;
import org.faktorips.devtools.model.internal.tablecontents.SingleTableContentsValidator;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

public class NewTableContentsValidator extends NewProductDefinitionValidator {

    public static final String MSG_NO_PROJECT = MSGCODE_PREFIX + "noProject"; //$NON-NLS-1$

    public static final String MSG_NO_STRUCTURE = MSGCODE_PREFIX + "noStructure"; //$NON-NLS-1$

    public static final String MSG_INVALID_STRUCTURE = MSGCODE_PREFIX + "invalidStructure"; //$NON-NLS-1$

    public NewTableContentsValidator(NewTableContentsPMO pmo) {
        super(pmo);
    }

    @Override
    public NewTableContentsPMO getPmo() {
        return (NewTableContentsPMO)super.getPmo();
    }

    @Override
    public MessageList validateAllPages() {
        MessageList result = validateTableContents();
        result.add(validateFolderAndPackage());
        return result;
    }

    public MessageList validateTableContents() {
        MessageList result = new MessageList();
        validateProject(result);
        validateStructure(result);
        validateTableContentName(result);
        return result;
    }

    private void validateProject(MessageList result) {
        if (getPmo().getIpsProject() == null) {
            result.add(new Message(MSG_NO_PROJECT, Messages.NewTableContentsValidator_msg_noProject, Message.ERROR));
        }
    }

    private void validateStructure(MessageList result) {
        if (getPmo().getSelectedStructure() == null) {
            result.add(
                    new Message(MSG_NO_STRUCTURE, Messages.NewTableContentsValidator_msg_noStructure, Message.ERROR));
        } else {
            validateSelectedStructure(result, getPmo().getIpsProject(), getPmo().getSelectedStructure());
        }
    }

    private void validateSelectedStructure(MessageList result,
            IIpsProject ipsProject,
            ITableStructure selectedTableStructure) {
        if (selectedTableStructure.getNumOfColumns() == 0) {
            result.add(new Message(MSG_INVALID_STRUCTURE, Messages.NewTableContentsValidator_msgInvalidStructure,
                    Message.ERROR));
        }
        SingleTableContentsValidator singleTableContentsValidator = new SingleTableContentsValidator(ipsProject,
                selectedTableStructure);
        if (singleTableContentsValidator.forbidsAdditionalContents()) {
            result.add(new Message(MSG_INVALID_STRUCTURE,
                    Messages.NewTableContentsValidator_msgNoAdditionalContentsAllowed, Message.ERROR));
        }
        DeprecationValidation.validateTableStructureIsNotDeprecated(null, getPmo().getQualifiedName(),
                selectedTableStructure, ipsProject, result);
    }

    private void validateTableContentName(MessageList result) {
        if (getPmo().getIpsProject() != null) {
            result.add(validateIpsObjectName(NewTableContentsPMO.PROPERTY_NAME));
        }
    }

    @Override
    protected MessageList validateBeforeFolderAndPacket() {
        return validateTableContents();
    }

}
