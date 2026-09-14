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

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.junit.jupiter.api.Test;

public class Migration_27_1_0Test extends AbstractIpsPluginTest {

    @Test
    public void testMigrate_SetsMessageCodeNotDerivedFromNameForExistingRule() {
        IIpsProject ipsProject = newIpsProject();
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "Policy");
        IValidationRule rule = policyCmptType.newRule();
        rule.setName("checkPostcode");
        IIpsSrcFile ipsSrcFile = policyCmptType.getIpsSrcFile();
        ipsSrcFile.save(null);

        Migration_27_1_0 migration = new Migration_27_1_0(ipsProject, "");
        migration.migrate(ipsSrcFile);

        assertThat(rule.isMessageCodeDerivedFromName(), is(false));
    }
}
