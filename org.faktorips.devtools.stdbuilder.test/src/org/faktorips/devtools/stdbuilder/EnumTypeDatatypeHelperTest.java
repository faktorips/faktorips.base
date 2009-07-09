/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class EnumTypeDatatypeHelperTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    
    public void testHelper() throws Exception {
        ipsProject = newIpsProject("TestProject");
        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setContainingValues(true);

        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setLiteralName(true);
        id.setIdentifier(true);
        id.setUnique(true);
        id.setName("id");
        IEnumValue value1 = paymentMode.newEnumValue();
        IEnumAttributeValue value1id = value1.getEnumAttributeValues().get(0);
        value1id.setValue("monthly");
        IEnumValue value2 = paymentMode.newEnumValue();
        IEnumAttributeValue value2id = value2.getEnumAttributeValues().get(0);
        value2id.setValue("annually");
        Datatype datatype = ipsProject.findDatatype("PaymentMode");
        DatatypeHelper datatypeHelper = ipsProject.getDatatypeHelper(datatype);
        assertTrue(datatypeHelper instanceof EnumTypeDatatypeHelper);
        
        EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)datatypeHelper;

        JavaCodeFragment fragment = enumHelper.newInstance("annually");
        assertEquals("PaymentMode.ANNUALLY", fragment.getSourcecode());
        
        //ensure than no exception is thrown if the enumtype doesn't have an id attribute
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
    
    public void testConstructor(){
        try {
            new EnumTypeDatatypeHelper(null, null);
            fail("Argument Check doesn't work.");
        } catch (Exception e) {
        }
    }
    

}
