/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
public class DeepCopyWizardPackageTest extends AbstractIpsPluginTest {
	
    private IProductCmpt inside;
    private IProductCmpt middle;
    
	/**
	 * {@inheritDoc}
	 */
    protected void setUp() throws Exception {
        super.setUp();
        
        IIpsProject prj = super.newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(prj, "BaseType");
        IProductCmptTypeAssociation association = productCmptType.newAssociation();
        association.setTarget("SomeOtherType");
        association.setTargetRoleSingular("RoleName");
        
        newProductCmpt(productCmptType, "Outside");
        middle = newProductCmpt(productCmptType, "one.Middle");
        inside = newProductCmpt(productCmptType, "one.two.Inside");
    }
   
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
    
    private SourcePage getSourcePageFor(IProductCmpt cmpt) {
        DeepCopyWizard wizard = new DeepCopyWizard(cmpt, DeepCopyWizard.TYPE_COPY_PRODUCT);
        WizardDialog d = new WizardDialog(new Shell(), wizard);
        d.setBlockOnOpen(false);
        d.open();
        SourcePage page = (SourcePage)wizard.getPage(SourcePage.PAGE_ID);
        d.showPage(page);
        return page;
    }
    
}
