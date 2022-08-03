/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.path;

/**
 * This interface represents a path location.
 * 
 * A {@link IHtmlPath} is used for setting relative links in the Html-Export
 * 
 * 
 * 
 * @author dicker
 */
public interface IHtmlPath {

    String EMPTY_PATH = ""; //$NON-NLS-1$

    /**
     * returns relative path of this location to the root
     * 
     */
    String getPathToRoot();

    /**
     * returns relative path of this location from the root
     * 
     * @param linkedFileType type of path
     * 
     */
    String getPathFromRoot(LinkedFileType linkedFileType);
}
