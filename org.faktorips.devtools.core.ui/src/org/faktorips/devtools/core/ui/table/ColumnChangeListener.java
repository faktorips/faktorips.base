/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

/**
 * A listener that informs that the value in a column has changed.
 */
public interface ColumnChangeListener {
    public void valueChanged(ColumnIdentifier columnIdentifier, Object value);
}
