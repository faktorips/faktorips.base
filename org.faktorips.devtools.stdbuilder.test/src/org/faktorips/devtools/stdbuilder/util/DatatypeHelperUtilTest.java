/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.junit.Test;

public class DatatypeHelperUtilTest extends AbstractStdBuilderTest {

    @Test
    public void testNewInstanceFromExpression_ExtensibleEnum() throws Exception {
        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setExtensible(true);
        paymentMode.newEnumLiteralNameAttribute();
        IEnumAttribute identifierAttribute = paymentMode.newEnumAttribute();
        identifierAttribute.setIdentifier(true);
        identifierAttribute.setName("id");
        identifierAttribute.setDatatype(Datatype.STRING.getName());

        Datatype datatype = ipsProject.findDatatype("PaymentMode");
        DatatypeHelper datatypeHelper = ipsProject.getDatatypeHelper(datatype);

        assertTrue(datatypeHelper instanceof EnumTypeDatatypeHelper);

        EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)datatypeHelper;

        JavaCodeFragment fragment = enumHelper.newInstanceFromExpression("getValue()");

        String repoExpression = "customRepo";

        assertEquals(".getExistingEnumValue(PaymentMode.class, getValue())", fragment.getSourcecode());
        String newInstanceFromExpression = repoExpression + fragment.getSourcecode();
        assertEquals(newInstanceFromExpression, DatatypeHelperUtil
                .getNewInstanceFromExpression(datatypeHelper, "getValue()", repoExpression).getSourcecode());
    }

    @Test
    public void testNewInstanceFromExpression_NonExtensibleEnum() throws Exception {
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

        Datatype datatype = ipsProject.findDatatype("PaymentMode");
        DatatypeHelper datatypeHelper = ipsProject.getDatatypeHelper(datatype);

        JavaCodeFragment newInstanceFromExpression = datatypeHelper.newInstanceFromExpression("getValue()");
        assertEquals(newInstanceFromExpression,
                DatatypeHelperUtil.getNewInstanceFromExpression(datatypeHelper, "getValue()", "repo"));
    }

    @Test
    public void testNewInstanceFromExpression_OtherDatatypes() throws Exception {
        DatatypeHelper booleanTypeHelper = ipsProject.getDatatypeHelper(Datatype.BOOLEAN);

        JavaCodeFragment newInstanceFromExpression = booleanTypeHelper.newInstanceFromExpression("getValue()");
        assertEquals(newInstanceFromExpression,
                DatatypeHelperUtil.getNewInstanceFromExpression(booleanTypeHelper, "getValue()", "repo"));
    }
}
