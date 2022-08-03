/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.ipsproject;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Configuration for a Faktor-IPS feature add-on.
 * 
 * @see IIpsProjectProperties#getFeatureConfiguration(String)
 */
public interface IIpsFeatureConfiguration {

    /**
     * Returns the value of the property identified by the given name or {@code null} if no value
     * has been set for that property.
     */
    @CheckForNull
    String get(String name);

}
