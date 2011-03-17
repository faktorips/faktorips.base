/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

/**
 * Enumerates all command IDs and command parameter IDs provided by the Faktor-IPS Standard Builder
 * UI plug-in.
 * 
 * @author Alexander Weickmann
 */
public enum StdBuilderUICommandId {

    COMMAND_NO_SOURCE_CODE_FOUND("org.faktorips.devtools.stdbuilder.ui.commands.NoSourceCodeFound"),

    /**
     * Capable of opening the generated Java types for a selected {@link IIpsObject}. Supports
     * normal selection or indirect selection via opened editor.
     */
    COMMAND_OPEN_IPS_OBJECT_IN_JAVA_EDITOR("org.faktorips.devtools.stdbuilder.ui.commands.OpenIpsObjectInJavaEditor");

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
