/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
