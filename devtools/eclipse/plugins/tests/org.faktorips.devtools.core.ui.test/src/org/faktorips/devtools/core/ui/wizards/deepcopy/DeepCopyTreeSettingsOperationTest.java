/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyTreeSettingsOperation.DeepCopyTreeSaveSettingsOperation;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.internal.tablestructure.TableStructure;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.AssociationType;
import org.junit.Before;
import org.junit.Test;

public class DeepCopyTreeSettingsOperationTest extends AbstractIpsPluginTest {

    private DeepCopyPresentationModel presentationModel;
    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("AProject");
        ProductCmptType productCmpt = newProductCmptType(ipsProject, "some.ProductType");
        ProductCmpt product = newProductCmpt(productCmpt, "some.Product 2025-01");

        ProductCmptType grundDeckungCmpt = newProductCmptType(ipsProject, "some.GrundDeckungType");
        ProductCmpt grundDeckung = newProductCmpt(grundDeckungCmpt, "some.GrundDeckung 2025-01");

        ProductCmptType zusatzDeckugCmpt = newProductCmptType(ipsProject, "some.ZusatzdeckungType");
        ProductCmpt zusatzDeckug = newProductCmpt(zusatzDeckugCmpt, "some.Zusatzdeckung 2025-01");

        IProductCmptTypeAssociation grundAssociation = productCmpt.newProductCmptTypeAssociation();
        grundAssociation.setAssociationType(AssociationType.AGGREGATION);
        grundAssociation.setTarget(grundDeckungCmpt.getQualifiedName());
        grundAssociation.setTargetRoleSingular(grundDeckungCmpt.getName());

        IProductCmptTypeAssociation zusatzAssociation = productCmpt.newProductCmptTypeAssociation();
        zusatzAssociation.setAssociationType(AssociationType.AGGREGATION);
        zusatzAssociation.setTarget(zusatzDeckugCmpt.getQualifiedName());
        zusatzAssociation.setTargetRoleSingular(zusatzDeckugCmpt.getName());

        IProductCmptLink grundLink = product.newLink(grundAssociation);
        grundLink.setTarget(grundDeckung.getQualifiedName());
        grundLink.setAssociation(grundAssociation.getName());

        IProductCmptLink zusatzLink = product.newLink(zusatzAssociation);
        zusatzLink.setTarget(zusatzDeckug.getQualifiedName());
        zusatzLink.setAssociation(zusatzAssociation.getName());

        TableStructure tableStructure = newTableStructure(ipsProject, "some.Table");
        TableContents tableContents = newTableContents(ipsProject, "some.Table 2025-01");
        tableContents.setTableStructure(tableStructure.getQualifiedName());

        ITableStructureUsage tableStructureUsage = grundDeckungCmpt.newTableStructureUsage();
        tableStructureUsage.setRoleName("atable");
        ITableContentUsage tableContentUsage = grundDeckung.getLatestProductCmptGeneration()
                .newTableContentUsage(tableStructureUsage);
        tableContentUsage.setTableContentName("some.Table 2025-01");

        presentationModel = new DeepCopyPresentationModel(product.getProductCmptGeneration(0));
        IIpsPackageFragmentRoot ipsPackageFragmentRoot = newIpsPackageFragmentRoot(ipsProject, null, "root");
        presentationModel.setTargetPackageRoot(ipsPackageFragmentRoot);
    }

    @Test
    public void testSaveOperation() throws IOException {
        DeepCopyTreeSaveSettingsOperation saveOp = new DeepCopyTreeSaveSettingsOperation(presentationModel);
        File tempFile = File.createTempFile("testJson", ".json");

        saveOp.performOperation(tempFile.getAbsolutePath(), saveOp.createGson());

        String json = Files.readString(tempFile.toPath());
        assertThat(json, is("""
                {
                  "null": [
                    {
                      "target": "some.Product 2025-01",
                      "checked": true,
                      "copyOrLink": "COPY"
                    }
                  ],
                  "some.GrundDeckung 2025-01": [
                    {
                      "linkType": "org.faktorips.devtools.model.internal.productcmpt.TableContentUsage",
                      "association": "atable",
                      "target": "some.Table 2025-01",
                      "checked": true,
                      "copyOrLink": "COPY"
                    }
                  ],
                  "some.Product 2025-01": [
                    {
                      "linkType": "org.faktorips.devtools.model.internal.productcmpt.ProductCmptLink",
                      "association": "GrundDeckungType",
                      "target": "some.GrundDeckung 2025-01",
                      "checked": true,
                      "copyOrLink": "COPY"
                    },
                    {
                      "linkType": "org.faktorips.devtools.model.internal.productcmpt.ProductCmptLink",
                      "association": "ZusatzdeckungType",
                      "target": "some.Zusatzdeckung 2025-01",
                      "checked": true,
                      "copyOrLink": "COPY"
                    }
                  ]
                }"""));

        tempFile.delete();
    }

    @Test
    public void testNoChildSaveOperation() throws IOException {
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "empty.ProductType");
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "empty.Product");
        DeepCopyPresentationModel model = new DeepCopyPresentationModel(productCmpt.getProductCmptGeneration(0));
        DeepCopyTreeSaveSettingsOperation saveOp = new DeepCopyTreeSaveSettingsOperation(model);
        File tempFile = File.createTempFile("testJson", ".json");

        saveOp.performOperation(tempFile.getAbsolutePath(), saveOp.createGson());

        String json = Files.readString(tempFile.toPath());
        assertThat(json, is("""
                {
                  "null": [
                    {
                      "target": "empty.Product",
                      "checked": true,
                      "copyOrLink": "COPY"
                    }
                  ]
                }"""));

        tempFile.delete();
    }
}
