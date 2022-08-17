/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

/**
 * A listener for state changes of the data changeable property.
 * 
 * @see org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess
 * 
 * @author Jan Ortmann
 */
@FunctionalInterface
public interface IDataChangeableStateChangeListener {

    /**
     * Called when the given object's data changeable state has changed.
     */
    void dataChangeableStateHasChanged(IDataChangeableReadAccess object);
}
