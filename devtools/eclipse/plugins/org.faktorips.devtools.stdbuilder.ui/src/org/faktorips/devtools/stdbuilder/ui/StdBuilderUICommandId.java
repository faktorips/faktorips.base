/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.ui;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;

/**
 * Enumerates all command IDs and command parameter IDs provided by the Faktor-IPS Standard Builder
 * UI plug-in.
 * 
 * @author Alexander Weickmann
 */
public enum StdBuilderUICommandId {

    COMMAND_NO_SOURCE_CODE_FOUND("org.faktorips.devtools.stdbuilder.ui.commands.NoSourceCodeFound"), //$NON-NLS-1$

    /**
     * Opens a context menu that allows to navigate to the generated Java source code for a selected
     * {@link IIpsObjectPartContainer}. Supports normal selection or indirect selection via opened
     * editor.
     */
    COMMAND_OPEN_JUMP_TO_SOURCE_CODE_CONTEXT_MENU(
            "org.faktorips.devtools.stdbuilder.ui.commands.OpenJumpToSourceCodeContextMenu"); //$NON-NLS-1$

    private String id;

    StdBuilderUICommandId(String id) {
        this.id = id;
    }

    /**
     * Returns the ID of this Faktor-IPS Standard Builder command or parameter.
     */
    public String getId() {
        return id;
    }

}
