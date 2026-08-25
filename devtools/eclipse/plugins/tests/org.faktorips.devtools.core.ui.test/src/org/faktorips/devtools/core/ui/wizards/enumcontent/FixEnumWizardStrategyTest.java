/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.devtools.model.enums.IEnumType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FixEnumWizardStrategyTest extends AbstractIpsEnumPluginTest {

    private FixEnumWizardStrategy strategy;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        strategy = new FixEnumWizardStrategy(genderEnumContent);
    }

    @Test
    public void testFindContentType_withNullQName_returnsReferencedEnumType() {
        IEnumType result = strategy.findContentType(ipsProject, null);

        assertThat(result, is(sameInstance(genderEnumType)));
    }

    @Test
    public void testFindContentType_withNullQName_andBrokenReference_returnsNull() {
        genderEnumContent.setEnumType("non.existing.EnumType");

        IEnumType result = strategy.findContentType(ipsProject, null);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void testFindContentType_withValidQName_returnsFoundType() {
        IEnumType result = strategy.findContentType(ipsProject, genderEnumType.getQualifiedName());

        assertThat(result, is(sameInstance(genderEnumType)));
    }

    @Test
    public void testFindContentType_withInvalidQName_returnsNull() {
        IEnumType result = strategy.findContentType(ipsProject, "non.existing.Type");

        assertThat(result, is(nullValue()));
    }

    @Test
    public void testFindContentType_withInvalidQName_doesNotOverwriteCachedSelection() {
        strategy.findContentType(ipsProject, genderEnumType.getQualifiedName());

        strategy.findContentType(ipsProject, "non.existing.Type");

        IEnumType result = strategy.findContentType(ipsProject, null);
        assertThat(result, is(sameInstance(genderEnumType)));
    }

    @Test
    public void testFindContentType_withNullQName_afterValidSelection_returnsCachedType() {
        genderEnumContent.setEnumType("non.existing.EnumType");

        strategy.findContentType(ipsProject, null);
        strategy.findContentType(ipsProject, genderEnumType.getQualifiedName());

        IEnumType result = strategy.findContentType(ipsProject, null);
        assertThat(result, is(sameInstance(genderEnumType)));
    }

    @Test
    public void testFindContentType_multipleValidSelections_cachesLatest() {
        IEnumType otherEnumType = newEnumType(ipsProject, "OtherEnumType");
        otherEnumType.setAbstract(false);
        otherEnumType.setExtensible(true);

        strategy.findContentType(ipsProject, genderEnumType.getQualifiedName());
        strategy.findContentType(ipsProject, otherEnumType.getQualifiedName());

        IEnumType result = strategy.findContentType(ipsProject, null);
        assertThat(result, is(sameInstance(otherEnumType)));
    }

    @Test
    public void testFindContentType_withEmptyString_returnsNull_andPreservesCache() {
        strategy.findContentType(ipsProject, genderEnumType.getQualifiedName());

        IEnumType result = strategy.findContentType(ipsProject, "");

        assertThat(result, is(nullValue()));
        assertThat(strategy.findContentType(ipsProject, null), is(sameInstance(genderEnumType)));
    }
}
