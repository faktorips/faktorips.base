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

package org.faktorips.abstracttest.builder;

import java.util.Map;

import org.faktorips.devtools.core.internal.model.ipsproject.IpsArtefactBuilderSetConfig;

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
