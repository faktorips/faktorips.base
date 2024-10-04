/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.migration;

import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

/**
 * Base class for custom migrations that migrate {@link IProductCmpt product components} after an
 * attribute was renamed in a model type.
 */
public class MigrationForChangedAttribute extends DefaultMigration {

    private String targetVersion;
    private String description;
    private ChangedAttribute[] changedAttributes;

    public MigrationForChangedAttribute(IIpsProject projectToMigrate, String featureId, String targetVersion,
            String description, ChangedAttribute... changedAttributes) {
        super(projectToMigrate, featureId);
        this.targetVersion = targetVersion;
        this.description = description;
        this.changedAttributes = changedAttributes;
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws IpsException {
        if (IpsObjectType.PRODUCT_CMPT.equals(srcFile.getIpsObjectType())) {
            IProductCmpt productCmpt = (IProductCmpt)srcFile.getIpsObject();
            IProductCmptType productCmptType = productCmpt.findProductCmptType(getIpsProject());
            List<IProductCmptType> productCmptTypes = productCmptType.getSupertypeHierarchy()
                    .getAllSupertypesInclSelf(productCmptType).stream().map(IProductCmptType.class::cast).toList();
            List<IPolicyCmptType> policyCmptTypes = productCmptTypes.stream()
                    .filter(IProductCmptType::isConfigurationForPolicyCmptType)
                    .map(t -> t.findPolicyCmptType(getIpsProject())).toList();
            Arrays.stream(changedAttributes).forEach(changedAttribute -> {
                if (productCmptTypes.stream()
                        .anyMatch(t -> t.getQualifiedName().equals(changedAttribute.qualifiedTypeName))) {
                    IAttributeValue attributeValue = productCmpt.getAttributeValue(changedAttribute.oldName);
                    if (attributeValue != null) {
                        attributeValue.setAttribute(changedAttribute.newName);
                        srcFile.save(null);
                    }
                }
                if (policyCmptTypes.stream()
                        .anyMatch(t -> t.getQualifiedName().equals(changedAttribute.qualifiedTypeName))) {
                    List<IPropertyValue> propertyValues = productCmpt.getPropertyValues(changedAttribute.oldName);
                    propertyValues.forEach(v -> {
                        if (v instanceof IConfigElement configElement) {
                            configElement.setPolicyCmptTypeAttribute(changedAttribute.newName);
                        }
                    });
                    srcFile.save(null);
                }
            });
        }
    }

    @Override
    public String getTargetVersion() {
        return targetVersion;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * A changed attribute is defined by the qualified name of the policy component type or product
     * component type it is defined in and the old and new name of the attribute.
     */
    public record ChangedAttribute(String qualifiedTypeName, String oldName, String newName) {

    }

}
