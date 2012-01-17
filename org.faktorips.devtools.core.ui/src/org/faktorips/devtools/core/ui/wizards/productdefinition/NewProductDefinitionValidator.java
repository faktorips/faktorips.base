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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public abstract class NewProductDefinitionValidator {

    public static final String MSGCODE_PREFIX = "NEW_PRODUCT_CMPT_WIZARD-"; //$NON-NLS-1$

    public static final String MSG_INVALID_PACKAGE_ROOT = MSGCODE_PREFIX + "invalidPackageRoot"; //$NON-NLS-1$

    public static final String MSG_INVALID_PACKAGE = MSGCODE_PREFIX + "invalidPackage"; //$NON-NLS-1$

    public static final String MSG_INVALID_EFFECTIVE_DATE = MSGCODE_PREFIX + "invalidEffectiveDate"; //$NON-NLS-1$

    private final NewProductDefinitionPMO pmo;

    public NewProductDefinitionValidator(NewProductDefinitionPMO pmo) {
        super();
        this.pmo = pmo;
    }

    public MessageList validateFolderAndPackage() {
        MessageList result = new MessageList();
        if ((result = validateBeforeFolderAndPacke()).containsErrorMsg()) {
            // validation only makes sense if there are no error on type selection page.
            return result;
        }

        if (pmo.getPackageRoot() == null) {
            result.add(new Message(MSG_INVALID_PACKAGE_ROOT,
                    Messages.NewProductDefinitionValidator_msg_invalidPackageRoot, Message.ERROR, pmo,
                    NewProductDefinitionPMO.PROPERTY_PACKAGE_ROOT));
        }

        if (pmo.getIpsPackage() == null || !pmo.getIpsPackage().getRoot().equals(pmo.getPackageRoot())) {
            result.add(new Message(MSG_INVALID_PACKAGE, Messages.NewProductDefinitionValidator_msg_invalidPackage,
                    Message.ERROR, pmo, NewProductDefinitionPMO.PROPERTY_IPS_PACKAGE));
        }

        return result;
    }

    protected abstract MessageList validateBeforeFolderAndPacke();

}