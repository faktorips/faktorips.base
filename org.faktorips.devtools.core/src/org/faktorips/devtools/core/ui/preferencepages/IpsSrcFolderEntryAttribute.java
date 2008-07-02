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
 * Implementation of IpsSrcFolderEntryAttribute
 * @author Roman Grutza
 */
public class IpsSrcFolderEntryAttribute implements IIpsSrcFolderEntryAttribute {

    private String name;
    private Object value;
    
    
    /**
     * 
     * @param name of the attribute
     * @param value object to be set
     * @param type IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES | IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES
     */
    public IpsSrcFolderEntryAttribute(String name, Object value) {
        this.name = name;
        this.value = value;
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
    public String getName() {
        return name;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isFolderForDerivedSources() {
        return (IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(name)
             || IIpsSrcFolderEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(name)); 
    }


    /**
     * {@inheritDoc}
     */
    public boolean isFolderForMergableSources() {
        return (IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(name)
             || IIpsSrcFolderEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(name));
    }

}
