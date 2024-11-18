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
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class Migration_25_1_0Test extends AbstractIpsPluginTest {

    @SuppressWarnings("restriction")
    @Test
    public void testMigrate_SetAttributesMandatory() {
        IIpsProject ipsProject = newIpsProject();
        EnumType enumType = newDefaultEnumType(ipsProject, "ET");
        IEnumAttribute enumAttribute = enumType.newEnumAttribute();
        enumAttribute.setName("ea");
        enumAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        enumAttribute.setMandatory(false);
        enumType.getEnumAttribute("name").setMandatory(false);
        IIpsSrcFile ipsSrcFile = enumType.getIpsSrcFile();
        ipsSrcFile.save(null);

        Migration_25_1_0 migration = new Migration_25_1_0(ipsProject, "");
        migration.setAllEnumAttributesMandatory(true);
        migration.migrate(ipsSrcFile);

        assertThat(enumAttribute.isMandatory(), is(true));
        assertThat(enumType.getEnumAttribute("id").isMandatory(), is(true));
        assertThat(enumType.getEnumAttribute("name").isMandatory(), is(true));
    }

    @SuppressWarnings("restriction")
    @Test
    public void testMigrate_DontSetAttributesMandatory() {
        IIpsProject ipsProject = newIpsProject();
        EnumType enumType = newDefaultEnumType(ipsProject, "ET");
        IEnumAttribute enumAttribute = enumType.newEnumAttribute();
        enumAttribute.setName("ea");
        enumAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        enumAttribute.setMandatory(false);
        enumType.getEnumAttribute("name").setMandatory(false);
        IIpsSrcFile ipsSrcFile = enumType.getIpsSrcFile();
        ipsSrcFile.save(null);

        Migration_25_1_0 migration = new Migration_25_1_0(ipsProject, "");
        migration.setAllEnumAttributesMandatory(false);
        migration.migrate(ipsSrcFile);

        assertThat(enumAttribute.isMandatory(), is(false));
        assertThat(enumType.getEnumAttribute("id").isMandatory(), is(true));
        assertThat(enumType.getEnumAttribute("name").isMandatory(), is(false));
    }

}
