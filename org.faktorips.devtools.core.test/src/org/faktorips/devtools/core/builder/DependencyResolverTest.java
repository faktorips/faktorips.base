/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.DatatypeDependency;
import org.faktorips.devtools.core.model.DependencyType;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.MultiMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DependencyResolverTest {

    @Mock
    private IIpsProject ipsProject1;

    @Mock
    private IIpsProject ipsProject2;

    @Mock
    private IIpsProject ipsProject3;

    @Mock
    private IIpsProject ipsProject4;

    @Mock
    private IDependencyGraph dependencyGraph1;

    @Mock
    private IDependencyGraph dependencyGraph2;

    @Mock
    private IDependencyGraph dependencyGraph3;

    @Mock
    private IDependencyGraph dependencyGraph4;

    @Mock
    private IIpsArtefactBuilderSet artefactBuilderSet;

    @Mock
    private IIpsElement ipsElement;

    @Mock
    private IEnumContent enumContentObject;

    @Mock
    private IEnumType enumTypeObject;

    private IIpsSrcFile ipsSrcFile1 = new IpsSrcFile(ipsElement, "ipsSrcFile1");

    private IIpsSrcFile ipsSrcFile2 = new IpsSrcFile(ipsElement, "ipsSrcFile2");

    private IIpsSrcFile ipsSrcFile3 = new IpsSrcFile(ipsElement, "ipsSrcFile3");

    private DependencyResolver dependencyResolver;

    private QualifiedNameType enumType = new QualifiedNameType("enumType", IpsObjectType.ENUM_TYPE);
    private QualifiedNameType enumContent = new QualifiedNameType("enumContent", IpsObjectType.ENUM_CONTENT);
    private IDependency depInstanceOfEnum = IpsObjectDependency.createInstanceOfDependency(enumContent, enumType);

    private QualifiedNameType superProductCmptType = new QualifiedNameType("superProductCmptType",
            IpsObjectType.PRODUCT_CMPT_TYPE);
    private QualifiedNameType subProductCmptType = new QualifiedNameType("subProductCmptType",
            IpsObjectType.PRODUCT_CMPT_TYPE);
    private IDependency depSubtype = IpsObjectDependency.createSubtypeDependency(subProductCmptType,
            superProductCmptType);

    private QualifiedNameType subSubProductCmptType = new QualifiedNameType("subSubProductCmptType",
            IpsObjectType.PRODUCT_CMPT_TYPE);
    private IDependency depSubSubtype = IpsObjectDependency.createSubtypeDependency(subSubProductCmptType,
            subProductCmptType);

    private QualifiedNameType productCmpt = new QualifiedNameType("productCmpt", IpsObjectType.PRODUCT_CMPT);
    private IDependency depInstanceOfProductCmpt = IpsObjectDependency.createInstanceOfDependency(productCmpt,
            subProductCmptType);

    private IDependency depInstanceOfProductCmpt2 = IpsObjectDependency.createInstanceOfDependency(productCmpt,
            subSubProductCmptType);

    private IDependency depInstanceOfProductCmpt3 = IpsObjectDependency.createInstanceOfDependency(productCmpt,
            superProductCmptType);

    private QualifiedNameType policyType1 = new QualifiedNameType("policyType1", IpsObjectType.POLICY_CMPT_TYPE);
    private QualifiedNameType policyType2 = new QualifiedNameType("policyType2", IpsObjectType.POLICY_CMPT_TYPE);
    private IDependency depRefCompMasterDetail = IpsObjectDependency.createCompostionMasterDetailDependency(
            policyType2, policyType1);

    private QualifiedNameType referencedPolicyType = new QualifiedNameType("referencedPolicyType",
            IpsObjectType.POLICY_CMPT_TYPE);
    private QualifiedNameType referencingPolicyType = new QualifiedNameType("referencingPolicyType",
            IpsObjectType.POLICY_CMPT_TYPE);
    private IDependency depReferencePolicy = IpsObjectDependency.createReferenceDependency(referencingPolicyType,
            referencedPolicyType);

    private IDependency depReferenceSubProductCmptTypeToSuperProductCmptType = IpsObjectDependency
            .createReferenceDependency(subProductCmptType, superProductCmptType);

    private QualifiedNameType referencingProductCmptType = new QualifiedNameType("referencingProductCmptType",
            IpsObjectType.PRODUCT_CMPT_TYPE);
    private IDependency depReferenceReferencingProductCmptTypeToSubProductCmptType = IpsObjectDependency
            .createReferenceDependency(referencingProductCmptType, subProductCmptType);

    private IDependency depReferenceReferencingProductCmptTypeToSuperProductCmptType = IpsObjectDependency
            .createReferenceDependency(referencingProductCmptType, superProductCmptType);

    private IDependency depReferenceSubProductCmptTypeToEnumType = IpsObjectDependency.createReferenceDependency(
            subProductCmptType, enumType);

    private QualifiedNameType superPolicyCmptType = new QualifiedNameType("superPolicyCmptType",
            IpsObjectType.PRODUCT_CMPT_TYPE);
    private IDependency depReferenceSuperProductCmptToSuperPolicyCmptType = IpsObjectDependency
            .createReferenceDependency(superProductCmptType, superPolicyCmptType);

    private IDependency depReferenceSuperPolicyCmptTypeToSuperProductCmptType = IpsObjectDependency
            .createReferenceDependency(superPolicyCmptType, superProductCmptType);

    private IDependency depReferenceReferencingProductCmptTypeToEnumContent = IpsObjectDependency
            .createReferenceDependency(referencingProductCmptType, enumContent);

    private IDependency depConfigurationSuperProductCmptToSuperPolicyCmptType = IpsObjectDependency
            .createConfigurationDependency(superProductCmptType, superPolicyCmptType);

    private IDependency depConfigurationSuperPolicyCmptTypeToSuperProductCmptType = IpsObjectDependency
            .createConfigurationDependency(superPolicyCmptType, superProductCmptType);

    private IDependency depConfigurationSubProductCmptTypeToSuperProductCmptType = IpsObjectDependency
            .createConfigurationDependency(subProductCmptType, superProductCmptType);

    private IDependency depDatatype = new DatatypeDependency(superProductCmptType, "enumType");

    private IDependency depDatatype2 = new DatatypeDependency(subProductCmptType, "superProductCmptType");

    private IDependency depDatatypeEnumTypeToSuperPolicyCmptType = new DatatypeDependency(superPolicyCmptType,
            "enumType");

    private QualifiedNameType tableContentType1 = new QualifiedNameType("tableContentType1",
            IpsObjectType.TABLE_CONTENTS);
    private QualifiedNameType tableContentType2 = new QualifiedNameType("tableContentType2",
            IpsObjectType.TABLE_CONTENTS);
    private IDependency depValidation = IpsObjectDependency.create(tableContentType2, tableContentType1,
            DependencyType.VALIDATION);

    /**
     * 
     * <strong>Scenario:</strong><br>
     * For better understanding of the test scenarios see the file
     * DependencyResolverTestDiagrams.png or for editing DependencyResovlerTestDiagrams.uxf. To edit
     * the .uxf file you need the open source tool UMLet.
     */
    @Before
    public void setUp() throws Exception {
        when(ipsProject1.getDependencyGraph()).thenReturn(dependencyGraph1);
        when(ipsProject1.canBeBuild()).thenReturn(true);
        when(ipsProject1.findReferencingProjects(false)).thenReturn(new IIpsProject[0]);
        when(ipsProject1.getIpsArtefactBuilderSet()).thenReturn(artefactBuilderSet);

        dependencyResolver = new DependencyResolver(ipsProject1);

        when(dependencyGraph1.getDependants(any(QualifiedNameType.class))).thenReturn(new IDependency[0]);

        when(ipsProject2.getDependencyGraph()).thenReturn(dependencyGraph2);
        when(ipsProject2.canBeBuild()).thenReturn(true);
        when(ipsProject2.findReferencingProjects(false)).thenReturn(new IIpsProject[0]);
        when(ipsProject2.getIpsArtefactBuilderSet()).thenReturn(artefactBuilderSet);

        when(dependencyGraph2.getDependants(any(QualifiedNameType.class))).thenReturn(new IDependency[0]);

        when(ipsProject3.getDependencyGraph()).thenReturn(dependencyGraph3);
        when(ipsProject3.canBeBuild()).thenReturn(true);
        when(ipsProject3.findReferencingProjects(false)).thenReturn(new IIpsProject[0]);
        when(ipsProject3.getIpsArtefactBuilderSet()).thenReturn(artefactBuilderSet);

        when(dependencyGraph3.getDependants(any(QualifiedNameType.class))).thenReturn(new IDependency[0]);

        when(ipsProject4.getDependencyGraph()).thenReturn(dependencyGraph4);
        when(ipsProject4.canBeBuild()).thenReturn(true);
        when(ipsProject4.findReferencingProjects(false)).thenReturn(new IIpsProject[0]);
        when(ipsProject4.getIpsArtefactBuilderSet()).thenReturn(artefactBuilderSet);

        when(dependencyGraph4.getDependants(any(QualifiedNameType.class))).thenReturn(new IDependency[0]);
    }

    @Test
    public void testCollectDependenciesForIncrementalBuild() {
        IIpsSrcFile spyIpsSrcFile1 = spy(ipsSrcFile1);
        IIpsSrcFile spyIpsSrcFile2 = spy(ipsSrcFile2);
        IIpsSrcFile spyIpsSrcFile3 = spy(ipsSrcFile3);

        doReturn(enumType).when(spyIpsSrcFile1).getQualifiedNameType();
        doReturn(superProductCmptType).when(spyIpsSrcFile2).getQualifiedNameType();
        doReturn(subProductCmptType).when(spyIpsSrcFile3).getQualifiedNameType();

        List<IIpsSrcFile> addedOrChangesIpsSrcFiles = new ArrayList<IIpsSrcFile>();
        addedOrChangesIpsSrcFiles.add(spyIpsSrcFile1);
        addedOrChangesIpsSrcFiles.add(spyIpsSrcFile2);
        List<IIpsSrcFile> removedIpsSrcFiles = new ArrayList<IIpsSrcFile>();
        removedIpsSrcFiles.add(spyIpsSrcFile3);

        dependencyResolver.collectDependenciesForIncrementalBuild(addedOrChangesIpsSrcFiles, removedIpsSrcFiles);

        verify(spyIpsSrcFile1, times(1)).getQualifiedNameType();
        verify(spyIpsSrcFile2, times(1)).getQualifiedNameType();
        verify(spyIpsSrcFile3, times(1)).getQualifiedNameType();

    }

    @Test
    public void testCollectDependencies_DependencyGraphIsNull() {
        when(ipsProject1.getDependencyGraph()).thenReturn(null);
        when(dependencyGraph1.getDependants(enumType)).thenReturn(new IDependency[] { depInstanceOfEnum });
        dependencyResolver = new DependencyResolver(ipsProject1);

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(0, collectedDependencies.keySet().size());
        assertEquals(0, collectedDependencies.get(ipsProject1).size());
        assertTrue(collectedDependencies.get(ipsProject1).isEmpty());
    }

    @Test
    public void testCollectDependencies_ipsProjectCanNotBeBuild() {
        when(ipsProject1.canBeBuild()).thenReturn(false);
        when(dependencyGraph1.getDependants(enumType)).thenReturn(new IDependency[] { depInstanceOfEnum });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(0, collectedDependencies.keySet().size());
        assertEquals(0, collectedDependencies.get(ipsProject1).size());
        assertTrue(collectedDependencies.get(ipsProject1).isEmpty());
    }

    @Test
    public void testCollectDependencies_INSTANCEOF() {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(new IDependency[] { depInstanceOfEnum });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(1, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItem(depInstanceOfEnum));
    }

    @Test
    public void testCollectDependencies_SUBTYPE() {
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(new IDependency[] { depSubtype });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(superProductCmptType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(1, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItem(depSubtype));
    }

    @Test
    public void testCollectDependencies_REFERENCE_COMPOSITION_MASTER_DETAIL() {
        when(artefactBuilderSet.containsAggregateRootBuilder()).thenReturn(false);
        when(dependencyGraph1.getDependants(policyType1)).thenReturn(new IDependency[] { depRefCompMasterDetail });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(policyType1);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(1, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItem(depRefCompMasterDetail));
    }

    @Test
    public void testCollectDependencies_REFERENCE_COMPOSITION_MASTER_DETAIL_ContainsAggregateRootBuilder() {
        when(artefactBuilderSet.containsAggregateRootBuilder()).thenReturn(true);
        when(dependencyGraph1.getDependants(policyType1)).thenReturn(new IDependency[] { depRefCompMasterDetail });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(policyType1);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(1, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItem(depRefCompMasterDetail));
    }

    @Test
    public void testCollectDependencies_REFERENCE() {
        when(dependencyGraph1.getDependants(referencedPolicyType)).thenReturn(new IDependency[] { depReferencePolicy });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(referencedPolicyType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(1, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItem(depReferencePolicy));
    }

    @Test
    public void testCollectDependencies_DATATYPE() {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(new IDependency[] { depDatatype });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(1, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItem(depDatatype));
    }

    @Test
    public void testCollectDependencies_VALIDATION() {
        when(dependencyGraph1.getDependants(tableContentType1)).thenReturn(new IDependency[] { depValidation });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(tableContentType1);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(1, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItem(depValidation));
    }

    @Test
    public void testCollectDependencies_FindAllDependenciesGoingOverSUBTYPE() {
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(new IDependency[] { depSubtype });
        when(dependencyGraph1.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt,
                        depReferenceReferencingProductCmptTypeToSubProductCmptType });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(superProductCmptType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(3, collectedDependencies.get(ipsProject1).size());
        assertThat(
                collectedDependencies.get(ipsProject1),
                hasItems(depSubtype, depInstanceOfProductCmpt,
                        depReferenceReferencingProductCmptTypeToSubProductCmptType));
    }

    @Test
    public void testCollectDependencies_FindAllDependenciesGoingOverCONFIGURATION() {
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(
                new IDependency[] { depConfigurationSubProductCmptTypeToSuperProductCmptType });
        when(dependencyGraph1.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt,
                        depReferenceReferencingProductCmptTypeToSubProductCmptType });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(superProductCmptType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(3, collectedDependencies.get(ipsProject1).size());
        assertThat(
                collectedDependencies.get(ipsProject1),
                hasItems(depConfigurationSubProductCmptTypeToSuperProductCmptType, depInstanceOfProductCmpt,
                        depReferenceReferencingProductCmptTypeToSubProductCmptType));
    }

    @Test
    public void testCollectDependencies_FindSpecificDependenciesGoingOverREFERENCE() {
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(
                new IDependency[] { depReferenceSubProductCmptTypeToSuperProductCmptType });
        when(dependencyGraph1.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt,
                        depReferenceReferencingProductCmptTypeToSubProductCmptType });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(superProductCmptType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(2, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1),
                hasItems(depReferenceSubProductCmptTypeToSuperProductCmptType, depInstanceOfProductCmpt));
    }

    @Test
    public void testCollectDependencies_FindSpecificDependenciesGoingOverDATATYPE() {
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(new IDependency[] { depDatatype2 });
        when(dependencyGraph1.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt,
                        depReferenceReferencingProductCmptTypeToSubProductCmptType });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(superProductCmptType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(2, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItems(depDatatype2, depInstanceOfProductCmpt));
    }

    @Test
    public void testCollectDependencies_TransitiveInstanceOfOverSubtype() {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(new IDependency[] { depInstanceOfEnum, depDatatype });
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(new IDependency[] { depSubtype });
        when(dependencyGraph1.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt,
                        depReferenceReferencingProductCmptTypeToSubProductCmptType });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(4, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1),
                hasItems(depInstanceOfEnum, depDatatype, depSubtype, depInstanceOfProductCmpt));
    }

    @Test
    public void testCollectDependencies_TransitiveInstanceOfOverSubSubtype() {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(new IDependency[] { depInstanceOfEnum, depDatatype });
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(new IDependency[] { depSubtype });
        when(dependencyGraph1.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depSubSubtype, depReferenceReferencingProductCmptTypeToSubProductCmptType });
        when(dependencyGraph1.getDependants(subSubProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt2 });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(5, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1),
                hasItems(depInstanceOfEnum, depDatatype, depSubtype, depSubSubtype, depInstanceOfProductCmpt2));
    }

    @Test
    public void testCollectDependencies_FindSpecificDependenciesGoingOverDATATYPEandREFERENCE() {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(
                new IDependency[] { depInstanceOfEnum, depDatatype, depReferenceSubProductCmptTypeToEnumType });
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(new IDependency[] { depSubtype });
        when(dependencyGraph1.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt,
                        depReferenceReferencingProductCmptTypeToSubProductCmptType });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(5, collectedDependencies.get(ipsProject1).size());
        assertThat(
                collectedDependencies.get(ipsProject1),
                hasItems(depInstanceOfEnum, depDatatype, depReferenceSubProductCmptTypeToEnumType, depSubtype,
                        depInstanceOfProductCmpt));
    }

    @Test
    public void testCollectDependencies_DependentProjects() {
        setUpObjectDependencies_forDependentProjects();
        when(ipsProject1.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject2 });
        when(ipsProject2.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject3 });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(3, collectedDependencies.keySet().size());
        assertEquals(2, collectedDependencies.get(ipsProject1).size());
        assertEquals(2, collectedDependencies.get(ipsProject2).size());
        assertEquals(1, collectedDependencies.get(ipsProject3).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItems(depDatatype, depSubtype));
        assertThat(collectedDependencies.get(ipsProject2),
                hasItems(depInstanceOfEnum, depReferenceSubProductCmptTypeToEnumType));
        assertThat(collectedDependencies.get(ipsProject3), hasItems(depInstanceOfProductCmpt));
    }

    @Test
    public void testCollectDependencies_MultiDependentProjects() {
        setUpObjectDependencies_forDependentProjects();

        when(ipsProject1.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject2, ipsProject3 });
        when(ipsProject2.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject3 });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(3, collectedDependencies.keySet().size());
        assertEquals(2, collectedDependencies.get(ipsProject1).size());
        assertEquals(2, collectedDependencies.get(ipsProject2).size());
        assertEquals(1, collectedDependencies.get(ipsProject3).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItems(depDatatype, depSubtype));
        assertThat(collectedDependencies.get(ipsProject2),
                hasItems(depInstanceOfEnum, depReferenceSubProductCmptTypeToEnumType));
        assertThat(collectedDependencies.get(ipsProject3), hasItems(depInstanceOfProductCmpt));
    }

    @Test
    public void testCollectDependencies_LoopDependentProjects() {
        setUpObjectDependencies_forDependentProjects();

        when(ipsProject1.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject2 });
        when(ipsProject2.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject3 });
        when(ipsProject3.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject1 });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(3, collectedDependencies.keySet().size());
        assertEquals(2, collectedDependencies.get(ipsProject1).size());
        assertEquals(2, collectedDependencies.get(ipsProject2).size());
        assertEquals(1, collectedDependencies.get(ipsProject3).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItems(depDatatype, depSubtype));
        assertThat(collectedDependencies.get(ipsProject2),
                hasItems(depInstanceOfEnum, depReferenceSubProductCmptTypeToEnumType));
        assertThat(collectedDependencies.get(ipsProject3), hasItems(depInstanceOfProductCmpt));
    }

    private void setUpObjectDependencies_forDependentProjects() {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(new IDependency[] { depDatatype });
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(new IDependency[] { depSubtype });
        when(dependencyGraph2.getDependants(enumType)).thenReturn(
                new IDependency[] { depInstanceOfEnum, depReferenceSubProductCmptTypeToEnumType });
        when(dependencyGraph2.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depReferenceReferencingProductCmptTypeToSubProductCmptType });
        when(dependencyGraph3.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt, });
        dependencyResolver = new DependencyResolver(ipsProject1);
    }

    @Test
    public void testCollectDependencies_DependentProjects_INSTANCEOF() {
        when(ipsProject1.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject2 });
        when(dependencyGraph1.getDependants(enumType)).thenReturn(new IDependency[] {});
        when(dependencyGraph2.getDependants(enumType)).thenReturn(new IDependency[] { depInstanceOfEnum });
        dependencyResolver = new DependencyResolver(ipsProject1);

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(0, collectedDependencies.get(ipsProject1).size());
        assertEquals(1, collectedDependencies.get(ipsProject2).size());
        assertThat(collectedDependencies.get(ipsProject2), hasItem(depInstanceOfEnum));
    }

    @Test
    public void testCollectDependencies_DependentProjects_REFERENCE() {
        when(ipsProject1.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject2 });
        when(dependencyGraph1.getDependants(referencedPolicyType)).thenReturn(new IDependency[] {});
        when(dependencyGraph2.getDependants(referencedPolicyType)).thenReturn(new IDependency[] { depReferencePolicy });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(referencedPolicyType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(0, collectedDependencies.get(ipsProject1).size());
        assertEquals(1, collectedDependencies.get(ipsProject2).size());
        assertThat(collectedDependencies.get(ipsProject2), hasItem(depReferencePolicy));
    }

    @Test
    public void testCollectDependencies_ResolveDatatypeDependenciesForEnumContent() throws CoreException {
        when(ipsProject1.findIpsObject(enumContent)).thenReturn(enumContentObject);
        when(enumContentObject.getEnumType()).thenReturn("enumType");
        when(enumContentObject.findEnumType(ipsProject1)).thenReturn(enumTypeObject);
        when(enumTypeObject.getIpsProject()).thenReturn(ipsProject1);
        when(dependencyGraph1.getDependants(enumType)).thenReturn(new IDependency[] { depInstanceOfEnum, depDatatype });
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(new IDependency[] { depSubtype });
        when(dependencyGraph1.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt,
                        depReferenceReferencingProductCmptTypeToSubProductCmptType });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumContent);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(4, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1),
                hasItems(depInstanceOfEnum, depDatatype, depSubtype, depInstanceOfProductCmpt));
    }

    @Test
    public void testCollectDependencies_ResolveDatatypeDependenciesForEnumContentOverDependentProjects()
            throws CoreException {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(new IDependency[] { depDatatype });
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(new IDependency[] { depSubtype });
        when(dependencyGraph2.getDependants(enumType)).thenReturn(new IDependency[] { depInstanceOfEnum });
        when(dependencyGraph2.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depReferenceReferencingProductCmptTypeToSubProductCmptType });
        when(dependencyGraph3.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt, });
        dependencyResolver = new DependencyResolver(ipsProject2);
        when(ipsProject1.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject2 });
        when(ipsProject2.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject3 });
        when(ipsProject2.findIpsObject(enumContent)).thenReturn(enumContentObject);
        when(enumContentObject.getEnumType()).thenReturn("enumType");
        when(enumContentObject.findEnumType(ipsProject2)).thenReturn(enumTypeObject);
        when(enumTypeObject.getIpsProject()).thenReturn(ipsProject1);

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumContent);

        assertEquals(3, collectedDependencies.keySet().size());
        assertEquals(2, collectedDependencies.get(ipsProject1).size());
        assertEquals(1, collectedDependencies.get(ipsProject2).size());
        assertEquals(1, collectedDependencies.get(ipsProject3).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItems(depDatatype, depSubtype));
        assertThat(collectedDependencies.get(ipsProject2), hasItems(depInstanceOfEnum));
        assertThat(collectedDependencies.get(ipsProject3), hasItems(depInstanceOfProductCmpt));
    }

    @Test
    public void testCollectDependencies_ResolveDatatypeDependenciesForEnumContentOverDependentProjectsAndREFERENCE()
            throws CoreException {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(
                new IDependency[] { depDatatypeEnumTypeToSuperPolicyCmptType });
        when(dependencyGraph1.getDependants(superPolicyCmptType)).thenReturn(
                new IDependency[] { depReferenceSuperProductCmptToSuperPolicyCmptType });
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(
                new IDependency[] { depSubtype, depReferenceSuperPolicyCmptTypeToSuperProductCmptType });
        when(dependencyGraph2.getDependants(enumType)).thenReturn(new IDependency[] { depInstanceOfEnum });
        when(dependencyGraph2.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depReferenceReferencingProductCmptTypeToSubProductCmptType });
        when(dependencyGraph3.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt });
        dependencyResolver = new DependencyResolver(ipsProject2);
        when(ipsProject1.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject2 });
        when(ipsProject2.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject3 });
        when(ipsProject2.findIpsObject(enumContent)).thenReturn(enumContentObject);
        when(enumContentObject.getEnumType()).thenReturn("enumType");
        when(enumContentObject.findEnumType(ipsProject2)).thenReturn(enumTypeObject);
        when(enumTypeObject.getIpsProject()).thenReturn(ipsProject1);

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumContent);

        assertEquals(2, collectedDependencies.keySet().size());
        assertEquals(1, collectedDependencies.get(ipsProject1).size());
        assertEquals(1, collectedDependencies.get(ipsProject2).size());
        assertEquals(0, collectedDependencies.get(ipsProject3).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItems(depDatatypeEnumTypeToSuperPolicyCmptType));
        assertThat(collectedDependencies.get(ipsProject2), hasItems(depInstanceOfEnum));
    }

    @Test
    public void testCollectDependencies_DoNotFindREFERENCEgoingOverDATATYPE() {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(
                new IDependency[] { depDatatypeEnumTypeToSuperPolicyCmptType });
        when(dependencyGraph1.getDependants(superPolicyCmptType)).thenReturn(
                new IDependency[] { depReferenceSuperProductCmptToSuperPolicyCmptType });
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(
                new IDependency[] { depReferenceSuperPolicyCmptTypeToSuperProductCmptType });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(1, collectedDependencies.get(ipsProject1).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItems(depDatatypeEnumTypeToSuperPolicyCmptType));
    }

    @Test
    public void testCollectDependencies_FindCONFIGURATIONgoingOverDATATYPE() {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(
                new IDependency[] { depDatatypeEnumTypeToSuperPolicyCmptType });
        when(dependencyGraph1.getDependants(superPolicyCmptType)).thenReturn(
                new IDependency[] { depConfigurationSuperProductCmptToSuperPolicyCmptType });
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(
                new IDependency[] { depConfigurationSuperPolicyCmptTypeToSuperProductCmptType });

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(3, collectedDependencies.get(ipsProject1).size());
        assertThat(
                collectedDependencies.get(ipsProject1),
                hasItems(depDatatypeEnumTypeToSuperPolicyCmptType,
                        depConfigurationSuperProductCmptToSuperPolicyCmptType,
                        depConfigurationSuperPolicyCmptTypeToSuperProductCmptType));
    }

    @Test
    public void testCollectDependencies_ResolveDatatypeDependenciesForEnumContentOverDependentProjectsAndCONFIGURATION()
            throws CoreException {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(
                new IDependency[] { depDatatypeEnumTypeToSuperPolicyCmptType });
        when(dependencyGraph1.getDependants(superPolicyCmptType)).thenReturn(
                new IDependency[] { depConfigurationSuperProductCmptToSuperPolicyCmptType });
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(
                new IDependency[] { depSubtype, depConfigurationSuperPolicyCmptTypeToSuperProductCmptType });
        when(dependencyGraph2.getDependants(enumType)).thenReturn(new IDependency[] { depInstanceOfEnum });
        when(dependencyGraph2.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depReferenceReferencingProductCmptTypeToSubProductCmptType });
        when(dependencyGraph3.getDependants(subProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt });
        dependencyResolver = new DependencyResolver(ipsProject2);
        when(ipsProject1.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject2 });
        when(ipsProject2.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject3 });
        when(ipsProject2.findIpsObject(enumContent)).thenReturn(enumContentObject);
        when(enumContentObject.getEnumType()).thenReturn("enumType");
        when(enumContentObject.findEnumType(ipsProject2)).thenReturn(enumTypeObject);
        when(enumTypeObject.getIpsProject()).thenReturn(ipsProject1);

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumContent);

        assertEquals(3, collectedDependencies.keySet().size());
        assertEquals(4, collectedDependencies.get(ipsProject1).size());
        assertEquals(1, collectedDependencies.get(ipsProject2).size());
        assertEquals(1, collectedDependencies.get(ipsProject3).size());
        assertThat(
                collectedDependencies.get(ipsProject1),
                hasItems(depDatatypeEnumTypeToSuperPolicyCmptType,
                        depConfigurationSuperProductCmptToSuperPolicyCmptType,
                        depConfigurationSuperPolicyCmptTypeToSuperProductCmptType, depSubtype));
        assertThat(collectedDependencies.get(ipsProject2), hasItems(depInstanceOfEnum));
        assertThat(collectedDependencies.get(ipsProject3), hasItems(depInstanceOfProductCmpt));
    }

    @Test
    public void testCollectDependencies_ResolveReferenceDependencyOnEnumContent() throws CoreException {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(new IDependency[] { depDatatype, depInstanceOfEnum });
        when(dependencyGraph1.getDependants(superProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt3 });
        when(dependencyGraph1.getDependants(enumContent)).thenReturn(
                new IDependency[] { depReferenceReferencingProductCmptTypeToEnumContent });
        dependencyResolver = new DependencyResolver(ipsProject1);
        when(ipsProject1.findIpsObject(enumContent)).thenReturn(enumContentObject);
        when(enumContentObject.getEnumType()).thenReturn("enumType");
        when(enumContentObject.findEnumType(ipsProject1)).thenReturn(enumTypeObject);
        when(enumTypeObject.getIpsProject()).thenReturn(ipsProject1);

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumContent);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(4, collectedDependencies.get(ipsProject1).size());
        assertThat(
                collectedDependencies.get(ipsProject1),
                hasItems(depDatatype, depInstanceOfEnum, depInstanceOfProductCmpt3,
                        depReferenceReferencingProductCmptTypeToEnumContent));
    }

    @Test
    public void testCollectDependencies_ResolveReferenceDependencyOnEnumContentOverDependentProjects()
            throws CoreException {
        when(dependencyGraph1.getDependants(enumType)).thenReturn(new IDependency[] { depDatatype });
        when(dependencyGraph2.getDependants(superProductCmptType)).thenReturn(
                new IDependency[] { depInstanceOfProductCmpt3 });
        when(dependencyGraph2.getDependants(enumType)).thenReturn(new IDependency[] { depInstanceOfEnum });
        when(dependencyGraph3.getDependants(enumContent)).thenReturn(
                new IDependency[] { depReferenceReferencingProductCmptTypeToEnumContent });
        dependencyResolver = new DependencyResolver(ipsProject2);
        when(ipsProject1.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject2 });
        when(ipsProject2.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject3 });
        when(ipsProject2.findIpsObject(enumContent)).thenReturn(enumContentObject);
        when(enumContentObject.getEnumType()).thenReturn("enumType");
        when(enumContentObject.findEnumType(ipsProject2)).thenReturn(enumTypeObject);
        when(enumTypeObject.getIpsProject()).thenReturn(ipsProject1);

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(enumContent);

        assertEquals(3, collectedDependencies.keySet().size());
        assertEquals(1, collectedDependencies.get(ipsProject1).size());
        assertEquals(2, collectedDependencies.get(ipsProject2).size());
        assertEquals(1, collectedDependencies.get(ipsProject3).size());
        assertThat(collectedDependencies.get(ipsProject1), hasItems(depDatatype));
        assertThat(collectedDependencies.get(ipsProject2), hasItems(depInstanceOfEnum, depInstanceOfProductCmpt3));
        assertThat(collectedDependencies.get(ipsProject3),
                hasItems(depReferenceReferencingProductCmptTypeToEnumContent));
    }

    @Test
    public void testCollectDependencies_CrossDependentProjects() {
        when(dependencyGraph4.getDependants(superProductCmptType)).thenReturn(
                new IDependency[] { depReferenceReferencingProductCmptTypeToSuperProductCmptType });
        when(ipsProject1.findReferencingProjects(false)).thenReturn(
                new IIpsProject[] { ipsProject2, ipsProject3, ipsProject4 });
        when(ipsProject2.findReferencingProjects(false)).thenReturn(new IIpsProject[] { ipsProject3 });
        dependencyResolver = new DependencyResolver(ipsProject1);

        MultiMap<IIpsProject, IDependency> collectedDependencies = collectDependenciesOf(superProductCmptType);

        assertEquals(1, collectedDependencies.keySet().size());
        assertEquals(1, collectedDependencies.get(ipsProject4).size());
        assertThat(collectedDependencies.get(ipsProject4),
                hasItems(depReferenceReferencingProductCmptTypeToSuperProductCmptType));
    }

    private MultiMap<IIpsProject, IDependency> collectDependenciesOf(QualifiedNameType objectNameType) {
        dependencyResolver.collectDependencies(objectNameType, new HashSet<IIpsProject>(), false);
        MultiMap<IIpsProject, IDependency> collectedDependencies = dependencyResolver.getCollectedDependencies();
        return collectedDependencies;
    }
}
