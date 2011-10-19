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
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;

/**
 * Migration to version 3.6.0.
 * <p>
 * Searches for all {@link IProductCmptType}s that have no supertype. For each such
 * {@link IProductCmptType}, default {@link IProductCmptCategory}s are created. The categories that
 * are created correspond to the UI sections as they are set up prior to version 3.6.0.
 * <p>
 * Furthermore, all IPS objects that are able to contain {@link IProductCmptProperty}s are marked
 * dirty so they are saved. This is necessary as a new feature called 'category' was added to these
 * properties.
 * 
 * @author Alexander Weickmann
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

        srcFile.markAsDirty();
    }

    private void createDefaultCategories(IProductCmptType productCmptType) {
        IProductCmptCategory attributes = productCmptType
                .newProductCmptCategory(Messages.Migration_3_6_0_nameDefaultCategoryAttributes);
        attributes.setDefaultForProductCmptTypeAttributes(true);
        IProductCmptCategory tablesAndFormulas = productCmptType
                .newProductCmptCategory(Messages.Migration_3_6_0_nameDefaultCategoryTablesAndFormulas);
        tablesAndFormulas.setDefaultForTableStructureUsages(true);
        tablesAndFormulas.setDefaultForFormulaSignatureDefinitions(true);
        IProductCmptCategory validationRules = productCmptType
                .newProductCmptCategory(Messages.Migration_3_6_0_nameDefaultCategoryValidationRules);
        validationRules.setDefaultForValidationRules(true);
        IProductCmptCategory defaultsAndValueSets = productCmptType
                .newProductCmptCategory(Messages.Migration_3_6_0_nameDefaultCategoryDefaultsAndValueSets);
        defaultsAndValueSets.setDefaultForPolicyCmptTypeAttributes(true);
        defaultsAndValueSets.setPosition(Position.RIGHT);
    }

    @Override
    public String getTargetVersion() {
        return "3.6.0"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return "Product component properties (product relevant policy component type attributes" //$NON-NLS-1$
                + ", configurable validation rules, product component type attributes, formula " //$NON-NLS-1$
                + "signature definitions and table structure usages) are now all assigned to " //$NON-NLS-1$
                + "categories in the corresponding product component type. For all product component " //$NON-NLS-1$
                + "types without a superclass, the default categories are created. The default" //$NON-NLS-1$
                + " categories correspond to the sorting as it is prior to version 3.6.0.\n\n" //$NON-NLS-1$
                + "Furthermore, the XML of all product component types and policy component types" //$NON-NLS-1$
                + " is updated as all product component properties now feature a new 'category' attribute."; //$NON-NLS-1$
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {

        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {

            return new Migration_3_6_0(ipsProject, featureId);
        }

    }

}
