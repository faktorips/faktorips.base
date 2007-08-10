/**
 * 
 */
package org.faktorips.devtools.core.internal.model.versionmanager;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

/**
 * Migration from 1.0.3 to 1.0.4.
 * 
 * @author Jan Ortmann
 */
public class Migration_1_0_3 extends DefaultMigration {

    public Migration_1_0_3(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Some bugs fixed. See http://bugs.faktorips.org/ for more details."; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetVersion() {
        return "1.0.4"; //$NON-NLS-1$
    }

    protected void migrate(IIpsObject object) throws CoreException {
        if (IpsObjectType.POLICY_CMPT_TYPE==object.getIpsObjectType()) {
            migrate((IPolicyCmptType)object);
        }
    }
    
    protected void migrate(IPolicyCmptType type) throws CoreException {
        type.getIpsSrcFile().markAsDirty(); // needed for renaming of reverse to inverse relation
        IRelation[] relations = type.getRelations();
        for (int i = 0; i < relations.length; i++) {
            if (relations[i].isComposition()) {
                relations[i].setInverseRelation("");
                if (relations[i].isCompositionDetailToMaster()) {
                    relations[i].setReadOnlyContainer(false);
                    relations[i].setContainerRelation("");
                }
            }
        }
    }

    
    
}