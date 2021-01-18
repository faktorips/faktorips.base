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

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.type.PolicyCmptType;

/**
 * Migration to version 3.4.0.
 * <p>
 * Changes {@link PolicyCmptType}s containing {@link IValidationRule}s. For each rule:
 * <ul>
 * <li>the attributes "configuredByProductComponent" and "activatedByDefault" are created and set to
 * <code>false</code> and <code>true</code> respectively</li>
 * <li>a default label (value="") is created for every language supported by the containing IPS
 * project</li>
 * </ul>
 * <p>
 * No {@link IProductComponent}s are changed by this migration as all previously existing
 * {@link IValidationRule}s are defined as not configurable.
 * <p>
 * Changes {@link IProductCmptType}s containing attributes. Each {@link IProductCmptTypeAttribute}
 * is set to be changing over time. Thus no product components (and generations) need to be changed
 * in that regard.
 * 
 * Note: This migration may be applied to the same ipsObjects again without overwriting manual
 * changes with the default values described above.
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
            if (migrateVRules((IPolicyCmptType)ipsObject)) {
                /*
                 * Loading an saving the type will automatically establish the default values. See
                 * ValidationRule#initPropertiesFromXml()
                 */
                srcFile.markAsDirty();
            }
        } else if (srcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT_TYPE)) {
            IIpsObject ipsObject = srcFile.getIpsObject();
            if (hasAttributes((IProductCmptType)ipsObject)) {
                /*
                 * Loading an saving the type will automatically establish the default values. See
                 * ProductCmptTypeAttribute#initPropertiesFromXml()
                 */
                srcFile.markAsDirty();
            }
        }
    }

    private boolean hasAttributes(IProductCmptType prodType) {
        return prodType.getNumOfAttributes() > 0;
    }

    /*
     * Create default label if absent. return true if type has rules.
     */
    private boolean migrateVRules(IPolicyCmptType type) {
        Set<ISupportedLanguage> supportedLanguages = type.getIpsProject().getReadOnlyProperties()
                .getSupportedLanguages();
        for (IValidationRule rule : type.getValidationRules()) {
            createAbsentLabels(rule, supportedLanguages);
        }
        return type.getNumOfRules() > 0;
    }

    private void createAbsentLabels(IValidationRule rule, Set<ISupportedLanguage> supportedLanguages) {
        for (ISupportedLanguage supportedLanguage : supportedLanguages) {
            if (!hasLabelForLanguage(rule, supportedLanguage)) {
                ILabel newLabel = rule.newLabel();
                newLabel.setValue(""); //$NON-NLS-1$
                newLabel.setLocale(supportedLanguage.getLocale());
            }
        }
    }

    protected boolean hasLabelForLanguage(IValidationRule rule, ISupportedLanguage supportedLanguage) {
        List<ILabel> labels = rule.getLabels();
        for (ILabel label : labels) {
            if (label.getLocale().equals(supportedLanguage.getLocale())) {
                return true;
            }
        }
        return false;
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
        return "3.4.0.rfinal"; //$NON-NLS-1$
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_4_0(ipsProject, featureId);
        }

    }

}
