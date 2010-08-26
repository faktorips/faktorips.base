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

package org.faktorips.devtools.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;

public class DatatypeDependencyTest extends TestCase {

    private QualifiedNameType source;
    private DatatypeDependency dependency;

    @Override
    public void setUp() {
        source = new QualifiedNameType("a.b.c", IpsObjectType.POLICY_CMPT_TYPE);
        dependency = new DatatypeDependency(source, "a.b.e");
    }

    public final void testHashCode() {
        QualifiedNameType source = new QualifiedNameType("a.b.c", IpsObjectType.POLICY_CMPT_TYPE);
        DatatypeDependency dependency = new DatatypeDependency(source, "a.b.e");

        QualifiedNameType source2 = new QualifiedNameType("a.b.c", IpsObjectType.POLICY_CMPT_TYPE);
        DatatypeDependency dependency2 = new DatatypeDependency(source2, "a.b.e");
        assertEquals(dependency.hashCode(), dependency2.hashCode());
    }

    public final void testGetSource() {
        assertEquals(source, dependency.getSource());
    }

    public final void testGetTargetAsQualifiedName() {
        assertEquals("a.b.e", dependency.getTargetAsQualifiedName());
    }

    public final void testGetTarget() {
        assertEquals("a.b.e", dependency.getTarget());
    }

    public final void testGetType() {
        assertEquals(DependencyType.DATATYPE, dependency.getType());
    }

    public final void testEqualsObject() {
        QualifiedNameType source = new QualifiedNameType("a.b.c", IpsObjectType.POLICY_CMPT_TYPE);
        DatatypeDependency dependency = new DatatypeDependency(source, "a.b.e");

        QualifiedNameType source2 = new QualifiedNameType("a.b.c", IpsObjectType.POLICY_CMPT_TYPE);
        DatatypeDependency dependency2 = new DatatypeDependency(source2, "a.b.e");
        assertEquals(dependency, dependency2);
    }

    public void testSerializable() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(dependency);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        DatatypeDependency dependency = (DatatypeDependency)ois.readObject();
        assertEquals(this.dependency, dependency);
    }

    public void testToString() {
        QualifiedNameType source = new QualifiedNameType("a.b.c", IpsObjectType.POLICY_CMPT_TYPE);
        DatatypeDependency dependency = new DatatypeDependency(source, "a.b.e");
        assertEquals("(PolicyCmptType: a.b.c -> a.b.e, type: datatype dependency)", dependency.toString());
    }
}
