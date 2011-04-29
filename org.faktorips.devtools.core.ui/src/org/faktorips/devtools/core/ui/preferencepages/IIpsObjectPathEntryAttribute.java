/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.util.message.MessageList;

/**
 * This class maps IPS object path entry attributes to values.
 * 
 * @author Roman Grutza
 */
public interface IIpsObjectPathEntryAttribute {

    public final static String DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES = "defaultOutputFolderMergable"; //$NON-NLS-1$
    public final static String SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES = "specificOutputFolderMergable"; //$NON-NLS-1$

    public final static String DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES = "defaultOutputFolderDerived"; //$NON-NLS-1$
    public final static String SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES = "specificOutputFolderDerived"; //$NON-NLS-1$

    public final static String DEFAULT_BASE_PACKAGE_MERGABLE = "defaultBasePackageMergable"; //$NON-NLS-1$
    public final static String SPECIFIC_BASE_PACKAGE_MERGABLE = "specificBasePackageMergable"; //$NON-NLS-1$

    public final static String DEFAULT_BASE_PACKAGE_DERIVED = "defaultBasePackageDerived"; //$NON-NLS-1$
    public final static String SPECIFIC_BASE_PACKAGE_DERIVED = "specificBasePackageDerived"; //$NON-NLS-1$

    public final static String SPECIFIC_TOC_PATH = "tocPath"; //$NON-NLS-1$

    /**
     * @return the mappings value, which can be any arbitrary object
     */
    public Object getValue();

    /**
     * Set the value object for this mapping
     * 
     * @param value an arbitrary object
     */
    public void setValue(Object value);

    /**
     * @return the type of this entry as defined in this interface
     */
    public String getType();

    /**
     * @return true if this mapping is a derived folder mapping (either the IPS object path's
     *         default folder for derived sources, or a IpsSrcPathEntry-specific output folder),
     *         false otherwise
     */
    public boolean isFolderForDerivedSources();

    /**
     * @return true if this mapping is a mergable folder mapping (either the IPS object path's
     *         default folder for mergable sources, or a IpsSrcPathEntry-specific output folder),
     *         false otherwise
     */
    public boolean isFolderForMergableSources();

    /**
     * @return true if this mapping is toc file mapping, false otherwise
     */
    public boolean isTocPath();

    /**
     * @return true if this mapping is a package name mapping for derived sources (either the IPS
     *         object path's default package name for derived sources, or a IpsSrcPathEntry-specific
     *         package name), false otherwise
     */
    public boolean isPackageNameForDerivedSources();

    /**
     * @return true if this mapping is a package name mapping for mergable sources (either the IPS
     *         object path's default package name for mergable sources, or a
     *         IpsSrcPathEntry-specific package name), false otherwise
     */
    public boolean isPackageNameForMergableSources();

    /**
     * Validates the object path entry attribute and returns the result as list of messages.
     */
    public MessageList validate() throws CoreException;
}
