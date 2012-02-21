/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
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
 * It implements the core interface {@link IIpsFeatureVersionManager} and uses an xPt retrieving all
 * registered migration operation classes. In particular it uses the core method
 * <code>IpsPlugin.getDefault().getRegisteredMigrations()</code> and the xPt definition there. The
 * registered migration operations will be filtered depending on the given feature id. Therefore
 * it's possible that different features can use this class in common.
 * <p>
 * 
 * <p>
 * <strong>Sub classing:</strong><br>
 * A concrete version manager should use the constructor to deliver the feature id. The abstract
 * method {@link AbstractExtendableVersionManager#retrieveInstalledVersion()} is a hook retrieving
 * the correct installed version id. The version id is used to decide whether a migration operation
 * should be performed or not and in which order it should be applied.
 * 
 * @since 3.7.0
 * 
 */
public abstract class AbstractExtendableVersionManager implements IIpsFeatureVersionManager {

    private String id;
    private String predecessorId;
    private String featureID;
    private String versionID;
    private Map<Version, IIpsProjectMigrationOperationFactory> registeredMigrations;

    /**
     * Constructor initializing the feature id, version id and the registered migration operations.
     * It has to be called from sub classes.
     */
    protected AbstractExtendableVersionManager(String featureID) {
        this.featureID = featureID;
        versionID = retrieveInstalledVersion();
        registeredMigrations = IpsPlugin.getDefault().getRegisteredMigrationOperations();
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
    public String getCurrentVersion() {
        return versionID;
    }

    public Map<Version, IIpsProjectMigrationOperationFactory> getRegisteredMigrations() {
        return registeredMigrations;
    }

    public void setRegisteredMigrations(Map<Version, IIpsProjectMigrationOperationFactory> registeredMigrations) {
        this.registeredMigrations = registeredMigrations;
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
        Version currentVersion = Version.parseVersion(getCurrentVersion());
        for (Version version : versionsWithMigration) {
            if (version.compareTo(projectsVersion) > 0 && version.compareTo(currentVersion) <= 0) {
                result.add(registeredMigrations.get(version).createIpsProjectMigrationOpertation(projectToMigrate,
                        getFeatureId()));
            }
        }
        return result;
    }

    /**
     * Returns the installed feature/plugin version.
     */
    protected abstract String retrieveInstalledVersion();

}
