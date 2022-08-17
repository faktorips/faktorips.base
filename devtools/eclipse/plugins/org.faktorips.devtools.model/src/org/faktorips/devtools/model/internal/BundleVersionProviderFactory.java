/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import java.io.IOException;

import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * The {@link BundleVersionProviderFactory} instantiates a {@link BundleVersionProvider} to
 * configure the version of the project by using the Manifest file.
 */
public class BundleVersionProviderFactory implements IVersionProviderFactory {

    public BundleVersionProviderFactory() {
        // Empty constructor needed because of Eclipse's extension point mechanism.
    }

    @Override
    public IVersionProvider<?> createVersionProvider(IIpsProject ipsProject) {
        try {
            return new BundleVersionProvider(ipsProject);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
