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

import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;

/**
 * Listeners that is notified if the active generation changes in a timed ips object editor.
 * 
 * @author Markus Blum
 */
public interface IActiveGenerationChangedListener {

    /**
     * Get the current generation after changes <code>generation</code>.
     * 
     * @param generation current generation.
     */
    void activeGenerationChanged(IIpsObjectGeneration generation);

}
