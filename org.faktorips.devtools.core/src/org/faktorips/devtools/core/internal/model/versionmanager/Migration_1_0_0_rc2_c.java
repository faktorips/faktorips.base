/**
 * 
 */
package org.faktorips.devtools.core.internal.model.versionmanager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Empty Migration 
 * 
 * @author Peter Erzberger
 */
public class Migration_1_0_0_rc2_c extends AbstractMigrationOperation {

    public Migration_1_0_0_rc2_c(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Some bugs fixed."; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetVersion() {
        return "1.0.0.rc3"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList migrate(IProgressMonitor monitor) throws CoreException {
        return new MessageList();
    }
}