/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.DependencyType;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ReferencesToIpsObjectSearchQueryTest {

    @Mock
    private IIpsProject proj;

    @Mock
    private IIpsProject proj2;

    @Mock
    private IIpsProject proj3;

    @Mock
    private IIpsObject objectReferenced;

    @Mock
    private IIpsObject objectReferenced2;

    @Mock
    private IProductCmpt objectReferencedProductCmpt;

    @Mock
    private IIpsSrcFile srcFileReferenced;

    @Mock
    private IIpsSrcFile srcFile1;

    @Mock
    private IIpsSrcFile srcFileReferenced2;

    @Mock
    private IIpsSrcFile srcFileReferencedProdCmpt;

    @Mock
    private IIpsObject object1;

    @Mock
    private IProductCmpt prodCmpt;

    @Mock
    private QualifiedNameType prodCmptQualifiedNameType;

    @Mock
    private QualifiedNameType object1QualifiedNameType;

    @Mock
    private QualifiedNameType objectReferencedQualifiedNameType;

    @Mock
    private QualifiedNameType objectReferenced2QualifiedNameType;

    @Mock
    private QualifiedNameType objRefProdCmptQualifiedNameType;

    @Mock
    private IProductCmptGeneration prodCmptGeneration1;

    @Mock
    private IProductCmptGeneration prodCmptGeneration2;

    @Mock
    private IIpsObjectPartContainer objectContainer1;

    @Mock
    private IIpsObjectPartContainer objectContainer2;

    @Mock
    private IDependencyDetail detail1;

    @Mock
    private IDependencyDetail detail2;

    @Before
    public void initSrcFilesSetUp() {
        MockitoAnnotations.initMocks(this);

        when(prodCmpt.getIpsProject()).thenReturn(proj);
        when(prodCmpt.getQualifiedNameType()).thenReturn(prodCmptQualifiedNameType);

        when(object1.getIpsProject()).thenReturn(proj);
        when(objectReferenced.getIpsProject()).thenReturn(proj);
        when(objectReferencedProductCmpt.getIpsProject()).thenReturn(proj);

        when(object1.getQualifiedNameType()).thenReturn(object1QualifiedNameType);
        when(objectReferenced.getQualifiedNameType()).thenReturn(objectReferencedQualifiedNameType);
        when(objectReferenced2.getQualifiedNameType()).thenReturn(objectReferenced2QualifiedNameType);
        when(objectReferencedProductCmpt.getQualifiedNameType()).thenReturn(objRefProdCmptQualifiedNameType);

        when(srcFile1.getIpsObject()).thenReturn(object1);
        when(srcFileReferenced.getIpsObject()).thenReturn(objectReferenced);
        when(srcFileReferenced2.getIpsObject()).thenReturn(objectReferenced2);
        when(srcFileReferencedProdCmpt.getIpsObject()).thenReturn(objectReferencedProductCmpt);

        when(prodCmptGeneration1.getIpsObject()).thenReturn(object1);
        when(prodCmptGeneration2.getIpsObject()).thenReturn(object1);

    }

    private void initProjectSetUp() {
        List<IIpsSrcFile> result = new ArrayList<>();
        result.add(srcFile1);
        result.add(srcFileReferenced);
        result.add(srcFileReferenced2);
        when(proj.findAllIpsSrcFiles()).thenReturn(result);
    }

    private void initDependencySetUp() {
        IDependency dependencyObj1ToObjRef = IpsObjectDependency.create(object1.getQualifiedNameType(),
                objectReferenced.getQualifiedNameType(), DependencyType.REFERENCE);
        IDependency dependencyObj1ToObjRef2 = IpsObjectDependency.create(object1.getQualifiedNameType(),
                objectReferenced2.getQualifiedNameType(), DependencyType.REFERENCE);
        when(object1.dependsOn()).thenReturn(new IDependency[] { dependencyObj1ToObjRef, dependencyObj1ToObjRef2 });
        IDependency dependencyObjRefToObj1 = IpsObjectDependency.create(objectReferenced.getQualifiedNameType(),
                object1.getQualifiedNameType(), DependencyType.REFERENCE);
        IDependency dependencyObjRefToObjRef2 = IpsObjectDependency.create(objectReferenced.getQualifiedNameType(),
                objectReferenced2.getQualifiedNameType(), DependencyType.REFERENCE);
        when(objectReferenced.dependsOn())
                .thenReturn(new IDependency[] { dependencyObjRefToObj1, dependencyObjRefToObjRef2 });
        when(objectReferenced2.dependsOn()).thenReturn(new IDependency[] {});
    }

    @Test
    public void testFindReferences() throws CoreRuntimeException {
        IIpsProject[] projects = new IIpsProject[] { proj, proj2, proj3 };
        when(proj.findReferencingProjectLeavesOrSelf()).thenReturn(projects);

        ReferencesToIpsObjectSearchQuery querySpy = spy(new ReferencesToIpsObjectSearchQuery(object1));
        querySpy.findReferences();

        verify(querySpy).findReferencingIpsObjTypes(proj);
        verify(querySpy).findReferencingIpsObjTypes(proj2);
        verify(querySpy).findReferencingIpsObjTypes(proj3);
        verify(querySpy).findReferences();
    }

    @Test
    public void testFindReferencingIpsObjTypes_NoSrcFile() throws CoreRuntimeException {
        when(proj.findAllIpsSrcFiles()).thenReturn(new ArrayList<IIpsSrcFile>());

        ReferencesToIpsObjectSearchQuery query = new ReferencesToIpsObjectSearchQuery(object1);
        Set<IIpsElement> resultFindReferencingIpsObjTypes = query.findReferencingIpsObjTypes(proj);

        assertEquals(0, resultFindReferencingIpsObjTypes.size());
    }

    @Test
    public void testFindReferencingIpsObjTypes_SrcFileWithoutDependenciesAndResultingReferences() throws CoreRuntimeException {
        initProjectSetUp();
        when(srcFile1.getIpsObject().dependsOn()).thenReturn(new IDependency[] {});
        when(srcFileReferenced.getIpsObject().dependsOn()).thenReturn(new IDependency[] {});
        when(srcFileReferenced2.getIpsObject().dependsOn()).thenReturn(new IDependency[] {});

        ReferencesToIpsObjectSearchQuery query = new ReferencesToIpsObjectSearchQuery(object1);
        Set<IIpsElement> resultFindReferencingIpsObjTypes = query.findReferencingIpsObjTypes(proj);

        assertEquals(0, resultFindReferencingIpsObjTypes.size());
    }

    @Test
    public void testFindReferencingIpsObjTypes_SrcFilesWithDependenciesAndResultingReference() throws CoreRuntimeException {
        initProjectSetUp();
        initDependencySetUp();

        ReferencesToIpsObjectSearchQuery query = new ReferencesToIpsObjectSearchQuery(object1);
        Set<IIpsElement> resultFindReferencingIpsObjTypes = query.findReferencingIpsObjTypes(proj);

        assertEquals(1, resultFindReferencingIpsObjTypes.size());
        assertFalse(resultFindReferencingIpsObjTypes.contains(objectReferenced2));
    }

    @Test
    public void testFindReferencingIpsObjTypes_SrcFilesWithNoDependenciesButResultingReferences() throws CoreRuntimeException {
        initProjectSetUp();
        initDependencySetUp();

        ReferencesToIpsObjectSearchQuery query = new ReferencesToIpsObjectSearchQuery(objectReferenced2);
        Set<IIpsElement> resultFindReferencingIpsObjTypes = query.findReferencingIpsObjTypes(proj);

        assertEquals(2, resultFindReferencingIpsObjTypes.size());
        assertTrue(resultFindReferencingIpsObjTypes.contains(objectReferenced));
        assertTrue(resultFindReferencingIpsObjTypes.contains(object1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFindReferencingIpsObjTypes_OtherMethods() throws CoreRuntimeException {
        List<IIpsSrcFile> ipsScrFiles = new ArrayList<>();
        ipsScrFiles.add(srcFile1);
        ipsScrFiles.add(srcFileReferencedProdCmpt);
        when(proj.findAllIpsSrcFiles()).thenReturn(ipsScrFiles);
        IDependency dependencyObj1ToObjRefProdCmp = IpsObjectDependency.create(object1.getQualifiedNameType(),
                objectReferencedProductCmpt.getQualifiedNameType(), DependencyType.REFERENCE);
        when(object1.dependsOn()).thenReturn(new IDependency[] { dependencyObj1ToObjRefProdCmp });
        IDependency dependencyRefProdCmptToObj1 = IpsObjectDependency.create(
                objectReferencedProductCmpt.getQualifiedNameType(), object1.getQualifiedNameType(),
                DependencyType.REFERENCE);
        when(objectReferencedProductCmpt.dependsOn()).thenReturn(new IDependency[] { dependencyRefProdCmptToObj1 });
        when(object1.getQualifiedNameType()).thenReturn((QualifiedNameType)dependencyRefProdCmptToObj1.getTarget());

        ReferencesToIpsObjectSearchQuery querySpy = spy(new ReferencesToIpsObjectSearchQuery(object1));
        querySpy.findReferencingIpsObjTypes(proj);

        verify(querySpy).findReferencingIpsObjTypes(proj);
        verify(querySpy).checkIIPsSrcFileDependencies(anySet(), eq(ipsScrFiles));
        verify(querySpy).addDependencyDetails(anySet(), eq(objectReferencedProductCmpt),
                eq(dependencyRefProdCmptToObj1));
        verifyNoMoreInteractions(querySpy);
    }

    @Test
    public void testAddProdCmpGenerations_NoGenerations() throws CoreRuntimeException {
        initProjectSetUp();

        ReferencesToIpsObjectSearchQuery query = new ReferencesToIpsObjectSearchQuery(objectReferenced);
        Set<IIpsElement> resultList = new HashSet<>();
        query.addDependencyDetails(resultList, prodCmpt, null);

        assertTrue(resultList.isEmpty());
    }

    @Test
    public void testAddProdCmpGenerations_OneOrMoreGenerations() throws CoreRuntimeException {
        IDependency dependencyObj1ToObjRef = IpsObjectDependency.create(prodCmpt.getQualifiedNameType(),
                objectReferenced.getQualifiedNameType(), DependencyType.REFERENCE);
        when(prodCmpt.dependsOn()).thenReturn(new IDependency[] { dependencyObj1ToObjRef });
        List<IDependencyDetail> obj1ProdCmptGenerations = new ArrayList<>();
        obj1ProdCmptGenerations.add(detail1);
        obj1ProdCmptGenerations.add(detail2);
        when(prodCmpt.getDependencyDetails(dependencyObj1ToObjRef)).thenReturn(obj1ProdCmptGenerations);
        when(detail1.getPart()).thenReturn(objectContainer1);
        when(detail2.getPart()).thenReturn(objectContainer2);

        ReferencesToIpsObjectSearchQuery query = new ReferencesToIpsObjectSearchQuery(objectReferenced);
        Set<IIpsElement> resultList = new HashSet<>();
        query.addDependencyDetails(resultList, prodCmpt, dependencyObj1ToObjRef);

        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(objectContainer1));
        assertTrue(resultList.contains(objectContainer2));
    }
}
