/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest.builder;

import java.util.Map;

import org.faktorips.devtools.model.internal.ipsproject.properties.IpsArtefactBuilderSetConfig;

public class TestBuilderSetConfig extends IpsArtefactBuilderSetConfig {

    private final Map<String, Object> properties;

    public TestBuilderSetConfig(Map<String, Object> properties) {
        super(properties);
        this.properties = properties;
    }

    /**
     * @return Returns the properties.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

}
