/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.runtime.MessageList;

/**
 * This class maps IPS object path entry attributes to values.
 * 
 * @author Roman Grutza
 */
public interface IIpsObjectPathEntryAttribute {

    public static final String DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES = "defaultOutputFolderMergable"; //$NON-NLS-1$
    public static final String SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES = "specificOutputFolderMergable"; //$NON-NLS-1$

    public static final String DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES = "defaultOutputFolderDerived"; //$NON-NLS-1$
    public static final String SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES = "specificOutputFolderDerived"; //$NON-NLS-1$

    public static final String DEFAULT_BASE_PACKAGE_MERGABLE = "defaultBasePackageMergable"; //$NON-NLS-1$
    public static final String SPECIFIC_BASE_PACKAGE_MERGABLE = "specificBasePackageMergable"; //$NON-NLS-1$

    public static final String DEFAULT_BASE_PACKAGE_DERIVED = "defaultBasePackageDerived"; //$NON-NLS-1$
    public static final String SPECIFIC_BASE_PACKAGE_DERIVED = "specificBasePackageDerived"; //$NON-NLS-1$

    public static final String SPECIFIC_TOC_PATH = "tocPath"; //$NON-NLS-1$

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
    public MessageList validate() throws IpsException;
}
