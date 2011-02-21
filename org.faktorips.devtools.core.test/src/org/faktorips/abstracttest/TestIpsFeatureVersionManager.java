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

package org.faktorips.abstracttest;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;

/**
 * 
 * @author Jan Ortmann
 */
public class TestIpsFeatureVersionManager implements IIpsFeatureVersionManager {

    private int compareToCurrentVersion = 0;
    private boolean compatible = true;

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
        return IpsPlugin.getInstalledFaktorIpsVersion();
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

}
