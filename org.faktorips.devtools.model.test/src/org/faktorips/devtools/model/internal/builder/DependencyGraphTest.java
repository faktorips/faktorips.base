/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.builder.IDependencyGraph;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.dependency.DatatypeDependency;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.util.CollectionUtil;
import org.faktorips.devtools.model.value.ValueFactory;
import org.junit.Before;
import org.junit.Test;

public class DependencyGraphTest extends AbstractIpsPluginTest {

    private IIpsPackageFragmentRoot root;
    private IIpsProject ipsProject;
    private IDependencyGraph graph;
    private IPolicyCmptType a;
    private IPolicyCmptType b;
    private IPolicyCmptType c;
    private IPolicyCmptType d;
    private IPolicyCmptType e;
    private IEnumType enum1;
    private IPolicyCmptTypeAssociation cToB;
    private IPolicyCmptTypeAssociation aToD;
    private IMethod cMethod;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "A");
        b = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "B");
        c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "C");
        d = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "D");
        e = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "E");

        enum1 = newEnumType(root, "AnEnum1");
        enum1.setExtensible(false);
        enum1.newEnumLiteralNameAttribute();
        IEnumAttribute idAttr = enum1.newEnumAttribute();
        idAttr.setDatatype(Datatype.STRING.getQualifiedName());
        idAttr.setIdentifier(true);
        idAttr.setUnique(true);
        idAttr.setUsedAsNameInFaktorIpsUi(true);
        IEnumValue firstValue = enum1.newEnumValue();
        firstValue.setEnumAttributeValue(idAttr, ValueFactory.createStringValue("P1"));

        IAttribute aAttr1 = a.newAttribute();
        aAttr1.setDatatype(enum1.getQualifiedName());
        aAttr1.setName("aAttr1");

        // dependencies c->b, c->a, a->d,
        aToD = a.newPolicyCmptTypeAssociation();
        aToD.setTarget(d.getQualifiedName());
        c.setSupertype(a.getQualifiedName());
        cToB = c.newPolicyCmptTypeAssociation();
        cToB.setTarget(b.getQualifiedName());
        cMethod = c.newMethod();
        cMethod.setDatatype(e.getQualifiedName());
        cMethod.setModifier(Modifier.PUBLIC);
        cMethod.setName("cMethod");

        a.getIpsSrcFile().save(null);
        c.getIpsSrcFile().save(null);
        graph = new DependencyGraph(ipsProject);
    }

    @Test
    public void testGetDependants2() {
        // c has a datatype dependency to e and a reference dependency to e
        IAssociation aToE = a.newAssociation();
        aToE.setAssociationType(AssociationType.ASSOCIATION);
        aToE.setTarget(e.getQualifiedName());
        graph = new DependencyGraph(ipsProject);

        IDependency[] dependants = graph.getDependants(e.getQualifiedNameType());
        List<IDependency> dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(new DatatypeDependency(c.getQualifiedNameType(), e.getQualifiedName())));
        assertTrue(dependsOnList.contains(IpsObjectDependency.createReferenceDependency(a.getQualifiedNameType(),
                e.getQualifiedNameType())));
        assertEquals(2, dependants.length);
    }

    @Test
    public void testDatatypeDependencyOfEnumType() {
        IDependency[] dependants = graph.getDependants(enum1.getQualifiedNameType());
        assertEquals(1, dependants.length);
        assertEquals(a.getQualifiedNameType(), dependants[0].getSource());
    }

    @Test
    public void testGetDependants() {
        IDependency[] dependants = graph.getDependants(a.getQualifiedNameType());
        List<IDependency> dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(IpsObjectDependency.createSubtypeDependency(c.getQualifiedNameType(),
                a.getQualifiedNameType())));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(b.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(IpsObjectDependency.createReferenceDependency(c.getQualifiedNameType(),
                b.getQualifiedNameType())));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(c.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertEquals(0, dependants.length);

        dependants = graph.getDependants(d.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(IpsObjectDependency.createReferenceDependency(a.getQualifiedNameType(),
                d.getQualifiedNameType())));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(e.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(new DatatypeDependency(c.getQualifiedNameType(), e.getQualifiedName())));
        assertEquals(1, dependants.length);
    }

    @Test
    public void testUpdate() throws Exception {
        a.getPolicyCmptTypeAssociations().get(0).delete();
        a.getIpsSrcFile().save(null);

        IDependency[] dependants = graph.getDependants(a.getQualifiedNameType());
        // not only the changed IpsObject has to be updated in the dependency graph but also all
        // dependants of it
        graph.update(a.getQualifiedNameType());
        for (IDependency dependant : dependants) {
            graph.update(((IpsObjectDependency)dependant).getTargetAsQNameType());
        }

        List<IDependency> dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(IpsObjectDependency.createSubtypeDependency(c.getQualifiedNameType(),
                a.getQualifiedNameType())));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(b.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(IpsObjectDependency.createReferenceDependency(c.getQualifiedNameType(),
                b.getQualifiedNameType())));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(c.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertEquals(0, dependants.length);

        dependants = graph.getDependants(d.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertEquals(0, dependants.length);
    }

}
