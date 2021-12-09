/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class BigModelPerformanceTest extends AbstractStdBuilderTest {

    // a package in the base, lob and prod packages is created for every layer
    private static final int NUMBER_OF_LAYERS = 8;
    private static final int NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER = 6;
    private static final int NUMBER_OF_ATTRIBUTES_PER_CMPTTYPE = 10;
    private static final int NUMBER_OF_PRODUCTS_PER_PRODUCTCMPTTYPE = 10;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IPolicyCmptType[][] basePolicyCmptTypes = new IPolicyCmptType[NUMBER_OF_LAYERS][NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER];
        IProductCmptType[][] baseProductCmptTypes = new IProductCmptType[NUMBER_OF_LAYERS][NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER];
        for (int i = 0; i < NUMBER_OF_LAYERS; i++) {
            for (int j = 0; j < NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER; j++) {
                basePolicyCmptTypes[i][j] = newPolicyAndProductCmptType(ipsProject, "base.l" + i + ".V_" + i + "_" + j,
                        "base.l" + i + ".P_" + i + "_" + j);
                baseProductCmptTypes[i][j] = basePolicyCmptTypes[i][j].findProductCmptType(ipsProject);
                for (int k = 0; k <= NUMBER_OF_ATTRIBUTES_PER_CMPTTYPE; k++) {
                    ValueDatatype datatype = ipsProject.getIpsModel().getPredefinedValueDatatypes()[k];
                    IPolicyCmptTypeAttribute policyAttribute = basePolicyCmptTypes[i][j]
                            .newPolicyCmptTypeAttribute("va_" + i + "_" + j + "_" + k);
                    policyAttribute.setDatatype(datatype.getQualifiedName());
                    policyAttribute.setValueSetConfiguredByProduct(true);
                    policyAttribute.setDefaultValue(datatype.getDefaultValue());

                    IProductCmptTypeAttribute productAttribute = baseProductCmptTypes[i][j]
                            .newProductCmptTypeAttribute("pa_" + i + "_" + j + "_" + k);
                    productAttribute
                            .setDatatype(datatype.getQualifiedName());
                    productAttribute.setDefaultValue(datatype.getDefaultValue());

                }
            }
        }
        for (int i = 0; i < NUMBER_OF_LAYERS - 1; i++) {
            for (int j = 0; j < NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER; j++) {
                for (int k = 0; k < NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER; k++) {
                    IPolicyCmptTypeAssociation composition = newComposition(basePolicyCmptTypes[i][j],
                            basePolicyCmptTypes[i + 1][k]);
                    IProductCmptTypeAssociation aggregation = newAggregation(baseProductCmptTypes[i][j],
                            baseProductCmptTypes[i + 1][k]);
                    composition.setMatchingAssociationName(aggregation.getName());
                    composition.setMatchingAssociationSource(baseProductCmptTypes[i][j].getQualifiedName());
                }
            }
        }
        for (int i = 1; i < NUMBER_OF_LAYERS; i++) {
            for (int j = 0; j < NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER; j++) {
                for (int k = i + 2; k < NUMBER_OF_LAYERS; k++) {
                    IPolicyCmptTypeAssociation association = newAssociation(basePolicyCmptTypes[i][j],
                            basePolicyCmptTypes[k][j]);
                    IProductCmptTypeAssociation aggregation = newAggregation(baseProductCmptTypes[i][j],
                            baseProductCmptTypes[k][j]);
                    association.setMatchingAssociationName(aggregation.getName());
                    association.setMatchingAssociationSource(baseProductCmptTypes[i][j].getQualifiedName());

                }
            }
        }
        for (int i = 0; i < NUMBER_OF_LAYERS; i++) {
            for (int j = 0; j < NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER; j++) {
                basePolicyCmptTypes[i][j].getIpsSrcFile().save(true, null);
                baseProductCmptTypes[i][j].getIpsSrcFile().save(true, null);
            }
        }
        IPolicyCmptType[][] lobPolicyCmptTypes = new IPolicyCmptType[NUMBER_OF_LAYERS][NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER];
        IProductCmptType[][] lobProductCmptTypes = new IProductCmptType[NUMBER_OF_LAYERS][NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER];
        for (int i = 0; i < NUMBER_OF_LAYERS; i++) {
            for (int j = 0; j < NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER; j++) {
                lobPolicyCmptTypes[i][j] = newPolicyAndProductCmptType(ipsProject, "lob.l" + i + ".LV_" + i + "_" + j,
                        "lob.l" + i + ".LP_" + i + "_" + j, false);
                lobPolicyCmptTypes[i][j].setSupertype(basePolicyCmptTypes[i][j].getQualifiedName());
                lobProductCmptTypes[i][j] = lobPolicyCmptTypes[i][j].findProductCmptType(ipsProject);
                lobProductCmptTypes[i][j].setSupertype(baseProductCmptTypes[i][j].getQualifiedName());
                lobProductCmptTypes[i][j].getIpsSrcFile().save(true, null);
                for (int k = 0; k <= NUMBER_OF_ATTRIBUTES_PER_CMPTTYPE; k++) {
                    ValueDatatype datatype = ipsProject.getIpsModel().getPredefinedValueDatatypes()[k];
                    IPolicyCmptTypeAttribute policyAttribute = lobPolicyCmptTypes[i][j]
                            .newPolicyCmptTypeAttribute("lobva_" + i + "_" + j + "_" + k);
                    policyAttribute.setDatatype(datatype.getQualifiedName());
                    policyAttribute.setValueSetConfiguredByProduct(true);
                    policyAttribute.setDefaultValue(datatype.getDefaultValue());

                    IProductCmptTypeAttribute productAttribute = lobProductCmptTypes[i][j]
                            .newProductCmptTypeAttribute("lobpa_" + i + "_" + j + "_" + k);
                    productAttribute.setDatatype(datatype.getQualifiedName());
                    productAttribute.setDefaultValue(datatype.getDefaultValue());
                }
            }
        }
        for (int i = 0; i < NUMBER_OF_LAYERS - 1; i++) {
            for (int j = 0; j < NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER; j++) {
                for (int k = 0; k < NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER; k++) {
                    IPolicyCmptType from = lobPolicyCmptTypes[i][j];
                    IPolicyCmptType to = lobPolicyCmptTypes[i + 1][k];
                    IPolicyCmptTypeAssociation master2detail = from.newPolicyCmptTypeAssociation();
                    master2detail.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
                    master2detail.setTarget(to.getQualifiedName());
                    master2detail.setTargetRoleSingular("Single" + to.getUnqualifiedName());
                    master2detail.setMinCardinality(0);
                    master2detail.setMaxCardinality(1);

                    IPolicyCmptTypeAssociation detail2master = to.newPolicyCmptTypeAssociation();
                    detail2master.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
                    detail2master.setTarget(from.getQualifiedName());
                    detail2master.setTargetRoleSingular("the" + from.getUnqualifiedName());
                    detail2master.setMinCardinality(1);
                    detail2master.setMaxCardinality(1);

                    // inverse settings
                    master2detail.setInverseAssociation(detail2master.getName());
                    detail2master.setInverseAssociation(master2detail.getName());

                    IProductCmptType from1 = lobProductCmptTypes[i][j];
                    IProductCmptType to1 = lobProductCmptTypes[i + 1][k];
                    IProductCmptTypeAssociation aggregation = from1.newProductCmptTypeAssociation();
                    aggregation.setAssociationType(AssociationType.AGGREGATION);
                    aggregation.setTarget(to1.getQualifiedName());
                    aggregation.setTargetRoleSingular("Single" + to1.getUnqualifiedName());
                    aggregation.setMinCardinality(0);
                    aggregation.setMaxCardinality(1);

                    master2detail.setMatchingAssociationName(aggregation.getName());
                    master2detail.setMatchingAssociationSource(lobProductCmptTypes[i][j].getQualifiedName());
                }
            }
        }
        for (int i = 1; i < NUMBER_OF_LAYERS; i++) {
            for (int j = 0; j < NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER; j++) {
                for (int k = i + 2; k < NUMBER_OF_LAYERS; k++) {
                    IPolicyCmptType from = lobPolicyCmptTypes[i][j];
                    IPolicyCmptType to = lobPolicyCmptTypes[k][j];
                    IPolicyCmptTypeAssociation association = from.newPolicyCmptTypeAssociation();
                    association.setAssociationType(AssociationType.ASSOCIATION);
                    association.setTarget(to.getQualifiedName());
                    association.setTargetRoleSingular("aSingle_" + to.getUnqualifiedName());
                    association.setMinCardinality(0);
                    association.setMaxCardinality(1);

                    IProductCmptType from1 = lobProductCmptTypes[i][j];
                    IProductCmptType to1 = lobProductCmptTypes[k][j];
                    IProductCmptTypeAssociation aggregation = from1.newProductCmptTypeAssociation();
                    aggregation.setAssociationType(AssociationType.AGGREGATION);
                    aggregation.setTarget(to1.getQualifiedName());
                    aggregation.setTargetRoleSingular("aSingle_" + to1.getUnqualifiedName());
                    aggregation.setMinCardinality(0);
                    aggregation.setMaxCardinality(1);

                    association.setMatchingAssociationName(aggregation.getName());
                    association.setMatchingAssociationSource(lobProductCmptTypes[i][j].getQualifiedName());

                }
            }
        }
        for (int i = 0; i < NUMBER_OF_LAYERS; i++) {
            for (int j = 0; j < NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER; j++) {
                lobPolicyCmptTypes[i][j].getIpsSrcFile().save(true, null);
                lobProductCmptTypes[i][j].getIpsSrcFile().save(true, null);
            }
        }
        for (int i = 0; i < NUMBER_OF_LAYERS; i++) {
            for (int j = 0; j < NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER; j++) {
                for (int k = 0; k < NUMBER_OF_PRODUCTS_PER_PRODUCTCMPTTYPE * (Math.ceil(Math.sqrt(i)) + 1); k++) {
                    ProductCmpt productCmpt = newProductCmpt(lobProductCmptTypes[i][j],
                            "prod.l" + i + ".P" + i + "_" + j + "_" + k
                                    + " " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
                    productCmpt.fixAllDifferencesToModel(ipsProject);
                }
            }
        }
        for (int i = 0; i < NUMBER_OF_LAYERS; i++) {
            for (int j = 0; j < NUMBER_OF_CMPTTYPE_PAIRS_PER_LAYER; j++) {
                IIpsSrcFile[] productCmptSrcFiles = ipsProject.findAllProductCmptSrcFiles(lobProductCmptTypes[i][j],
                        false);
                for (IIpsSrcFile productCmptSrcFile : productCmptSrcFiles) {
                    IProductCmptGeneration gen = ((ProductCmpt)productCmptSrcFile.getIpsObject())
                            .getLatestProductCmptGeneration();
                    for (IAssociation association : lobProductCmptTypes[i][j].findAllAssociations(ipsProject)) {
                        IProductCmptTypeAssociation productCmptTypeAssociation = (IProductCmptTypeAssociation)association;
                        IProductCmptType targetProductCmptType = productCmptTypeAssociation
                                .findTargetProductCmptType(ipsProject);
                        IIpsSrcFile[] targetProductCmptSrcFiles = ipsProject
                                .findAllProductCmptSrcFiles(targetProductCmptType, true);
                        if (productCmptTypeAssociation.is1To1()) {
                            IProductCmptLink link = gen.newLink(productCmptTypeAssociation);
                            link.setTarget(targetProductCmptSrcFiles[0].getQualifiedNameType().getName());
                        } else {
                            for (IIpsSrcFile targetProductCmptSrcFile : targetProductCmptSrcFiles) {
                                IProductCmptLink link = gen.newLink(productCmptTypeAssociation);
                                link.setTarget(targetProductCmptSrcFile.getQualifiedNameType().getName());
                            }
                        }
                    }
                    gen.getIpsSrcFile().save(true, null);
                }
            }
        }
    }

    @Override
    @After
    public void tearDown() throws Exception {
        // For manual tests, set a breakpoint here and copy the project from the junit-workspace.
        // Since the generated .ipsproject doesn't work with the created component types, it has
        // to be recreated. Delete it and the FIPS entries from .project, and re-add the
        // FIPS-nature. English then has to be added as a supported language.
        super.tearDown();
    }

    // Remove @Ignore for performance tests. We don't want it to run in the regular build.
    @Ignore
    @Test
    public void testPerformance() throws CoreRuntimeException {
        int warmup = 3;
        int n = 10;
        long average = 0;
        for (int i = 0; i < warmup + n; i++) {
            long start = System.nanoTime();
            ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
            long end = System.nanoTime();
            long duration = (end - start) / 1_000_000;
            if (i < warmup) {
                System.out.print("WARMUP ");
            } else {
                average += duration;
            }
            System.out.println("Clean Build took " + duration + "ms");
        }
        System.out.println("AVERAGE clean Build took " + average / n + "ms");
    }

}
