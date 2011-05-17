/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.migrationextensions;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRule;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.internal.ProductComponent;

/**
 * Migration to version 3.4.0.
 * <p>
 * Changes {@link PolicyCmptType}s containing {@link ValidationRule}s. For each rule the attributes
 * "configuredByProductComponent" and "activatedByDefault" are created and set to <code>false</code>
 * and <code>true</code> respectively.
 * <p>
 * No {@link ProductComponent}s are changed by this migration as all previously existing
 * {@link ValidationRule}s are defined as not configurable.
 * 
 * @author Stefan Widmaier
 */
public class Migration_3_4_0 extends DefaultMigration {

    public Migration_3_4_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        if (srcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE)) {
            IIpsObject ipsObject = srcFile.getIpsObject();
            if (migrateType((IPolicyCmptType)ipsObject)) {
                srcFile.markAsDirty();
            }
        }
        // TODO ProdCmptType for constant Attributes
    }

    private boolean migrateType(IPolicyCmptType type) {
        List<IValidationRule> rules = type.getValidationRules();
        boolean result = false;
        for (IValidationRule rule : rules) {
            rule.setConfigurableByProductComponent(false);
            rule.setActivatedByDefault(true);
            result = true;
        }
        return result;
    }

    @Override
    protected boolean migrate(IFile file) throws CoreException {
        return false;
    }

    @Override
    public String getDescription() {
        return "Changes PolicyCmptTypes for ValidationRules. For each rule the attributes \"configuredByProductComponent\" and \"activatedByDefault\" are created. Both attributes are set to false."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "3.4.0"; //$NON-NLS-1$
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_4_0(ipsProject, featureId);
        }

    }

}
