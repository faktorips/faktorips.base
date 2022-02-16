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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;

/**
 * Base class for migrating a single IPS project to one version to another. Note that if the project
 * needs several Faktor-IPS features, then all features are migrated.
 * <p>
 * Also note that it is essential <strong>NOT TO SAVE ANY CHANGES</strong> made by this migration
 * operation. This is because we need the ability to rollback all changes if any error occurs later
 * on in the migration process. This might change if we start to support working copies.
 * 
 * @author Thorsten Guenther
 */
public abstract class AbstractIpsProjectMigrationOperation {

    private IIpsProject project;
    private String featureId;

    public AbstractIpsProjectMigrationOperation(IIpsProject projectToMigrate, String featureId) {
        project = projectToMigrate;
        this.featureId = featureId;
    }

    /**
     * Returns the project to migrate (or the one which was migrated, depending on the time of
     * call).
     */
    public IIpsProject getIpsProject() {
        return project;
    }

    /**
     * Returns the id of the feature this is a migration operation for. That means, that this
     * migration operation does changes <strong>only</strong> to match the needs of the feature
     * which id is returned.
     */
    public String getFeatureId() {
        return featureId;
    }

    /**
     * Returns the version reached after this migration was executed.
     */
    public abstract String getTargetVersion();

    /**
     * Returns <code>true</code> if this operation does no structural changes but only documents
     * that the version this is a migration for and the target version of this migration are
     * compatible.
     */
    public abstract boolean isEmpty();

    /**
     * Returns a description for this migration. Keep in mind that this description is used to
     * display it to the end user, if any, who has to decide to start this migration based on this
     * description. So do not return useless stuff like "Migration from a to b" but what is
     * migrated, for example "Move description from an attribute to an own tag".
     */
    public abstract String getDescription();

    /**
     * Does the real work. Note that it is essential <strong>NOT TO SAVE ANY CHANGES</strong> made
     * by this migration operation. This is because we need the ability to rollback all changes if
     * any error occurs later on in the migration process. This might change if we start to support
     * working copies.
     * <p>
     * A list of messages describing any problems occurred during migration. If this list is empty,
     * migration was successful. If this list contains a message with severity error, it was not.
     * 
     * @param monitor Progress monitor to report progress to, can be <code>null</code>.
     */
    // CSOFF: ThrowsCount
    public abstract MessageList migrate(IProgressMonitor monitor) throws IpsException, InvocationTargetException,
            InterruptedException;
    // CSON: ThrowsCount

    /**
     * Returns {@link IpsMigrationOption configuration options} for this migration operation or an
     * empty collection if this operation does not require configuration.
     */
    public Collection<IpsMigrationOption> getOptions() {
        return Collections.emptySet();
    }

}
