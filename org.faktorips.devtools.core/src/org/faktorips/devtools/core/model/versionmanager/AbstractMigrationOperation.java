/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.model.versionmanager;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.util.message.MessageList;

/**
 * Base class for the migration from one feature-version to another.
 * 
 * Note that it is essential <strong>NOT TO SAVE ANY CHANGES</strong> made by this migration
 * operation. This is because we need the ability to rollback all changes if any error occurs later
 * on in the migration process. This might change if we support WorkingCopies.
 * 
 * @author Thorsten Guenther
 */
public abstract class AbstractMigrationOperation extends WorkspaceModifyOperation {

    private IIpsProject project;
    private String featureId;

    /**
     * The default-constructor is forbidden for external calls...
     */
    private AbstractMigrationOperation() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new migration operation.
     */
    public AbstractMigrationOperation(IIpsProject projectToMigrate, String featureId) {
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
        return this.project;
    }

    /**
     * @return The id of the feature this is a migration operation for. That means, that this
     *         migration operation does changes <strong>only</strong> to match the needs of the
     *         featuer which id is returned.
     */
    public String getFeatureId() {
        return this.featureId;
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
     * @return A list of messages describing any problems occured during migration. If this list is
     *         empty, migration was successfull. If this list contains a message with severity
     *         error, it was not.
     */
    public abstract MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException;

}
