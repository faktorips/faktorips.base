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

package org.faktorips.runtime;

/**
 * The modification checker holds a version or time stamp for the version or time it is up to date.
 * You could ask the modification checker whether a given version or time stamp is expired and you
 * could get the actual version.
 * 
 * @author dirmeier
 */
public interface IVersionChecker {

    /**
     * Returns true if the new version is compatible to the old version or both versions are equal
     * 
     * @param oldVersion the old version
     * @param newVersion the new version
     * @return true if versions are compatible
     */
    public boolean isCompatibleVersion(String oldVersion, String newVersion);

}
