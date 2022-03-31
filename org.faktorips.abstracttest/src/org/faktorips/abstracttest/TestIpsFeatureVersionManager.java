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

import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;

/**
 * 
 * @author Jan Ortmann
 */
public class TestIpsFeatureVersionManager implements IIpsFeatureVersionManager {

    private final String featureId;
    private int compareToCurrentVersion = 0;
    private boolean compatible = true;
    private boolean requiredForAllProjects = true;

    public TestIpsFeatureVersionManager() {
        this("org.faktorips.feature"); //$NON-NLS-1$
    }

    public TestIpsFeatureVersionManager(String featureId) {
        this.featureId = featureId;
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
        return Abstractions.getVersion().toString();
    }

    @Override
    public String getFeatureId() {
        return featureId;
    }

    @Override
    public String getId() {
        return "TestFeatureVersionManager";
    }

    @Override
    public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate) {
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
