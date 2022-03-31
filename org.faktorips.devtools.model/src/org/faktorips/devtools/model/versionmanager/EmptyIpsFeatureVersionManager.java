/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.versionmanager;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsModelActivator;

/**
 * 
 * @author Jan Ortmann
 */
public class EmptyIpsFeatureVersionManager implements IIpsFeatureVersionManager {

    public static final EmptyIpsFeatureVersionManager INSTANCE = new EmptyIpsFeatureVersionManager();

    private AbstractIpsProjectMigrationOperation[] emptyOps = new AbstractIpsProjectMigrationOperation[0];

    private EmptyIpsFeatureVersionManager() {
        // Singleton constructor.
    }

    @Override
    public int compareToCurrentVersion(String otherVersion) {
        return 0;
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
        return "EmptyIpsFeatureVersionManager"; //$NON-NLS-1$
    }

    @Override
    public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate) {
        return emptyOps;
    }

    @Override
    public String getPredecessorId() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public boolean isCurrentVersionCompatibleWith(String otherVersion) {
        return true;
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
        return true;
    }

    @Override
    public void setRequiredForAllProjects(boolean required) {
        throw new UnsupportedOperationException();
    }

}
