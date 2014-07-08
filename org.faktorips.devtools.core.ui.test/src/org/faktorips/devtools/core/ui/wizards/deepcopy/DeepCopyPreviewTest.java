/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.deepcopy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptReference;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.junit.Before;
import org.junit.Test;

public class DeepCopyPreviewTest extends AbstractIpsPluginTest {

    private IIpsProject subIpsProject;

    private IIpsProject suPerIpsProject;

    private ProductCmptType superType;

    private ProductCmptType subType;

    private ProductCmpt aSub;

    private ProductCmptReference cmptReferenceASub;

    private DeepCopyPreview deepCopy;

    private ProductCmpt bSuper;

    @Override
    @Before
    public void setUp() throws Exception {
        subIpsProject = newIpsProject("SuperProject");
        suPerIpsProject = newIpsProject("SubProject");
        subIpsProject.isReferencing(suPerIpsProject);

        superType = newProductCmptType(suPerIpsProject, "some.superType");
        bSuper = newProductCmpt(superType, "some.BSuper");

        subType = newProductCmptType(superType, "some.subType");
        aSub = newProductCmpt(subType, "some.ASub");

        IProductCmptTypeAssociation subAssociation = subType.newProductCmptTypeAssociation();
        subAssociation.setAssociationType(AssociationType.ASSOCIATION);
        subAssociation.setTarget(superType.getName());

        DeepCopyPresentationModel presentationModel = new DeepCopyPresentationModel(bSuper.getProductCmptGeneration(0));
        deepCopy = new DeepCopyPreview(presentationModel);

        cmptReferenceASub = new ProductCmptReference(null, null, aSub, null);

    }

    @Test
    public void testValidateAlreadyExistingFile_FileExists() {
        deepCopy.validateAlreadyExistingFile("some", aSub.getName(), IpsObjectType.PRODUCT_CMPT.getFileExtension(),
                cmptReferenceASub);
        Map<IProductCmptStructureReference, String> errorElements = deepCopy.getErrorElements();

        assertEquals(1, errorElements.size());
        assertTrue(errorElements.get(cmptReferenceASub)
                .contains(Messages.ReferenceAndPreviewPage_msgFileAllreadyExists));
        assertTrue(errorElements.get(cmptReferenceASub).contains(Messages.ReferenceAndPreviewPage_msgCanNotCreateFile));
    }

    @Test
    public void testValidateAlreadyExistingFile_ExistInOtherPackage() {

        deepCopy.validateAlreadyExistingFile("somenew", aSub.getName(), IpsObjectType.PRODUCT_CMPT.getFileExtension(),
                cmptReferenceASub);
        Map<IProductCmptStructureReference, String> errorElements = deepCopy.getErrorElements();

        assertNotNull(errorElements);
        assertEquals(0, errorElements.size());
    }

    @Test
    public void testValidateAlreadyExistingFile_notExisting() {

        deepCopy.validateAlreadyExistingFile("some", "NewName", IpsObjectType.PRODUCT_CMPT.getFileExtension(),
                cmptReferenceASub);
        Map<IProductCmptStructureReference, String> errorElements = deepCopy.getErrorElements();

        assertNotNull(errorElements);
        assertEquals(0, errorElements.size());
    }
}
