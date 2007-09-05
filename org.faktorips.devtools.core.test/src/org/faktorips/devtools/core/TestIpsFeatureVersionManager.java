/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractMigrationOperation;
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

    /**
     * {@inheritDoc}
     */
    public int compareToCurrentVersion(String otherVersion) {
        return compareToCurrentVersion;
    }
    
    public void setCompareToCurrentVersion(int newReturnValue) {
        this.compareToCurrentVersion = newReturnValue;
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentVersion() {
        return IpsPlugin.getInstalledFaktorIpsVersion();
    }

    /**
     * {@inheritDoc}
     */
    public String getFeatureId() {
        return "org.faktorips.feature"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return "TestFeatureVersionManager";
    }

    /**
     * {@inheritDoc}
     */
    public AbstractMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate) throws CoreException {
        return new AbstractMigrationOperation[0];
    }

    /**
     * {@inheritDoc}
     */
    public String getPredecessorId() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCurrentVersionCompatibleWith(String otherVersion) {
        return compatible;
    }
    
    public void setCurrentVersionCompatibleWith(boolean newReturnValue) {
        compatible = newReturnValue;
    }

    /**
     * {@inheritDoc}
     */
    public void setFeatureId(String featureId) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public void setId(String id) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public void setPredecessorId(String predecessorId) {
        throw new UnsupportedOperationException();
    }
    
}
