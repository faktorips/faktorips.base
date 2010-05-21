/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
        // Singleton constructor.
    }

    @Override
    public int compareToCurrentVersion(String otherVersion) {
        return 0;
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
        return "EmptyIpsFeatureVersionManager"; //$NON-NLS-1$
    }

    @Override
    public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate)
            throws CoreException {
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

}
