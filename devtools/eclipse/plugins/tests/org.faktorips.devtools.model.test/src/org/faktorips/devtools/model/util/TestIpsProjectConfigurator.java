/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 
 * 3. 
 *  
 * Please see LICENSE.txt for full license terms, including the additional permissions and 
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/package org.faktorips.devtools.model.util;

import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

class TestIpsProjectConfigurator implements IIpsProjectConfigurator {

    @Override
    public boolean canConfigure(AJavaProject javaProject) {
        return true;
    }

    @Override
    public boolean isGroovySupported(AJavaProject javaProject) {
        return false;
    }

    @Override
    public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties)
            throws IpsException {
        // nothing to do
    }

}