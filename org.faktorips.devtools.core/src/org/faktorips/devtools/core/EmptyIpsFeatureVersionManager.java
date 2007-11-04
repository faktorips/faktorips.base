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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;

/**
 * 
 * @author Jan Ortmann
 */
public class EmptyIpsFeatureVersionManager implements IIpsFeatureVersionManager {

    public final static EmptyIpsFeatureVersionManager INSTANCE = new EmptyIpsFeatureVersionManager(); 
    
    private AbstractIpsProjectMigrationOperation[] emptyOps = new AbstractIpsProjectMigrationOperation[0];
    
    private EmptyIpsFeatureVersionManager() {
    }

    /**
     * {@inheritDoc}
     */
    public int compareToCurrentVersion(String otherVersion) {
        return 0;
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
        return "org.faktorips.feature";
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return "EmptyIpsFeatureVersionManager";
    }

    /**
     * {@inheritDoc}
     */
    public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate) throws CoreException {
        return emptyOps;
    }

    /**
     * {@inheritDoc}
     */
    public String getPredecessorId() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCurrentVersionCompatibleWith(String otherVersion) {
        return true;
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
