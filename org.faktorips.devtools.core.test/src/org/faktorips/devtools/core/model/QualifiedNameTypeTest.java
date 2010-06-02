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

package org.faktorips.devtools.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;

public class QualifiedNameTypeTest extends TestCase {

    @Override
    public void setUp() {

    }

    public void testNewQualifiedNameType() {
        QualifiedNameType qNameType = QualifiedNameType.newQualifedNameType("base/motor/Motorpolicy."
                + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension());
        assertEquals("base.motor.Motorpolicy", qNameType.getName());
        assertEquals(IpsObjectType.POLICY_CMPT_TYPE, qNameType.getIpsObjectType());

        try {
            QualifiedNameType.newQualifedNameType("Motorpolicy");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            QualifiedNameType.newQualifedNameType("Motorpolicy.");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            QualifiedNameType.newQualifedNameType("Motorpolicy.invalidextension");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSerialize() throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        QualifiedNameType qnt = new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        oos.writeObject(qnt);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        QualifiedNameType qnt2 = (QualifiedNameType)ois.readObject();
        assertEquals(qnt, qnt2);
    }

    public void testGetPackageName() {
        QualifiedNameType qnt = new QualifiedNameType("Policy", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals("", qnt.getPackageName());

        qnt = new QualifiedNameType("motor.Policy", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals("motor", qnt.getPackageName());
    }

    public void testGetUnqualifiedName() {
        QualifiedNameType qnt = new QualifiedNameType("Policy", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals("Policy", qnt.getUnqualifiedName());

        qnt = new QualifiedNameType("motor.Policy", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals("Policy", qnt.getUnqualifiedName());

        qnt = new QualifiedNameType("motor.", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals("", qnt.getUnqualifiedName());

    }

    public void testToPath() {
        QualifiedNameType qnt = new QualifiedNameType("Policy", IpsObjectType.POLICY_CMPT_TYPE);
        Path expectedPath = new Path("Policy." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension());
        assertEquals(expectedPath, qnt.toPath());

        qnt = new QualifiedNameType("mycompany.motor.Policy", IpsObjectType.POLICY_CMPT_TYPE);
        expectedPath = new Path("mycompany/motor/Policy." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension());
        assertEquals(expectedPath, qnt.toPath());
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
     * Test method for 'org.faktorips.plugin.model.QualifiedNameType.QualifiedNameType(String,
     * IpsObjectType)'
     */
    public void testQualifiedNameType() {
        new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        try {
            new QualifiedNameType(null, IpsObjectType.POLICY_CMPT_TYPE);
            fail("Exception because of null argument expected");
        } catch (Exception e) {
        }
        try {
            new QualifiedNameType("test", null);
            fail("Exception because of null argument expected");
        } catch (Exception e) {
        }
    }

    public void testGetFilename() {
        QualifiedNameType qnt = new QualifiedNameType("test.Motorpolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals("Motorpolicy." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension(), qnt.getFileName());
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
        assertEquals(type1.toString(), "PolicyCmptType: test");
    }

}
