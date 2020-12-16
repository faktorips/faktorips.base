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

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.valueset.IStringLengthValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.devtools.core.util.DesignTimeSeverity;
import org.faktorips.util.message.MessageList;

public class Migration_20_6_0 extends MarkAsDirtyMigration {

    private static final String VERSION = "20.6.0"; //$NON-NLS-1$
    private static final Set<IpsObjectType> TYPES_TO_MIGRATE = ImmutableSet.of(IpsObjectType.POLICY_CMPT_TYPE,
            IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.TABLE_CONTENTS, IpsObjectType.PRODUCT_CMPT,
            IpsObjectType.PRODUCT_TEMPLATE, IpsObjectType.TEST_CASE_TYPE, IpsObjectType.TEST_CASE);

    public Migration_20_6_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId, TYPES_TO_MIGRATE, VERSION,
                Messages.Migration_20_6_0_description);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException {
        IIpsProjectProperties properties = getIpsProject().getProperties();
        properties.setDuplicateProductComponentSeverity(DesignTimeSeverity.WARNING);
        properties.setPersistenceColumnSizeChecksSeverity(DesignTimeSeverity.WARNING);
        getIpsProject().setProperties(properties);
        return super.migrate(monitor);
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        if (getIpsProject().isPersistenceSupportEnabled()) {
            migratePersistentTypes(srcFile);
        }
        super.migrate(srcFile);
    }

    private void migratePersistentTypes(IIpsSrcFile srcFile) {
        if (srcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE)) {
            IPolicyCmptType policyType = (IPolicyCmptType)srcFile.getIpsObject();
            migratePersistentAttributes(policyType);
            migrateCascadeTypes(policyType);
        } else if (srcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
            IProductCmpt productCmpt = (IProductCmpt)srcFile.getIpsObject();
            migratePersistentAttributes(productCmpt);
        }
    }

    private void migrateCascadeTypes(IPolicyCmptType policyType) {
        for (IPolicyCmptTypeAssociation association : policyType.getPolicyCmptTypeAssociations()) {
            if (association.isCompositionDetailToMaster()) {
                IPersistentAssociationInfo persistenceInfo = association.getPersistenceAssociatonInfo();
                persistenceInfo.setCascadeTypePersist(false);
                persistenceInfo.setCascadeTypeMerge(false);
                persistenceInfo.setCascadeTypeRemove(false);
                persistenceInfo.setCascadeTypeRefresh(false);
            }
        }
    }

    private void migratePersistentAttributes(IPolicyCmptType policyType) {
        for (IAttribute attribute : policyType.getAttributes()) {
            IPolicyCmptTypeAttribute polAttr = (IPolicyCmptTypeAttribute)attribute;
            if (polAttr.findValueDatatype(getIpsProject()) instanceof StringDatatype
                    && (polAttr.getValueSet() == null || polAttr.getValueSet().isUnrestricted())) {
                int tableColumnSize = polAttr.getPersistenceAttributeInfo().getTableColumnSize();
                changeValueSetToStringLength(polAttr, tableColumnSize);
            }
        }
    }

    private void migratePersistentAttributes(IProductCmpt productCmpt) {
        for (IConfiguredValueSet propertyValue : productCmpt.getPropertyValues(IConfiguredValueSet.class)) {
            try {
                if (propertyValue.getValueSet() != null && propertyValue.getValueSet().isUnrestricted()) {
                    IPolicyCmptTypeAttribute polAttr = propertyValue.findPcTypeAttribute(getIpsProject());
                    if (polAttr.findValueDatatype(getIpsProject()) instanceof StringDatatype
                            && (polAttr.getValueSet() == null || polAttr.getValueSet().isUnrestricted()
                                    || polAttr.getValueSet().isStringLength())) {
                        int tableColumnSize = polAttr.getPersistenceAttributeInfo().getTableColumnSize();
                        changeValueSetToStringLength(propertyValue, tableColumnSize);
                    }
                }
            } catch (CoreException e) {
                continue;
            }
        }
    }

    private void changeValueSetToStringLength(IValueSetOwner polAttr, int tableColumnSize) {
        polAttr.changeValueSetType(ValueSetType.STRINGLENGTH);
        IStringLengthValueSet set = (IStringLengthValueSet)polAttr.getValueSet();
        set.setMaximumLength(String.valueOf(tableColumnSize));
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_20_6_0(ipsProject, featureId);
        }
    }
}
