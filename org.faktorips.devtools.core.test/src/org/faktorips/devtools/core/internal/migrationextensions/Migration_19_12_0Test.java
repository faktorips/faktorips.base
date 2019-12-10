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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.enums.EnumContent;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.internal.model.testcase.TestCase;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

public class Migration_19_12_0Test extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private MarkAsDirtyMigration migration;
    private PolicyCmptType policyCmptType;
    private TestCase testCase;
    private ProductCmptType productCmptType;
    private TableStructure tableStructure;
    private EnumType enumType;
    private EnumContent enumContent;

    private void setUpMigration() throws CoreException {
        ipsProject = newIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "TestPolicy1");
        policyCmptType.getIpsSrcFile().save(true, null);
        productCmptType = newProductCmptType(ipsProject, "TestProduct");
        tableStructure = newTableStructure(ipsProject, "TestStructure");
        enumType = newEnumType(ipsProject, "TestEnum");
        enumContent = newEnumContent(enumType, "TestEnumContent");
        testCase = newTestCase(ipsProject, "TestCase");
        migration = (MarkAsDirtyMigration)new Migration_19_12_0_Factory()
                .createIpsProjectMigrationOpertation(ipsProject, "irrelevant");
    }

    @Test
    public void testMigrate_SrcFilesAreDirty() throws Exception {
        setUpMigration();
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
