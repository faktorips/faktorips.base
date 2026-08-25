/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.editors.pctype.PersistentTypeInfoSection.PersistenceTableNamePmo;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypeInfo;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypeInfo.PersistentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PersistenceTableNamePmoTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private PolicyCmptType policyCmptType;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPersistenceSupport(true);
        ipsProject.setProperties(props);

        policyCmptType = newPolicyCmptType(ipsProject, "Policy1");
        policyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
    }

    @Test
    public void testGetTableNameOwnTableIsUsed() {
        IPersistentTypeInfo persistentTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persistentTypeInfo.setUseTableDefinedInSupertype(false);
        persistentTypeInfo.setTableName("Policy1Table");

        PersistenceTableNamePmo pmo = new PersistenceTableNamePmo(persistentTypeInfo);

        assertThat(pmo.getTableName(), is("Policy1Table"));
    }

    @Test
    public void testGetTableNameFromDirectSupertype() {
        PolicyCmptType superPcType = newPolicyCmptType(ipsProject, "SuperPolicy");
        policyCmptType.setSupertype(superPcType.getQualifiedName());

        IPersistentTypeInfo superInfo = superPcType.getPersistenceTypeInfo();
        superInfo.setPersistentType(PersistentType.ENTITY);
        superInfo.setUseTableDefinedInSupertype(false);
        superInfo.setTableName("SuperTable");

        IPersistentTypeInfo persistentTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persistentTypeInfo.setUseTableDefinedInSupertype(true);

        PersistenceTableNamePmo pmo = new PersistenceTableNamePmo(persistentTypeInfo);

        assertThat(pmo.getTableName(), is("SuperTable"));
    }

    @Test
    public void testGetTableNameTraversesUpToNearestTypeDefiningItsOwnTableNotRoot() {

        PolicyCmptType rootPcType = newPolicyCmptType(ipsProject, "RootPolicy");
        PolicyCmptType superPcType = newPolicyCmptType(ipsProject, "SuperPolicy");
        superPcType.setSupertype(rootPcType.getQualifiedName());
        policyCmptType.setSupertype(superPcType.getQualifiedName());

        IPersistentTypeInfo rootInfo = rootPcType.getPersistenceTypeInfo();
        rootInfo.setPersistentType(PersistentType.ENTITY);
        rootInfo.setUseTableDefinedInSupertype(false);
        rootInfo.setTableName("RootTable");

        IPersistentTypeInfo superInfo = superPcType.getPersistenceTypeInfo();
        superInfo.setPersistentType(PersistentType.ENTITY);
        superInfo.setUseTableDefinedInSupertype(false);
        superInfo.setTableName("SuperTable");

        IPersistentTypeInfo persistentTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persistentTypeInfo.setUseTableDefinedInSupertype(true);

        PersistenceTableNamePmo pmo = new PersistenceTableNamePmo(persistentTypeInfo);

        assertThat(pmo.getTableName(), is("SuperTable"));
    }

    @Test
    public void testGetTableNameNoSupertypeReturnsSupertypeNotFoundMessage() {
        IPersistentTypeInfo persistentTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persistentTypeInfo.setUseTableDefinedInSupertype(true);

        PersistenceTableNamePmo pmo = new PersistenceTableNamePmo(persistentTypeInfo);

        assertThat(pmo.getTableName(), is(Messages.PersistentTypeInfoSection_textSupertypeNotFound));
    }

    @Test
    public void testGetTableNameCycleInHierarchyReturnsSupertypeNotFoundMessage() {
        PolicyCmptType otherPcType = newPolicyCmptType(ipsProject, "OtherPolicy");
        policyCmptType.setSupertype(otherPcType.getQualifiedName());
        otherPcType.setSupertype(policyCmptType.getQualifiedName());

        IPersistentTypeInfo persistentTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persistentTypeInfo.setUseTableDefinedInSupertype(true);
        otherPcType.getPersistenceTypeInfo().setUseTableDefinedInSupertype(true);

        PersistenceTableNamePmo pmo = new PersistenceTableNamePmo(persistentTypeInfo);

        assertThat(pmo.getTableName(), is(Messages.PersistentTypeInfoSection_textSupertypeNotFound));
    }

    @Test
    public void testSetTableNameDelegatesToPersistentTypeInfo() {
        IPersistentTypeInfo persistentTypeInfo = policyCmptType.getPersistenceTypeInfo();
        PersistenceTableNamePmo pmo = new PersistenceTableNamePmo(persistentTypeInfo);

        pmo.setTableName("NewTableName");

        assertThat(persistentTypeInfo.getTableName(), is("NewTableName"));
    }

    @Test
    public void testGetPolicyCmptType() {
        IPersistentTypeInfo persistentTypeInfo = policyCmptType.getPersistenceTypeInfo();
        PersistenceTableNamePmo pmo = new PersistenceTableNamePmo(persistentTypeInfo);

        assertThat(pmo.getPolicyCmptType(), is(policyCmptType));
    }

}
