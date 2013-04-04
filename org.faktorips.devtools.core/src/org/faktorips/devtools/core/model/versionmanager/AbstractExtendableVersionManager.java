/*******************************************************************************
 * Copyright (c) Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.versionmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.osgi.framework.Version;

/**
 * This is an abstract version manager, managing the compatibility between new and old project
 * versions.
 * <p>
 * It implements the core interface {@link IIpsFeatureVersionManager} and uses an extension point
 * retrieving all registered migration operation classes. In particular it uses the core method
 * {@link IpsPlugin#getRegisteredMigrationOperations(String)} and the extension point definition
 * there. The registered migration operations will be filtered depending on the given contributor
 * name.
 * <p>
 * 
 * <p>
 * <strong>Sub classing:</strong><br>
 * A concrete version manager should use the constructor to deliver the feature id. The abstract
 * method {@link AbstractExtendableVersionManager#getCurrentVersion()} needs to be implemented and
 * return the current installed version. The version is used to decide whether a migration operation
 * should be performed or not and in which order it should be applied.
 * 
 * @since 3.9.0
 * 
 */
public abstract class AbstractExtendableVersionManager implements IIpsFeatureVersionManager {

    private static final Version NONE = new Version(0, 0, 0);
    private String id;
    private String predecessorId;
    private String featureID;
    private boolean requiredForAllProjects;
    private Map<Version, IIpsProjectMigrationOperationFactory> registeredMigrations;

    /**
     * Constructor initializing the feature id, version id and the registered migration operations.
     * It has to be called from sub classes.
     * 
     * @param featureID The id of the feature for which the migrations are managed by this
     *            {@link IIpsFeatureVersionManager}.
     * @param contributorName The symbolic name of the bundle that provides the migrations for this
     *            version manager
     */
    protected AbstractExtendableVersionManager(String featureID, String contributorName) {
        this.featureID = featureID;
        registeredMigrations = getRegisteredOperations(contributorName);
    }

    private Map<Version, IIpsProjectMigrationOperationFactory> getRegisteredOperations(String contributorName) {
        IpsPlugin ipsPlugin = IpsPlugin.getDefault();
        if (ipsPlugin != null) {
            return ipsPlugin.getRegisteredMigrationOperations(contributorName);
        } else {
            return new HashMap<Version, IIpsProjectMigrationOperationFactory>();
        }
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setPredecessorId(String predecessorId) {
        this.predecessorId = predecessorId;
    }

    @Override
    public String getPredecessorId() {
        return predecessorId;
    }

    @Override
    public void setFeatureId(String featureId) {
        featureID = featureId;
    }

    @Override
    public String getFeatureId() {
        return featureID;
    }

    @Override
    public abstract String getCurrentVersion();

    @Override
    public boolean isRequiredForAllProjects() {
        return requiredForAllProjects;
    }

    @Override
    public void setRequiredForAllProjects(boolean requiredForAllProjects) {
        this.requiredForAllProjects = requiredForAllProjects;
    }

    @Override
    public boolean isCurrentVersionCompatibleWith(String otherVersion) {
        if (compareToCurrentVersion(otherVersion) > 0) {
            return false;
        }

        if (compareToCurrentVersion(otherVersion) == 0) {
            return true;
        }

        Version currentVersion = Version.parseVersion(getCurrentVersion());
        Object otherVersionVersion = Version.parseVersion(otherVersion);
        SortedSet<Version> versionsWithMigration = new TreeSet<Version>(registeredMigrations.keySet());
        for (Version version : versionsWithMigration) {
            if (version.compareTo(otherVersionVersion) > 0 && version.compareTo(currentVersion) <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareToCurrentVersion(String otherVersion) {
        Version outer = Version.parseVersion(otherVersion);
        Version inner = Version.parseVersion(getCurrentVersion());
        return outer.compareTo(inner);
    }

    @Override
    public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate)
            throws CoreException {
        Version projectsVersion = Version.parseVersion(projectToMigrate.getProperties().getMinRequiredVersionNumber(
                getFeatureId()));
        List<AbstractIpsProjectMigrationOperation> result = getMigrationOperations(projectToMigrate, projectsVersion);
        return result.toArray(new AbstractIpsProjectMigrationOperation[result.size()]);
    }

    private List<AbstractIpsProjectMigrationOperation> getMigrationOperations(IIpsProject projectToMigrate,
            Version projectsVersion) {
        SortedSet<Version> versionsWithMigration = new TreeSet<Version>(registeredMigrations.keySet());
        List<AbstractIpsProjectMigrationOperation> result = new ArrayList<AbstractIpsProjectMigrationOperation>();

        if (NONE.equals(projectsVersion)) {
            // No version means no installed feature, so no migration required
            return result;
        }

        Version currentVersion = Version.parseVersion(getCurrentVersion());
        for (Version version : versionsWithMigration) {
            if (version.compareTo(projectsVersion) > 0 && version.compareTo(currentVersion) <= 0) {
                result.add(registeredMigrations.get(version).createIpsProjectMigrationOpertation(projectToMigrate,
                        getFeatureId()));
            }
        }
        return result;
    }

    /* private */void setRegisteredMigrations(Map<Version, IIpsProjectMigrationOperationFactory> registeredMigrations) {
        this.registeredMigrations = registeredMigrations;
    }

}
