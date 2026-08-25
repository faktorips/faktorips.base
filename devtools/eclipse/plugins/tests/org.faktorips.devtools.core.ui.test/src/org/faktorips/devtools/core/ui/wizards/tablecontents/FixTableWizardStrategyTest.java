/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FixTableWizardStrategyTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITableStructure structure;
    private ITableContents table;
    private FixTableWizardStrategy strategy;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestStructure");
        structure.newColumn();
        table = newTableContents(structure, "TestContents");
        strategy = new FixTableWizardStrategy(table);
    }

    @Test
    public void testFindContentType_withNullQName_returnsReferencedStructure() {
        ITableStructure result = strategy.findContentType(project, null);

        assertThat(result, is(sameInstance(structure)));
    }

    @Test
    public void testFindContentType_withNullQName_andBrokenReference_returnsNull() {
        table.setTableStructure("non.existing.Structure");

        ITableStructure result = strategy.findContentType(project, null);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void testFindContentType_withValidQName_returnsFoundStructure() {
        ITableStructure result = strategy.findContentType(project, structure.getQualifiedName());

        assertThat(result, is(sameInstance(structure)));
    }

    @Test
    public void testFindContentType_withInvalidQName_returnsNull() {
        ITableStructure result = strategy.findContentType(project, "non.existing.Structure");

        assertThat(result, is(nullValue()));
    }

    @Test
    public void testFindContentType_withInvalidQName_doesNotOverwriteCachedSelection() {
        strategy.findContentType(project, structure.getQualifiedName());

        strategy.findContentType(project, "non.existing.Structure");

        ITableStructure result = strategy.findContentType(project, null);
        assertThat(result, is(sameInstance(structure)));
    }

    @Test
    public void testFindContentType_withNullQName_afterValidSelection_returnsCachedStructure() {
        table.setTableStructure("non.existing.Structure");

        strategy.findContentType(project, null);
        strategy.findContentType(project, structure.getQualifiedName());

        ITableStructure result = strategy.findContentType(project, null);
        assertThat(result, is(sameInstance(structure)));
    }

    @Test
    public void testFindContentType_multipleValidSelections_cachesLatest() {
        ITableStructure otherStructure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE,
                "OtherStructure");
        otherStructure.newColumn();

        strategy.findContentType(project, structure.getQualifiedName());
        strategy.findContentType(project, otherStructure.getQualifiedName());

        ITableStructure result = strategy.findContentType(project, null);
        assertThat(result, is(sameInstance(otherStructure)));
    }

    @Test
    public void testFindContentType_withEmptyString_returnsNull_andPreservesCache() {
        strategy.findContentType(project, structure.getQualifiedName());

        ITableStructure result = strategy.findContentType(project, "");

        assertThat(result, is(nullValue()));
        assertThat(strategy.findContentType(project, null), is(sameInstance(structure)));
    }
}
