/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssociationDecoratorTest extends AbstractIpsPluginTest {

    private AssociationDecorator decorator;

    @Mock
    private IProductCmptTypeAssociation aProductAssociation;

    @Mock
    private IPolicyCmptTypeAssociation aPolicyAssociation;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IPolicyCmptTypeAttribute aPolicyCmptAttribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        decorator = new AssociationDecorator();
        when(aProductAssociation.isChangingOverTime()).thenReturn(true);
        when(aProductAssociation.isConstrain()).thenReturn(false);
    }

    @Test
    public void testGetImageDescriptor_Null() throws Exception {
        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(decorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_BaseNameAggregation() throws Exception {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.AGGREGATION);

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptor(AssociationDecorator.ASSOCIATION_TYPE_AGGREGATION_IMAGE).equals(
                imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_BaseNameAssociation() throws Exception {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.ASSOCIATION);

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptor(AssociationDecorator.ASSOCIATION_TYPE_ASSOCIATION_IMAGE).equals(
                imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_BaseNameCompositionDetailToMaster() throws Exception {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptor(
                AssociationDecorator.ASSOCIATION_TYPE_COMPOSITION_DETAIL_TO_MASTER_IMAGE)
                        .equals(imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_BaseNameCompositionMasterToDetail() throws Exception {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptor(AssociationDecorator.ASSOCIATION_TYPE_COMPOSITION_IMAGE).equals(
                imageDescriptor));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetImageDescriptor_BaseNameUnknownType() throws Exception {
        when(aProductAssociation.getAssociationType()).thenReturn(null);

        decorator.getImageDescriptor(aProductAssociation);
    }

    @Test
    public void testGetImageDescriptor_OverlayChangeOverTime() {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.ASSOCIATION);
        when(aProductAssociation.isChangingOverTime()).thenReturn(false);

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptorWithChangeOvertime(
                AssociationDecorator.ASSOCIATION_TYPE_ASSOCIATION_IMAGE).equals(imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_DoesNotOverlayChangeOverTime() {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.ASSOCIATION);
        when(aProductAssociation.isChangingOverTime()).thenReturn(false);

        decorator = new AssociationDecorator(false);
        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertFalse(createImageDescriptorWithChangeOvertime(
                AssociationDecorator.ASSOCIATION_TYPE_ASSOCIATION_IMAGE).equals(imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_OverlayProductRelevant() throws Exception {
        when(aPolicyAssociation.getAssociationType()).thenReturn(AssociationType.ASSOCIATION);
        when(aPolicyAssociation.isConfigurable()).thenReturn(true);
        when(aPolicyAssociation.getIpsProject()).thenReturn(ipsProject);
        when(aPolicyAssociation.isConstrainedByProductStructure(ipsProject)).thenReturn(true);

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(aPolicyAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptorWithProductRelevant(
                AssociationDecorator.ASSOCIATION_TYPE_ASSOCIATION_IMAGE).equals(imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_OverlayConstrains() {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.ASSOCIATION);
        when(aProductAssociation.isConstrain()).thenReturn(true);

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptorWithConstrains(AssociationDecorator.ASSOCIATION_TYPE_ASSOCIATION_IMAGE)
                .equals(imageDescriptor));
    }

    private ImageDescriptor createImageDescriptor(String baseName) {
        return createImageDescriptorWithOverwrittenOverlay(baseName, false, false, false);
    }

    private ImageDescriptor createImageDescriptorWithChangeOvertime(String baseName) {
        return createImageDescriptorWithOverwrittenOverlay(baseName, false, false, true);
    }

    private ImageDescriptor createImageDescriptorWithProductRelevant(String baseName) {
        return createImageDescriptorWithOverwrittenOverlay(baseName, false, true, false);
    }

    private ImageDescriptor createImageDescriptorWithConstrains(String baseName) {
        return createImageDescriptorWithOverwrittenOverlay(baseName, true, false, false);
    }

    private ImageDescriptor createImageDescriptorWithOverwrittenOverlay(String baseName,
            boolean isOverride,
            boolean isProductRelevant,
            boolean noChangeOverTime) {
        String[] overlays = new String[4];
        if (isOverride) {
            overlays[3] = OverlayIcons.OVERRIDE;
        }
        if (isProductRelevant) {
            overlays[1] = OverlayIcons.PRODUCT_RELEVANT;
        }
        if (noChangeOverTime) {
            overlays[0] = OverlayIcons.STATIC;
        }
        return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(baseName, overlays);
    }

    @Test
    public void testGetLabel() throws Exception {
        when(aProductAssociation.getTargetRolePlural()).thenReturn("plural");
        when(aProductAssociation.getTargetRoleSingular()).thenReturn("singular");
        when(aPolicyCmptAttribute.getName()).thenReturn("name");
        when(aProductAssociation.is1ToMany()).thenReturn(true);

        assertEquals("plural", decorator.getLabel(aProductAssociation));

        when(aProductAssociation.is1ToMany()).thenReturn(false);

        assertEquals("singular", decorator.getLabel(aProductAssociation));
        assertEquals("name", decorator.getLabel(aPolicyCmptAttribute));
    }

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor sharedImageDescriptor = IIpsDecorators.getImageHandling().getSharedImageDescriptor(
                AssociationDecorator.ASSOCIATION_TYPE_ASSOCIATION_IMAGE, true);
        assertEquals(sharedImageDescriptor, decorator.getDefaultImageDescriptor());

    }

}
