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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.ui.ide.IGotoMarker;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * An adapter interface for editors, which allows the editor to reveal the position of a given
 * {@link IIpsObjectPart} inside the {@link IIpsObject} displayed in the editor.
 * 
 * @since 3.6
 * @see IGotoMarker
 */
public interface IGotoIpsObjectPart {
    /**
     * Sets the cursor and selection state for an editor to reveal the position of the given
     * {@link IIpsObjectPart}.
     * 
     * @param part the {@link IIpsObjectPart}
     */
    public void gotoIpsObjectPart(IIpsObjectPart part);

}
