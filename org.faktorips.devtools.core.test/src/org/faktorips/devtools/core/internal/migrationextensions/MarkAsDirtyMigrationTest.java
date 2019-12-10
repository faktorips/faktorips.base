/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.migrationextensions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.enums.EnumContent;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.internal.model.testcase.TestCase;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

public class MarkAsDirtyMigrationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private MarkAsDirtyMigration migration;

    private void setUpMigration() throws CoreException {
        ipsProject = newIpsProject();
        migration = new MarkAsDirtyMigration(ipsProject, "irrelevant", ImmutableSet.of(IpsObjectType.POLICY_CMPT_TYPE,
                IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.ENUM_TYPE), "47.11", "Lorem Ipsum");
    }

    @Test
    public void testTargetVersion() throws CoreException {
        setUpMigration();

        assertThat(migration.getTargetVersion(), is("47.11"));
    }

    @Test
    public void testDescription() throws CoreException {
        setUpMigration();

        assertThat(migration.getDescription(), is("Lorem Ipsum"));
    }

    @Test
    public void testMigrate_SrcFilesAreDirty() throws Exception {
        setUpMigration();
        PolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "TestPolicy1");
        policyCmptType.getIpsSrcFile().save(true, null);
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "TestProduct");
        TableStructure tableStructure = newTableStructure(ipsProject, "TestStructure");
        EnumType enumType = newEnumType(ipsProject, "TestEnum");
        EnumContent enumContent = newEnumContent(enumType, "TestEnumContent");
        TestCase testCase = newTestCase(ipsProject, "TestCase");

        assertFalse(policyCmptType.getIpsSrcFile().isDirty());
        assertFalse(productCmptType.getIpsSrcFile().isDirty());
        assertFalse(tableStructure.getIpsSrcFile().isDirty());
        assertFalse(enumType.getIpsSrcFile().isDirty());
        assertFalse(enumContent.getIpsSrcFile().isDirty());
        assertFalse(testCase.getIpsSrcFile().isDirty());

        migration.migrate(new NullProgressMonitor());

        assertTrue(policyCmptType.getIpsSrcFile().isDirty());
        assertTrue(productCmptType.getIpsSrcFile().isDirty());
        assertFalse(tableStructure.getIpsSrcFile().isDirty());
        assertTrue(enumType.getIpsSrcFile().isDirty());
        assertFalse(enumContent.getIpsSrcFile().isDirty());
        assertFalse(testCase.getIpsSrcFile().isDirty());
    }

}
