/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.versionmanager;

import org.faktorips.devtools.model.IIpsModelExtensions;

/**
 * This interface extends the {@link IIpsFeatureVersionManager} with the property contributor name.
 * This property is used to identify extended migration strategies registered in the different
 * plug-Ins.
 * <p>
 * If you register a version manager that implements this interface, the contributor name will be
 * set automatically by the {@link IIpsModelExtensions}.
 * 
 * @author dirmeier
 */
public interface IExtendableVersionManager extends IIpsFeatureVersionManager {

    /**
     * Returns the name of the contributing plug-In in which this version manager is registered.
     */
    String getContributorName();

    /**
     * Setting the name of the contributing plug-In in which this version manager is registered
     * 
     * @param contributorName The name of the contibutin plug-In
     */
    void setContributorName(String contributorName);

}
