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

package org.faktorips.devtools.core.model.versionmanager;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.util.message.MessageList;

/**
 * Base class for migrating a single ips project one version to another. Note that if the project
 * needs several Faktor-IPS features, than all features are migrated.
 * 
 * Note that it is essential <strong>NOT TO SAVE ANY CHANGES</strong> made by this migration
 * operation. This is because we need the ability to rollback all changes if any error occurs later
 * on in the migration process. This might change if we support WorkingCopies.
 * 
 * @author Thorsten Guenther
 */
public abstract class AbstractIpsProjectMigrationOperation extends WorkspaceModifyOperation {

    private IIpsProject project;
    private String featureId;

    /**
     * Creates a new migration operation.
     */
    public AbstractIpsProjectMigrationOperation(IIpsProject projectToMigrate, String featureId) {
        project = projectToMigrate;
        this.featureId = featureId;
    }

    /**
     * Note that it is essential <strong>NOT TO SAVE ANY CHANGES</strong> made by this migration
     * operation. This is because we need the ability to rollback all changes if any error occurs
     * later on in the migration process. This might change if we support WorkingCopies.
     * 
     * {@inheritDoc}
     */
    @Override
    protected final void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {
        migrate(monitor);
        if (!isEmpty()) {
            IIpsProjectProperties props = project.getProperties();
            props.setMinRequiredVersionNumber(getFeatureId(), getTargetVersion());
            project.setProperties(props);
        }
    }

    /**
     * @return The project to migrate (or the one which was migrated, depending on the time of
     *         call).
     */
    public IIpsProject getIpsProject() {
        return project;
    }

    /**
     * @return The id of the feature this is a migration operation for. That means, that this
     *         migration operation does changes <strong>only</strong> to match the needs of the
     *         featuer which id is returned.
     */
    public String getFeatureId() {
        return featureId;
    }

    /**
     * @return The version reached after this migration was executed.
     */
    public abstract String getTargetVersion();

    /**
     * @return <code>true</code> if this operation does no structural changes but only documents
     *         that the version this is a migration for and the target version of this migration are
     *         compatible.
     */
    public abstract boolean isEmpty();

    /**
     * @return A description for this migration. Keep in mind that this description is used to
     *         display it to the end user, if any, who has to decide to start this migration based
     *         on this description. So do not return useless stuff like "Migration from a to b" but
     *         what is migrated, for example "Move description from an attribute to an own tag".
     */
    public abstract String getDescription();

    /**
     * Does the real work. Note that it is essential <strong>NOT TO SAVE ANY CHANGES</strong> made
     * by this migration operation. This is because we need the ability to rollback all changes if
     * any error occurs later on in the migration process. This might change if we support
     * WorkingCopies.
     * 
     * @param monitor Progress monitor to report progress to, can be <code>null</code>.
     * @return A list of messages describing any problems occurred during migration. If this list is
     *         empty, migration was successful. If this list contains a message with severity error,
     *         it was not.
     */
    public abstract MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException;

}
