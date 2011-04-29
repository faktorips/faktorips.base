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

package org.faktorips.devtools.stdbuilder.ui;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * Enumerates all command IDs and command parameter IDs provided by the Faktor-IPS Standard Builder
 * UI plug-in.
 * 
 * @author Alexander Weickmann
 */
public enum StdBuilderUICommandId {

    COMMAND_NO_SOURCE_CODE_FOUND("org.faktorips.devtools.stdbuilder.ui.commands.NoSourceCodeFound"),

    /**
     * Opens a context menu that allows to navigate to the generated Java source code for a selected
     * {@link IIpsObjectPartContainer}. Supports normal selection or indirect selection via opened
     * editor.
     */
    COMMAND_OPEN_JUMP_TO_SOURCE_CODE_CONTEXT_MENU(
            "org.faktorips.devtools.stdbuilder.ui.commands.OpenJumpToSourceCodeContextMenu");

    private String id;

    private StdBuilderUICommandId(String id) {
        this.id = id;
    }

    /**
     * Returns the ID of this Faktor-IPS Standard Builder command or parameter.
     */
    public String getId() {
        return id;
    }

}
