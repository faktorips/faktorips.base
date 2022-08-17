/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
