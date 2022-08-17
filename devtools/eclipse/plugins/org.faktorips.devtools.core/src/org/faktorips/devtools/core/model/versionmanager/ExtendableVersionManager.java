/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.versionmanager;

import static org.faktorips.runtime.internal.IpsStringUtils.isBlank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.abstraction.mapping.OsgiVersionMapping;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IExtendableVersionManager;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.osgi.framework.Bundle;

/**
 * This is a special version manager, managing the compatibility between new and old project
 * versions. The migration strategies are registered by the extension point
 * org.faktorips.devtools.core.ipsMigrationOperation. If you use this version manager, every
 * migration that is registered using this extension point in the same plug-In like this version
 * manager is used.
 * <p>
 * The current version is derived from the Bundle-Version of the contributor plug-In which is
 * identified by the contributor name. If you want to retrieve the version from another source you
 * could overwrite the method {@link #getVersion()}.
 * <p>
 * <strong>Sub classing:</strong><br>
 * A concrete version manager should use the constructor to provide the feature id and the
 * contributor name. In fact this does not really need a subclass. But to register the version
 * manager by extension point you need to have a zero argument constructor.
 * 
 * @since 3.9.0
 * 
 */
public class ExtendableVersionManager implements IExtendableVersionManager {

    private static final AVersion NONE = AVersion.VERSION_ZERO;

    private String featureID;

    private String contributorName;

    private String id;

    private String predecessorId;

    private boolean requiredForAllProjects;

    private Map<AVersion, IIpsProjectMigrationOperationFactory> registeredMigrations;

    private AVersion currentVersion;

    /**
     * The default constructor simply creates the version manager but initializes nothing. To
     * initialize the version manager you have to set at least the contributor name by calling
     * {@link #setContributorName(String)} and you should set a feature ID to tell the version
     * manager which feature ID it is responsible for.
     */
    public ExtendableVersionManager() {
    }

    private Map<AVersion, IIpsProjectMigrationOperationFactory> getRegisteredOperations(String contributorName) {
        IIpsModelExtensions modelExtensions = IIpsModelExtensions.get();
        if (modelExtensions != null) {
            return modelExtensions.getRegisteredMigrationOperations(contributorName);
        } else {
            return new HashMap<>();
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
    public String getContributorName() {
        return contributorName;
    }

    @Override
    public void setContributorName(String contributorName) {
        this.contributorName = contributorName;
        registeredMigrations = getRegisteredOperations(contributorName);
    }

    protected AVersion getVersion() {
        if (currentVersion == null) {
            currentVersion = retrieveContributorVersion();
        }
        return currentVersion;
    }

    private AVersion retrieveContributorVersion() {
        Bundle bundle = Platform.getBundle(getContributorName());
        currentVersion = OsgiVersionMapping.toAVersion(bundle.getVersion());
        return currentVersion;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation retrieve the current version from the method {@link #getVersion()}. If
     * you want to change the source for getting the current version you need to overwrite
     * {@link #getVersion()} instead of this method.
     */
    @Override
    public final String getCurrentVersion() {
        return getVersion().toString();
    }

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

        AVersion otherVersionVersion = AVersion.parse(otherVersion);
        SortedSet<AVersion> versionsWithMigration = new TreeSet<>(registeredMigrations.keySet());
        for (AVersion version : versionsWithMigration) {
            if (version.compareTo(otherVersionVersion) > 0 && version.compareTo(getVersion()) <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareToCurrentVersion(String otherVersion) {
        AVersion outer = AVersion.parse(otherVersion);
        AVersion inner = getVersion();
        return outer.compareTo(inner);
    }

    @Override
    public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate) {
        String minRequiredVersionNumber = projectToMigrate.getReadOnlyProperties()
                .getMinRequiredVersionNumber(getFeatureId());
        AVersion projectsVersion = isBlank(minRequiredVersionNumber) ? NONE : AVersion.parse(minRequiredVersionNumber);
        List<AbstractIpsProjectMigrationOperation> result = getMigrationOperations(projectToMigrate, projectsVersion);
        return result.toArray(new AbstractIpsProjectMigrationOperation[result.size()]);
    }

    private List<AbstractIpsProjectMigrationOperation> getMigrationOperations(IIpsProject projectToMigrate,
            AVersion projectsVersion) {

        if (NONE.equals(projectsVersion)) {
            // No version means no installed feature, so no migration required
            return Collections.emptyList();
        }

        SortedSet<AVersion> versionsWithMigration = new TreeSet<>(registeredMigrations.keySet());
        List<AbstractIpsProjectMigrationOperation> result = new ArrayList<>();

        for (AVersion version : versionsWithMigration) {
            if (version.compareTo(projectsVersion) > 0 && version.compareTo(getVersion()) <= 0) {
                result.add(registeredMigrations.get(version).createIpsProjectMigrationOpertation(projectToMigrate,
                        getFeatureId()));
            }
        }
        return result;
    }

    /* private */void setRegisteredMigrations(
            Map<AVersion, IIpsProjectMigrationOperationFactory> registeredMigrations) {
        this.registeredMigrations = registeredMigrations;
    }

}
