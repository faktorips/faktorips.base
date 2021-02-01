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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAssociationInfo;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.util.DesignTimeSeverity;
import org.faktorips.devtools.model.valueset.IStringLengthValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.Before;
import org.junit.Test;

public class Migration_20_6_0Test extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private Migration_20_6_0 migration;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private ITableContents tableContents;
    private IProductCmpt productCmpt;
    private IProductCmpt productTmpl;
    private ITestCaseType testCaseType;
    private ITestCase testCase;

    @Before
    public void setUpMigration() throws CoreException {
        ipsProject = newIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "TestPolicyType");
        productCmptType = newProductCmptType(ipsProject, "TestProductType");
        tableContents = newTableContents(ipsProject, "TestStructure");
        productCmpt = newProductCmpt(ipsProject, "TestProduct");
        productTmpl = newProductTemplate(ipsProject, "TestProductTemplate");
        testCaseType = newTestCaseType(ipsProject, "TestCaseType");
        testCase = newTestCase(ipsProject, "TestCase");
        migration = new Migration_20_6_0(ipsProject, "irrelevant");
    }

    @Test
    public void testMigrate_Properties() throws InvocationTargetException, CoreException {
        migration.migrate(new NullProgressMonitor());
        IIpsProjectProperties properties = ipsProject.getProperties();
        assertThat(properties.getDuplicateProductComponentSeverity(), is(DesignTimeSeverity.WARNING));
        assertThat(properties.getPersistenceColumnSizeChecksSeverity(), is(DesignTimeSeverity.WARNING));
    }

    @Test
    public void testMigrate_NoPersistence() throws InvocationTargetException, CoreException {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute("stringAttr");
        attr.setDatatype(Datatype.STRING.getQualifiedName());
        attr.setValueSetType(ValueSetType.UNRESTRICTED);

        migration.migrate(new NullProgressMonitor());

        assertThat(attr.getValueSet().isStringLength(), is(false));
    }

    @Test
    public void testMigrate_PersistentAttributes() throws InvocationTargetException, CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPersistenceSupport(true);
        ipsProject.setProperties(properties);
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute("stringAttr");
        attr.setDatatype(Datatype.STRING.getQualifiedName());
        attr.setValueSetType(ValueSetType.UNRESTRICTED);

        migration.migrate(new NullProgressMonitor());

        assertThat(attr.getValueSet().isStringLength(), is(true));
        assertThat(((IStringLengthValueSet)attr.getValueSet()).getMaximumLength(), is("255"));
    }

    @Test
    public void testMigrate_PersistedAttributesInProduct() throws CoreException, InvocationTargetException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPersistenceSupport(true);
        ipsProject.setProperties(properties);
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute("stringAttr");
        attr.setDatatype(Datatype.STRING.getQualifiedName());
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.getPersistenceAttributeInfo().setTableColumnSize(42);
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(productCmpt.getQualifiedName());
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        productCmpt.setProductCmptType(productCmptType.getQualifiedName());

        IConfiguredValueSet configuredValueSet = productCmpt.newPropertyValue(attr, IConfiguredValueSet.class);
        configuredValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);

        migration.migrate(new NullProgressMonitor());

        assertThat(configuredValueSet.getValueSet().isStringLength(), is(true));
        assertThat(((IStringLengthValueSet)configuredValueSet.getValueSet()).getMaximumLength(), is("42"));
    }

    @Test
    public void testMigrate_PersistentAttributes_TableColumnSizeDeclared()
            throws InvocationTargetException, CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPersistenceSupport(true);
        ipsProject.setProperties(properties);
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute("stringAttr");
        attr.setDatatype(Datatype.STRING.getQualifiedName());
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.getPersistenceAttributeInfo().setTableColumnSize(42);

        migration.migrate(new NullProgressMonitor());

        assertThat(attr.getValueSet().isStringLength(), is(true));
        assertThat(((IStringLengthValueSet)attr.getValueSet()).getMaximumLength(), is("42"));
    }

    @Test
    public void testMigrate_CascadeTypes_DetailToMaster()
            throws InvocationTargetException, CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPersistenceSupport(true);
        ipsProject.setProperties(properties);
        IPolicyCmptTypeAssociation association = policyCmptType.newPolicyCmptTypeAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        IPersistentAssociationInfo persistenceInfo = association.getPersistenceAssociatonInfo();
        persistenceInfo.setCascadeTypePersist(true);
        persistenceInfo.setCascadeTypeMerge(true);
        persistenceInfo.setCascadeTypeRemove(true);
        persistenceInfo.setCascadeTypeRefresh(true);

        migration.migrate(new NullProgressMonitor());

        assertThat(persistenceInfo.isCascadeTypePersist(), is(false));
        assertThat(persistenceInfo.isCascadeTypeMerge(), is(false));
        assertThat(persistenceInfo.isCascadeTypeRemove(), is(false));
        assertThat(persistenceInfo.isCascadeTypeRefresh(), is(false));
    }

    @Test
    public void testMigrate_CascadeTypes_MasterToDetail()
            throws InvocationTargetException, CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPersistenceSupport(true);
        ipsProject.setProperties(properties);
        IPolicyCmptTypeAssociation association = policyCmptType.newPolicyCmptTypeAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        IPersistentAssociationInfo persistenceInfo = association.getPersistenceAssociatonInfo();
        persistenceInfo.setCascadeTypePersist(true);
        persistenceInfo.setCascadeTypeMerge(false);
        persistenceInfo.setCascadeTypeRemove(true);
        persistenceInfo.setCascadeTypeRefresh(false);

        migration.migrate(new NullProgressMonitor());

        assertThat(persistenceInfo.isCascadeTypePersist(), is(true));
        assertThat(persistenceInfo.isCascadeTypeMerge(), is(false));
        assertThat(persistenceInfo.isCascadeTypeRemove(), is(true));
        assertThat(persistenceInfo.isCascadeTypeRefresh(), is(false));
    }

    @Test
    public void testMigrate_SrcFilesAreDirty() throws Exception {
        migration.migrate(new NullProgressMonitor());

        assertThat(policyCmptType.getIpsSrcFile().isDirty(), is(true));
        assertThat(productCmptType.getIpsSrcFile().isDirty(), is(true));
        assertThat(tableContents.getIpsSrcFile().isDirty(), is(true));
        assertThat(productCmpt.getIpsSrcFile().isDirty(), is(true));
        assertThat(productTmpl.getIpsSrcFile().isDirty(), is(true));
        assertThat(testCaseType.getIpsSrcFile().isDirty(), is(true));
        assertThat(testCase.getIpsSrcFile().isDirty(), is(true));
    }

}
