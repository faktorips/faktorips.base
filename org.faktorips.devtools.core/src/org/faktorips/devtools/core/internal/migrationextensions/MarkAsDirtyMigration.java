/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;

/**
 * Marks classes generated for a select set of types as dirty to trigger the clean build.
 */
public class MarkAsDirtyMigration extends DefaultMigration {

    private final Set<IpsObjectType> typesToMigrate;
    private String targetVersion;
    private String description;

    MarkAsDirtyMigration(IIpsProject projectToMigrate, String featureId, Set<IpsObjectType> typesToMigrate,
            String targetVersion, String description) {
        super(projectToMigrate, featureId);
        this.typesToMigrate = typesToMigrate;
        this.targetVersion = targetVersion;
        this.description = description;
    }

    @Override
    public String getTargetVersion() {
        return targetVersion;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException {
        MessageList messageList = super.migrate(monitor);
        MigrationUtil.updateBuilderSetDefaults(getIpsProject());
        return messageList;
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        if (typesToMigrate.contains(srcFile.getIpsObjectType())) {
            srcFile.markAsDirty();
        }
    }
}
