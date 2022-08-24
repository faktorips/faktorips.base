/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.properties;

import java.util.Map;

import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.util.ArgumentCheck;

/**
 * The default implementation of the IIpsArtefactBuilderSetConfig interface
 * 
 * @author Peter Erzberger
 */
public class IpsArtefactBuilderSetConfig implements IIpsArtefactBuilderSetConfig {

    private Map<String, Object> properties;

    /**
     * This constructor is for test purposes only.
     */
    public IpsArtefactBuilderSetConfig(Map<String, Object> properties) {
        ArgumentCheck.notNull(properties);
        this.properties = properties;
    }

    @Override
    public Object getPropertyValue(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public String[] getPropertyNames() {
        return properties.keySet().toArray(new String[properties.size()]);
    }

    @Override
    public Boolean getPropertyValueAsBoolean(String propertyName) {
        return (Boolean)getPropertyValue(propertyName);
    }

    @Override
    public Integer getPropertyValueAsInteger(String propertyName) {
        return (Integer)getPropertyValue(propertyName);
    }

    @Override
    public String getPropertyValueAsString(String propertyName) {
        return (String)getPropertyValue(propertyName);
    }

}
