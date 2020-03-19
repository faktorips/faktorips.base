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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.testcase.TestCase;
import org.faktorips.devtools.core.internal.model.testcasetype.TestCaseType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.runtime.Severity;
import org.junit.Before;
import org.junit.Test;

public class Migration_20_6_0Test extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private Migration_20_6_0 migration;
    private PolicyCmptType policyCmptType;
    private ProductCmptType productCmptType;
    private TableContents tableContents;
    private ProductCmpt productCmpt;
    private ProductCmpt productTmpl;
    private TestCaseType testCaseType;
    private TestCase testCase;

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

        assertThat(properties.getDuplicateProductComponentSeverity(), is(Severity.WARNING));
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
