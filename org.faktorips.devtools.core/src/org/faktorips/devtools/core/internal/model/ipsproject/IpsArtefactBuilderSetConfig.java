/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Map;

import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.util.ArgumentCheck;

/**
 * The default implementation of the IIpsArtefactBuilderSetConfig interface
 * 
 * @author Peter Erzberger
 */
public class IpsArtefactBuilderSetConfig implements IIpsArtefactBuilderSetConfig {

    private Map properties;
    
    /**
     * This constructor is for test purposes only.
     */
    public IpsArtefactBuilderSetConfig(Map properties) {
        ArgumentCheck.notNull(properties);
        this.properties = properties;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object getPropertyValue(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getPropertyNames() {
        return (String[])properties.keySet().toArray(new String[properties.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public Boolean getPropertyValueAsBoolean(String propertyName) {
        return (Boolean)getPropertyValue(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    public Integer getPropertyValueAsInteger(String propertyName) {
        return (Integer)getPropertyValue(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    public String getPropertyValueAsString(String propertyName) {
        return (String)getPropertyValue(propertyName);
    }

}
