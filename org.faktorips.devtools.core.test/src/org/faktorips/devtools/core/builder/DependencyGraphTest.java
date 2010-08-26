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

package org.faktorips.devtools.core.builder;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.DependencyGraph;
import org.faktorips.devtools.core.model.DatatypeDependency;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.util.CollectionUtil;

public class DependencyGraphTest extends AbstractIpsPluginTest {

    private IIpsPackageFragmentRoot root;
    private IIpsProject ipsProject;
    private DependencyGraph graph;
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
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        a = newPolicyCmptType(root, "A");
        b = newPolicyCmptType(root, "B");
        c = newPolicyCmptType(root, "C");
        d = newPolicyCmptType(root, "D");
        e = newPolicyCmptType(root, "E");
        a.setProductCmptType("");
        b.setProductCmptType("");
        c.setProductCmptType("");
        d.setProductCmptType("");
        e.setProductCmptType("");

        enum1 = newEnumType(root, "AnEnum1");
        enum1.setContainingValues(true);
        enum1.newEnumLiteralNameAttribute();
        IEnumAttribute idAttr = enum1.newEnumAttribute();
        idAttr.setDatatype(Datatype.STRING.getQualifiedName());
        idAttr.setIdentifier(true);
        idAttr.setUnique(true);
        idAttr.setUsedAsNameInFaktorIpsUi(true);
        IEnumValue firstValue = enum1.newEnumValue();
        firstValue.setEnumAttributeValue(idAttr, "P1");

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

        a.getIpsSrcFile().save(true, null);
        c.getIpsSrcFile().save(true, null);
        graph = new DependencyGraph(ipsProject);
    }

    public void testGetDependants2() throws CoreException {
        // c has a datatype dependency to e and a reference dependency to e
        IAssociation aToE = a.newAssociation();
        aToE.setAssociationType(AssociationType.ASSOCIATION);
        aToE.setTarget(e.getQualifiedName());
        graph = new DependencyGraph(ipsProject);

        IDependency[] dependants = graph.getDependants(e.getQualifiedNameType());
        List<IDependency> dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(new DatatypeDependency(c.getQualifiedNameType(), e.getQualifiedName())));
        assertTrue(dependsOnList.contains(IpsObjectDependency.createReferenceDependency(a.getQualifiedNameType(), e
                .getQualifiedNameType())));
        assertEquals(2, dependants.length);
    }

    public void testDatatypeDependencyOfEnumType() {
        IDependency[] dependants = graph.getDependants(enum1.getQualifiedNameType());
        assertEquals(1, dependants.length);
        assertEquals(a.getQualifiedNameType(), dependants[0].getSource());
    }

    public void testGetDependants() {
        IDependency[] dependants = graph.getDependants(a.getQualifiedNameType());
        List<IDependency> dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(IpsObjectDependency.createSubtypeDependency(c.getQualifiedNameType(), a
                .getQualifiedNameType())));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(b.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(IpsObjectDependency.createReferenceDependency(c.getQualifiedNameType(), b
                .getQualifiedNameType())));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(c.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertEquals(0, dependants.length);

        dependants = graph.getDependants(d.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(IpsObjectDependency.createReferenceDependency(a.getQualifiedNameType(), d
                .getQualifiedNameType())));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(e.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(new DatatypeDependency(c.getQualifiedNameType(), e.getQualifiedName())));
        assertEquals(1, dependants.length);
    }

    public void testUpdate() throws Exception {
        a.getPolicyCmptTypeAssociations()[0].delete();
        a.getIpsSrcFile().save(true, null);

        IDependency[] dependants = graph.getDependants(a.getQualifiedNameType());
        // not only the changed IpsObject has to be updated in the dependency graph but also all
        // dependants of it
        graph.update(a.getQualifiedNameType());
        for (IDependency dependant : dependants) {
            graph.update(((IpsObjectDependency)dependant).getTargetAsQNameType());
        }

        List<IDependency> dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(IpsObjectDependency.createSubtypeDependency(c.getQualifiedNameType(), a
                .getQualifiedNameType())));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(b.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(IpsObjectDependency.createReferenceDependency(c.getQualifiedNameType(), b
                .getQualifiedNameType())));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(c.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertEquals(0, dependants.length);

        dependants = graph.getDependants(d.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertEquals(0, dependants.length);
    }

}
