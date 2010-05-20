/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.versionmanager;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * A manager for version-related topics in Faktor-IPS.
 * 
 * @author Thorsten Guenther
 */
public interface IIpsFeatureVersionManager {

    /**
     * Sets the id of this manager. This method is used by the framework to initialize the manager
     * and <strong>must not be called by clients</strong>.
     * 
     * @param id The new id for this manager.
     */
    public void setId(String id);

    /**
     * Returns the id of this manager.
     */
    public String getId();

    /**
     * Sets the id of the predecessor manager. This method is used by the framework to initialize
     * the manager and <strong>must not be called by clients</strong>.
     * 
     * @param predecessorId The id of the predecessor of this manager.
     */
    public void setPredecessorId(String predecessorId);

    /**
     * Returns the id of the predecessor for this manager.
     */
    public String getPredecessorId();

    /**
     * Sets the id of the feature this is a version manager for. This method is used by the
     * framework to initialize this manager and <strong>must not be called by clients</strong>.
     * 
     * @param featureId The feature id.
     */
    public void setFeatureId(String featureId);

    /**
     * Returns the id of the feature this is the version manager for.
     */
    public String getFeatureId();

    /**
     * Returns the current version of the feature this manager is responsible for.
     */
    public String getCurrentVersion();

    /**
     * Returns <code>true</code> if the current version is compatible to the given one. If
     * otherVersion is greater than the current version, <code>false</code> is returned, because
     * only backward-compatibility is provided by these method.
     * 
     * @param otherVersion The version-string to check for compatibility.
     */
    public boolean isCurrentVersionCompatibleWith(String otherVersion);

    /**
     * Returns a value less 0 if otherVersion is less then currentVersion, 0 if they are equal and a
     * value greater 0 if otherVersion is greater then currentVersion.
     * 
     * @param otherVersion The version-string to compare the current version to.
     */
    public int compareToCurrentVersion(String otherVersion);

    /**
     * Returns n array of all migration operations which have to run to migrate the given project to
     * the current version. The content of the array is sorted, the operation of index 0 has to be
     * run first, the one at index 1 has to be run next and so on.
     * 
     * @param projectToMigrate The IpsProject to migrate to the current version.
     * 
     * @throws CoreException If an error occurs during evaluation or instantiation of the migration
     *             operations.
     */
    public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate)
            throws CoreException;

}
