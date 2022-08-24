/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.productrelease;

import java.net.PasswordAuthentication;

/**
 * This object represents a target system for the release and deployment extension. It is used by
 * the {@link IReleaseAndDeploymentOperation}.
 * 
 * The intreface only provides the method {@link #getName()}, returning a readable name of this
 * system that is displayed to the user.
 * 
 * @author dirmeier
 */
public interface ITargetSystem {

    /**
     * Getting a readable name of this target system
     * 
     * return the name of the target system
     */
    String getName();

    /**
     * Setting the {@link PasswordAuthentication} for this target system.
     * 
     * @param passwordAuthentication the {@link PasswordAuthentication} containing the username and
     *            password
     */
    void setPasswordAuthentication(PasswordAuthentication passwordAuthentication);

    /**
     * Whether the {@link PasswordAuthentication} is valid. If the target system does not need any
     * authentication this method should always return true. As far this method returns false, the
     * system will ask the user for name and password.
     * 
     * @return true if the {@link PasswordAuthentication} is valid
     */
    boolean isValidAuthentication();

}
