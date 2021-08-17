/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.m2e.version;

import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class MavenVersionProviderFactory implements IVersionProviderFactory {

    public MavenVersionProviderFactory() {
        // Empty constructor needed because of Eclipse's extension point mechanism.
    }

    @Override
    public MavenVersionProvider createVersionProvider(IIpsProject ipsProject) {
        return new MavenVersionProvider(ipsProject);
    }

}
