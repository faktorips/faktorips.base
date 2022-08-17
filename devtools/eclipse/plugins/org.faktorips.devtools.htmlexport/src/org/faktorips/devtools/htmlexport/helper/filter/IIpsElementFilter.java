/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.model.IIpsElement;

/**
 * Filter for {@link IIpsElement IIpsElement}
 * 
 * @author dicker
 * 
 */
public interface IIpsElementFilter {
    /**
     * @param element filtered {@link IIpsElement}
     * @return true, if the given {@link IIpsElement} fulfills the conditions of the filter
     */
    boolean accept(IIpsElement element);

}
