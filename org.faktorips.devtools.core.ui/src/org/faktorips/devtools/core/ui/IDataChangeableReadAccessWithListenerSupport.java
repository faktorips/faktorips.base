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
 * Extension of the SwitchDataChangeableSupport with additional listener support.
 * 
 * @author Jan Ortmann
 */
public interface IDataChangeableReadAccessWithListenerSupport extends IDataChangeableReadAccess {

    /**
     * Adds the listener.
     */
    public void addDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener);

    /**
     * Removes the listener.
     */
    public void removeDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener);

}
