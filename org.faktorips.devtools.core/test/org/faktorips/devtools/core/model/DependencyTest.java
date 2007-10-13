/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import junit.framework.TestCase;


public class DependencyTest extends TestCase {

    public final void testEqualsObject() {
        
        Dependency dependency1 = Dependency.createReferenceDependency(new QualifiedNameType("a.b.A", IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));
        Dependency dependency2 = Dependency.createReferenceDependency(new QualifiedNameType("a.b.A", IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));
        assertEquals(dependency1, dependency2);
        
        dependency2 = Dependency.createReferenceDependency(new QualifiedNameType("a.b.A", IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.C", IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.equals(dependency2));

        dependency2 = Dependency.createSubtypeDependency(new QualifiedNameType("a.b.A", IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.equals(dependency2));
        
        assertFalse(dependency1.equals(null));
    }

    public void testHashCode(){
        Dependency dependency1 = Dependency.createReferenceDependency(new QualifiedNameType("a.b.A", IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));
        Dependency dependency2 = Dependency.createReferenceDependency(new QualifiedNameType("a.b.A", IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));
        assertEquals(dependency1.hashCode(), dependency2.hashCode());
        
        dependency2 = Dependency.createReferenceDependency(new QualifiedNameType("a.b.A", IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.C", IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.hashCode() ==  dependency2.hashCode());

        dependency2 = Dependency.createSubtypeDependency(new QualifiedNameType("a.b.A", IpsObjectType.POLICY_CMPT_TYPE), new QualifiedNameType("a.b.B", IpsObjectType.POLICY_CMPT_TYPE));
        assertFalse(dependency1.equals(dependency2));

    }
}
