/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.migration;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.util.QNameUtil;
import org.osgi.framework.Version;

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
            for (int i = 0; i < operations.length; i++) {
                if (!operations[i].isEmpty()) {
                    return false;
                }
            }
            return true;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }

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
        String version = projectToMigrate.getReadOnlyProperties().getMinRequiredVersionNumber(getFeatureId());
        if (version == null) {
            // no version entry was found in the properties, therefore no migration operation will
            // be available
            // for this project
            return new AbstractIpsProjectMigrationOperation[0];
        }
        return getMigrationOperations(projectToMigrate, version);
    }

    private AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate,
            String versionToStart) throws CoreException {

        if (IpsPlugin.getDefault().isTestMode()) {
            loader = (ClassLoader)IpsPlugin.getDefault().getTestAnswerProvider().getAnswer();
        }

        ArrayList<AbstractIpsProjectMigrationOperation> operations = new ArrayList<AbstractIpsProjectMigrationOperation>();
        String migrationClassName = null;
        try {
            AbstractIpsProjectMigrationOperation migrationOperation = null;
            String version = versionToStart;
            while (compareToCurrentVersion(version) < 0) {
                String underscoreVersion = version.replace('.', '_');
                underscoreVersion = underscoreVersion.replace('-', '_');
                String packageName = QNameUtil.getPackageName(CoreVersionManager.class.getName());
                migrationClassName = packageName + ".Migration_" + underscoreVersion; //$NON-NLS-1$
                Class<?> clazz = Class.forName(migrationClassName, true, loader);
                Constructor<?> constructor = clazz.getConstructor(new Class[] { IIpsProject.class, String.class });
                migrationOperation = (AbstractIpsProjectMigrationOperation)constructor.newInstance(new Object[] {
                        projectToMigrate, getFeatureId() });
                operations.add(migrationOperation);
                version = migrationOperation.getTargetVersion();
            }
        } catch (ClassNotFoundException e) {
            // there is no migration strategy for the expected version.
            // however we have to migrate as much we found and should not throw any exception
            // because maybe other migration managers fix this problem
            // If any migration is still missing, the validation should report the problem!
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        }

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

}
