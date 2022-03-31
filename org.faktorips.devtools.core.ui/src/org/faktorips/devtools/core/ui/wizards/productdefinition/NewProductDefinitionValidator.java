/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * This validator is called to get all relevant messages while creating an the new object. The
 * messages will be shown in message section on top of the dialog.
 * <p>
 * Currently this basic implementation is used for the last page (select root-folder and package).
 * You have to implement at least the method {@link #validateBeforeFolderAndPacket()}.
 * 
 * @author dirmeier
 */
public abstract class NewProductDefinitionValidator {

    public static final String MSGCODE_PREFIX = "NEW_PRODUCT_CMPT_WIZARD-"; //$NON-NLS-1$

    public static final String MSG_INVALID_PACKAGE_ROOT = MSGCODE_PREFIX + "invalidPackageRoot"; //$NON-NLS-1$

    public static final String MSG_INVALID_PACKAGE = MSGCODE_PREFIX + "invalidPackage"; //$NON-NLS-1$

    public static final String MSG_INVALID_EFFECTIVE_DATE = MSGCODE_PREFIX + "invalidEffectiveDate"; //$NON-NLS-1$

    public static final String MSG_SRC_FILE_EXISTS = MSGCODE_PREFIX + "sourceFileExists"; //$NON-NLS-1$

    private final NewProductDefinitionPMO pmo;

    public NewProductDefinitionValidator(NewProductDefinitionPMO pmo) {
        this.pmo = pmo;
    }

    /**
     * This method should validate everything. It is called by the wizard to allow finish or to
     * disable finish. Finish is allowed if there is no error message. You have to include
     * {@link #validateFolderAndPackage()} and {@link #validateIpsObjectName(String)} in your
     * result.
     * 
     * @return A {@link MessageList} containing all validation errors for creation of the page.
     */
    public abstract MessageList validateAllPages();

    public MessageList validateFolderAndPackage() {
        MessageList result = validateBeforeFolderAndPacket();
        if (result.containsErrorMsg()) {
            // validation only makes sense if there are no error on type selection page.
            return result;
        }

        if (getPmo().getPackageRoot() == null) {
            result.add(new Message(MSG_INVALID_PACKAGE_ROOT,
                    Messages.NewProductDefinitionValidator_msg_invalidPackageRoot, Message.ERROR, getPmo(),
                    NewProductDefinitionPMO.PROPERTY_PACKAGE_ROOT));
        }

        if (getPmo().getIpsPackage() == null || !getPmo().getIpsPackage().getRoot().equals(getPmo().getPackageRoot())) {
            result.add(new Message(MSG_INVALID_PACKAGE, Messages.NewProductDefinitionValidator_msg_invalidPackage,
                    Message.ERROR, getPmo(), NewProductDefinitionPMO.PROPERTY_IPS_PACKAGE));
        }

        return result;
    }

    protected MessageList validateIpsObjectName(String propertyOfNameField) {
        MessageList result = new MessageList();
        if (getPmo().getIpsProject() != null) {
            IIpsProjectNamingConventions namingConventions = getPmo().getIpsProject().getNamingConventions();
            MessageList msgListName = namingConventions.validateUnqualifiedIpsObjectName(getPmo()
                    .getIpsObjectType(), getPmo().getName());
            result.add(addInvalidObjectProperty(msgListName, propertyOfNameField));
            IIpsSrcFile file = getPmo().getIpsProject().findIpsSrcFile(getPmo().getIpsObjectType(),
                    getPmo().getQualifiedName());
            if (file != null) {
                result.add(new Message(MSG_SRC_FILE_EXISTS,
                        Messages.NewProductDefinitionValidator_msg_srcFileExists, Message.ERROR, getPmo(),
                        propertyOfNameField));
            }
        }
        return result;
    }

    protected MessageList addInvalidObjectProperty(MessageList msgListName, String propertyOfNameField) {
        MessageList result = new MessageList();
        for (Message msg : msgListName) {
            result.add(new Message(msg.getCode(), msg.getText(), msg.getSeverity(), getPmo(), propertyOfNameField));
        }
        return result;
    }

    /**
     * This method is called before the root folder and package validation is called to be sure.
     * Only if this validation does return no error the further validations will be performed.
     * 
     * @return The {@link MessageList} containing all validation error except those of the root
     *         folder and the package
     */
    protected abstract MessageList validateBeforeFolderAndPacket();

    public NewProductDefinitionPMO getPmo() {
        return pmo;
    }

}