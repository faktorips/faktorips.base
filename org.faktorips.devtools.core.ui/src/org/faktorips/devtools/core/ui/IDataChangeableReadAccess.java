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
 * An interface that marks an user interface component (control, editor, editor page) as being able
 * to tell, if the data shown can be modified by the user or not.
 * 
 * @author Jan Ortmann
 */
public interface IDataChangeableReadAccess {

    /**
     * Returns <code>true</code> if the data shown in this user interface component can be changed,
     * otherwise <code>false</code>.
     */
    public boolean isDataChangeable();

}
