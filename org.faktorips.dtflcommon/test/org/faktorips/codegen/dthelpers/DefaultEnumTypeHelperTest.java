package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.JavaCodeFragment;

import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class DefaultEnumTypeHelperTest extends TestCase {

    public void testDefaultEnumTypeHelper() {
        DefaultEnumTypeHelper helper = new DefaultEnumTypeHelper(PaymentMode.class, "getEnumType", "getPaymentMode");
        assertEquals(PaymentMode.getEnumType(), helper.getDatatype());
    }
    
    public void testNewInstance() {
        DefaultEnumTypeHelper helper = new DefaultEnumTypeHelper(PaymentMode.class, "getEnumType", "getPaymentMode");
        JavaCodeFragment fragment = helper.newInstance("a");
        assertEquals(1, fragment.getImportDeclaration().getNoOfImports());
        assertEquals("PaymentMode.getPaymentMode(\"a\")", fragment.getSourcecode());
    }

}
