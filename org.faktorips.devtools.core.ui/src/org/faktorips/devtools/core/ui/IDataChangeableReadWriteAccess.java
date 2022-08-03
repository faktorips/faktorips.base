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
 * An interface that is implemented by user interface components that allow to switch whether the
 * data shown is changeable or not.
 * 
 * @author Jan Ortmann
 */
public interface IDataChangeableReadWriteAccess extends IDataChangeableReadAccess {

    /**
     * Sets if the data shown in this user interface component can be changed or not.
     */
    void setDataChangeable(boolean changeable);

}
