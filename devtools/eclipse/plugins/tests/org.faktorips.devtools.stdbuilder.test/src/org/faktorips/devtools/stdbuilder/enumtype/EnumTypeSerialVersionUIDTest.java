/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.xtend.enumtype.EnumTypeBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that the generated {@code serialVersionUID} in extensible enum classes is {@code 3L}
 * (FIPS-14499).
 */
public class EnumTypeSerialVersionUIDTest extends AbstractStdBuilderTest {

    private static final String ENUM_TYPE_NAME = "TestExtensibleEnum";

    private IEnumType enumType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        enumType = newEnumType(ipsProject, ENUM_TYPE_NAME);
        enumType.setExtensible(true);
        enumType.setEnumContentName(ENUM_TYPE_NAME + "Content");
        IEnumAttribute idAttribute = enumType.newEnumAttribute();
        idAttribute.setName("id");
        idAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        idAttribute.setIdentifier(true);
        idAttribute.setUnique(true);
        IEnumAttribute nameAttribute = enumType.newEnumAttribute();
        nameAttribute.setName("name");
        nameAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        nameAttribute.setUsedAsNameInFaktorIpsUi(true);
        enumType.newEnumLiteralNameAttribute();
        enumType.getIpsSrcFile().save(null);
    }

    @Test
    public void testSerialVersionUID_isThree() throws Exception {
        EnumTypeBuilder enumTypeBuilder = builderSet.getEnumTypeBuilder();
        MultiStatus status = new MultiStatus("org.faktorips.devtools.stdbuilder", 0, "test", null);
        enumTypeBuilder.beforeBuildProcess(ipsProject, ABuildKind.FULL);
        enumTypeBuilder.beforeBuild(enumType.getIpsSrcFile(), status);
        enumTypeBuilder.build(enumType.getIpsSrcFile());
        enumTypeBuilder.afterBuild(enumType.getIpsSrcFile());

        AFile javaFile = enumTypeBuilder.getJavaFile(enumType.getIpsSrcFile());
        assertThat("Generated Java file does not exist: " + javaFile.toString(), javaFile.exists(), is(true));
        assertThat(getFileContent(javaFile), containsString("serialVersionUID = 3L"));
    }

}
