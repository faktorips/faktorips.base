/*******************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.faktorips.devtools.core.ui.actions.ToggleLinkingAction;

/**
 * A view may implement this interface in order to support 'link with editor' 
 * behaviour.
 * 
 *  {@link ToggleLinkingAction}
 * 
 * @author Markus Blum
 */
public interface IToggleLinking {

    /**
     * Is the editor linked to the navigator.
     * 
     * @return <code>true</code> if linking is enabled.
     */
    public boolean isLinkingEnabled();

    /**
     * Save the state and link editor with navigator if linkingEnabled is <code>true</code>.
     * 
     * @param linkingEnabled Should linking be enabled.
     */
    public void setLinkingEnabled(boolean linkingEnabled);

}
