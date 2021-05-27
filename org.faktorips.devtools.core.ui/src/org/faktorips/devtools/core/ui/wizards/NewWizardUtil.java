/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

/**
 * Contains utility methods for Faktor-IPS 'New' wizards.
 * 
 * @since 3.6
 */
public class NewWizardUtil {

    private NewWizardUtil() {
        // Utility class not to be instantiated
    }

    /**
     * Creates the default categories "Attributes", "Tables and Formulas", "Validation Rules" and
     * "Defaults and Value Sets" for the indicated {@link IProductCmptType} if it has no supertype.
     * 
     * @param productCmptType the {@link IProductCmptType} to create the default categories for if
     *            necessary
     */
    public static void createDefaultCategoriesIfNecessary(IProductCmptType productCmptType) {
        if (productCmptType.hasSupertype()) {
            return;
        }

        IProductCmptCategory attributes = productCmptType
                .newCategory(Messages.NewWizardUtil_nameDefaultCategoryAttributes);
        attributes.setDefaultForProductCmptTypeAttributes(true);
        attributes.setPosition(Position.LEFT);

        IProductCmptCategory tablesAndFormulas = productCmptType
                .newCategory(Messages.NewWizardUtil_nameDefaultCategoryTablesAndFormulas);
        tablesAndFormulas.setDefaultForTableStructureUsages(true);
        tablesAndFormulas.setDefaultForFormulaSignatureDefinitions(true);
        tablesAndFormulas.setPosition(Position.LEFT);

        IProductCmptCategory validationRules = productCmptType
                .newCategory(Messages.NewWizardUtil_nameDefaultCategoryValidationRules);
        validationRules.setDefaultForValidationRules(true);
        validationRules.setPosition(Position.LEFT);

        IProductCmptCategory defaultsAndValueSets = productCmptType
                .newCategory(Messages.NewWizardUtil_nameDefaultCategoryDefaultsAndValueSets);
        defaultsAndValueSets.setDefaultForPolicyCmptTypeAttributes(true);
        defaultsAndValueSets.setPosition(Position.RIGHT);
    }
}
