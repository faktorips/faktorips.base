/**
 * 
 */
package org.faktorips.devtools.core.internal.model.versionmanager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.versionmanager.AbstractMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Empty Migration 
 * 
 * @author Peter Erzberger
 */
public class Migration_1_0_0_rc1 extends AbstractMigrationOperation {

    public Migration_1_0_0_rc1(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Xml-Format for ranges now uses the standard format for values. Some bugs fixed."; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetVersion() {
        return "1.0.0.rc2"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList migrate(IProgressMonitor monitor) throws CoreException {
        MessageList messages = new MessageList();
        IIpsPackageFragmentRoot[] roots = getIpsProject().getSourceIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            IIpsPackageFragment[] packs = roots[i].getIpsPackageFragments();
            for (int j = 0; j < packs.length; j++) {
                migrate(packs[j], messages, monitor);
                if (monitor.isCanceled()) {
                    return messages;
                }
            }
        }
        return messages;
    }
    
    private void migrate(IIpsPackageFragment pack, MessageList list, IProgressMonitor monitor) throws CoreException {
        IIpsSrcFile[] files = pack.getIpsSrcFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].getIpsObject();
            files[i].markAsDirty();
            files[i].save(false, monitor);
            if (monitor.isCanceled()) {
                break;
            }
            monitor.worked(1);
        }
    }
    
    
    
}
