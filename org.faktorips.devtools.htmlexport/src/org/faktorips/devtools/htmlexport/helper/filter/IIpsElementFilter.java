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

package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Filter for {@link IpsElement}s
 * 
 * @author dicker
 * 
 */
public interface IIpsElementFilter {
    /**
     * @param element filtered {@link IIpsElement}
     * @return true, if the given IIpsElement fullfills the conditions of the filter
     */
    public boolean accept(IIpsElement element);

}
