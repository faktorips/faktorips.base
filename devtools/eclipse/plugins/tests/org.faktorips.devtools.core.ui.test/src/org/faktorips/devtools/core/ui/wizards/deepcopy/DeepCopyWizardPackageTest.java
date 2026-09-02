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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.OptionalInt;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.AssociationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
public class DeepCopyWizardPackageTest extends AbstractIpsPluginTest {

    private IProductCmpt inside;
    private IProductCmpt middle;
    private ITableStructureUsage tableStructureUsage;
    private IIpsProject project;
    private List<Runnable> cleanups = new LinkedList<>();
    private GregorianCalendar originalDefaultValidityDate;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        originalDefaultValidityDate = IpsUIPlugin.getDefault().getDefaultValidityDate();

        project = super.newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(project, "BaseType");

        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setTarget("SomeOtherType");
        association.setTargetRoleSingular("RoleName");

        tableStructureUsage = productCmptType.newTableStructureUsage();
        tableStructureUsage.setRoleName("tableRoleName");

        newProductCmpt(productCmptType, "Outside");
        middle = newProductCmpt(productCmptType, "one.Middle");
        inside = newProductCmpt(productCmptType, "one.two.Inside");

        newTableContents(project, "tableContentsWithoutKindId");
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        for (ListIterator<Runnable> iter = cleanups.listIterator(); iter.hasNext();) {
            Runnable cleanup = iter.next();
            cleanup.run();
            iter.remove();
        }
        IpsUIPlugin.getDefault().setDefaultValidityDate(originalDefaultValidityDate);
        super.tearDown();
    }

    @Test
    public void testGetPackage() throws Exception {
        SourcePage page = getSourcePageFor(inside);
        assertEquals(inside.getIpsPackageFragment(), page.getTargetPackage());
    }

    @Test
    public void testGetPackage_LinkedRoleOutsideRootPackage_DoesNotExpandDefaultPackage() throws Exception {
        IProductCmptGeneration gen = inside.getProductCmptGeneration(0);
        IProductCmptLink link = gen.newLink("RoleName");
        link.setTarget(middle.getQualifiedName());
        inside.getIpsSrcFile().save(null);

        SourcePage page = getSourcePageFor(inside);
        assertThat(page.getTargetPackage(), is(inside.getIpsPackageFragment()));
    }

    @Test
    public void testGetPackage_CompositionInSubPackageAndLinkedAssociationOutside_KeepsRootPackage()
            throws Exception {
        IProductCmptType rootType = newProductCmptType(project, "RootType");
        IProductCmptType detailType = newProductCmptType(project, "DetailType");

        IProductCmptTypeAssociation compositionAssociation = rootType.newProductCmptTypeAssociation();
        compositionAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        compositionAssociation.setTarget(detailType.getQualifiedName());
        compositionAssociation.setTargetRoleSingular("Grunddeckung");

        IProductCmptTypeAssociation plainAssociation = rootType.newProductCmptTypeAssociation();
        plainAssociation.setAssociationType(AssociationType.ASSOCIATION);
        plainAssociation.setTarget(detailType.getQualifiedName());
        plainAssociation.setTargetRoleSingular("Zusatzdeckung");

        IProductCmpt root = newProductCmpt(rootType, "produkte.produkt2026.MeinZuhause");
        IProductCmpt grunddeckung = newProductCmpt(detailType, "produkte.produkt2026.deckungen.Grunddeckung");
        IProductCmpt zusatzdeckung = newProductCmpt(detailType, "produkte.zusatzdeckungen.Fahrraddiebstahl");

        IProductCmptGeneration gen = root.getProductCmptGeneration(0);
        IProductCmptLink grunddeckungLink = gen.newLink("Grunddeckung");
        grunddeckungLink.setTarget(grunddeckung.getQualifiedName());
        IProductCmptLink zusatzdeckungLink = gen.newLink("Zusatzdeckung");
        zusatzdeckungLink.setTarget(zusatzdeckung.getQualifiedName());
        root.getIpsSrcFile().save(null);

        SourcePage page = getSourcePageFor(root);

        assertThat(page.getTargetPackage(), is(root.getIpsPackageFragment()));
    }

    @Test
    public void testExtractYear_FourDigitToken() {
        assertThat(DeepCopyWizard.extractYear("produkte.produkt2026"), is(OptionalInt.of(2026)));
    }

    @Test
    public void testExtractYear_NoDigits_ReturnsEmpty() {
        assertThat(DeepCopyWizard.extractYear("produkte.produkt"), is(OptionalInt.empty()));
    }

    @Test
    public void testExtractYear_DigitsNotFourLong_ReturnsEmpty() {
        assertThat(DeepCopyWizard.extractYear("produkte.produkt26"), is(OptionalInt.empty()));
    }

    @Test
    public void testExtractYear_MultipleFourDigitTokens_ReturnsLastMatch() {
        assertThat(DeepCopyWizard.extractYear("produkte.2024.produkt2025"), is(OptionalInt.of(2025)));
    }

    @Test
    public void testExtractYear_NotAYearDigitNumber_IsIgnored() {
        assertThat(DeepCopyWizard.extractYear("produkte.version0007.produkt"), is(OptionalInt.empty()));
    }

    @Test
    public void testExtractYear_NotAYearDigitNumber_LastPlausibleMatchWins() {
        assertThat(DeepCopyWizard.extractYear("produkte.version0007.produkt2025"), is(OptionalInt.of(2025)));
    }

    @Test
    public void testReplaceYearToken_FourDigitMatch() {
        assertThat(DeepCopyWizard.replaceYearToken("produkte.produkt2025", 2025, 2027), is("produkte.produkt2027"));
    }

    @Test
    public void testReplaceYearToken_TwoDigitMatchWhenNoFourDigitMatch() {
        assertThat(DeepCopyWizard.replaceYearToken("produkte.produkt25", 2025, 2027), is("produkte.produkt27"));
    }

    @Test
    public void testReplaceYearToken_NoMatch_ReturnsNull() {
        assertThat(DeepCopyWizard.replaceYearToken("produkte.produkt", 2025, 2027), is(nullValue()));
    }

    @Test
    public void testUpdateTargetPackageForNewValidFrom_AppliesAlreadyOnOpen() throws Exception {
        IpsUIPlugin.getDefault().setDefaultValidityDate(new GregorianCalendar(2027, Calendar.JANUARY, 1));

        IProductCmptType yearType = newProductCmptType(project, "YearType");
        IProductCmpt yearProduct = newProductCmpt(yearType, "produkte.produkt2025.MeinZuhause");
        yearProduct.getIpsSrcFile().save(null);

        DeepCopyWizard wizard = new DeepCopyWizard((IProductCmptGeneration)yearProduct.getGeneration(0),
                DeepCopyWizard.TYPE_COPY_PRODUCT);

        assertThat(wizard.getPresentationModel().getTargetPackage().getName(), is("produkte.produkt2027"));
    }

    @Test
    public void testUpdateTargetPackageForNewValidFrom_UpdatesYearInTargetPackage() throws Exception {
        IpsUIPlugin.getDefault().setDefaultValidityDate(new GregorianCalendar(2025, Calendar.JANUARY, 1));

        IProductCmptType yearType = newProductCmptType(project, "YearType");
        IProductCmpt yearProduct = newProductCmpt(yearType, "produkte.produkt2025.MeinZuhause");
        yearProduct.getIpsSrcFile().save(null);

        DeepCopyWizard wizard = new DeepCopyWizard((IProductCmptGeneration)yearProduct.getGeneration(0),
                DeepCopyWizard.TYPE_COPY_PRODUCT);
        WizardDialog d = new WizardDialog(new Shell(), wizard);
        cleanups.add(d::close);
        d.setBlockOnOpen(false);
        d.open();
        SourcePage page = (SourcePage)wizard.getPage(SourcePage.PAGE_ID);
        d.showPage(page);

        assertThat(wizard.getPresentationModel().getTargetPackage().getName(), is("produkte.produkt2025"));

        wizard.getPresentationModel().setNewValidFrom(new GregorianCalendar(2027, Calendar.JANUARY, 1));

        assertThat(wizard.getPresentationModel().getTargetPackage().getName(), is("produkte.produkt2027"));

        wizard.getPresentationModel().setNewValidFrom(new GregorianCalendar(2028, Calendar.JANUARY, 1));

        assertThat(wizard.getPresentationModel().getTargetPackage().getName(), is("produkte.produkt2028"));
    }

    @Test
    public void testGetNewNameWithoutKindId() throws Exception {
        IIpsProjectProperties properties = project.getProperties();
        DateBasedProductCmptNamingStrategy strategy = new DateBasedProductCmptNamingStrategy();
        strategy.setVersionIdSeparator(" ");
        properties.setProductCmptNamingStrategy(strategy);
        project.setProperties(properties);

        IProductCmptGeneration gen = inside.getProductCmptGeneration(0);
        ITableContentUsage usage = gen.newTableContentUsage(tableStructureUsage);
        String oldName = "tableContentsWithoutKindId";
        usage.setTableContentName(oldName);

        DeepCopyPresentationModel deepCopyPresentationModel = new DeepCopyPresentationModel(gen);

        DeepCopyPreview deepCopyPreview = new DeepCopyPreview(deepCopyPresentationModel);
        String newName = deepCopyPreview.getNewName(null, usage.getIpsObject());
        assertFalse(newName.equals(oldName));
    }

    private SourcePage getSourcePageFor(IProductCmpt cmpt) throws IllegalArgumentException,
            CycleInProductStructureException {
        DeepCopyWizard wizard = new DeepCopyWizard((IProductCmptGeneration)cmpt.getGeneration(0),
                DeepCopyWizard.TYPE_COPY_PRODUCT);
        WizardDialog d = new WizardDialog(new Shell(), wizard);
        cleanups.add(d::close);
        d.setBlockOnOpen(false);
        d.open();
        SourcePage page = (SourcePage)wizard.getPage(SourcePage.PAGE_ID);
        d.showPage(page);
        return page;
    }

}
