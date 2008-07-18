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

package org.faktorips.devtools.core.ui.preferencepages;


/**
 * Implementation of IIpsObjectPathEntryAttribute
 * @author Roman Grutza
 */
public class IpsObjectPathEntryAttribute implements IIpsObjectPathEntryAttribute {

    String type;
    private Object value;
    
    
    /**
     * 
     * @param type of the attribute, can be one of the defined String constants as defined in IIpsObjectPathEntryAttribute
     * @param value object to be set
     */
    public IpsObjectPathEntryAttribute(String type, Object value) {
        if (IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED.equals(type)
                || IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_MERGABLE.equals(type)
                || IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type)
                || IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_MERGABLE.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_TOC_PATH.equals(type))
        {
            this.type = type;
            this.value = value;
            return;
        }
        throw new IllegalArgumentException("Attribute type must be one of the constants defined in IIpsObjectPathEntryAttribute");
    }
    
    /**
     * {@inheritDoc}
     */
    public Object getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFolderForDerivedSources() {
        return (IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type)
             || IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type)); 
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFolderForMergableSources() {
        return (IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type)
             || IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type));
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isTocPath() {
        return (IIpsObjectPathEntryAttribute.SPECIFIC_TOC_PATH.equals(type));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPackageNameForDerivedSources() {
        return (IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED.equals(type)
                || (IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED.equals(type)));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPackageNameForMergableSources() {
        return (IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_MERGABLE.equals(type)
                || (IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_MERGABLE.equals(type)));        
    }

}
