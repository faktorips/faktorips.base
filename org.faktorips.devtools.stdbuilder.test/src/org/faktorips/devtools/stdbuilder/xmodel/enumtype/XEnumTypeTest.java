/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.enumtype;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class XEnumTypeTest extends AbstractStdBuilderTest {

    private static final String DISPLAYNAME_NAME = "name";

    private static final String IDENTIFIER_NAME = "id";

    private static final String REPO = "repo";

    private static final String ENUM_TYPE_NAME = "TestEnumType";

    private IEnumType enumType;

    private IEnumAttribute idAttribute;

    private IEnumAttribute nameAttribute;

    private IEnumAttribute inheritedAttribute;

    private XEnumType xenumType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        superEnumType.setAbstract(true);
        IEnumAttribute superEnumAttribute = superEnumType.newEnumAttribute();
        superEnumAttribute.setName("superAttribute");
        superEnumAttribute.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        superEnumType.getIpsSrcFile().save(true, null);

        enumType = newEnumType(ipsProject, ENUM_TYPE_NAME);
        enumType.setAbstract(true);
        enumType.setExtensible(false);
        enumType.setSuperEnumType(superEnumType.getQualifiedName());
        idAttribute = enumType.newEnumAttribute();
        idAttribute.setName(IDENTIFIER_NAME);
        idAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        idAttribute.setUnique(true);
        idAttribute.setIdentifier(true);
        nameAttribute = enumType.newEnumAttribute();
        nameAttribute.setName(DISPLAYNAME_NAME);
        nameAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        nameAttribute.setUsedAsNameInFaktorIpsUi(true);
        inheritedAttribute = enumType.newEnumAttribute();
        inheritedAttribute.setName(superEnumAttribute.getName());
        inheritedAttribute.setInherited(true);

        xenumType = builderSet.getModelNode(enumType, XEnumType.class);
    }

    @Test
    public void testGetNewInstanceCodeFragement_inextensibleEnum() {
        EnumTypeDatatypeAdapter enumTypeAdapter = new EnumTypeDatatypeAdapter(enumType, null);
        enumType.newEnumLiteralNameAttribute();
        createEnumValue();
        xenumType = builderSet.getModelNode(enumType, XEnumType.class);

        JavaCodeFragment codeFragement = xenumType.getNewInstanceCodeFragement(enumTypeAdapter, "1", null);

        assertEquals(ENUM_TYPE_NAME + ".ABC", codeFragement.getSourcecode());
    }

    @Test
    public void testGetNewInstanceCodeFragement_extensibleEnum() {
        enumType.setExtensible(true);
        enumType.newEnumLiteralNameAttribute();
        IEnumContent enumContent = newEnumContent(enumType, ENUM_TYPE_NAME + "Content");
        EnumTypeDatatypeAdapter enumTypeAdapter = new EnumTypeDatatypeAdapter(enumType, enumContent);
        createEnumValue();
        xenumType = builderSet.getModelNode(enumType, XEnumType.class);

        JavaCodeFragment codeFragement = xenumType.getNewInstanceCodeFragement(enumTypeAdapter, "1", null);
        System.out.println(codeFragement.getSourcecode());

        assertEquals(
                "this.getRepository().getExistingEnumValue(TestEnumType.class, IpsStringUtils.isEmpty(\"1\") ? null : Integer.valueOf(\"1\"))",
                codeFragement.getSourcecode());
    }

    @Test
    public void testGetNewInstanceCodeFragement_extensibleEnumWithoutContent() {
        enumType.setExtensible(true);
        enumType.newEnumLiteralNameAttribute();
        EnumTypeDatatypeAdapter enumTypeAdapter = new EnumTypeDatatypeAdapter(enumType, null);
        createEnumValue();
        xenumType = builderSet.getModelNode(enumType, XEnumType.class);

        JavaCodeFragment codeFragement = xenumType.getNewInstanceCodeFragement(enumTypeAdapter, "1", null);

        assertEquals(ENUM_TYPE_NAME + ".ABC", codeFragement.getSourcecode());
    }

    @Test
    public void testGetCallGetValueByIdentifierCodeFragment_inextensible() {
        xenumType = builderSet.getModelNode(enumType, XEnumType.class);

        assertEquals(ENUM_TYPE_NAME + ".getValueById(" + newInstanceFromExpressionForInteger("1") + ")",
                xenumType.getCallGetValueByIdentifierCodeFragment("1", new JavaCodeFragment(REPO)).getSourcecode());
    }

    @Test
    public void testGetCallGetValueByIdentifierCodeFragment_extensible() {
        enumType.setExtensible(true);
        xenumType = builderSet.getModelNode(enumType, XEnumType.class);

        assertEquals(
                REPO + ".getExistingEnumValue(" + ENUM_TYPE_NAME + ".class, " + newInstanceFromExpressionForInteger("1")
                        + ")",
                xenumType.getCallGetValueByIdentifierCodeFragment("1", new JavaCodeFragment(REPO)).getSourcecode());
    }

    @Test
    public void testGetMethodNameGetValueByIdentifier() {
        xenumType = builderSet.getModelNode(enumType, XEnumType.class);
        assertEquals("getValueBy" + StringUtils.capitalize(IDENTIFIER_NAME),
                xenumType.getMethodNameGetValueByIdentifier());
    }

    private void createEnumValue() {
        IEnumValue enumValue = enumType.newEnumValue();
        enumValue.getEnumAttributeValues().get(0).setValue(ValueFactory.createStringValue("1"));
        enumValue.getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue(DISPLAYNAME_NAME));
        enumValue.getEnumAttributeValues().get(3).setValue(ValueFactory.createStringValue("ABC"));
    }

    /* is provided by data type helper : newInstanceFromExpression */
    private static String newInstanceFromExpressionForInteger(String value) {
        return "IpsStringUtils.isEmpty(" + value + ") ? null : Integer.valueOf(" + value + ")";
    }
}
