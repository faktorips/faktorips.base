/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.migration.CoreVersionManager;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;

/**
 * This is the new version manager for the Faktor-IPS core feature. It uses the old
 * {@link CoreVersionManager} for compatibility to old projects. The new Version manager is
 * introduced with Faktor-IPS 3.2.0.
 * <p>
 * Read {@link org.faktorips.devtools.core.internal.migrationextensions} (or package.html) for
 * instructions creating a new migration strategy!
 *
 * @author dirmeier
 */
public class IpsExtendableVersionManager extends CoreVersionManager {

    private Map<AVersion, IIpsProjectMigrationOperationFactory> registeredMigrations;

    public IpsExtendableVersionManager() {
        super();
        registeredMigrations = IIpsModelExtensions.get().getRegisteredMigrationOperations(IpsPlugin.PLUGIN_ID);
    }

    @Override
    public boolean isCurrentVersionCompatibleWith(String otherVersion) {
        if (!super.isCurrentVersionCompatibleWith(otherVersion)) {
            return false;
        }

        AVersion currentVersion = AVersion.parse(getCurrentVersion());
        AVersion otherVersionVersion = AVersion.parse(otherVersion);
        SortedSet<AVersion> versionsWithMigration = new TreeSet<>(getRegisteredMigrations().keySet());
        for (AVersion version : versionsWithMigration) {
            if (version.compareTo(otherVersionVersion) > 0 && version.compareTo(currentVersion) <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate) {
        String minRequiredVersionNumber = projectToMigrate.getReadOnlyProperties()
                .getMinRequiredVersionNumber(getFeatureId());
        if (minRequiredVersionNumber == null) {
            IpsPlugin.log(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID,
                    "Migration of \"%s\" failed because version \"null\" is not valid. See Problems View for errors." //$NON-NLS-1$
                            .formatted(projectToMigrate.getName())));
            return new AbstractIpsProjectMigrationOperation[0];
        }
        AVersion projectsVersion = AVersion.parse(minRequiredVersionNumber);
        List<AbstractIpsProjectMigrationOperation> result = getMigrationOperations(projectToMigrate, projectsVersion);
        result.addAll(0, Arrays.asList(super.getMigrationOperations(projectToMigrate)));
        return result.toArray(new AbstractIpsProjectMigrationOperation[result.size()]);
    }

    private List<AbstractIpsProjectMigrationOperation> getMigrationOperations(IIpsProject projectToMigrate,
            AVersion projectsVersion) {
        SortedSet<AVersion> versionsWithMigration = new TreeSet<>(getRegisteredMigrations().keySet());
        List<AbstractIpsProjectMigrationOperation> result = new ArrayList<>();
        AVersion currentVersion = AVersion.parse(getCurrentVersion());
        for (AVersion version : versionsWithMigration) {
            if (version.compareTo(projectsVersion) > 0 && version.compareTo(currentVersion) <= 0
                    || isSameVersionBeforeRelease(projectsVersion, currentVersion, version)) {
                result.add(getRegisteredMigrations().get(version).createIpsProjectMigrationOpertation(projectToMigrate,
                        getFeatureId()));
            }
        }
        return result;
    }

    private boolean isSameVersionBeforeRelease(AVersion projectsVersion, AVersion currentVersion, AVersion version) {
        boolean isMigrationToProjectVersion = version.majorMinorPatch().equals(projectsVersion.majorMinorPatch());
        boolean isMigrationToCurrentVersion = version.majorMinorPatch().equals(currentVersion.majorMinorPatch());
        boolean projectAndCurrentVersionIsRelease = currentVersion.isRelease() && projectsVersion.isRelease();
        return isMigrationToProjectVersion && isMigrationToCurrentVersion && !projectAndCurrentVersionIsRelease;
    }

    public void setRegisteredMigrations(Map<AVersion, IIpsProjectMigrationOperationFactory> registeredMigrations) {
        this.registeredMigrations = registeredMigrations;
    }

    public Map<AVersion, IIpsProjectMigrationOperationFactory> getRegisteredMigrations() {
        return registeredMigrations;
    }

}
