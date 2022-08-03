/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search;

import org.faktorips.devtools.core.ui.search.scope.IIpsSearchScope;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Is the interface for the base presentasion models for model and product search
 * <p>
 * IIpsSearchPresentationModel contains functionality for
 * <ul>
 * <li>using the {@link IIpsSearchScope}</li>
 * <li>searching the name of {@link IIpsSrcFile IIpsSrcFiles}</li>
 * </ul>
 * 
 * 
 * @author dicker
 */
public interface IIpsSearchPresentationModel extends IIpsSearchPartPresentationModel {

    String SRC_FILE_PATTERN = "srcFilePattern"; //$NON-NLS-1$

    /**
     * @see #getSearchScope()
     */
    void setSearchScope(IIpsSearchScope searchScope);

    /**
     * Returns the {@link IIpsSearchScope} of the search
     */
    IIpsSearchScope getSearchScope();

    /**
     * @see #getSrcFilePattern()
     */
    void setSrcFilePattern(String newValue);

    /**
     * Returns the pattern for matching {@link IIpsSrcFile IIpsSrcFiles}
     */
    String getSrcFilePattern();
}
