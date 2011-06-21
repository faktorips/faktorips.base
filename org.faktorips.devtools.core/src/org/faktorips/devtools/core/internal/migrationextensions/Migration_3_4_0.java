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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRule;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
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
 * <p>
 * Changes {@link ProductCmptType}s containing attributes. Each {@link ProductCmptTypeAttribute} is
 * set to be changing over time. Thus no product components need to be changed in that regard.
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
            if (hasVRules((IPolicyCmptType)ipsObject)) {
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

    private boolean hasVRules(IPolicyCmptType type) {
        return type.getNumOfRules() > 0;
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
        return "3.4.0.ms2"; //$NON-NLS-1$
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_4_0(ipsProject, featureId);
        }

    }

}
