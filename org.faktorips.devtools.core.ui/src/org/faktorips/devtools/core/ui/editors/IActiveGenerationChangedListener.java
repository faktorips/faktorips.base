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

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;

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
    public void activeGenerationChanged(IIpsObjectGeneration generation);

}
