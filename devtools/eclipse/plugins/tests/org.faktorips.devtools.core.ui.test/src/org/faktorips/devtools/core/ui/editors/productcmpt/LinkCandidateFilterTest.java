/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class LinkCandidateFilterTest {
    private static final String TYPE_NAME = "bla.bla.bla.Type";
    @Mock
    private IProductCmpt prodCmpt;
    @Mock
    private IProductCmptGeneration prodCmptGeneration;
    @Mock
    private IIpsSrcFile srcFile;
    @Mock
    private IProductCmptType type;
    @Mock
    private IProductCmptTypeAssociation association;
    @Mock
    private IIpsProject ipsProject;

    private LinkCandidateFilter filter;
    private GregorianCalendar validAt;

    @Before
    public void setUp() {
        when(prodCmpt.getGenerationEffectiveOn(any(GregorianCalendar.class))).thenReturn(prodCmptGeneration);

        when(association.getName()).thenReturn("association");
        when(association.findTargetProductCmptType(ipsProject)).thenReturn(type);
        when(association.getMaxCardinality()).thenReturn(3);

        when(prodCmptGeneration.getLinks(association.getName())).thenReturn(new IProductCmptLink[0]);
        when(prodCmptGeneration.getIpsSrcFile()).thenReturn(srcFile);
        when(prodCmptGeneration.getIpsProject()).thenReturn(ipsProject);

        when(type.isSubtypeOrSameType(eq(type), any(IIpsProject.class))).thenReturn(true);
        validAt = new GregorianCalendar(2013, 4, 1);
    }

    @Test
    public void testWrongObjectType() {
        createFilter();

        IIpsSrcFile sourceFileWrongType = createSourceFile(ipsProject, type);
        when(sourceFileWrongType.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_STRUCTURE);

        assertFalse(filter.filter(sourceFileWrongType));
    }

    @Test
    public void testImmutableProductCmpt() {
        when(srcFile.isReadOnly()).thenReturn(true);

        createFilter();

        assertFalse(filter.filter(createSourceFile(ipsProject, type)));
    }

    @Test
    public void testWorkingModeBrowse() {
        createFilter(true);

        assertFalse(filter.filter(createSourceFile(ipsProject, type)));
    }

    @Test
    public void testInProject() {
        createFilter();

        IIpsSrcFile srcFileSameProject = createSourceFile(ipsProject, type);

        IIpsProject referencedProject = mock(IIpsProject.class);
        IIpsProject notReferencedProject = mock(IIpsProject.class);

        IIpsSrcFile srcFileReferencedProject = createSourceFile(referencedProject, type);
        when(ipsProject.isReferencing(referencedProject)).thenReturn(true);

        IIpsSrcFile srcFileNotReferencedProject = createSourceFile(notReferencedProject, type);
        when(ipsProject.isReferencing(notReferencedProject)).thenReturn(false);

        assertTrue(filter.filter(srcFileSameProject));
        assertTrue(filter.filter(srcFileReferencedProject));
        assertFalse(filter.filter(srcFileNotReferencedProject));
    }

    @Test
    public void testSameTargetType() {
        createFilter();

        IIpsSrcFile srcFileSameType = createSourceFile(ipsProject, type);
        assertTrue(filter.filter(srcFileSameType));
    }

    @Test
    public void testSubOfTargetType() {
        createFilter();

        IProductCmptType subType = mock(IProductCmptType.class);
        when(subType.isSubtypeOrSameType(eq(type), any(IIpsProject.class))).thenReturn(true);

        IIpsSrcFile srcFileSubType = createSourceFile(ipsProject, subType);
        assertTrue(filter.filter(srcFileSubType));
    }

    @Test
    public void testSuperOfTargetType() {
        createFilter();

        IProductCmptType superType = mock(IProductCmptType.class);
        when(superType.isSubtypeOrSameType(eq(type), any(IIpsProject.class))).thenReturn(false);

        IIpsSrcFile srcFileSuperType = createSourceFile(ipsProject, superType);
        assertFalse(filter.filter(srcFileSuperType));
    }

    @Test
    public void testAnotherTargetType() {
        createFilter();

        IProductCmptType anotherType = mock(IProductCmptType.class);
        when(anotherType.isSubtypeOrSameType(eq(type), any(IIpsProject.class))).thenReturn(false);

        IIpsSrcFile srcFileAnyType = createSourceFile(ipsProject, anotherType);

        assertFalse(filter.filter(srcFileAnyType));
    }

    @Test
    public void testAlreadyAssociated() {
        createFilter();

        IIpsSrcFile srcFileNotLinked = createSourceFile(ipsProject, type);
        when(srcFileNotLinked.getQualifiedNameType().getName()).thenReturn("de.not.linked.PC");

        IIpsSrcFile srcFileAlreadyLinked = createSourceFile(ipsProject, type);
        String linkedName = "de.linked.PC";
        when(srcFileAlreadyLinked.getQualifiedNameType().getName()).thenReturn(linkedName);

        IProductCmptLink link = mock(IProductCmptLink.class);
        when(link.getTarget()).thenReturn(linkedName);

        List<IProductCmptLink> links = Arrays.asList(link);
        when(prodCmptGeneration.getLinksAsList()).thenReturn(links);

        assertTrue(filter.filter(srcFileNotLinked));
        assertFalse(filter.filter(srcFileAlreadyLinked));
    }

    @Test
    public void testAssociationAlreadyFull() {
        IProductCmptLink link = mock(IProductCmptLink.class);

        IProductCmptLink[] links = { link };

        when(association.getMaxCardinality()).thenReturn(1);
        when(prodCmptGeneration.getLinks(association.getName())).thenReturn(links);

        createFilter();

        IIpsSrcFile srcFileNotLinked = createSourceFile(ipsProject, type);

        assertFalse(filter.filter(srcFileNotLinked));
    }

    private void createFilter() {
        createFilter(false);
    }

    private void createFilter(boolean workingModeBrowse) {
        IProductCmptTypeAssociationReference structureReference = mock(IProductCmptTypeAssociationReference.class);
        when(structureReference.getAssociation()).thenReturn(association);

        IProductCmptTreeStructure structure = mock(IProductCmptTreeStructure.class);
        when(structure.getValidAt()).thenReturn(validAt);

        when(structureReference.getStructure()).thenReturn(structure);

        IProductCmptReference parentReference = mock(IProductCmptReference.class);
        when(structureReference.getParent()).thenReturn(parentReference);
        when(parentReference.getProductCmpt()).thenReturn(prodCmpt);

        filter = new LinkCandidateFilter(structureReference, workingModeBrowse);
    }

    private IIpsSrcFile createSourceFile(IIpsProject project, IProductCmptType productCmptType) {
        IpsSrcFile srcFile = mock(IpsSrcFile.class);
        QualifiedNameType type = mock(QualifiedNameType.class);
        when(srcFile.getQualifiedNameType()).thenReturn(type);

        when(srcFile.getIpsProject()).thenReturn(project);

        when(srcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE)).thenReturn(TYPE_NAME);
        when(project.findProductCmptType(TYPE_NAME)).thenReturn(productCmptType);
        when(srcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        return srcFile;
    }
}
