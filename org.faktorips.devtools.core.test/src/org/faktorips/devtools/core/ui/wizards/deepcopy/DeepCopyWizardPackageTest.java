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

import java.util.GregorianCalendar;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
public class DeepCopyWizardPackageTest extends AbstractIpsPluginTest {
	
    ProductCmpt inside;
    ProductCmpt middle;
    ProductCmpt outside;
    GregorianCalendar validFrom = new GregorianCalendar(2000, 1, 1);
    
	/**
	 * {@inheritDoc}
	 */
    protected void setUp() throws Exception {
        super.setUp();
        
        IIpsProject prj = super.newIpsProject("TestProject");
        PolicyCmptType type = super.newPolicyCmptType(prj, "Base");
        type.setConfigurableByProductCmptType(true);
        type.setUnqualifiedProductCmptType("BaseType");
        IRelation rel = type.newRelation();
        rel.setTarget(type.getQualifiedName());
        rel.setTargetRoleSingular("rel");
        rel.setTargetRoleSingularProductSide("relType");
        rel.setProductRelevant(true);
        
        
        outside = super.newProductCmpt(prj, "Outside");
        outside.setPolicyCmptType(type.getQualifiedName());
        IIpsObjectGeneration gen = outside.newGeneration(validFrom);
        
        middle = super.newProductCmpt(prj, "one.Middle");
        middle.setPolicyCmptType(type.getQualifiedName());
        gen = middle.newGeneration(validFrom);
        
        inside = super.newProductCmpt(prj, "one.two.Inside");
        inside.setPolicyCmptType(type.getQualifiedName());
        gen = inside.newGeneration(validFrom);
    }
   
    public void testGetPackage() throws Exception {
        SourcePage page = getSourcePageFor(inside);
        assertEquals(inside.getIpsPackageFragment(), page.getTargetPackage());
        
        IProductCmptGeneration gen = (IProductCmptGeneration)inside.getGenerationByEffectiveDate(validFrom);
        IProductCmptRelation rel = gen.newRelation("relType");
        rel.setTarget(middle.getQualifiedName());

        
        inside.getIpsSrcFile().save(true, null);
        
        page = getSourcePageFor(inside);
        assertEquals(middle.getIpsPackageFragment(), page.getTargetPackage());
        
    }
    
    private SourcePage getSourcePageFor(ProductCmpt cmpt) {
        DeepCopyWizard wizard = new DeepCopyWizard(inside, DeepCopyWizard.TYPE_COPY_PRODUCT);
        WizardDialog d = new WizardDialog(new Shell(), wizard);
        d.setBlockOnOpen(false);
        d.open();
        SourcePage page = (SourcePage)wizard.getPage(SourcePage.PAGE_ID);
        d.showPage(page);
        return page;
    }
    
}
