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
 * This class maps IPS source path entry attributes to values. 
 * @author Roman Grutza
 */

public interface IIpsSrcFolderEntryAttribute {

    public final static String DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES = "defaultOutputFolderMergable";
    public final static String SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES = "specificOutputFolderMergable";
    
    public final static String DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES = "defaultOutputFolderDerived";
    public final static String SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES = "specificOutputFolderDerived";
    
    public final static String DEFAULT_BASE_PACKAGE_MERGABLE = "defaultBasePackageMergable";
    public final static String SPECIFIC_BASE_PACKAGE_MERGABLE = "specificBasePackageMergable";
    
    public final static String DEFAULT_BASE_PACKAGE_DERIVED = "defaultBasePackageDerived";
    public final static String SPECIFIC_BASE_PACKAGE_DERIVED = "specificBasePackageDerived";
    
    public final static String SPECIFIC_TOC_PATH = "tocPath";

    /**
     * @return the name of the mapping
     */
    public String getName();

    /**
     * @return the mappings value, which can be any arbitrary object
     */
    public Object getValue();
    
    /**
     * Set the value object for this mapping
     * @param value, an arbitrary object
     */
    public void setValue(Object value);
    
    
    /**
     * @return if this mapping is a derived folder mapping (either the IPS object path's default folder for derived sources,
     * or a IpsSrcPathEntry-specific output folder) 
     */
    public boolean isFolderForDerivedSources();
    
    
    /**
     * @return if this mapping is a mergable folder mapping (either the IPS object path's default folder for mergable sources,
     * or a IpsSrcPathEntry-specific output folder) 
     */
    public boolean isFolderForMergableSources();
    
}
