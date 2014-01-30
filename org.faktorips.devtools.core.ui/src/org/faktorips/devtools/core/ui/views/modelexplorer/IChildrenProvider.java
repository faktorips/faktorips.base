/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.core.runtime.CoreException;

/**
 * This IChildrenProvider provides children for instance of the given type.
 * 
 * @author dicker
 */
public interface IChildrenProvider<T> {

    /**
     * returns an array of Objects, which represents the children of the given element.
     */
    Object[] getChildren(T element) throws CoreException;

}
