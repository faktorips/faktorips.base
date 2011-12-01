/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;

/**
 * Migration to version 3.6.0.
 * <p>
 * Searches for all product component types that have no supertype. For each such
 * {@link IProductCmptType}, the default categories are created if they are missing. The default
 * categories correspond to the UI sections as they were set up prior to version 3.6:
 * <ul>
 * <li>Attributes
 * <li>Tables and Formulas
 * <li>Validation Rules
 * <li>Defaults and Value Sets
 * </ul>
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann, Faktor Zehn AG
 * 
 * @see IProductCmptType
 * @see IProductCmptCategory
 */
public class Migration_3_6_0 extends DefaultMigration {

    public Migration_3_6_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        IpsObjectType srcFileObjectType = srcFile.getIpsObjectType();
        if (!srcFileObjectType.equals(IpsObjectType.PRODUCT_CMPT_TYPE)
                && !srcFileObjectType.equals(IpsObjectType.POLICY_CMPT_TYPE)) {
            return;
        }

        if (srcFileObjectType.equals(IpsObjectType.PRODUCT_CMPT_TYPE)) {
            IProductCmptType productCmptType = (IProductCmptType)srcFile.getIpsObject();
            if (!productCmptType.hasSupertype()) {
                createDefaultCategories(productCmptType);
            }
        }
    }

    private void createDefaultCategories(IProductCmptType productCmptType) throws CoreException {
        createDefaultCategoryAttributes(productCmptType);
        createDefaultCategoryTablesAndFormulas(productCmptType);
        createDefaultCategoryValidationRules(productCmptType);
        createDefaultCategoryDefaultsAndValueSets(productCmptType);
    }

    private void createDefaultCategoryAttributes(IProductCmptType productCmptType) throws CoreException {
        if (productCmptType.findDefaultCategoryForProductCmptTypeAttributes(productCmptType.getIpsProject()) == null) {
            IProductCmptCategory attributes = productCmptType
                    .newCategory(Messages.Migration_3_6_0_nameDefaultCategoryAttributes);
            attributes.setDefaultForProductCmptTypeAttributes(true);
            attributes.setPosition(Position.LEFT);
        }
    }

    private void createDefaultCategoryTablesAndFormulas(IProductCmptType productCmptType) throws CoreException {
        boolean defaultTableStructureUsagesExists = productCmptType
                .findDefaultCategoryForTableStructureUsages(productCmptType.getIpsProject()) != null;
        boolean defaultFormulaSignatureDefinitionsExists = productCmptType
                .findDefaultCategoryForFormulaSignatureDefinitions(productCmptType.getIpsProject()) != null;
        if (!defaultTableStructureUsagesExists || !defaultFormulaSignatureDefinitionsExists) {
            IProductCmptCategory tablesAndFormulas = productCmptType
                    .newCategory(Messages.Migration_3_6_0_nameDefaultCategoryTablesAndFormulas);
            tablesAndFormulas.setDefaultForTableStructureUsages(!defaultTableStructureUsagesExists);
            tablesAndFormulas.setDefaultForFormulaSignatureDefinitions(!defaultFormulaSignatureDefinitionsExists);
            tablesAndFormulas.setPosition(Position.LEFT);
        }
    }

    private void createDefaultCategoryValidationRules(IProductCmptType productCmptType) throws CoreException {
        if (productCmptType.findDefaultCategoryForValidationRules(productCmptType.getIpsProject()) == null) {
            IProductCmptCategory validationRules = productCmptType
                    .newCategory(Messages.Migration_3_6_0_nameDefaultCategoryValidationRules);
            validationRules.setDefaultForValidationRules(true);
            validationRules.setPosition(Position.LEFT);
        }
    }

    private void createDefaultCategoryDefaultsAndValueSets(IProductCmptType productCmptType) throws CoreException {
        if (productCmptType.findDefaultCategoryForPolicyCmptTypeAttributes(productCmptType.getIpsProject()) == null) {
            IProductCmptCategory defaultsAndValueSets = productCmptType
                    .newCategory(Messages.Migration_3_6_0_nameDefaultCategoryDefaultsAndValueSets);
            defaultsAndValueSets.setDefaultForPolicyCmptTypeAttributes(true);
            defaultsAndValueSets.setPosition(Position.RIGHT);
        }
    }

    @Override
    public String getTargetVersion() {
        return "3.6.0"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return "Product component properties (changeable and configurable policy component type attributes" //$NON-NLS-1$
                + ", configurable validation rules, product component type attributes, formula " //$NON-NLS-1$
                + "signature definitions and table structure usages) are now all assigned to " //$NON-NLS-1$
                + "categories in the corresponding product component type. For all product component " //$NON-NLS-1$
                + "types without a superclass, the default categories are created. The default" //$NON-NLS-1$
                + " categories correspond to the UI setup as it was prior to version 3.6"; //$NON-NLS-1$
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {

        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {

            return new Migration_3_6_0(ipsProject, featureId);
        }

    }

}
