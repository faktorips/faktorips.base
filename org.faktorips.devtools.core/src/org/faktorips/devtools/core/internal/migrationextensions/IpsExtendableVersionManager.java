/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.migration.CoreVersionManager;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.osgi.framework.Version;

/**
 * This is the new version manager for the Faktor-IPS core feature. It uses the old
 * {@link CoreVersionManager} for compatibility to old projects. The new Version manager is
 * introduced with Faktor-IPS 3.2.0.
 * <p>
 * Read {@link org.faktorips.devtools.core.internal.migrationextensions} (or pacakge.html) for
 * instructions creating a new migration strategy!
 * 
 * @author dirmeier
 */
public class IpsExtendableVersionManager extends CoreVersionManager {

    private Map<Version, IIpsProjectMigrationOperationFactory> registeredMigrations;

    public IpsExtendableVersionManager() {
        super();
        registeredMigrations = IpsPlugin.getDefault().getRegisteredMigrationOperations(IpsPlugin.PLUGIN_ID);
    }

    @Override
    public boolean isCurrentVersionCompatibleWith(String otherVersion) {
        if (!super.isCurrentVersionCompatibleWith(otherVersion)) {
            return false;
        }

        Version currentVersion = Version.parseVersion(getCurrentVersion());
        Version otherVersionVersion = Version.parseVersion(otherVersion);
        SortedSet<Version> versionsWithMigration = new TreeSet<Version>(getRegisteredMigrations().keySet());
        for (Version version : versionsWithMigration) {
            if (version.compareTo(otherVersionVersion) > 0 && version.compareTo(currentVersion) <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate)
            throws CoreException {
        Version projectsVersion = Version.parseVersion(projectToMigrate.getReadOnlyProperties()
                .getMinRequiredVersionNumber(getFeatureId()));
        List<AbstractIpsProjectMigrationOperation> result = getMigrationOperations(projectToMigrate, projectsVersion);
        result.addAll(0, Arrays.asList(super.getMigrationOperations(projectToMigrate)));
        return result.toArray(new AbstractIpsProjectMigrationOperation[result.size()]);
    }

    private List<AbstractIpsProjectMigrationOperation> getMigrationOperations(IIpsProject projectToMigrate,
            Version projectsVersion) {
        SortedSet<Version> versionsWithMigration = new TreeSet<Version>(getRegisteredMigrations().keySet());
        List<AbstractIpsProjectMigrationOperation> result = new ArrayList<AbstractIpsProjectMigrationOperation>();
        Version currentVersion = Version.parseVersion(getCurrentVersion());
        for (Version version : versionsWithMigration) {
            if (version.compareTo(projectsVersion) > 0 && version.compareTo(currentVersion) <= 0) {
                result.add(getRegisteredMigrations().get(version).createIpsProjectMigrationOpertation(projectToMigrate,
                        getFeatureId()));
            }
        }
        return result;
    }

    public void setRegisteredMigrations(Map<Version, IIpsProjectMigrationOperationFactory> registeredMigrations) {
        this.registeredMigrations = registeredMigrations;
    }

    public Map<Version, IIpsProjectMigrationOperationFactory> getRegisteredMigrations() {
        return registeredMigrations;
    }

}
