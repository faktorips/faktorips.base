/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IPolicyCmptLinkCardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TemplatePolicyCmptLinkCardinalityPmoTest {

    @Mock
    private IPolicyCmptLinkCardinality cardinality;

    @Mock
    private IPolicyCmptLinkCardinality templateCardinality;

    @Mock
    private IProductCmptLinkContainer templateContainer;

    @Mock
    private IProductCmpt templateProductCmpt;

    @Mock
    private IIpsProject ipsProject;

    @Test
    public void testGetTemplateValueStatus_noCardinality() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();

        assertThat(pmo.getTemplateValueStatus(), is(TemplateValueUiStatus.NEWLY_DEFINED));
    }

    @Test
    public void testGetTemplateValueStatus_withCardinality() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();
        when(cardinality.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(cardinality.getIpsProject()).thenReturn(ipsProject);
        when(cardinality.findTemplateProperty(any())).thenReturn(null);

        pmo.setCardinality(cardinality);

        assertThat(pmo.getTemplateValueStatus(), is(TemplateValueUiStatus.NEWLY_DEFINED));
    }

    @Test
    public void testGetToolTipText_noCardinality() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();

        assertThat(pmo.getToolTipText(),
                is(Messages.TemplatePolicyCmptLinkCardinalityPmo_Status_NewlyDefined));
    }

    @Test
    public void testIsStatusButtonEnabled_noCardinalityNoCreator() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();

        assertThat(pmo.isStatusButtonEnabled(), is(false));
    }

    @Test
    public void testIsStatusButtonEnabled_withCardinality() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();
        pmo.setCardinality(cardinality);

        assertThat(pmo.isStatusButtonEnabled(), is(true));
    }

    @Test
    public void testSetCardinality_null() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();
        pmo.setCardinality(cardinality);
        pmo.setCardinality(null);

        assertThat(pmo.getCardinality(), is(nullValue()));
    }

    @Test
    public void testFindTemplateCardinality_noCardinality() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();

        assertThat(pmo.findTemplateCardinality(), is(nullValue()));
    }

    @Test
    public void testFindTemplateCardinality_withCardinality() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();
        when(cardinality.getIpsProject()).thenReturn(ipsProject);
        when(cardinality.findTemplateProperty(ipsProject)).thenReturn(templateCardinality);

        pmo.setCardinality(cardinality);

        assertThat(pmo.findTemplateCardinality(), is(templateCardinality));
    }

    @Test
    public void testGetToolTipText_inherited() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();
        when(cardinality.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);
        when(cardinality.getIpsProject()).thenReturn(ipsProject);
        when(cardinality.findTemplateProperty(ipsProject)).thenReturn(templateCardinality);
        when(cardinality.getAssociation()).thenReturn("HausratVertragsTeil");
        when(templateCardinality.getTemplatedValueContainer()).thenReturn(templateContainer);
        when(templateContainer.getProductCmpt()).thenReturn(templateProductCmpt);
        when(templateProductCmpt.getName()).thenReturn("HausratTemplate");

        pmo.setCardinality(cardinality);

        String toolTip = pmo.getToolTipText();
        assertThat(toolTip.contains("HausratVertragsTeil"), is(true));
        assertThat(toolTip.contains("HausratTemplate"), is(true));
    }

    @Test
    public void testGetToolTipText_overwrite() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();
        when(cardinality.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(cardinality.getIpsProject()).thenReturn(ipsProject);
        when(cardinality.findTemplateProperty(ipsProject)).thenReturn(templateCardinality);
        when(cardinality.getValueComparator()).thenReturn((o1, o2) -> 1);
        when(cardinality.getValueGetter()).thenReturn(c -> null);
        when(templateCardinality.getValueGetter()).thenReturn(c -> null);
        when(cardinality.getAssociation()).thenReturn("HausratVertragsTeil");
        when(templateCardinality.getTemplatedValueContainer()).thenReturn(templateContainer);
        when(templateContainer.getProductCmpt()).thenReturn(templateProductCmpt);
        when(templateProductCmpt.getName()).thenReturn("HausratTemplate");

        pmo.setCardinality(cardinality);

        String toolTip = pmo.getToolTipText();
        assertThat(toolTip.contains("HausratVertragsTeil"), is(true));
        assertThat(toolTip.contains("HausratTemplate"), is(true));
    }

    @Test
    public void testGetToolTipText_overwriteEqual() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();
        when(cardinality.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(cardinality.getIpsProject()).thenReturn(ipsProject);
        when(cardinality.findTemplateProperty(ipsProject)).thenReturn(templateCardinality);
        when(cardinality.getValueComparator()).thenReturn((o1, o2) -> 0);
        when(cardinality.getValueGetter()).thenReturn(c -> null);
        when(templateCardinality.getValueGetter()).thenReturn(c -> null);
        when(templateCardinality.getTemplatedValueContainer()).thenReturn(templateContainer);
        when(templateContainer.getProductCmpt()).thenReturn(templateProductCmpt);
        when(templateProductCmpt.getName()).thenReturn("HausratTemplate");

        pmo.setCardinality(cardinality);

        String toolTip = pmo.getToolTipText();
        assertThat(toolTip.contains("HausratTemplate"), is(true));
    }

    @Test
    public void testGetToolTipText_undefined() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();
        when(cardinality.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);
        when(cardinality.getIpsProject()).thenReturn(ipsProject);
        when(cardinality.findTemplateProperty(ipsProject)).thenReturn(templateCardinality);
        when(cardinality.getAssociation()).thenReturn("HausratVertragsTeil");
        when(templateCardinality.getTemplatedValueContainer()).thenReturn(templateContainer);
        when(templateContainer.getProductCmpt()).thenReturn(templateProductCmpt);
        when(templateProductCmpt.getName()).thenReturn("HausratTemplate");

        pmo.setCardinality(cardinality);

        String toolTip = pmo.getToolTipText();
        assertThat(toolTip.contains("HausratVertragsTeil"), is(true));
        assertThat(toolTip.contains("HausratTemplate"), is(true));
    }

    @Test
    public void testOnClick_cyclesStatusWhenCardinalityExists() {
        TemplatePolicyCmptLinkCardinalityPmo pmo = new TemplatePolicyCmptLinkCardinalityPmo();
        when(cardinality.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(cardinality.getIpsProject()).thenReturn(ipsProject);
        when(cardinality.findTemplateProperty(ipsProject)).thenReturn(templateCardinality);
        when(cardinality.getValueComparator()).thenReturn((o1, o2) -> 0);
        when(cardinality.getValueGetter()).thenReturn(c -> null);
        when(templateCardinality.getValueGetter()).thenReturn(c -> null);
        pmo.setCardinality(cardinality);

        pmo.onClick();

        verify(cardinality).switchTemplateValueStatus();
    }
}
