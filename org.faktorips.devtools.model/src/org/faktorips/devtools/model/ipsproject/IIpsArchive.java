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

import java.nio.file.Path;

import org.eclipse.core.runtime.IPath;

/**
 * An IPS archive is an archive for IPS objects. It is physically stored in a file. The file's
 * format is jar.
 * 
 * @author Jan Ortmann
 */
public interface IIpsArchive extends IIpsStorage {

    /**
     * Constant for the top-level folder in the archive file that contains the entries for the IPS
     * objects.
     */
    String IPSOBJECTS_FOLDER = "ipsobjects"; //$NON-NLS-1$

    /**
     * Constant for the jar entry name" that contains additional ipsobjects properties like the
     * mapping to Java base packages.
     */
    String JAVA_MAPPING_ENTRY_NAME = IPSOBJECTS_FOLDER + IPath.SEPARATOR + "ipsobjects.properties"; //$NON-NLS-1$

    String QNT_PROPERTY_POSTFIX_SEPARATOR = "#"; //$NON-NLS-1$

    String PROPERTY_POSTFIX_BASE_PACKAGE_MERGABLE = "basePackageMergable"; //$NON-NLS-1$

    String PROPERTY_POSTFIX_BASE_PACKAGE_DERIVED = "basePackageDerived"; //$NON-NLS-1$

    /**
     * Returns the path to the underlying file. Note that the file might exists outside the
     * workspace or might not exists at all. Do not use this method to locate the archive because
     * this path may be project relative or workspace relative. Use {@link #getLocation()} instead!
     */
    Path getArchivePath();

}
