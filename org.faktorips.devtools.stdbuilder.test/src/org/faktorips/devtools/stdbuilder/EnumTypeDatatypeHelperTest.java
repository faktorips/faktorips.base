/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.junit.Test;

public class EnumTypeDatatypeHelperTest extends AbstractStdBuilderTest {

    @Test
    public void testHelper() throws Exception {
        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setExtensible(false);
        paymentMode.newEnumLiteralNameAttribute();

        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setIdentifier(true);
        id.setUnique(true);
        id.setName("id");

        IEnumValue value1 = paymentMode.newEnumValue();
        value1.setEnumAttributeValue(0, ValueFactory.createStringValue("MONTHLY"));
        value1.setEnumAttributeValue(1, ValueFactory.createStringValue("monthly"));
        IEnumValue value2 = paymentMode.newEnumValue();
        value2.setEnumAttributeValue(0, ValueFactory.createStringValue("ANNUALLY"));
        value2.setEnumAttributeValue(1, ValueFactory.createStringValue("annually"));
        Datatype datatype = ipsProject.findDatatype("PaymentMode");
        DatatypeHelper datatypeHelper = ipsProject.getDatatypeHelper(datatype);
        assertTrue(datatypeHelper instanceof EnumTypeDatatypeHelper);

        EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)datatypeHelper;

        JavaCodeFragment fragment = enumHelper.newInstance("annually");
        assertEquals("PaymentMode.ANNUALLY", fragment.getSourcecode());

        // Ensure than no exception is thrown if the EnumType doesn't have an identifier attribute.
        id.setIdentifier(false);
        enumHelper.newInstance("annually");
        id.setIdentifier(true);

        fragment = enumHelper.newInstanceFromExpression("getValue()");
        assertTrue(fragment.getSourcecode().indexOf("PaymentMode.getValueById(getValue())") >= 0);

        fragment = enumHelper.nullExpression();
        assertEquals("null", fragment.getSourcecode());

        String packageName = ipsProject.getIpsObjectPath().getSourceFolderEntries()[0]
                .getBasePackageNameForMergableJavaClasses();
        assertEquals(packageName + ".PaymentMode", enumHelper.getJavaClassName());
    }

    @Test
    public void testConstructor() {
        try {
            new EnumTypeDatatypeHelper(null, null);
            fail("Argument Check doesn't work.");
        } catch (Exception e) {
        }
    }

}
