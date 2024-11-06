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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.junit.Test;

public class MarkAsDirtyMigrationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private MarkAsDirtyMigration migration;

    private void setUpMigration() {
        ipsProject = newIpsProject();
        migration = new MarkAsDirtyMigration(ipsProject, "irrelevant", Set.of(IpsObjectType.POLICY_CMPT_TYPE,
                IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.ENUM_TYPE), "47.11", "Lorem Ipsum");
    }

    @Test
    public void testTargetVersion() {
        setUpMigration();

        assertThat(migration.getTargetVersion(), is("47.11"));
    }

    @Test
    public void testGetTargetVersion_SameReleaseButAlpha() {
        migration = new MarkAsDirtyMigration(ipsProject, "irrelevant", Set.of(IpsObjectType.POLICY_CMPT_TYPE,
                IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.ENUM_TYPE), "47.11", "Lorem Ipsum") {
            @Override
            String getFaktorIpsVersion() {
                return "47.11.0.a20471101-01";
            }
        };

        assertThat(migration.getTargetVersion(), is("47.11.a20471101-01"));
    }

    @Test
    public void testGetTargetVersion_SameReleaseButFinalRelease() {
        migration = new MarkAsDirtyMigration(ipsProject, "irrelevant", Set.of(IpsObjectType.POLICY_CMPT_TYPE,
                IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.ENUM_TYPE), "47.11.0", "Lorem Ipsum") {
            @Override
            String getFaktorIpsVersion() {
                return "47.11.0.release";
            }
        };

        assertThat(migration.getTargetVersion(), is("47.11"));
    }

    @Test
    public void testDescription() {
        setUpMigration();

        assertThat(migration.getDescription(), is("Lorem Ipsum"));
    }

    @Test
    public void testMigrate_SrcFilesAreDirty() throws Exception {
        setUpMigration();
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "TestPolicy1");
        policyCmptType.getIpsSrcFile().save(null);
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "TestProduct");
        ITableStructure tableStructure = newTableStructure(ipsProject, "TestStructure");
        IEnumType enumType = newEnumType(ipsProject, "TestEnum");
        IEnumContent enumContent = newEnumContent(enumType, "TestEnumContent");
        ITestCase testCase = newTestCase(ipsProject, "TestCase");

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

    @Test
    public void testMigrate_NonParsableFilesAreNotMigrated() throws Exception {
        setUpMigration();
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "TestPolicy1");
        IIpsSrcFile ipsSrcFile = policyCmptType.getIpsSrcFile();
        ipsSrcFile.save(null);
        ipsSrcFile.getCorrespondingFile()
                .setContents(new ByteArrayInputStream("broken".getBytes(StandardCharsets.UTF_8)), false, null);

        assertFalse(ipsSrcFile.isDirty());

        migration.migrate(new NullProgressMonitor());

        assertFalse(ipsSrcFile.isDirty());
        assertThat(new String(ipsSrcFile.getCorrespondingFile().getContents().readAllBytes(), StandardCharsets.UTF_8),
                is("broken"));
    }

}
