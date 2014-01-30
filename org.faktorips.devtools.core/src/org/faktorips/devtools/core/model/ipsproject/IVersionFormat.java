/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsproject;

/**
 * A version format is able to validate a version string for specified syntax.
 * 
 * @author dirmeier
 */
public interface IVersionFormat {

    /**
     * Validates the version string for the syntax of this version format object
     * 
     * @param version the version string to validate, could be null
     * @return true if the version format is correct, false otherwise
     */
    public boolean isCorrectVersionFormat(String version);

    /**
     * Returns a user readable string representation of the version format.
     * 
     * @return a readable version of the format
     */
    public String getVersionFormat();

}
