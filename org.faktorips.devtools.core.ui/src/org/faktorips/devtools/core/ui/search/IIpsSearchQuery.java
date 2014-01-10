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

package org.faktorips.devtools.core.ui.search;

import org.eclipse.search.ui.ISearchQuery;

/**
 * Extends the {@link ISearchQuery} for the model and product search.
 * 
 * @author dicker
 */
public interface IIpsSearchQuery extends ISearchQuery {

    /**
     * Returns a the text for the result label using the
     * 
     * @param matchCount number of matches
     */
    public String getResultLabel(int matchCount);

}