/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migration;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.util.QNameUtil;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;

/**
 * Version manager for the core-feature of FaktorIps.
 * 
 * This manager is based on classes named
 * org.faktorips.devtools.core.internal.model.versionmanager.Migration_&lt;version&gt;. version is
 * the version-number the migration-class can migrate.
 * 
 * @author Thorsten Guenther
 */
public class CoreVersionManager implements IIpsFeatureVersionManager {

    private String version;
    private String id;
    private String predecessorId;
    private boolean requiredForAllProjects;

    /**
     * The class loader to be used if the migration-operations are loaded. This is only used for
     * tests ...
     */
    private ClassLoader loader;

    public CoreVersionManager() {
        super();
        loader = getClass().getClassLoader();
        version = IpsPlugin.getInstalledFaktorIpsVersion();
    }

    @Override
    public void setFeatureId(String featureId) {
        // this version manager supports the core Faktor-IPS feature. No need to maintain its id
    }

    @Override
    public String getFeatureId() {
        return "org.faktorips.feature"; //$NON-NLS-1$
    }

    @Override
    public String getCurrentVersion() {
        return version;
    }

    @Override
    public boolean isCurrentVersionCompatibleWith(String otherVersion) {
        if (compareToCurrentVersion(otherVersion) > 0) {
            return false;
        }

        if (compareToCurrentVersion(otherVersion) == 0) {
            return true;
        }

        try {
            AbstractIpsProjectMigrationOperation[] operations = getMigrationOperations(null, otherVersion);
            for (AbstractIpsProjectMigrationOperation operation : operations) {
                if (!operation.isEmpty()) {
                    return false;
                }
            }
            return true;
        } catch (IpsException e) {
            IpsPlugin.log(e);
            return false;
        }

    }

    @Override
    public int compareToCurrentVersion(String otherVersion) {
        AVersion outer = AVersion.parse(otherVersion);
        AVersion inner = AVersion.parse(getCurrentVersion());
        return outer.compareTo(inner);
    }

    @Override
    public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate) {
        String minRequiredVersion = projectToMigrate.getReadOnlyProperties()
                .getMinRequiredVersionNumber(getFeatureId());
        if (minRequiredVersion == null) {
            // no version entry was found in the properties, therefore no migration operation will
            // be available
            // for this project
            return new AbstractIpsProjectMigrationOperation[0];
        }
        return getMigrationOperations(projectToMigrate, minRequiredVersion);
    }

    private AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate,
            String versionToStart) {
        ArrayList<AbstractIpsProjectMigrationOperation> operations = new ArrayList<>();
        String migrationClassName = null;
        try {
            AbstractIpsProjectMigrationOperation migrationOperation = null;
            String currentVersion = versionToStart;
            while (compareToCurrentVersion(currentVersion) < 0) {
                String underscoreVersion = currentVersion.replace('.', '_');
                underscoreVersion = underscoreVersion.replace('-', '_');
                String packageName = QNameUtil.getPackageName(CoreVersionManager.class.getName());
                migrationClassName = packageName + ".Migration_" + underscoreVersion; //$NON-NLS-1$
                Class<?> clazz = Class.forName(migrationClassName, true, loader);
                Constructor<?> constructor = clazz.getConstructor(IIpsProject.class, String.class);
                migrationOperation = (AbstractIpsProjectMigrationOperation)constructor
                        .newInstance(projectToMigrate, getFeatureId());
                operations.add(migrationOperation);
                currentVersion = migrationOperation.getTargetVersion();
            }
        } catch (ClassNotFoundException e) {
            // there is no migration strategy for the expected version.
            // however we have to migrate as much we found and should not throw any exception
            // because maybe other migration managers fix this problem
            // If any migration is still missing, the validation should report the problem!
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            throw new IpsException(new IpsStatus(e));
        }
        // CSON: IllegalCatch

        return operations.toArray(new AbstractIpsProjectMigrationOperation[operations.size()]);
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
    public boolean isRequiredForAllProjects() {
        return requiredForAllProjects;
    }

    @Override
    public void setRequiredForAllProjects(boolean requiredForAllProjects) {
        this.requiredForAllProjects = requiredForAllProjects;
    }

}
