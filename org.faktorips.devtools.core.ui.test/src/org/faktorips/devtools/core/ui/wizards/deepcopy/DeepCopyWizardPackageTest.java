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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.junit.Before;
import org.junit.Test;

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

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

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

    @Test
    public void testGetPackage() throws Exception {
        SourcePage page = getSourcePageFor(inside);
        assertEquals(inside.getIpsPackageFragment(), page.getTargetPackage());

        IProductCmptGeneration gen = inside.getProductCmptGeneration(0);
        IProductCmptLink link = gen.newLink("RoleName");
        link.setTarget(middle.getQualifiedName());
        inside.getIpsSrcFile().save(true, null);

        page = getSourcePageFor(inside);
        assertEquals(middle.getIpsPackageFragment(), page.getTargetPackage());
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
        d.setBlockOnOpen(false);
        d.open();
        SourcePage page = (SourcePage)wizard.getPage(SourcePage.PAGE_ID);
        d.showPage(page);
        return page;
    }

}
