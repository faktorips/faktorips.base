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
        AVersion projectsVersion = AVersion.parse(projectToMigrate.getReadOnlyProperties()
                .getMinRequiredVersionNumber(getFeatureId()));
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
                    || isSameVersionBeforRelease(projectsVersion, currentVersion, version)) {
                result.add(getRegisteredMigrations().get(version).createIpsProjectMigrationOpertation(projectToMigrate,
                        getFeatureId()));
            }
        }
        return result;
    }

    private boolean isSameVersionBeforRelease(AVersion projectsVersion, AVersion currentVersion, AVersion version) {
        return version.majorMinorPatch().equals(projectsVersion.majorMinorPatch()) && !currentVersion.isRelease();
    }

    public void setRegisteredMigrations(Map<AVersion, IIpsProjectMigrationOperationFactory> registeredMigrations) {
        this.registeredMigrations = registeredMigrations;
    }

    public Map<AVersion, IIpsProjectMigrationOperationFactory> getRegisteredMigrations() {
        return registeredMigrations;
    }

}
