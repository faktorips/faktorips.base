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

package org.faktorips.devtools.core.refactor;

import org.eclipse.ltk.core.refactoring.participants.RenameArguments;

/**
 * Arguments for Faktor-IPS "Rename" refactorings.
 * <p>
 * In addition to LTK's {@link RenameArguments} this class contains information about the new plural
 * name of the element to be refactored.
 * 
 * @author Alexander Weickmann
 */
public final class IpsRenameArguments extends RenameArguments {

    private final String newPluralName;

    public IpsRenameArguments(String newName, String newPluralName, boolean updateReferences) {
        super(newName, updateReferences);
        this.newPluralName = newPluralName;
    }

    public String getNewPluralName() {
        return newPluralName;
    }

}
