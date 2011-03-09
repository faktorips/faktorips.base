/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.pctype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.type.TypeHierarchy;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ITypeHierarchy;
import org.junit.Before;
import org.junit.Test;

public class TypeHierarchyTestProductCmptType extends AbstractIpsPluginTest {
    private IIpsProject pdProject;
    private IIpsPackageFragmentRoot pdRootFolder;
    private IIpsPackageFragment pdFolder;
    private IIpsSrcFile pdSrcFile;
    private ProductCmptType pcType;
    private IProductCmptType supertype;
    private IProductCmptType supersupertype;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        pdProject = this.newIpsProject("TestProject");
        pdRootFolder = pdProject.getIpsPackageFragmentRoots()[0];
        pdFolder = pdRootFolder.createPackageFragment("products.folder", true, null);
        pdSrcFile = pdFolder.createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE, "TestProduct", true, null);
        pcType = (ProductCmptType)pdSrcFile.getIpsObject();

        // create two more types that act as supertype and supertype's supertype
        IIpsSrcFile file1 = pdFolder.createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE, "Supertype", true, null);
        supertype = (ProductCmptType)file1.getIpsObject();
        IIpsSrcFile file2 = pdFolder.createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE, "Supersupertype", true, null);
        supersupertype = (ProductCmptType)file2.getIpsObject();
    }

    @Test
    public void testGetSubtypeHierarchy() throws Exception {
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        ITypeHierarchy hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);
        assertFalse(hierarchy.containsCycle());

        assertNull(hierarchy.getSupertype(supersupertype));
        assertEquals(1, hierarchy.getSubtypes(supersupertype).size());
        assertEquals(supertype, hierarchy.getSubtypes(supersupertype).get(0));

        assertEquals(supersupertype, hierarchy.getSupertype(supertype));
        assertEquals(1, hierarchy.getSubtypes(supertype).size());
        assertEquals(pcType, hierarchy.getSubtypes(supertype).get(0));

        assertEquals(supertype, hierarchy.getSupertype(pcType));
        assertEquals(0, hierarchy.getSubtypes(pcType).size());

        // test if it works if the subtypes are in different projects
        IIpsProject project2 = newIpsProject("Project2");
        IProductCmptType newSubType = newProductCmptType(project2, "NewSubType");
        newSubType.setSupertype(pcType.getQualifiedName());

        IIpsProject project3 = newIpsProject("Project3");
        IProductCmptType newSubSubType = newProductCmptType(project3, "NewSubSubType");
        newSubSubType.setSupertype(newSubType.getQualifiedName());

        // no project dependencies => nothing could be found
        hierarchy = TypeHierarchy.getSubtypeHierarchy(pcType);
        assertEquals(0, hierarchy.getSubtypes(pcType).size());

        // setup project dependencies
        IIpsObjectPath path = project2.getIpsObjectPath();
        path.newIpsProjectRefEntry(pdProject);
        project2.setIpsObjectPath(path);
        path = project3.getIpsObjectPath();
        path.newIpsProjectRefEntry(project2);
        project3.setIpsObjectPath(path);

        hierarchy = TypeHierarchy.getSubtypeHierarchy(pcType);
        assertEquals(1, hierarchy.getSubtypes(pcType).size());
        assertEquals(newSubType, hierarchy.getSubtypes(pcType).get(0));
        assertEquals(1, hierarchy.getSubtypes(newSubType).size());
        assertEquals(newSubSubType, hierarchy.getSubtypes(newSubType).get(0));
    }

    @Test
    public void testIsSubtypeOf_SupertypeHierarchy() throws Exception {
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertTrue(hierarchy.isSubtypeOf(supertype, supersupertype));
        assertTrue(hierarchy.isSubtypeOf(pcType, supersupertype));
        assertFalse(hierarchy.isSubtypeOf(supertype, pcType));
        assertFalse(hierarchy.isSubtypeOf(supertype, null));
        assertFalse(hierarchy.isSubtypeOf(null, supertype));

        // now same with cycle in hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertTrue(hierarchy.isSubtypeOf(supertype, supersupertype));
        assertTrue(hierarchy.isSubtypeOf(pcType, supersupertype));
        assertFalse(hierarchy.isSubtypeOf(supertype, pcType));
        assertFalse(hierarchy.isSubtypeOf(supertype, null));
        assertFalse(hierarchy.isSubtypeOf(null, supertype));
    }

    @Test
    public void testIsSubtypeOf_SubtypeHierarchy() throws Exception {
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        ITypeHierarchy hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);
        assertTrue(hierarchy.isSubtypeOf(supertype, supersupertype));
        assertTrue(hierarchy.isSubtypeOf(pcType, supersupertype));
        assertFalse(hierarchy.isSubtypeOf(supertype, pcType));
        assertFalse(hierarchy.isSubtypeOf(supertype, null));
        assertFalse(hierarchy.isSubtypeOf(null, supertype));

        // now same with cycle in hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);
        assertTrue(hierarchy.isSubtypeOf(supertype, supersupertype));
        assertTrue(hierarchy.isSubtypeOf(pcType, supersupertype));
        assertFalse(hierarchy.isSubtypeOf(supertype, pcType));
        assertFalse(hierarchy.isSubtypeOf(supertype, null));
        assertFalse(hierarchy.isSubtypeOf(null, supertype));
    }

    @Test
    public void testGetSupertype() throws Exception {
        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertNull(hierarchy.getSupertype(pcType)); // supertype relationship hasn't been
        // established
        assertNull(hierarchy.getSupertype(supertype));
        assertEquals(0, hierarchy.getSubtypes(pcType).size());

        // create the supertype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(supertype, hierarchy.getSupertype(pcType));
        assertEquals(supersupertype, hierarchy.getSupertype(supertype));
        assertNull(hierarchy.getSupertype(supersupertype));
        assertEquals(0, hierarchy.getSubtypes(pcType).size());

        // now same with cycle in hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(supertype, hierarchy.getSupertype(pcType));
        assertEquals(supersupertype, hierarchy.getSupertype(supertype));
        assertNull(hierarchy.getSupertype(supersupertype));
        assertEquals(0, hierarchy.getSubtypes(pcType).size());
    }

    @Test
    public void testGetAllSupertypes() throws Exception {
        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(0, hierarchy.getAllSupertypes(pcType).size()); // supertype relationship hasn't
        // been established

        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        List<IType> supertypes = hierarchy.getAllSupertypes(pcType);
        assertEquals(2, supertypes.size());
        assertEquals(supertype, supertypes.get(0));
        assertEquals(supersupertype, supertypes.get(1));

        // now same with cycle in hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        supertypes = hierarchy.getAllSupertypes(pcType);
        assertEquals(2, supertypes.size());
        assertEquals(supertype, supertypes.get(0));
        assertEquals(supersupertype, supertypes.get(1));
    }

    @Test
    public void testGetAllSupertypesInclSelf() throws Exception {
        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(1, hierarchy.getAllSupertypesInclSelf(pcType).size()); // supertype
        // relationship hasn't
        // been established
        assertEquals(pcType, hierarchy.getAllSupertypesInclSelf(pcType).get(0));

        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        List<IType> supertypes = hierarchy.getAllSupertypesInclSelf(pcType);
        assertEquals(3, supertypes.size());
        assertEquals(pcType, supertypes.get(0));
        assertEquals(supertype, supertypes.get(1));
        assertEquals(supersupertype, supertypes.get(2));

        // now same with cycle in hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        supertypes = hierarchy.getAllSupertypesInclSelf(pcType);
        assertEquals(3, supertypes.size());
        assertEquals(pcType, supertypes.get(0));
        assertEquals(supertype, supertypes.get(1));
        assertEquals(supersupertype, supertypes.get(2));
    }

    @Test
    public void testGetSubtypes() throws Exception {
        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(0, hierarchy.getSubtypes(pcType).size());
    }

    @Test
    public void testIsSupertypeOf() throws Exception {
        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        // supertype relationship hasn't been established
        assertFalse(hierarchy.isSupertypeOf(supertype, pcType));

        // create the supertype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertTrue(hierarchy.isSupertypeOf(supertype, pcType));
        assertTrue(hierarchy.isSupertypeOf(supersupertype, pcType));

        assertFalse(hierarchy.isSupertypeOf(supertype, null));
        assertFalse(hierarchy.isSupertypeOf(null, pcType));

        // now same with cycle in hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertTrue(hierarchy.isSupertypeOf(supertype, pcType));
        assertTrue(hierarchy.isSupertypeOf(supersupertype, pcType));

        assertFalse(hierarchy.isSupertypeOf(supertype, null));
        assertFalse(hierarchy.isSupertypeOf(null, pcType));

    }

    @Test
    public void testGetAllAttributes() throws Exception {
        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        IProductCmptTypeAttribute a1 = pcType.newProductCmptTypeAttribute();
        IProductCmptTypeAttribute a2 = supertype.newProductCmptTypeAttribute();
        IProductCmptTypeAttribute a3 = supersupertype.newProductCmptTypeAttribute();
        TypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        List<IAttribute> attributes = hierarchy.getAllAttributes(pcType);
        assertEquals(3, attributes.size());
        assertEquals(a1, attributes.get(0));
        assertEquals(a2, attributes.get(1));
        assertEquals(a3, attributes.get(2));

        // now same with cycle in hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        attributes = hierarchy.getAllAttributes(pcType);
        assertEquals(3, attributes.size());
        assertEquals(a1, attributes.get(0));
        assertEquals(a2, attributes.get(1));
        assertEquals(a3, attributes.get(2));

    }

    @Test
    public void testGetAllMethods() throws Exception {
        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        IMethod m1 = pcType.newMethod();
        IMethod m2 = supertype.newMethod();
        IMethod m3 = supersupertype.newMethod();
        TypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        List<IMethod> methods = hierarchy.getAllMethods(pcType);
        assertEquals(3, methods.size());
        assertEquals(m1, methods.get(0));
        assertEquals(m2, methods.get(1));
        assertEquals(m3, methods.get(2));

        // now same with cycle in hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        methods = hierarchy.getAllMethods(pcType);
        assertEquals(3, methods.size());
        assertEquals(m1, methods.get(0));
        assertEquals(m2, methods.get(1));
        assertEquals(m3, methods.get(2));

    }

    @Test
    public void testFindAttribute() throws Exception {
        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        IProductCmptTypeAttribute a1 = pcType.newProductCmptTypeAttribute();
        a1.setName("a1");
        IProductCmptTypeAttribute a2 = supersupertype.newProductCmptTypeAttribute();
        a2.setName("a2");

        TypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(a1, hierarchy.findAttribute(pcType, "a1"));
        assertEquals(a2, hierarchy.findAttribute(pcType, "a2"));
        assertEquals(a2, hierarchy.findAttribute(supertype, "a2"));
        assertNull(hierarchy.findAttribute(pcType, "unkown"));

        // now same with cycle in hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(a1, hierarchy.findAttribute(pcType, "a1"));
        assertEquals(a2, hierarchy.findAttribute(pcType, "a2"));
        assertEquals(a2, hierarchy.findAttribute(supertype, "a2"));
        assertNull(hierarchy.findAttribute(pcType, "unkown"));

    }

    @Test
    public void testCycleDetection() throws Exception {
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertFalse(hierarchy.containsCycle());

        hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);
        assertFalse(hierarchy.containsCycle());

        supersupertype.setSupertype(pcType.getQualifiedName());

        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertTrue(hierarchy.containsCycle());

        hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);
        assertTrue(hierarchy.containsCycle());
    }

    @Test
    public void testGetAllSubtypes() throws Exception {
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        TypeHierarchy hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);

        List<IType> types = hierarchy.getAllSubtypes(supersupertype);
        assertEquals(2, types.size());
        assertSame(supertype, types.get(0));
        assertSame(pcType, types.get(1));

        types = hierarchy.getAllSubtypes(supertype);
        assertEquals(1, types.size());
        assertSame(pcType, types.get(0));

        types = hierarchy.getAllSubtypes(pcType);
        assertEquals(0, types.size());

        // now same with cycle in hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);
        assertTrue(hierarchy.containsCycle());
        types = hierarchy.getAllSubtypes(supersupertype);
        assertEquals(2, types.size());
        assertSame(supertype, types.get(0));
        assertSame(pcType, types.get(1));
    }

    @Test
    public void testIsNodeOfHierarchy() throws CoreException {
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        IIpsSrcFile file3 = pdFolder.createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE, "Intependent", true, null);
        IProductCmptType intependent = (ProductCmptType)file3.getIpsObject();
        ITypeHierarchy hierarchy = TypeHierarchy.getTypeHierarchy(pcType);

        assertTrue(hierarchy.isPartOfHierarchy(pcType.getSupertype()));
        assertFalse(hierarchy.isPartOfHierarchy(intependent.getQualifiedName()));
        assertTrue(hierarchy.isPartOfHierarchy(supersupertype.getQualifiedName()));
    }
}
