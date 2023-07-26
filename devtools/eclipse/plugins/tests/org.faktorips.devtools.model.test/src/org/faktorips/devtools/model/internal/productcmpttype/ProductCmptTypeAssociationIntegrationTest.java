/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpttype;

import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 *
 * @author Jan Ortmann
 */
public class ProductCmptTypeAssociationIntegrationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmptType productType;
    private IProductCmptType coverageType;
    private IProductCmptTypeAssociation association;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productType = newProductCmptType(ipsProject, "Product");
        coverageType = newProductCmptType(ipsProject, "CoverageType");
        association = productType.newProductCmptTypeAssociation();
    }

    @Test
    public void testFindPolicyCmptTypeAssociation() {
        assertNull(association.findMatchingPolicyCmptTypeAssociation(ipsProject));

        association.setTarget(coverageType.getQualifiedName());
        assertNull(association.findMatchingPolicyCmptTypeAssociation(ipsProject));

        IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "Policy");
        productType.setPolicyCmptType(policyType.getQualifiedName());
        productType.setConfigurationForPolicyCmptType(true);
        policyType.setProductCmptType(productType.getQualifiedName());

        IPolicyCmptTypeAssociation policyTypeAssociation = policyType.newPolicyCmptTypeAssociation();
        policyTypeAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertNull(association.findMatchingPolicyCmptTypeAssociation(ipsProject));

        IPolicyCmptType coverage = newPolicyCmptType(ipsProject, "Coverage");
        policyTypeAssociation.setTarget(coverage.getQualifiedName());
        assertNull(association.findMatchingPolicyCmptTypeAssociation(ipsProject));

        IPolicyCmptTypeAssociation detailToMasterAssoc = policyType.newPolicyCmptTypeAssociation();
        detailToMasterAssoc.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        detailToMasterAssoc.setTarget(coverage.getQualifiedName());

        coverageType.setPolicyCmptType(coverage.getQualifiedName());
        coverageType.setConfigurationForPolicyCmptType(true);
        assertEquals(policyTypeAssociation, association.findMatchingPolicyCmptTypeAssociation(ipsProject));

        IProductCmptTypeAssociation association2 = productType.newProductCmptTypeAssociation();
        association2.setTargetRoleSingular("otherAssociation");
        association2.setTarget(coverageType.getQualifiedName());
        assertNull(association2.findMatchingPolicyCmptTypeAssociation(ipsProject));
    }

    /**
     * This is testing the special combination of product and policy type associations discussed in
     * FIPS-563
     *
     */
    @Test
    public void testFindPolicyCmptTypeAssociation2() {
        PolicyCmptType police = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = police.findProductCmptType(ipsProject);

        PolicyCmptType versPerson = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "VersPerson");

        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IPolicyCmptTypeAssociation policeToVersPerson = police.newPolicyCmptTypeAssociation();
        policeToVersPerson.setTargetRoleSingular("VersPers");
        policeToVersPerson.setTarget(versPerson.getQualifiedName());

        IPolicyCmptTypeAssociation versPersonToTarifvereinbarung = versPerson.newPolicyCmptTypeAssociation();
        versPersonToTarifvereinbarung.setTargetRoleSingular("versPersonToTarifvereinbarung");
        versPersonToTarifvereinbarung.setTarget(tarifvereinbarung.getQualifiedName());

        IProductCmptTypeAssociation produktToTarif = produkt.newProductCmptTypeAssociation();
        produktToTarif.setTargetRoleSingular("produktToTarif");
        produktToTarif.setTarget(tarif.getQualifiedName());

        assertNull(produktToTarif.findMatchingPolicyCmptTypeAssociation(ipsProject));

        versPersonToTarifvereinbarung.setMatchingAssociationSource(produkt.getQualifiedName());
        versPersonToTarifvereinbarung.setMatchingAssociationName(produktToTarif.getName());

        assertNull(produktToTarif.findMatchingPolicyCmptTypeAssociation(ipsProject));

        produktToTarif.setMatchingAssociationSource(police.getQualifiedName());
        produktToTarif.setMatchingAssociationName(policeToVersPerson.getName());

        assertEquals(policeToVersPerson, produktToTarif.findMatchingPolicyCmptTypeAssociation(ipsProject));

        produktToTarif.setMatchingAssociationSource(versPerson.getQualifiedName());
        produktToTarif.setMatchingAssociationName(versPersonToTarifvereinbarung.getName());
        assertEquals(versPersonToTarifvereinbarung, produktToTarif.findMatchingPolicyCmptTypeAssociation(ipsProject));
    }

    /**
     * Test for FIPS-734
     */
    @Test
    public void testFindDefaultPolicyCmptTypeAssociation_FindNoMatchingAssociationForDifferingHierarchy()
            throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Policy", "MyProduct");
        IProductCmptType product = policy.findProductCmptType(ipsProject);
        ProductCmptType subProduct = newProductCmptType(ipsProject, "SubProduct", policy);
        subProduct.setSupertype(product.getQualifiedName());

        PolicyCmptType cover = newPolicyAndProductCmptType(ipsProject, "Coverage", "MyCoverageType");
        IProductCmptType coverType = cover.findProductCmptType(ipsProject);

        IPolicyCmptTypeAssociation policyToCover = newPolicyCmptTypeAssociation(policy, cover);
        IProductCmptTypeAssociation productToCoverType = newProductCmptTypeAssociation(product, coverType);

        assertEquals(policyToCover, productToCoverType.findDefaultPolicyCmptTypeAssociation(ipsProject));
        assertEquals(productToCoverType, policyToCover.findDefaultMatchingProductCmptTypeAssociation(ipsProject));

        IProductCmptTypeAssociation subProductToCoverType = newProductCmptTypeAssociation(subProduct, coverType);

        assertNull(subProductToCoverType.findDefaultPolicyCmptTypeAssociation(ipsProject));
    }

    /**
     * Test for FIPS-4966
     */
    @Test
    public void testFindDefaultPolicyCmptTypeAssociation_FindMatchingAssociationForConstrainedAssociation()
            throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Policy", "MyProduct");
        IProductCmptType product = policy.findProductCmptType(ipsProject);
        ProductCmptType subProduct = newProductCmptType(ipsProject, "SubProduct", policy);
        subProduct.setSupertype(product.getQualifiedName());

        PolicyCmptType cover = newPolicyAndProductCmptType(ipsProject, "Coverage", "MyCoverageType");
        IProductCmptType coverType = cover.findProductCmptType(ipsProject);
        ProductCmptType subCoverType = newProductCmptType(ipsProject, "SubCoverType", cover);
        subCoverType.setSupertype(coverType.getQualifiedName());

        IPolicyCmptTypeAssociation policyToCover = newPolicyCmptTypeAssociation(policy, cover);
        newProductCmptTypeAssociation(product, coverType);

        IProductCmptTypeAssociation subProductToSubCoverType = newProductCmptTypeAssociation(subProduct, subCoverType);
        subProductToSubCoverType.setConstrain(true);

        assertThat(subProductToSubCoverType.findDefaultPolicyCmptTypeAssociation(ipsProject), is(policyToCover));
    }

    /**
     * Test for FIPS-4966
     */
    @Test
    public void testFindDefaultPolicyCmptTypeAssociation_FindMatchingAssociationForConstrainedAssociation_twoHierarchyStages()
            throws Exception {
        PolicyCmptType superPolicy = newPolicyAndProductCmptType(ipsProject, "SuperPolicy", "SuperMyProduct");
        IProductCmptType superProduct = superPolicy.findProductCmptType(ipsProject);

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Policy", "MyProduct");
        policy.setSupertype(superPolicy.getQualifiedName());
        IProductCmptType product = policy.findProductCmptType(ipsProject);
        product.setSupertype(superProduct.getQualifiedName());
        ProductCmptType subProduct = newProductCmptType(ipsProject, "SubProduct", policy);
        subProduct.setSupertype(product.getQualifiedName());

        PolicyCmptType superCover = newPolicyAndProductCmptType(ipsProject, "SuperCoverage", "MySuperCoverageType");
        IProductCmptType superCoverType = superCover.findProductCmptType(ipsProject);
        PolicyCmptType cover = newPolicyAndProductCmptType(ipsProject, "Coverage", "MyCoverageType");
        IProductCmptType coverType = cover.findProductCmptType(ipsProject);
        ProductCmptType subCoverType = newProductCmptType(ipsProject, "SubCoverType", cover);
        subCoverType.setSupertype(coverType.getQualifiedName());

        newPolicyCmptTypeAssociation(superPolicy, superCover);
        newProductCmptTypeAssociation(superProduct, superCoverType);

        IPolicyCmptTypeAssociation policyToCover = newPolicyCmptTypeAssociation(policy, cover);
        policyToCover.setConstrain(true);
        IProductCmptTypeAssociation productToCoverType = newProductCmptTypeAssociation(product, coverType);
        productToCoverType.setConstrain(true);

        IProductCmptTypeAssociation subProductToSubCoverType = newProductCmptTypeAssociation(subProduct, subCoverType);
        subProductToSubCoverType.setConstrain(true);

        assertThat(subProductToSubCoverType.findDefaultPolicyCmptTypeAssociation(ipsProject), is(policyToCover));
    }

    /**
     * Test for FIPS-8562
     */
    @Test
    public void testFindDefaultPolicyCmptTypeAssociation_FindMatchingAssociationForConstrainedAssociation_TargetHierarchy()
            throws Exception {
        PolicyCmptType ereignis = newPolicyAndProductCmptType(ipsProject, "Ereignis", "SuperEreignisKonfiguration");
        IProductCmptType superEreignisKonfiguration = ereignis.findProductCmptType(ipsProject);

        superEreignisKonfiguration.setAbstract(true);
        ProductCmptType ereignisKonfiguration = newProductCmptType(ipsProject, "EreignisKonfiguration", ereignis);
        ereignisKonfiguration.setSupertype(superEreignisKonfiguration.getQualifiedName());

        PolicyCmptType lvb = newPolicyAndProductCmptType(ipsProject, "Lvb", "LvbBaustein");
        IProductCmptType lvbBaustein = lvb.findProductCmptType(ipsProject);

        IPolicyCmptTypeAssociation lvbToEreignis = newPolicyCmptTypeAssociation(lvb, ereignis);
        IProductCmptTypeAssociation lvbBausteinToSuperEreignisKonfiguration = newProductCmptTypeAssociation(lvbBaustein,
                superEreignisKonfiguration);

        IProductCmptTypeAssociation lvbBausteinToEreignisKonfiguration = newProductCmptTypeAssociation(lvbBaustein,
                ereignisKonfiguration);

        assertThat(lvbBausteinToSuperEreignisKonfiguration.findDefaultPolicyCmptTypeAssociation(ipsProject),
                is(lvbToEreignis));
        assertThat(lvbBausteinToEreignisKonfiguration.findDefaultPolicyCmptTypeAssociation(ipsProject),
                is(not(lvbToEreignis)));
    }

    /**
     * Test for FIPS-8562
     */
    @Test
    public void testFindDefaultPolicyCmptTypeAssociation_FindMatchingAssociationForConstrainedAssociation_BothSidesHierarchy()
            throws Exception {
        PolicyCmptType superZiel = newPolicyAndProductCmptType(ipsProject, "SuperZiel", "SuperZielKonfiguration");
        IProductCmptType superZielKonfiguration = superZiel.findProductCmptType(ipsProject);
        PolicyCmptType ziel = newPolicyAndProductCmptType(ipsProject, "Ziel", "ZielKonfiguration");
        IProductCmptType zielKonfiguration = ziel.findProductCmptType(ipsProject);
        ziel.setSupertype(superZiel.getQualifiedName());
        zielKonfiguration.setSupertype(superZielKonfiguration.getQualifiedName());

        PolicyCmptType quelle = newPolicyAndProductCmptType(ipsProject, "Quelle", "QuellKonfiguration");
        IProductCmptType quellKonfiguration = quelle.findProductCmptType(ipsProject);

        IPolicyCmptTypeAssociation quelleZuSuperZiel = newPolicyCmptTypeAssociation(quelle, superZiel);
        IProductCmptTypeAssociation quellKonfigZuSuperZielKonfig = newProductCmptTypeAssociation(quellKonfiguration,
                superZielKonfiguration);
        IPolicyCmptTypeAssociation quelleZuZiel = newPolicyCmptTypeAssociation(quelle, ziel);
        IProductCmptTypeAssociation quellKonfigZuZielKonfig = newProductCmptTypeAssociation(quellKonfiguration,
                zielKonfiguration);

        assertThat(quellKonfigZuZielKonfig.findDefaultPolicyCmptTypeAssociation(ipsProject), is(quelleZuZiel));
        assertThat(quellKonfigZuSuperZielKonfig.findDefaultPolicyCmptTypeAssociation(ipsProject),
                is(quelleZuSuperZiel));
    }

    /**
     * Validate constrained association with subclasses on product side only (FIPS-4966)
     */
    @Test
    public void testValidateMatchingAsoociation_FindMatchingAssociationForConstrainedAssociation() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Policy", "MyProduct");
        IProductCmptType product = policy.findProductCmptType(ipsProject);
        ProductCmptType subProduct = newProductCmptType(ipsProject, "SubProduct", policy);
        subProduct.setSupertype(product.getQualifiedName());

        PolicyCmptType cover = newPolicyAndProductCmptType(ipsProject, "Coverage", "MyCoverageType");
        IProductCmptType coverType = cover.findProductCmptType(ipsProject);
        ProductCmptType subCoverType = newProductCmptType(ipsProject, "SubCoverType", cover);
        subCoverType.setSupertype(coverType.getQualifiedName());

        newPolicyCmptTypeAssociation(policy, cover);
        newProductCmptTypeAssociation(product, coverType);

        IProductCmptTypeAssociation subProductToSubCoverType = newProductCmptTypeAssociation(subProduct, subCoverType);
        subProductToSubCoverType.setConstrain(true);

        assertThat(subProductToSubCoverType.validate(ipsProject), isEmpty());
    }

    private IPolicyCmptTypeAssociation newPolicyCmptTypeAssociation(IPolicyCmptType from, IPolicyCmptType to) {
        IPolicyCmptTypeAssociation policyToCover = from.newPolicyCmptTypeAssociation();
        policyToCover.setTarget(to.getQualifiedName());
        policyToCover.setTargetRoleSingular("coverage");
        policyToCover.setTargetRolePlural("coverages");
        return policyToCover;
    }

    private IProductCmptTypeAssociation newProductCmptTypeAssociation(IProductCmptType from, IProductCmptType to) {
        IProductCmptTypeAssociation policyToCover = from.newProductCmptTypeAssociation();
        policyToCover.setTarget(to.getQualifiedName());
        policyToCover.setTargetRoleSingular("coverageType");
        policyToCover.setTargetRolePlural("coverageTypes");
        return policyToCover;
    }

    /**
     * Test scenario described in FIPS-563
     */
    @Test
    public void testFindPossibleMatchingPolicyCmptTypeAssociations() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = policy.findProductCmptType(ipsProject);

        PolicyCmptType versPerson = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "VersPerson");

        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IProductCmptTypeAssociation produktToTarif = newAggregation(produkt, tarif);

        Set<IPolicyCmptTypeAssociation> possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(0, possibleMatchingPolicyCmptTypeAssociations.size());

        IPolicyCmptTypeAssociation policyToVersPerson = newComposition(policy, versPerson);

        possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(0, possibleMatchingPolicyCmptTypeAssociations.size());

        IPolicyCmptTypeAssociation versPersonToTarifvereinbarung = newComposition(versPerson, tarifvereinbarung);

        possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(2, possibleMatchingPolicyCmptTypeAssociations.size());
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(policyToVersPerson));
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(versPersonToTarifvereinbarung));

        versPerson.delete();

        possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(0, possibleMatchingPolicyCmptTypeAssociations.size());
    }

    @Test
    public void testFindPossibleMatchingPolicyCmptTypeAssociations2() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = policy.findProductCmptType(ipsProject);
        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IPolicyCmptTypeAssociation policyToTarifvereinbarung = newComposition(policy, tarifvereinbarung);
        IProductCmptTypeAssociation produktToTarif = newAggregation(produkt, tarif);

        Set<IPolicyCmptTypeAssociation> possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(1, possibleMatchingPolicyCmptTypeAssociations.size());
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(policyToTarifvereinbarung));

        PolicyCmptType versPerson = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "VersPerson");

        possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(1, possibleMatchingPolicyCmptTypeAssociations.size());
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(policyToTarifvereinbarung));

        IPolicyCmptTypeAssociation versPersonToTarifvereinbarung = newComposition(versPerson, tarifvereinbarung);
        IPolicyCmptTypeAssociation policyToVersPerson = newComposition(policy, versPerson);

        possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(3, possibleMatchingPolicyCmptTypeAssociations.size());
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(policyToTarifvereinbarung));
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(policyToVersPerson));
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(versPersonToTarifvereinbarung));

    }

    @Test
    public void testFindPossibleMatchingPolicyCmptTypeAssociations3() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = policy.findProductCmptType(ipsProject);

        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IProductCmptTypeAssociation produktToTarif = newAggregation(produkt, tarif);

        PolicyCmptType vp1 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "VP1");
        PolicyCmptType vp2 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "VP2");

        IPolicyCmptTypeAssociation policyToVp1 = newComposition(policy, vp1);
        IPolicyCmptTypeAssociation vp1ToVp2 = newComposition(vp1, vp2);
        IPolicyCmptTypeAssociation vp2ToTarifvereinbarung = newComposition(vp2, tarifvereinbarung);

        Set<IPolicyCmptTypeAssociation> possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(3, possibleMatchingPolicyCmptTypeAssociations.size());
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(policyToVp1));
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(vp1ToVp2));
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(vp2ToTarifvereinbarung));
    }

    @Test
    public void testFindPossibleMatchingAssociationsCycle() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = policy.findProductCmptType(ipsProject);

        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IProductCmptTypeAssociation produktToTarif = newAggregation(produkt, tarif);

        PolicyCmptType polRef = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "PolRef");

        newComposition(policy, polRef);
        newComposition(polRef, polRef);

        Set<IPolicyCmptTypeAssociation> possiblyMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(0, possiblyMatchingPolicyCmptTypeAssociations.size());
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer#toXml(org.w3c.dom.Document)}
     * .
     */
    @Test
    public void testToXml() {
        association.setTarget("pack1.CoverageType");
        association.setTargetRoleSingular("CoverageType");
        association.setTargetRolePlural("CoverageTypes");
        association.setMinCardinality(2);
        association.setMaxCardinality(4);
        association.setDerivedUnion(true);
        association.setSubsettedDerivedUnion("BaseCoverageType");
        association.setAssociationType(AssociationType.AGGREGATION);
        // Default is true/changing over time
        assertTrue(association.isChangingOverTime());
        association.setChangingOverTime(false);
        // Default is true/visible
        assertTrue(association.isRelevant());
        association.setRelevant(false);

        Element el = association.toXml(newDocument());
        association = productType.newProductCmptTypeAssociation();
        association.initFromXml(el);

        assertEquals(AssociationType.AGGREGATION, association.getAssociationType());
        assertEquals("pack1.CoverageType", association.getTarget());
        assertEquals("CoverageType", association.getTargetRoleSingular());
        assertEquals("CoverageTypes", association.getTargetRolePlural());
        assertEquals(2, association.getMinCardinality());
        assertEquals(4, association.getMaxCardinality());
        assertTrue(association.isDerivedUnion());
        assertEquals("BaseCoverageType", association.getSubsettedDerivedUnion());
        assertFalse(association.isChangingOverTime());
        assertFalse(association.isRelevant());
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer#initFromXml(org.w3c.dom.Element)}
     * .
     */
    @Test
    public void testInitFromXmlElement() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, 0);
        association.initFromXml(el);
        assertEquals(AssociationType.AGGREGATION, association.getAssociationType());
        assertEquals("pack1.CoverageType", association.getTarget());
        assertEquals("CoverageType", association.getTargetRoleSingular());
        assertEquals("CoverageTypes", association.getTargetRolePlural());
        assertEquals(1, association.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, association.getMaxCardinality());
        assertTrue(association.isDerivedUnion());
        assertEquals("BaseCoverageType", association.getSubsettedDerivedUnion());
        assertEquals("blabla", association.getDescriptionText(Locale.US));
        assertFalse(association.isChangingOverTime());
        assertFalse(association.isRelevant());
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer#initFromXml(org.w3c.dom.Element)}
     * .
     */
    @Test
    public void testInitFromXmlElement_WithoutChangingOverTimeProperty() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, 1);
        association.initFromXml(el);
        assertTrue(association.isChangingOverTime());
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAssociation#findTarget(IIpsProject)
     * )} .
     */
    @Test
    public void testFindTarget() {
        association.setTarget("");
        assertNull(association.findTarget(ipsProject));

        association.setTarget("unknown");
        assertNull(association.findTarget(ipsProject));

        association.setTarget(coverageType.getQualifiedName());
        assertEquals(coverageType, association.findTarget(ipsProject));
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAssociation#setTarget(java.lang.String)}
     * .
     */
    @Test
    public void testSetTarget() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_TARGET,
                association, "newTarget");
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAssociation#setTargetRoleSingular(java.lang.String)}
     * .
     */
    @Test
    public void testSetTargetRoleSingular() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR, association, "newRole");
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAssociation#setTargetRolePlural(java.lang.String)}
     * .
     */
    @Test
    public void testSetTargetRolePlural() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL, association, "newRoles");
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAssociation#setMinCardinality(int)}
     * .
     */
    @Test
    public void testSetMinCardinality() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY, association, Integer.valueOf(42));
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAssociation#setMaxCardinality(int)}
     * .
     */
    @Test
    public void testSetMaxCardinality() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY, association, Integer.valueOf(42));
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAssociation#setRelevant(boolean)}
     * .
     */
    @Test
    public void testSetRelevant() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.PROPERTY_RELEVANT, association, false);
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAssociation#isSubsetOfADerivedUnion()}
     * .
     */
    @Test
    public void testIsSubsetOfADerivedUnion() {
        association.setSubsettedDerivedUnion("");
        assertFalse(association.isSubsetOfADerivedUnion());
        association.setSubsettedDerivedUnion("someContainerRelation");
        assertTrue(association.isSubsetOfADerivedUnion());
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAssociation#setSubsettedDerivedUnion(java.lang.String)}
     * .
     */
    @Test
    public void testSetSubsettedDerivedUnion() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.PROPERTY_SUBSETTED_DERIVED_UNION, association, "SomeUnion");
    }

    @Test
    public void testValidate_constrainedNotChangeOverTime() throws Exception {
        ProductCmptType subType = newProductCmptType(ipsProject, "SubType");
        subType.setSupertype(productType.getQualifiedName());
        IProductCmptTypeAssociation subAssociation = subType.newProductCmptTypeAssociation();
        subAssociation.setTargetRoleSingular(association.getTargetRoleSingular());
        subAssociation.setTargetRolePlural(association.getTargetRolePlural());
        subAssociation.setConstrain(true);
        subAssociation.setChangingOverTime(true);
        association.setChangingOverTime(false);

        MessageList messageList = subAssociation.validate(ipsProject);

        assertNotNull(
                messageList.getMessageByCode(IProductCmptTypeAssociation.MSGCODE_CONSTRAINED_CHANGEOVERTIME_MISMATCH));
    }

    @Test
    public void testValidate_constrainedChangeOverTime() throws Exception {
        ProductCmptType subType = newProductCmptType(ipsProject, "SubType");
        subType.setSupertype(productType.getQualifiedName());
        IProductCmptTypeAssociation subAssociation = subType.newProductCmptTypeAssociation();
        subAssociation.setTargetRoleSingular(association.getTargetRoleSingular());
        subAssociation.setTargetRolePlural(association.getTargetRolePlural());
        subAssociation.setConstrain(true);
        subAssociation.setChangingOverTime(false);
        association.setChangingOverTime(true);

        MessageList messageList = subAssociation.validate(ipsProject);

        assertNotNull(
                messageList.getMessageByCode(IProductCmptTypeAssociation.MSGCODE_CONSTRAINED_CHANGEOVERTIME_MISMATCH));
    }

    @Test
    public void testValidate_constrainedMatchChangeOverTime() throws Exception {
        ProductCmptType subType = newProductCmptType(ipsProject, "SubType");
        subType.setSupertype(productType.getQualifiedName());
        IProductCmptTypeAssociation subAssociation = subType.newProductCmptTypeAssociation();
        subAssociation.setTargetRoleSingular(association.getTargetRoleSingular());
        subAssociation.setTargetRolePlural(association.getTargetRolePlural());
        subAssociation.setConstrain(true);
        subAssociation.setChangingOverTime(true);
        association.setChangingOverTime(true);

        MessageList messageList = subAssociation.validate(ipsProject);

        assertNull(
                messageList.getMessageByCode(IProductCmptTypeAssociation.MSGCODE_CONSTRAINED_CHANGEOVERTIME_MISMATCH));
    }

    @Test
    public void testValidate_constrainedMatchNotChangeOverTime() throws Exception {
        ProductCmptType subType = newProductCmptType(ipsProject, "SubType");
        subType.setSupertype(productType.getQualifiedName());
        IProductCmptTypeAssociation subAssociation = subType.newProductCmptTypeAssociation();
        subAssociation.setTargetRoleSingular(association.getTargetRoleSingular());
        subAssociation.setTargetRolePlural(association.getTargetRolePlural());
        subAssociation.setConstrain(true);
        subAssociation.setChangingOverTime(false);
        association.setChangingOverTime(false);

        MessageList messageList = subAssociation.validate(ipsProject);

        assertNull(
                messageList.getMessageByCode(IProductCmptTypeAssociation.MSGCODE_CONSTRAINED_CHANGEOVERTIME_MISMATCH));
    }

    @Test
    public void testValidateChangingOverTime_typeDoesNotAcceptChangingOverTime() {
        association.setTargetRoleSingular("targetRoleSingular");
        productType.setChangingOverTime(true);
        association.setChangingOverTime(false);

        MessageList ml = association.validate(association.getIpsProject());
        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productType.setChangingOverTime(true);
        association.setChangingOverTime(true);

        ml = association.validate(association.getIpsProject());
        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productType.setChangingOverTime(false);
        association.setChangingOverTime(false);

        ml = association.validate(association.getIpsProject());
        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        association.setChangingOverTime(false);
        association.setChangingOverTime(true);

        ml = association.validate(association.getIpsProject());
        assertNotNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testChangingOverTime_default() {
        productType.setChangingOverTime(false);
        association = productType.newProductCmptTypeAssociation();

        assertFalse(association.isChangingOverTime());

        productType.setChangingOverTime(true);
        association = productType.newProductCmptTypeAssociation();

        assertTrue(association.isChangingOverTime());
    }

    @Test
    public void testFIPS6441() {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Policy", "PolicyType");
        IProductCmptType policyType = policy.findProductCmptType(ipsProject);

        PolicyCmptType contract = newPolicyAndProductCmptType(ipsProject, "Contract", "ContractType");
        IProductCmptType contractType = contract.findProductCmptType(ipsProject);

        IPolicyCmptTypeAssociation assoc1 = (IPolicyCmptTypeAssociation)policy.newAssociation();
        assoc1.setTargetRoleSingular("assoc1");
        assoc1.setTargetRolePlural("assoc1s");
        assoc1.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assoc1.setTarget(contract.getQualifiedName());

        IPolicyCmptTypeAssociation assoc2 = (IPolicyCmptTypeAssociation)contract.newAssociation();
        assoc2.setTargetRoleSingular("assoc2");
        assoc2.setTargetRolePlural("assoc2s");
        assoc2.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assoc2.setTarget(policy.getQualifiedName());

        assoc1.setInverseAssociation(assoc2.getName());
        assoc2.setInverseAssociation(assoc1.getName());

        IAssociation assoc3 = contractType.newAssociation();
        assoc3.setTargetRoleSingular("assoc3");
        assoc3.setTargetRolePlural("assoc3s");
        assoc3.setAssociationType(AssociationType.AGGREGATION);
        assoc3.setTarget(policyType.getQualifiedName());

        IAssociation assoc4 = contract.newAssociation();
        assoc4.setTargetRoleSingular("assoc4");
        assoc4.setTargetRolePlural("assoc4s");
        assoc4.setAssociationType(AssociationType.ASSOCIATION);
        assoc4.setTarget(policy.getQualifiedName());

        for (IAssociation association : Arrays.asList(assoc1, assoc2, assoc3, assoc4)) {
            // No NullPointerException should be thrown
            MessageList messages = association.validate(association.getIpsProject());
            assertNotNull(messages);
        }
    }

    @Test
    public void testFIPS9915() {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Policy", "PolicyType");
        IProductCmptType policyType = policy.findProductCmptType(ipsProject);
        PolicyCmptType contract = newPolicyAndProductCmptType(ipsProject, "Contract", "ContractType");
        IProductCmptType contractType = contract.findProductCmptType(ipsProject);

        IAssociation assoc = policy.newAssociation();
        assoc.setTargetRoleSingular("assoc");
        assoc.setTargetRoleSingular("assocs");
        assoc.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assoc.setTarget(contract.getQualifiedName());

        IAssociation assocType = createProdctCmptTypeAssociation(policyType, contractType);
        ViewModelSimulation assocViewModel = new ViewModelSimulation((IProductCmptTypeAssociation)assocType);

        // simulate a fresh read-in, with delete and creating the "same" association
        assocType.delete();
        IAssociation newAssocType = createProdctCmptTypeAssociation(policyType, contractType);

        // fails if this == association
        assertThat(assocViewModel.association.findMatchingAssociation(), is(assoc));
        // tests the equals methods
        assertThat(assocViewModel.association, is(newAssocType));
        // make sure not to ask IpsObjectPart because it does: id.equals(o.id)
        assertThat(assocViewModel.association.getId(), is(not(newAssocType.getId())));
    }

    private IAssociation createProdctCmptTypeAssociation(IProductCmptType policyType, IProductCmptType contractType) {
        IAssociation assocType = policyType.newAssociation();
        assocType.setTargetRoleSingular("assocType");
        assocType.setTargetRolePlural("assocType");
        assocType.setAssociationType(AssociationType.AGGREGATION);
        assocType.setTarget(contractType.getQualifiedName());
        return assocType;
    }

    private static class ViewModelSimulation {
        private final IProductCmptTypeAssociation association;

        public ViewModelSimulation(IProductCmptTypeAssociation association) {
            this.association = association;
        }

        @Override
        public int hashCode() {
            return Objects.hash(association);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            ViewModelSimulation other = (ViewModelSimulation)obj;
            return Objects.equals(association, other.association);
        }

    }
}
