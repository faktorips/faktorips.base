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
 * Extension of the SwitchDataChangeableSupport with additional listener support.
 * 
 * @author Jan Ortmann
 */
public interface IDataChangeableReadAccessWithListenerSupport extends IDataChangeableReadAccess {

    /**
     * Adds the listener.
     */
    void addDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener);

    /**
     * Removes the listener.
     */
    void removeDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener);

}
