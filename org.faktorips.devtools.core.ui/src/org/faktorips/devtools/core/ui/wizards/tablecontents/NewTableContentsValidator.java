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

import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionValidator;
import org.faktorips.util.message.MessageList;

public class NewTableContentsValidator extends NewProductDefinitionValidator {

    private final NewTableContentsPMO pmo;

    public NewTableContentsValidator(NewTableContentsPMO pmo) {
        super(pmo);
        this.pmo = pmo;
    }

    @Override
    protected MessageList validateBeforeFolderAndPacke() {
        // TODO Auto-generated method stub
        return new MessageList();
    }

}
