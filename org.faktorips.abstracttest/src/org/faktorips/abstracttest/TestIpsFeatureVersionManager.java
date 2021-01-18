/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;

/**
 * 
 * @author Jan Ortmann
 */
public class TestIpsFeatureVersionManager implements IIpsFeatureVersionManager {

    private int compareToCurrentVersion = 0;
    private boolean compatible = true;
    private boolean requiredForAllProjects = true;

    public TestIpsFeatureVersionManager() {

    }

    @Override
    public int compareToCurrentVersion(String otherVersion) {
        return compareToCurrentVersion;
    }

    public void setCompareToCurrentVersion(int newReturnValue) {
        this.compareToCurrentVersion = newReturnValue;
    }

    @Override
    public String getCurrentVersion() {
        return IpsModelActivator.getInstalledFaktorIpsVersion();
    }

    @Override
    public String getFeatureId() {
        return "org.faktorips.feature"; //$NON-NLS-1$
    }

    @Override
    public String getId() {
        return "TestFeatureVersionManager";
    }

    @Override
    public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate)
            throws CoreException {
        return new AbstractIpsProjectMigrationOperation[0];
    }

    @Override
    public String getPredecessorId() {
        return null;
    }

    @Override
    public boolean isCurrentVersionCompatibleWith(String otherVersion) {
        return compatible;
    }

    public void setCurrentVersionCompatibleWith(boolean newReturnValue) {
        compatible = newReturnValue;
    }

    @Override
    public void setFeatureId(String featureId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setId(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPredecessorId(String predecessorId) {
        throw new UnsupportedOperationException();
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
