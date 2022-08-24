/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.productrelease;

import java.net.PasswordAuthentication;

import org.faktorips.devtools.model.eclipse.productrelease.ITargetSystem;

/**
 * Default implementation for {@link ITargetSystem} only provides the name of the target system
 * 
 * @author dirmeier
 */
public class DefaultTargetSystem implements ITargetSystem {

    private final String name;
    private PasswordAuthentication passwordAuthentication;

    public DefaultTargetSystem(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean isValidAuthentication() {
        return true;
    }

    @Override
    public void setPasswordAuthentication(PasswordAuthentication passwordAuthentication) {
        this.passwordAuthentication = passwordAuthentication;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return passwordAuthentication;
    }

}
