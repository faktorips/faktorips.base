/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;

import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.junit.Test;

public class QualifiedNameTypeTest {

    @Test
    public void testNewQualifiedNameType() {
        QualifiedNameType qNameType = QualifiedNameType.newQualifedNameType("base/motor/Motorpolicy."
                + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension());
        assertEquals("base.motor.Motorpolicy", qNameType.getName());
        assertEquals(IpsObjectType.POLICY_CMPT_TYPE, qNameType.getIpsObjectType());

        try {
            QualifiedNameType.newQualifedNameType("Motorpolicy");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            QualifiedNameType.newQualifedNameType("Motorpolicy.");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            QualifiedNameType.newQualifedNameType("Motorpolicy.invalidextension");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
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

    @Test
    public void testGetPackageName() {
        QualifiedNameType qnt = new QualifiedNameType("Policy", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals("", qnt.getPackageName());

        qnt = new QualifiedNameType("motor.Policy", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals("motor", qnt.getPackageName());
    }

    @Test
    public void testGetUnqualifiedName() {
        QualifiedNameType qnt = new QualifiedNameType("Policy", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals("Policy", qnt.getUnqualifiedName());

        qnt = new QualifiedNameType("motor.Policy", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals("Policy", qnt.getUnqualifiedName());

        qnt = new QualifiedNameType("motor.", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals("", qnt.getUnqualifiedName());

    }

    @Test
    public void testToPath() {
        QualifiedNameType qnt = new QualifiedNameType("Policy", IpsObjectType.POLICY_CMPT_TYPE);
        Path expectedPath = Path.of("Policy." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension());
        assertEquals(expectedPath, qnt.toPath());

        qnt = new QualifiedNameType("mycompany.motor.Policy", IpsObjectType.POLICY_CMPT_TYPE);
        expectedPath = Path.of("mycompany/motor/Policy." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension());
        assertEquals(expectedPath, qnt.toPath());
    }

    @Test
    public void testHashCode() {
        QualifiedNameType type1 = new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        QualifiedNameType type2 = new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals(type1.hashCode(), type2.hashCode());

        QualifiedNameType type3 = new QualifiedNameType("test", IpsObjectType.TABLE_STRUCTURE);
        assertFalse(type1.hashCode() == type3.hashCode());
    }

    @Test
    public void testQualifiedNameType() {
        new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        try {
            new QualifiedNameType(null, IpsObjectType.POLICY_CMPT_TYPE);
            fail("Exception because of null argument expected");
        } catch (Exception e) {
            // expected
        }
        try {
            new QualifiedNameType("test", null);
            fail("Exception because of null argument expected");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testGetFilename() {
        QualifiedNameType qnt = new QualifiedNameType("test.Motorpolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals("Motorpolicy." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension(), qnt.getFileName());
    }

    @Test
    public void testEqualsObject() {
        QualifiedNameType type1 = new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        QualifiedNameType type2 = new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals(type1, type2);

        QualifiedNameType type3 = new QualifiedNameType("test", IpsObjectType.TABLE_STRUCTURE);
        assertFalse(type1.equals(type3));

        QualifiedNameType type4 = new QualifiedNameType("test1", IpsObjectType.POLICY_CMPT_TYPE);
        assertFalse(type1.equals(type4));
    }

    @Test
    public void testToString() {
        QualifiedNameType type1 = new QualifiedNameType("test", IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals(type1.toString(), IpsObjectType.POLICY_CMPT_TYPE.getDisplayName() + ": test");
    }

}
