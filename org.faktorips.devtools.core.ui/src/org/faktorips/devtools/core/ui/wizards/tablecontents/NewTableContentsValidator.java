/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionValidator;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class NewTableContentsValidator extends NewProductDefinitionValidator {

    public static final String MSG_NO_PROJECT = MSGCODE_PREFIX + "noProject"; //$NON-NLS-1$

    public static final String MSG_INVALID_PROJECT = MSGCODE_PREFIX + "invalidProject"; //$NON-NLS-1$

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
        if (getPmo().getIpsProject() == null) {
            result.add(new Message(MSG_NO_PROJECT, Messages.NewTableContentsValidator_msg_noProject, Message.ERROR));
        } else if (!getPmo().getIpsProject().isProductDefinitionProject()) {
            result.add(new Message(MSG_INVALID_PROJECT, Messages.NewTableContentsValidator_msg_invalidProject,
                    Message.ERROR));
        }
        if (getPmo().getSelectedStructure() == null) {
            result.add(new Message(MSG_NO_STRUCTURE, Messages.NewTableContentsValidator_msg_noStructure, Message.ERROR));
        } else if (getPmo().getSelectedStructure().getNumOfColumns() == 0) {
            result.add(new Message(MSG_INVALID_STRUCTURE, Messages.NewTableContentsValidator_msgInvalidStructure,
                    Message.ERROR));
        }
        if (getPmo().getIpsProject() != null) {
            validateIpsObjectName(NewTableContentsPMO.PROPERTY_NAME);
        }

        return result;
    }

    @Override
    protected MessageList validateBeforeFolderAndPacket() {
        return validateTableContents();
    }

}
