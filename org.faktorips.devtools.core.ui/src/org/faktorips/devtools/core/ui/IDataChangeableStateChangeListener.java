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

package org.faktorips.devtools.core.ui;

/**
 * A listener for state changes of the data changeable property.
 * 
 * @see org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess
 * 
 * @author Jan Ortmann
 */
public interface IDataChangeableStateChangeListener {

    /**
     * Called when the given object's data changeable state has changed.
     */
    public void dataChangeableStateHasChanged(IDataChangeableReadAccess object);
}
