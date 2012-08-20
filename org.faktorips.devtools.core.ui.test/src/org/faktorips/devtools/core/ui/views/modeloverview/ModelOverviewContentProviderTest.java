/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.modeloverview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Test;

public class ModelOverviewContentProviderTest extends AbstractIpsPluginTest {

    private IIpsProject grundmodell;
    private IIpsProject hausratmodell;

    private final String GRUNDMODELL_VERTRAG = "Vertrag";
    private final String GRUNDMODELL_DECKUNG = "Deckung";
    private final String GRUNDMODELL_PRODUKT = "Produkt";
    private final String GRUNDMODELL_DECKUNGSTYP = "Deckungstyp";

    private final String HAUSRATMODELL_GRUNDDECKUNG = "HausratGrunddeckung";
    private final String HAUSRATMODELL_ZUSATZDECKUNG = "HausratZusatzdeckung";
    private final String HAUSRATMODELL_VERTRAG = "HausratVertrag";
    private final String HAUSRATMODELL_GRUNDDECKUNGSTYP = "HausratGrunddeckungstyp";
    private final String HAUSRATMODELL_ZUSATZDECKUNGSTYP = "HausratZusatzdeckungstyp";
    private final String HAUSRATMODELL_PRODUKT = "HausratProdukt";

    @Override
    public void setUp() throws CoreException {
        grundmodell = newIpsProject();
        hausratmodell = newIpsProject();

        /**********************************************************
         * Setup the grundmodell
         **********************************************************/
        // Create Components
        PolicyCmptType polCmptVertrag = newPolicyCmptTypeWithoutProductCmptType(grundmodell, GRUNDMODELL_VERTRAG);
        PolicyCmptType polCmptDeckung = newPolicyCmptTypeWithoutProductCmptType(grundmodell, GRUNDMODELL_DECKUNG);

        ProductCmptType prodCmptProdukt = newProductCmptType(grundmodell, GRUNDMODELL_PRODUKT);
        ProductCmptType prodCmptDeckungstyp = newProductCmptType(grundmodell, GRUNDMODELL_DECKUNGSTYP);

        // set components
        polCmptVertrag.setProductCmptType(GRUNDMODELL_PRODUKT);
        polCmptDeckung.setProductCmptType(GRUNDMODELL_DECKUNGSTYP);

        prodCmptProdukt.setPolicyCmptType(GRUNDMODELL_VERTRAG);
        prodCmptDeckungstyp.setPolicyCmptType(GRUNDMODELL_DECKUNG);

        // set Associations
        // associations between vertrag and deckung
        IAssociation polCmptVertragAssoc = polCmptVertrag.newAssociation();
        polCmptVertragAssoc.setTarget(GRUNDMODELL_DECKUNG);
        polCmptVertragAssoc.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        polCmptVertragAssoc.setMinCardinality(0);
        polCmptVertragAssoc.setMaxCardinality(IAssociation.CARDINALITY_MANY);

        IAssociation polCmptDeckungAssoc = polCmptDeckung.newAssociation();
        polCmptDeckungAssoc.setTarget(GRUNDMODELL_VERTRAG);
        polCmptDeckungAssoc.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        polCmptDeckungAssoc.setMinCardinality(0);
        polCmptDeckungAssoc.setMaxCardinality(IAssociation.CARDINALITY_ONE);

        // associations between produkt and deckungstyp
        IAssociation polCmptProduktAssoc = prodCmptProdukt.newAssociation();
        polCmptProduktAssoc.setTarget(GRUNDMODELL_DECKUNGSTYP);
        polCmptProduktAssoc.setMinCardinality(0);
        polCmptProduktAssoc.setMaxCardinality(IAssociation.CARDINALITY_MANY);
        polCmptProduktAssoc.setAssociationType(AssociationType.AGGREGATION);

        /**********************************************************
         * Setup the hausratmodell
         **********************************************************/
        // Create Components
        PolicyCmptType polCmptHausratVertrag = newPolicyCmptTypeWithoutProductCmptType(hausratmodell,
                HAUSRATMODELL_VERTRAG);
        PolicyCmptType polCmptHausratGrunddeckung = newPolicyCmptTypeWithoutProductCmptType(hausratmodell,
                HAUSRATMODELL_GRUNDDECKUNG);
        PolicyCmptType polCmptHausratZusatzdeckung = newPolicyCmptTypeWithoutProductCmptType(hausratmodell,
                HAUSRATMODELL_ZUSATZDECKUNG);

        ProductCmptType prodCompHausratProdukt = newProductCmptType(hausratmodell, HAUSRATMODELL_PRODUKT);
        ProductCmptType prodCompHausratGrunddeckungstyp = newProductCmptType(hausratmodell,
                HAUSRATMODELL_GRUNDDECKUNGSTYP);
        ProductCmptType prodCompHausratZusatzdeckungstyp = newProductCmptType(hausratmodell,
                HAUSRATMODELL_ZUSATZDECKUNGSTYP);

        // set components
        polCmptHausratVertrag.setProductCmptType(HAUSRATMODELL_PRODUKT);
        polCmptHausratGrunddeckung.setProductCmptType(HAUSRATMODELL_GRUNDDECKUNGSTYP);
        polCmptHausratZusatzdeckung.setProductCmptType(HAUSRATMODELL_ZUSATZDECKUNGSTYP);

        prodCompHausratProdukt.setPolicyCmptType(HAUSRATMODELL_VERTRAG);
        prodCompHausratGrunddeckungstyp.setPolicyCmptType(HAUSRATMODELL_GRUNDDECKUNG);
        prodCompHausratZusatzdeckungstyp.setPolicyCmptType(HAUSRATMODELL_ZUSATZDECKUNG);

        // set supertypes
        polCmptHausratVertrag.setSupertype(GRUNDMODELL_VERTRAG);
        polCmptHausratGrunddeckung.setSupertype(GRUNDMODELL_DECKUNG);
        polCmptHausratZusatzdeckung.setSupertype(GRUNDMODELL_DECKUNG);
        prodCompHausratGrunddeckungstyp.setSupertype(GRUNDMODELL_DECKUNGSTYP);
        prodCompHausratZusatzdeckungstyp.setSupertype(GRUNDMODELL_DECKUNGSTYP);
        prodCompHausratProdukt.setSupertype(GRUNDMODELL_PRODUKT);

        // set associations
        // associations between HausratVertrag, HausratGrunddeckung und HausratZusatzdeckung
        IAssociation polCmptHausratGrunddeckungAssoc = polCmptHausratGrunddeckung.newAssociation();
        polCmptHausratGrunddeckungAssoc.setTarget(HAUSRATMODELL_VERTRAG);
        polCmptHausratGrunddeckungAssoc.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        polCmptHausratGrunddeckungAssoc.setMinCardinality(0);
        polCmptHausratGrunddeckungAssoc.setMaxCardinality(1);

        IAssociation polCmptHausratZusatzdeckungAssoc = polCmptHausratZusatzdeckung.newAssociation();
        polCmptHausratZusatzdeckungAssoc.setTarget(HAUSRATMODELL_VERTRAG);
        polCmptHausratZusatzdeckungAssoc.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        polCmptHausratZusatzdeckungAssoc.setMinCardinality(0);
        polCmptHausratZusatzdeckungAssoc.setMaxCardinality(1);

        IAssociation polCmptHausratVertragAssoc = polCmptHausratVertrag.newAssociation();
        polCmptHausratVertragAssoc.setTarget(HAUSRATMODELL_GRUNDDECKUNG);
        polCmptHausratVertragAssoc.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        polCmptHausratVertragAssoc.setMinCardinality(0);
        polCmptHausratVertragAssoc.setMaxCardinality(1);

        IAssociation polCmptHausratVertragAssoc2 = polCmptHausratVertrag.newAssociation();
        polCmptHausratVertragAssoc2.setTarget(HAUSRATMODELL_ZUSATZDECKUNG);
        polCmptHausratVertragAssoc2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        polCmptHausratVertragAssoc2.setMinCardinality(0);
        polCmptHausratVertragAssoc2.setMaxCardinality(IAssociation.CARDINALITY_MANY);

        // associations between HausratProdukt, HausratGrunddeckungstyp, HausratZusatzdeckungstyp
        IAssociation prodCmptHausratProduktAssociation = prodCompHausratProdukt.newAssociation();
        prodCmptHausratProduktAssociation.setTarget(HAUSRATMODELL_GRUNDDECKUNGSTYP);
        prodCmptHausratProduktAssociation.setAssociationType(AssociationType.AGGREGATION);
        prodCmptHausratProduktAssociation.setMinCardinality(0);
        prodCmptHausratProduktAssociation.setMaxCardinality(1);

        IAssociation prodCmptHausratProduktAssociation2 = prodCompHausratProdukt.newAssociation();
        prodCmptHausratProduktAssociation2.setTarget(HAUSRATMODELL_ZUSATZDECKUNGSTYP);
        prodCmptHausratProduktAssociation2.setAssociationType(AssociationType.AGGREGATION);
        prodCmptHausratProduktAssociation2.setMinCardinality(0);
        prodCmptHausratProduktAssociation2.setMaxCardinality(IAssociation.CARDINALITY_MANY);

        List<IAssociation> associations = polCmptHausratVertrag.findAllAssociations(hausratmodell);

        IIpsObjectPath ipsObjectPath = hausratmodell.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(grundmodell);
        hausratmodell.setIpsObjectPath(ipsObjectPath);
    }

    public void testSpielwiese() throws CoreException {

        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
        IpsObjectType[] filter = { IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.POLICY_CMPT_TYPE };
        hausratmodell.findAllIpsSrcFiles(srcFiles, filter);

        List<IType> componentsFromSrcFiles = getComponentsFromSrcFiles(srcFiles);
        List<IType> rootComponents = getPolicyRootComponents(componentsFromSrcFiles);
        List<IType> productRootComponents = getProductRootComponents(componentsFromSrcFiles);

        System.out.println("\nStructure, Policy Hierarchie");
        for (IType iType : rootComponents) {
            System.out.println(1 + "" + iType.getName());
            printStructuredTree(componentsFromSrcFiles, iType, "", 1);
        }

        System.out.println("\nStructure, Product Hierarchie");
        for (IType iType : productRootComponents) {
            System.out.println(1 + "" + iType.getName());
            printStructuredTree(componentsFromSrcFiles, iType, "", 1);
        }

        System.out.println("<<<<Ready>>>>");
    }

    @Test
    public void testHasNoChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        newProductCmptType(project, "TestProductComponentType");

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // test
        for (Object object : elements) {
            assertFalse(contentProvider.hasChildren(object));
        }
    }

    @Test
    public void testHasSubtypeChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");
        subCmptType.setSupertype(cmptType.getQualifiedName());

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType subProdCmptType = newProductCmptType(project, "TestSubProductComponentType");
        subProdCmptType.setSupertype(prodCmptType.getQualifiedName());

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // test
        for (Object element : elements) {
            assertTrue(contentProvider.hasChildren(element));
        }
    }

    @Test
    public void testGetRootElements() throws CoreException {
        // setup
        // project1: Status of root elements depends only on associations
        IIpsProject project1 = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project1, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project1, "TestPolicyComponentType2");
        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IType prodCmptType = newProductCmptType(project1, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project1, "TestProductComponentType2");
        IAssociation association2 = prodCmptType.newAssociation();
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        // project2: Status of root elements depends only on supertypes
        IIpsProject project2 = newIpsProject();
        IType cmptType2 = newPolicyCmptTypeWithoutProductCmptType(project2, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project2, "TestSubPolicyComponentType");
        subCmptType.setSupertype(cmptType2.getQualifiedName());

        IType prodCmptType2 = newProductCmptType(project2, "TestProductComponentType");
        IType subProdCmptType = newProductCmptType(project2, "TestSubProductComponentType");
        subProdCmptType.setSupertype(prodCmptType2.getQualifiedName());

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements1 = contentProvider.getElements(project1);
        Object[] elements2 = contentProvider.getElements(project2);

        // test the number of existing root elements
        assertEquals(2, elements1.length);
        assertEquals(2, elements2.length);

    }

    @Test
    public void testHasAssociationChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");
        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");
        IAssociation association2 = prodCmptType.newAssociation();
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // test
        for (Object element : elements) {
            assertTrue(contentProvider.hasChildren(element));
        }
    }

    @Test
    public void testHasAssociationAndSubtypeChildren() throws CoreException {
        // setup
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");
        IType subProdCmptType = newProductCmptType(project, "TestSubProductComponentType");

        subCmptType.setSupertype(cmptType.getQualifiedName());
        subProdCmptType.setSupertype(prodCmptType.getQualifiedName());

        IAssociation association = cmptType.newAssociation();
        IAssociation association2 = prodCmptType.newAssociation();

        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        Object[] elements = contentProvider.getElements(project);

        // test
        assertEquals(2, elements.length);
        assertTrue(contentProvider.hasChildren(elements[0]));
        assertTrue(contentProvider.hasChildren(elements[1]));
        assertEquals(2, contentProvider.getChildren(elements[0]).length);
        assertEquals(2, contentProvider.getChildren(elements[1]).length);
    }

    @Test
    public void testGetChildrenEmpty() throws CoreException {
        // setup
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        PolicyCmptType polType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        ProductCmptType prodType = newProductCmptType(project, "TestProductComponentType");

        Object[] elements = contentProvider.getElements(project);

        // test
        assertNotNull(contentProvider.getChildren(elements[0]));
        assertNotNull(contentProvider.getChildren(elements[1]));
        assertEquals(0, contentProvider.getChildren(elements[0]).length);
        assertEquals(0, contentProvider.getChildren(elements[1]).length);
    }

    @Test
    public void testGetChildrenNotEmpty() throws CoreException {
        // setup
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        subCmptType.setSupertype(cmptType.getQualifiedName());

        IIpsProject project2 = newIpsProject();
        IType prodCmptType = newProductCmptType(project2, "TestProductComponentType");
        IType subProdCmptType = newProductCmptType(project2, "TestSubProductComponentType");

        subProdCmptType.setSupertype(prodCmptType.getQualifiedName());

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        Object[] elements = contentProvider.getElements(project);
        Object[] elements2 = contentProvider.getElements(project2);

        // test
        System.out.println(((ComponentNode)elements[0]).getValue().getQualifiedName());
        System.out.println(((ComponentNode)elements2[0]).getValue().getQualifiedName());
        assertEquals(2, contentProvider.getChildren(elements[0]).length);
        assertEquals(1, contentProvider.getChildren(elements2[0]).length);

    }

    private void printStructuredTree(List<IType> components, IType component, String prefix, int level)
            throws CoreException {
        // List<IType> subtypes = getSubtypes(components, component);
        List<IType> subtypes = component.searchSubtypes(false, false);
        List<IType> associations = getAssociations(component);
        if (associations.size() > 0) {
            System.out.println(level + prefix + "Associations");
            for (IType association : associations) {
                System.out.println(level + prefix + "\tA: " + association.getIpsObject().getName());
                printStructuredTree(components, association, "\t", ++level);
            }
        }
        if (subtypes.size() > 0) {
            System.out.println(level + prefix + "Subtypes");
            for (IType subtype : subtypes) {
                System.out.println(level + prefix + "\tS: " + subtype.getName());
                printStructuredTree(components, subtype, "\t", ++level);
            }
        }

    }

    private List<IType> getAssociations(IType object) throws CoreException {
        List<IType> associations = new ArrayList<IType>();

        for (IAssociation association : object.findAllAssociations(object.getIpsProject())) {
            if (association.getAssociationType().equals(AssociationType.COMPOSITION_MASTER_TO_DETAIL)
                    || association.getAssociationType().equals(AssociationType.AGGREGATION)) {
                associations.add(association.findTarget(association.getIpsProject()));
            }
        }
        return associations;
    }

    // Computes the elements which have no supertype and do not depend on other components
    private List<IType> getPolicyRootComponents(List<IType> components) throws CoreException {
        List<IType> rootComponents = new ArrayList<IType>();
        for (IType iType : components) {
            if (iType instanceof PolicyCmptType) {
                PolicyCmptType policy = (PolicyCmptType)iType;
                if (policy.isAggregateRoot() && !policy.hasSupertype()) {
                    rootComponents.add(iType);
                }
            }
        }
        return rootComponents;
    }

    private List<IType> getProductRootComponents(List<IType> components) throws CoreException {
        ArrayList<IType> rootComponents = new ArrayList<IType>();
        for (IType product : components) {
            if (product instanceof ProductCmptType) {
                if (!product.hasSupertype() && !isAssociationTarget(product.getQualifiedName(), components)) {
                    rootComponents.add(product);
                }
            }
        }
        return rootComponents;
    }

    // Gets the PolicyCmpTypes and ProductCmptTypes from a list of IIpsSrcFiles
    private List<IType> getComponentsFromSrcFiles(List<IIpsSrcFile> srcFiles) throws CoreException {
        List<IType> components = new ArrayList<IType>(srcFiles.size());
        for (IIpsSrcFile file : srcFiles) {
            IType ipsObject = (IType)file.getIpsObject();
            components.add(ipsObject);
        }
        return components;
    }

    // checks if target is contained in the associations from the components list
    private boolean isAssociationTarget(String target, List<IType> components) throws CoreException {
        for (IType component : components) {
            for (IType association : getAssociations(component)) {
                if (association.getQualifiedName().equals(target)) {
                    return true;
                }
            }
        }
        return false;
    }
}