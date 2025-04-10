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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyTreeSettingsOperation.DeepCopyTreeLoadSettingsOperation;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyTreeSettingsOperation.DeepCopyTreeSaveSettingsOperation;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.internal.tablestructure.TableStructure;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.AssociationType;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

public class DeepCopyTreeSettingsOperationTest extends AbstractIpsPluginTest {

    private DeepCopyPresentationModel presentationModel;
    private IIpsProject ipsProject;

    private ProductCmpt product;

    private IProductCmptLink grundLink;
    private ProductCmpt grundDeckung;

    private IProductCmptLink zusatzLink;
    private ProductCmpt zusatzDeckug;

    private ITableContentUsage tableContentUsage;
    private TableContents tableContents;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("AProject");
        ProductCmptType productCmpt = newProductCmptType(ipsProject, "some.ProductType");
        product = newProductCmpt(productCmpt, "some.Product 2025-01");

        ProductCmptType grundDeckungCmpt = newProductCmptType(ipsProject, "some.GrundDeckungType");
        grundDeckung = newProductCmpt(grundDeckungCmpt, "some.GrundDeckung 2025-01");

        ProductCmptType zusatzDeckugCmpt = newProductCmptType(ipsProject, "some.ZusatzdeckungType");
        zusatzDeckug = newProductCmpt(zusatzDeckugCmpt, "some.Zusatzdeckung 2025-01");

        IProductCmptTypeAssociation grundAssociation = productCmpt.newProductCmptTypeAssociation();
        grundAssociation.setAssociationType(AssociationType.AGGREGATION);
        grundAssociation.setTarget(grundDeckungCmpt.getQualifiedName());
        grundAssociation.setTargetRoleSingular(grundDeckungCmpt.getName());

        IProductCmptTypeAssociation zusatzAssociation = productCmpt.newProductCmptTypeAssociation();
        zusatzAssociation.setAssociationType(AssociationType.AGGREGATION);
        zusatzAssociation.setTarget(zusatzDeckugCmpt.getQualifiedName());
        zusatzAssociation.setTargetRoleSingular(zusatzDeckugCmpt.getName());

        grundLink = product.newLink(grundAssociation);
        grundLink.setTarget(grundDeckung.getQualifiedName());
        grundLink.setAssociation(grundAssociation.getName());

        zusatzLink = product.newLink(zusatzAssociation);
        zusatzLink.setTarget(zusatzDeckug.getQualifiedName());
        zusatzLink.setAssociation(zusatzAssociation.getName());

        TableStructure tableStructure = newTableStructure(ipsProject, "some.Table");
        tableContents = newTableContents(ipsProject, "some.Table 2025-01");
        tableContents.setTableStructure(tableStructure.getQualifiedName());

        ITableStructureUsage tableStructureUsage = grundDeckungCmpt.newTableStructureUsage();
        tableStructureUsage.setRoleName("atable");
        tableContentUsage = grundDeckung.getLatestProductCmptGeneration()
                .newTableContentUsage(tableStructureUsage);
        tableContentUsage.setTableContentName("some.Table 2025-01");

        presentationModel = new DeepCopyPresentationModel(product.getProductCmptGeneration(0));
        IIpsPackageFragmentRoot ipsPackageFragmentRoot = newIpsPackageFragmentRoot(ipsProject, null, "root");
        presentationModel.setTargetPackageRoot(ipsPackageFragmentRoot);
    }

    @Test
    public void testSaveOperation() throws IOException {
        DeepCopyTreeSettingsOperation saveOp = new DeepCopyTreeSaveSettingsOperation(presentationModel);
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

        // tempFile.delete();
    }

    @Test
    public void testNoChildSaveOperation() throws IOException {
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "empty.ProductType");
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "empty.Product");
        DeepCopyPresentationModel model = new DeepCopyPresentationModel(productCmpt.getProductCmptGeneration(0));
        DeepCopyTreeSettingsOperation saveOp = new DeepCopyTreeSaveSettingsOperation(model);
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

    @Test
    public void testLoadOperation() {
        File jsonFile = readJson("allOk.json");

        DeepCopyTreeSettingsOperation loadOp = new DeepCopyTreeLoadSettingsOperation(presentationModel);
        loadOp.performOperation(jsonFile.getAbsolutePath(), loadOp.createGson());

        Map<IProductCmpt, Map<IIpsObjectPart, LinkStatus>> treeStatus = presentationModel.getTreeStatus()
                .getTreeStatus();

        assertThat(treeStatus.get(null).get(null),
                isLinkStatus(null, product, true, CopyOrLink.COPY));

        assertThat(treeStatus.get(product).get(zusatzLink),
                isLinkStatus(zusatzLink, zusatzDeckug, false, CopyOrLink.COPY));
        assertThat(treeStatus.get(product).get(grundLink),
                isLinkStatus(grundLink, grundDeckung, true, CopyOrLink.COPY));

        assertThat(treeStatus.get(grundDeckung).get(tableContentUsage),
                isLinkStatus(tableContentUsage, tableContents, true, CopyOrLink.LINK));
    }

    @Test
    public void testEmptyLoadOperation() {
        File jsonFile = readJson("empty.json");
        DeepCopyTreeSettingsOperation loadOp = new DeepCopyTreeLoadSettingsOperation(presentationModel);
        loadOp.performOperation(jsonFile.getAbsolutePath(), loadOp.createGson());

        Map<IProductCmpt, Map<IIpsObjectPart, LinkStatus>> treeStatus = presentationModel.getTreeStatus()
                .getTreeStatus();
        assertThat(treeStatus.keySet(), is(empty()));
    }

    @Test
    public void testMissingObjLoadOperation() {
        File jsonFile = readJson("missing.json");
        DeepCopyTreeSettingsOperation loadOp = new DeepCopyTreeLoadSettingsOperation(presentationModel);
        loadOp.performOperation(jsonFile.getAbsolutePath(), loadOp.createGson());

        Map<IProductCmpt, Map<IIpsObjectPart, LinkStatus>> treeStatus = presentationModel.getTreeStatus()
                .getTreeStatus();

        assertThat(treeStatus.get(null).get(null),
                isLinkStatus(null, product, true, CopyOrLink.COPY));

        assertThat(treeStatus.get(product).get(zusatzLink),
                isLinkStatus(zusatzLink, zusatzDeckug, false, CopyOrLink.COPY));

        assertThat(treeStatus.get(grundDeckung).get(tableContentUsage),
                isLinkStatus(tableContentUsage, tableContents, true, CopyOrLink.LINK));
    }

    @Test
    public void testOldVersionLoadOperation() {
        File jsonFile = readJson("oldVersion.json");

        DeepCopyTreeSettingsOperation loadOp = new DeepCopyTreeLoadSettingsOperation(presentationModel);
        loadOp.performOperation(jsonFile.getAbsolutePath(), loadOp.createGson());

        Map<IProductCmpt, Map<IIpsObjectPart, LinkStatus>> treeStatus = presentationModel.getTreeStatus()
                .getTreeStatus();

        assertThat(treeStatus.get(null).get(null),
                isLinkStatus(null, product, true, CopyOrLink.COPY));

        assertThat(treeStatus.get(product).get(zusatzLink),
                isLinkStatus(zusatzLink, zusatzDeckug, false, CopyOrLink.COPY));
        assertThat(treeStatus.get(product).get(grundLink),
                isLinkStatus(grundLink, grundDeckung, true, CopyOrLink.COPY));

        assertThat(treeStatus.get(grundDeckung).get(tableContentUsage),
                isLinkStatus(tableContentUsage, tableContents, true, CopyOrLink.LINK));
    }

    /**
     * checks if a LinkStatus object has the same properties as the given
     */
    private static Matcher<LinkStatus> isLinkStatus(IIpsObjectPart ipsObjectPart,
            IIpsObject target,
            boolean checked,
            CopyOrLink copyOrLink) {
        return new BaseMatcher<>() {

            @Override
            public boolean matches(Object actual) {
                if (actual instanceof LinkStatus link) {
                    return Objects.equals(link.getIpsObjectPart(), ipsObjectPart)
                            && Objects.equals(link.getTarget(), target)
                            && Objects.equals(link.getCopyOrLink(), copyOrLink)
                            && link.isChecked() == checked;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format(
                        "a Linkstatus with: ipsObjectPart \"%s\", target \"%s\", checked \"%s\", copyOrLink \"%s\"",
                        ipsObjectPart, target, checked, copyOrLink));
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                if (item instanceof LinkStatus link) {
                    description.appendText(
                            String.format("was ipsObjectPart \"%s\", target \"%s\", checked \"%s\", copyOrLink \"%s\"",
                                    link.getIpsObjectPart(), link.getTarget(), link.isChecked(), link.getCopyOrLink()));
                } else {
                    description.appendText("was no a Linkstatus \"" + String.valueOf(item) + "\"");
                }
            }
        };
    }

    private File readJson(String fileName) {
        String packageFolder = this.getClass().getPackageName().replace('.', '/');
        return new File(String.format("./src/%s/%s", packageFolder, fileName));
    }
}
