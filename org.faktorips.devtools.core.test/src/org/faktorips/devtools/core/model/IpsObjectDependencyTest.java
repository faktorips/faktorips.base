/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.junit.Test;

public class IpsObjectDependencyTest extends AbstractIpsPluginTest {
    @Test
    public void testEqualsObject() {

        IpsObjectDependency dependency1 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));
        IpsObjectDependency dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));
        assertEquals(dependency1, dependency2);

        dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.C", IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.equals(dependency2));

        dependency2 = IpsObjectDependency.createSubtypeDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.equals(dependency2));

        assertFalse(dependency1.equals(null));
    }

    @Test
    public void testHashCode() {
        IpsObjectDependency dependency1 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));
        IpsObjectDependency dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));
        assertEquals(dependency1.hashCode(), dependency2.hashCode());

        dependency2 = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.C", IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.hashCode() == dependency2.hashCode());

        dependency2 = IpsObjectDependency.createSubtypeDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.equals(dependency2));
    }

    @Test
    public void testSerializable() throws Exception {
        IpsObjectDependency dependency = IpsObjectDependency.createReferenceDependency(new QualifiedNameType("a.b.A",
                IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));

        ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(dependency);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        IpsObjectDependency dependency2 = (IpsObjectDependency)ois.readObject();
        assertEquals(dependency, dependency2);
    }
}
