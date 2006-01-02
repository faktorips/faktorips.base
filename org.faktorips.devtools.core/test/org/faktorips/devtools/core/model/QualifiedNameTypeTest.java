package org.faktorips.devtools.core.model;

import junit.framework.TestCase;

public class QualifiedNameTypeTest extends TestCase {

    public void setUp(){
        
    }
    
    /*
     * Test method for 'org.faktorips.plugin.model.QualifiedNameType.hashCode()'
     */
    public void testHashCode() {
        QualifiedNameType type1 = new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        QualifiedNameType type2 = new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals(type1.hashCode(), type2.hashCode());
        
        QualifiedNameType type3 = new QualifiedNameType("test", IpsObjectType.TABLE_STRUCTURE);
        assertFalse(type1.hashCode() == type3.hashCode());
    }

    /*
     * Test method for 'org.faktorips.plugin.model.QualifiedNameType.QualifiedNameType(String, IpsObjectType)'
     */
    public void testQualifiedNameType() {
        new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        try{
            new QualifiedNameType(null, IpsObjectType.POLICY_CMPT_TYPE);
            fail("Exception because of null argument expected");
        }
        catch(Exception e){}
        try{
            new QualifiedNameType("test", null);
            fail("Exception because of null argument expected");
        }
        catch(Exception e){}
    }

    /*
     * Test method for 'org.faktorips.plugin.model.QualifiedNameType.equals(Object)'
     */
    public void testEqualsObject() {
        QualifiedNameType type1 = new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        QualifiedNameType type2 = new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals(type1, type2);
        
        QualifiedNameType type3 = new QualifiedNameType("test", IpsObjectType.TABLE_STRUCTURE);
        assertFalse(type1.equals(type3));
        
        QualifiedNameType type4 = new QualifiedNameType("test1", IpsObjectType.POLICY_CMPT_TYPE);
        assertFalse(type1.equals(type4));
    }

    /*
     * Test method for 'org.faktorips.plugin.model.QualifiedNameType.toString()'
     */
    public void testToString() {
        QualifiedNameType type1 = new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals(type1.toString(), "IPolicyCmptType: test");
    }

}
