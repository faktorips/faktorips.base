/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.ui.ide.IGotoMarker;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

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
    void gotoIpsObjectPart(IIpsObjectPart part);

}
