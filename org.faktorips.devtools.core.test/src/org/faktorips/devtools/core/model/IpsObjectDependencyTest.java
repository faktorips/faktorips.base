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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

public class IpsObjectDependencyTest extends AbstractIpsPluginTest {

    public final void testEqualsObject() throws CoreException {

        IpsObjectDependency dependency1 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, null, new QualifiedNameType("a.b.B",
                IpsObjectType.POLICY_CMPT_TYPE));
        IpsObjectDependency dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, null, new QualifiedNameType("a.b.B",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertEquals(dependency1, dependency2);

        dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, null, new QualifiedNameType("a.b.C",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.equals(dependency2));

        dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, "egon", new QualifiedNameType("a.b.C",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.equals(dependency2));

        IIpsProject prj = newIpsProject("testprj");
        IPolicyCmptType type = newPolicyCmptType(prj, "A");

        dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), type, null, new QualifiedNameType("a.b.C",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.equals(dependency2));

        dependency2 = IpsObjectDependency.createSubtypeDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, null, new QualifiedNameType("a.b.B",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.equals(dependency2));

        assertFalse(dependency1.equals(null));

        dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), type, "egon", new QualifiedNameType("a.b.C",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.equals(dependency2));

        dependency1 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), type, "egon", new QualifiedNameType("a.b.C",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertEquals(dependency1, dependency2);

    }

    public void testHashCode() throws CoreException {
        IpsObjectDependency dependency1 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, null, new QualifiedNameType("a.b.B",
                IpsObjectType.POLICY_CMPT_TYPE));
        IpsObjectDependency dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, null, new QualifiedNameType("a.b.B",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertEquals(dependency1.hashCode(), dependency2.hashCode());

        dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, null, new QualifiedNameType("a.b.C",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.hashCode() == dependency2.hashCode());

        dependency2 = IpsObjectDependency.createSubtypeDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, null, new QualifiedNameType("a.b.B",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.equals(dependency2));

        dependency1 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, "egon", new QualifiedNameType("a.b.B",
                IpsObjectType.POLICY_CMPT_TYPE));
        dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, "egon", new QualifiedNameType("a.b.B",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertEquals(dependency1.hashCode(), dependency2.hashCode());

        IIpsProject prj = newIpsProject("testprj");
        IPolicyCmptType type = newPolicyCmptType(prj, "A");
        dependency1 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), type, "egon", new QualifiedNameType("a.b.B",
                IpsObjectType.POLICY_CMPT_TYPE));
        dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), type, "egon", new QualifiedNameType("a.b.B",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertEquals(dependency1.hashCode(), dependency2.hashCode());

        dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, null, new QualifiedNameType("a.b.C",
                IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.hashCode() == dependency2.hashCode());
    }

    public void testSerializable() throws Exception {
        IpsObjectDependency dependency = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), null, null, new QualifiedNameType("a.b.B",
                IpsObjectType.POLICY_CMPT_TYPE));

        ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(dependency);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        IpsObjectDependency dependency2 = (IpsObjectDependency)ois.readObject();
        assertEquals(dependency, dependency2);
    }
}
