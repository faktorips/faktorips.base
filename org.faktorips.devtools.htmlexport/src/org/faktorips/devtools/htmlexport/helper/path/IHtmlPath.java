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

    public static final String EMPTY_PATH = ""; //$NON-NLS-1$

    /**
     * returns relative path of this location to the root
     * 
     */
    public abstract String getPathToRoot();

    /**
     * returns relative path of this location from the root
     * 
     * @param linkedFileType type of path
     * 
     */
    public abstract String getPathFromRoot(LinkedFileType linkedFileType);
}