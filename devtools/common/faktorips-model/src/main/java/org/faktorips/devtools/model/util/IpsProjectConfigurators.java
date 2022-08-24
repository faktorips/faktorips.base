/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.util.stream.Stream;

import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * Utility for working with {@link IIpsProjectConfigurator}.
 */
public class IpsProjectConfigurators {

    private IpsProjectConfigurators() {
        // Utility class
    }

    /**
     * Returns all {@link IIpsProjectConfigurator IIpsProjectConfigurators} that
     * {@link IIpsProjectConfigurator#canConfigure(AJavaProject) can configure} the given
     * {@link AJavaProject}.
     *
     * @see IIpsModelExtensions#getIpsProjectConfigurators()
     */
    public static Stream<IIpsProjectConfigurator> applicableTo(AJavaProject project) {
        return IIpsModelExtensions.get()
                .getIpsProjectConfigurators().stream()
                .filter(c -> c.canConfigure(project));
    }

    /**
     * Checks whether at least one extension of the extension point
     * {@link ExtensionPoints#ADD_IPS_NATURE} supports Groovy and is responsible for configuring the
     * passed {@link AJavaProject}.
     * 
     * @return {@code true} whether at least one extension supports Groovy
     */
    public static final boolean isGroovySupported(AJavaProject javaProject) {
        return IpsProjectConfigurators
                .applicableTo(javaProject)
                .anyMatch(c -> c.isGroovySupported(javaProject));
    }

}
