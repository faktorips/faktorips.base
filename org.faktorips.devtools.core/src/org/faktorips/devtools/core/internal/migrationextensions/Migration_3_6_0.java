/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
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
 * @see IProductCmptType
 * @see IProductCmptCategory
 */
public class Migration_3_6_0 extends DefaultMigration {

    private static final String CATEGORY_ATTRIBUTES = "Attributes"; //$NON-NLS-1$
    private static final String CATEGORY_ATTRIBUTES_DE = "Eigenschaften"; //$NON-NLS-1$
    private static final String CATEGORY_TABLES_FORMULAS = "Tables and Formulas"; //$NON-NLS-1$
    private static final String CATEGORY_TABLES_FORMULAS_DE = "Tabellen und Berechnungsvorschriften"; //$NON-NLS-1$
    private static final String CATEGORY_VALIDATION_RULES = "Validation Rules"; //$NON-NLS-1$
    private static final String CATEGORY_VALIDATION_RULES_DE = "Validierungsregeln"; //$NON-NLS-1$
    private static final String CATEGORY_DEFAULTS_VALUE_SETS = "Defaults and Value Sets"; //$NON-NLS-1$
    private static final String CATEGORY_DEFAULTS_VALUE_SETS_DE = "Vorbelegungen und Wertebereiche"; //$NON-NLS-1$
    private final boolean useGermanCategories;
    private boolean useEnglishLabels;
    private boolean useGermanLabels;

    public Migration_3_6_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
        ISupportedLanguage defaultLanguage = projectToMigrate.getProperties().getDefaultLanguage();
        useGermanCategories = Locale.GERMAN.getLanguage().equals(defaultLanguage.getLanguageName());
        useGermanLabels = projectToMigrate.getReadOnlyProperties().getSupportedLanguage(Locale.GERMAN) != null;
        useEnglishLabels = projectToMigrate.getReadOnlyProperties().getSupportedLanguage(Locale.ENGLISH) != null;
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

    private IProductCmptCategory createCategory(IProductCmptType productCmptType,
            String label,
            String germanLabel,
            Position position) {
        IProductCmptCategory category = productCmptType.newCategory((useGermanCategories ? germanLabel : label));
        category.setPosition(position);
        if (useEnglishLabels) {
            category.setLabelValue(Locale.ENGLISH, label);
        }
        if (useGermanLabels) {
            category.setLabelValue(Locale.GERMAN, germanLabel);
        }
        return category;
    }

    private void createDefaultCategoryAttributes(IProductCmptType productCmptType) throws CoreException {
        if (productCmptType.findDefaultCategoryForProductCmptTypeAttributes(productCmptType.getIpsProject()) == null) {
            IProductCmptCategory attributes = createCategory(productCmptType, CATEGORY_ATTRIBUTES,
                    CATEGORY_ATTRIBUTES_DE, Position.LEFT);
            attributes.setDefaultForProductCmptTypeAttributes(true);
        }
    }

    private void createDefaultCategoryTablesAndFormulas(IProductCmptType productCmptType) throws CoreException {
        boolean defaultTableStructureUsagesExists = productCmptType
                .findDefaultCategoryForTableStructureUsages(productCmptType.getIpsProject()) != null;
        boolean defaultFormulaSignatureDefinitionsExists = productCmptType
                .findDefaultCategoryForFormulaSignatureDefinitions(productCmptType.getIpsProject()) != null;
        if (!defaultTableStructureUsagesExists || !defaultFormulaSignatureDefinitionsExists) {
            IProductCmptCategory tablesAndFormulas = createCategory(productCmptType, CATEGORY_TABLES_FORMULAS,
                    CATEGORY_TABLES_FORMULAS_DE, Position.LEFT);
            tablesAndFormulas.setDefaultForTableStructureUsages(!defaultTableStructureUsagesExists);
            tablesAndFormulas.setDefaultForFormulaSignatureDefinitions(!defaultFormulaSignatureDefinitionsExists);
        }
    }

    private void createDefaultCategoryValidationRules(IProductCmptType productCmptType) throws CoreException {
        if (productCmptType.findDefaultCategoryForValidationRules(productCmptType.getIpsProject()) == null) {
            IProductCmptCategory validationRules = createCategory(productCmptType, CATEGORY_VALIDATION_RULES,
                    CATEGORY_VALIDATION_RULES_DE, Position.LEFT);
            validationRules.setDefaultForValidationRules(true);
        }
    }

    private void createDefaultCategoryDefaultsAndValueSets(IProductCmptType productCmptType) throws CoreException {
        if (productCmptType.findDefaultCategoryForPolicyCmptTypeAttributes(productCmptType.getIpsProject()) == null) {
            IProductCmptCategory defaultsAndValueSets = createCategory(productCmptType, CATEGORY_DEFAULTS_VALUE_SETS,
                    CATEGORY_DEFAULTS_VALUE_SETS_DE, Position.RIGHT);
            defaultsAndValueSets.setDefaultForPolicyCmptTypeAttributes(true);
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
