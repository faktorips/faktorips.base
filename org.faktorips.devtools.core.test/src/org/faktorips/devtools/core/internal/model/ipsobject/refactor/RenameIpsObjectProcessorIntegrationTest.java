/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.junit.Test;

public class RenameIpsObjectProcessorIntegrationTest extends AbstractIpsPluginTest {

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IPolicyCmptType} defines product relevant policy component type attributes that are
     * assigned to a specific {@link IProductCmptCategory} of the configuring
     * {@link IProductCmptType}. Within this {@link IProductCmptCategory}, the ordering of the
     * attributes is changed. This causes {@link IProductCmptPropertyReference} objects to be
     * created in the {@link IProductCmptType}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptPropertyReference} objects must be updated to reference the renamed
     * {@link IPolicyCmptType}.
     */
    @Test
    public void testRenamePolicyCmptType_UpdateProductCmptPropertyReferences() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute("a1");
        IPolicyCmptTypeAttribute a2 = policyCmptType.newPolicyCmptTypeAttribute("a2");
        a1.setDatatype(Datatype.INTEGER.getQualifiedName());
        a1.setProductRelevant(true);
        a2.setDatatype(Datatype.INTEGER.getQualifiedName());
        a2.setProductRelevant(true);

        // Create property references by moving the properties within a category
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
        IProductCmptCategory category = productCmptType.newCategory("myCategory");
        productCmptType.changeCategoryAndDeferPolicyChange(a1, category.getName());
        productCmptType.changeCategoryAndDeferPolicyChange(a2, category.getName());
        category.moveProductCmptProperties(new int[] { 0 }, false, productCmptType);

        productCmptType.getIpsSrcFile().save(true, null);
        policyCmptType.getIpsSrcFile().save(true, null);

        // Rename the policy component type
        performRenameRefactoring(policyCmptType, "Foo");

        // If the references are updated accordingly the order within the category remains
        List<IProductCmptProperty> properties = category.findProductCmptProperties(productCmptType, false, ipsProject);
        assertEquals(a2.getName(), properties.get(0).getName());
        assertEquals(a1.getName(), properties.get(1).getName());
    }

}
