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

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionValidator;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class NewTableContentsValidator extends NewProductDefinitionValidator {

    public static final String MSG_NO_PROJECT = MSGCODE_PREFIX + "noProject"; //$NON-NLS-1$

    public static final String MSG_INVALID_PROJECT = MSGCODE_PREFIX + "invalidProject"; //$NON-NLS-1$

    public static final String MSG_NO_STRUCTURE = MSGCODE_PREFIX + "noStructure"; //$NON-NLS-1$

    public static final String MSG_INVALID_STRUCTURE = MSGCODE_PREFIX + "invalidStructure"; //$NON-NLS-1$

    private final NewTableContentsPMO pmo;

    public NewTableContentsValidator(NewTableContentsPMO pmo) {
        super(pmo);
        this.pmo = pmo;
    }

    public MessageList validateTableContents() {
        MessageList result = new MessageList();
        if (pmo.getIpsProject() == null) {
            result.add(new Message(MSG_NO_PROJECT, "Please select a valid Project", Message.ERROR));
        } else if (!pmo.getIpsProject().isProductDefinitionProject()) {
            result.add(new Message(MSG_INVALID_PROJECT, "Please select a Product Definition Project", Message.ERROR));
        }
        if (pmo.getSelectedStructure() == null) {
            result.add(new Message(MSG_NO_STRUCTURE, "Please select a valid Table Structure", Message.ERROR));
        } else if (pmo.getSelectedStructure().getNumOfColumns() == 0) {
            result.add(new Message(
                    MSG_INVALID_STRUCTURE,
                    "The selected table structure does not contains any colums, cannot create contents for this structure.",
                    Message.ERROR));
        }
        if (pmo.getIpsProject() != null) {
            IIpsProjectNamingConventions namingConventions = pmo.getIpsProject().getNamingConventions();
            try {
                result.add(namingConventions.validateUnqualifiedIpsObjectName(IpsObjectType.TABLE_CONTENTS,
                        pmo.getName()));
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

        return result;
    }

    @Override
    protected MessageList validateBeforeFolderAndPacke() {
        return validateTableContents();
    }

}
