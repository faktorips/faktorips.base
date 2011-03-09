/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migration;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;

/**
 * Migration from version 3.0.0.ms4 to version 3.0.0.ms5. See getDescription() for details.
 * 
 * @author ortmann2
 */
public class Migration_3_0_0_ms4 extends DefaultMigration {

    public Migration_3_0_0_ms4(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "JPA: Sets the default annotation OrphanRemoval on all master to detail compositions."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "3.0.0.ms5"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    protected boolean migrate(IFile file) throws CoreException {
        return false;
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        if (srcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE)) {
            IPolicyCmptType policyCmptType = (IPolicyCmptType)srcFile.getIpsObject();
            List<IPolicyCmptTypeAssociation> policyCmptTypeAssociations = policyCmptType
                    .getPolicyCmptTypeAssociations();
            for (IPolicyCmptTypeAssociation ass : policyCmptTypeAssociations) {
                migrateAssociation(ass);
            }
        }
    }

    private void migrateAssociation(IPolicyCmptTypeAssociation ass) throws CoreException {
        IPersistentAssociationInfo persistenceAssociatonInfo = ass.getPersistenceAssociatonInfo();
        if (persistenceAssociatonInfo == null) {
            return;
        }
        persistenceAssociatonInfo.setOrphanRemoval(persistenceAssociatonInfo.isOrphanRemovalRequired());

        persistenceAssociatonInfo.initDefaultsCascadeTypes();
    }
}
