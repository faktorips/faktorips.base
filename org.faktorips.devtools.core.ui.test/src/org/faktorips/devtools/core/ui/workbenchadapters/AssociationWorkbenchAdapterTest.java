/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.workbenchadapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssociationWorkbenchAdapterTest extends AbstractIpsPluginTest {

    private AssociationWorkbenchAdapter workbenchAdapter;

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
        workbenchAdapter = new AssociationWorkbenchAdapter();
        when(aProductAssociation.isChangingOverTime()).thenReturn(true);
        when(aProductAssociation.isConstrain()).thenReturn(false);
    }

    @Test
    public void testGetImageDescriptor_BaseNameAggregation() throws Exception {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.AGGREGATION);

        ImageDescriptor imageDescriptor = workbenchAdapter.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptor(AssociationWorkbenchAdapter.ASSOCIATION_TYPE_AGGREGATION_IMAGE).equals(
                imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_BaseNameAssociation() throws Exception {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.ASSOCIATION);

        ImageDescriptor imageDescriptor = workbenchAdapter.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptor(AssociationWorkbenchAdapter.ASSOCIATION_TYPE_ASSOCIATION_IMAGE).equals(
                imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_BaseNameCompositionDetailToMaster() throws Exception {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        ImageDescriptor imageDescriptor = workbenchAdapter.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptor(
                AssociationWorkbenchAdapter.ASSOCIATION_TYPE_COMPOSITION_DETAIL_TO_MASTER_IMAGE)
                .equals(imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_BaseNameCompositionMasterToDetail() throws Exception {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        ImageDescriptor imageDescriptor = workbenchAdapter.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptor(AssociationWorkbenchAdapter.ASSOCIATION_TYPE_COMPOSITION_IMAGE).equals(
                imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_OverlayChangeOverTime() {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.ASSOCIATION);
        when(aProductAssociation.isChangingOverTime()).thenReturn(false);

        ImageDescriptor imageDescriptor = workbenchAdapter.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptorWithChangeOvertime(
                AssociationWorkbenchAdapter.ASSOCIATION_TYPE_ASSOCIATION_IMAGE).equals(imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_OverlayProductRelevant() throws Exception {
        when(aPolicyAssociation.getAssociationType()).thenReturn(AssociationType.ASSOCIATION);
        when(aPolicyAssociation.isConfigured()).thenReturn(true);
        when(aPolicyAssociation.getIpsProject()).thenReturn(ipsProject);
        when(aPolicyAssociation.isConstrainedByProductStructure(ipsProject)).thenReturn(true);

        ImageDescriptor imageDescriptor = workbenchAdapter.getImageDescriptor(aPolicyAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptorWithProductRelevant(
                AssociationWorkbenchAdapter.ASSOCIATION_TYPE_ASSOCIATION_IMAGE).equals(imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_OverlayConstrains() {
        when(aProductAssociation.getAssociationType()).thenReturn(AssociationType.ASSOCIATION);
        when(aProductAssociation.isConstrain()).thenReturn(true);

        ImageDescriptor imageDescriptor = workbenchAdapter.getImageDescriptor(aProductAssociation);

        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptorWithConstrains(AssociationWorkbenchAdapter.ASSOCIATION_TYPE_ASSOCIATION_IMAGE)
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
            overlays[3] = OverlayIcons.OVERRIDE_OVR;
        }
        if (isProductRelevant) {
            overlays[1] = OverlayIcons.PRODUCT_OVR;
        }
        if (noChangeOverTime) {
            overlays[0] = OverlayIcons.NOT_CHANGEOVERTIME_OVR;
        }
        return IpsUIPlugin.getImageHandling().getSharedOverlayImage(baseName, overlays);
    }

    @Test
    public void testGetLabel() throws Exception {
        when(aProductAssociation.getTargetRolePlural()).thenReturn("plural");
        when(aProductAssociation.getTargetRoleSingular()).thenReturn("singular");
        when(aPolicyCmptAttribute.getName()).thenReturn("name");
        when(aProductAssociation.is1ToMany()).thenReturn(true);

        assertEquals("plural", workbenchAdapter.getLabel(aProductAssociation));

        when(aProductAssociation.is1ToMany()).thenReturn(false);

        assertEquals("singular", workbenchAdapter.getLabel(aProductAssociation));
        assertEquals("name", workbenchAdapter.getLabel(aPolicyCmptAttribute));
    }

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor sharedImageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(
                AssociationWorkbenchAdapter.ASSOCIATION_TYPE_ASSOCIATION_IMAGE, true);
        assertEquals(sharedImageDescriptor, workbenchAdapter.getDefaultImageDescriptor());

    }

}
