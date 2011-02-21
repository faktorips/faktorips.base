/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

public class EnumTypeDatatypeHelperTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Test
    public void testHelper() throws Exception {
        ipsProject = newIpsProject("TestProject");
        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setContainingValues(true);
        paymentMode.newEnumLiteralNameAttribute();

        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setIdentifier(true);
        id.setUnique(true);
        id.setName("id");

        IEnumValue value1 = paymentMode.newEnumValue();
        value1.setEnumAttributeValue(0, "MONTHLY");
        value1.setEnumAttributeValue(1, "monthly");
        IEnumValue value2 = paymentMode.newEnumValue();
        value2.setEnumAttributeValue(0, "ANNUALLY");
        value2.setEnumAttributeValue(1, "annually");
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
        System.out.println(fragment.getSourcecode());
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
