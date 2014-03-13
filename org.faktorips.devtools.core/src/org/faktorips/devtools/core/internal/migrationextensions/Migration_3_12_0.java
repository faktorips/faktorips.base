/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Migration to version 3.12.0 Changes the .ipsproject settings file as follows:
 * <ul>
 * <li>renames the tag "productRelease" to "ProductRelease"</li>
 * <li>introduces a new tag "Version". The "productRelease"-attribute "version" is moved to the new
 * tag "Version"</li>
 * </ul>
 */
public class Migration_3_12_0 extends AbstractIpsProjectMigrationOperation {

    public Migration_3_12_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "Renames the tag \"productRelease\" to \"ProductRelease\". Introduces a new tag \"Version\". The \"productRelease\"-attribute \"version\" is moved to the new tag \"Version\""; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "3.12.0"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {
        try {
            migrateInternal();
        } catch (MigrationRuntimeException e) {
            IpsPlugin.log(new IpsStatus(e));
        }
        return new MessageList();
    }

    private void migrateInternal() {
        IpsProject ipsProject = (IpsProject)getIpsProject();
        IFile file = ipsProject.getIpsProjectPropertiesFile();

        Document doc = loadXMLDocument(file);
        // migrateXML(doc);
        parseAndApplyProperties(ipsProject, file, doc.getDocumentElement());
    }

    private Document loadXMLDocument(IFile file) {
        if (!file.exists()) {
            throw new MigrationRuntimeException("File does not exist: " + file); //$NON-NLS-1$
        }
        Document doc;
        InputStream is;
        try {
            is = file.getContents(true);
        } catch (CoreException e) {
            throw new MigrationRuntimeException("Error reading file contents " + file, e); //$NON-NLS-1$
        }
        try {
            doc = IpsPlugin.getDefault().getDocumentBuilder().parse(is);
        } catch (Exception e) {
            throw new MigrationRuntimeException("Error parsing contents of file " + file, e); //$NON-NLS-1$
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                throw new MigrationRuntimeException("Error closing input stream after reading file " + file, e); //$NON-NLS-1$
            }
        }
        return doc;
    }

    private void parseAndApplyProperties(IpsProject ipsProject, IFile file, Element documentElement) {
        IpsProjectProperties projectProperties = createPropertiesFromXML(ipsProject, documentElement);
        projectProperties.setLastPersistentModificationTimestamp(new Long(file.getModificationStamp()));
        applyProjectProperties(ipsProject, projectProperties);
    }

    private IpsProjectProperties createPropertiesFromXML(IpsProject ipsProject, Element documentElement) {
        IpsProjectProperties migratedProjectProperties = new IpsProjectProperties();
        try {
            migratedProjectProperties = IpsProjectProperties.createFromXml(ipsProject, documentElement);
            migratedProjectProperties.setCreatedFromParsableFileContents(true);
        } catch (Exception e) {
            migratedProjectProperties.setCreatedFromParsableFileContents(false);
            // throw new MigrationRuntimeException("Error creating properties from xml", e); //$NON-NLS-1$
        }
        return migratedProjectProperties;
    }

    private void applyProjectProperties(IpsProject ipsProject, IpsProjectProperties migratedProjectProperties) {
        try {
            ipsProject.setProperties(migratedProjectProperties);
        } catch (CoreException e) {
            throw new MigrationRuntimeException("Error applying project properties", e); //$NON-NLS-1$
        }
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_12_0(ipsProject, featureId);
        }
    }
}
