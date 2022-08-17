/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jface.viewers.StyledString;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Test;

public class LinkViewItemLabelStylerTest extends AbstractIpsPluginTest {

    @Test
    public void testGetStyledLabel_NonTemplatedLink() {
        IIpsProject project = newIpsProject();

        IPolicyCmptType policyType = newPolicyAndProductCmptType(project, "PolicyType", "ProductType");
        IProductCmptType type = policyType.findProductCmptType(project);

        IPolicyCmptTypeAssociation policyAssociation = policyType.newPolicyCmptTypeAssociation();
        policyAssociation.setTargetRoleSingular("PolicyAssociation");

        IProductCmptTypeAssociation association = type.newProductCmptTypeAssociation();
        association.setMatchingAssociationSource(policyType.getQualifiedName());
        association.setMatchingAssociationName(policyAssociation.getName());

        IProductCmpt product = newProductCmpt(type, "Product");
        IProductCmptLink productLink = product.newLink(association);
        productLink.setTarget("target");
        productLink.setCardinality(new Cardinality(0, 1, 0));

        LinkViewItemLabelStyler styler = new LinkViewItemLabelStyler(new LinkViewItem(productLink));
        StyledString label = styler.getStyledLabel();

        assertThat(label.getString(), is("target [0..1, 0]"));
    }

    @Test
    public void testGetStyledLabel_NonConfiguredLink() {
        IIpsProject project = newIpsProject();

        IPolicyCmptType policyType = newPolicyAndProductCmptType(project, "PolicyType", "ProductType");
        IProductCmptType type = policyType.findProductCmptType(project);

        IProductCmptTypeAssociation association = type.newProductCmptTypeAssociation();
        association.setMatchingAssociationSource(policyType.getQualifiedName());

        IProductCmpt product = newProductCmpt(type, "Product");
        IProductCmptLink productLink = product.newLink(association);
        productLink.setTarget("target");
        productLink.setCardinality(new Cardinality(0, 1, 0));

        LinkViewItemLabelStyler styler = new LinkViewItemLabelStyler(new LinkViewItem(productLink));
        StyledString label = styler.getStyledLabel();

        assertThat(label.getString(), is("target"));
    }

    @Test
    public void testGetStyledLabel_TemplatedNonConfiguredLink() {
        IIpsProject project = newIpsProject();

        IPolicyCmptType policyType = newPolicyAndProductCmptType(project, "PolicyType", "ProductType");
        IProductCmptType type = policyType.findProductCmptType(project);

        IProductCmptTypeAssociation association = type.newProductCmptTypeAssociation();
        association.setMatchingAssociationSource(policyType.getQualifiedName());

        IProductCmpt template = newProductTemplate(type, "Template");
        IProductCmpt product = newProductCmpt(type, "Product");
        product.setTemplate(template.getQualifiedName());

        IProductCmptLink templateLink = template.newLink(association);
        templateLink.setTarget("target");
        templateLink.setCardinality(new Cardinality(1, 1, 1));

        IProductCmptLink productLink = product.newLink(association);
        productLink.setTarget("target");
        productLink.setCardinality(new Cardinality(1, 1, 1));
        productLink.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        LinkViewItemLabelStyler styler = new LinkViewItemLabelStyler(new LinkViewItem(productLink));
        StyledString label = styler.getStyledLabel();

        assertThat(label.getString(), is("target" + LinkViewItemLabelStyler.INHERITED_SIGN));
    }

    @Test
    public void testGetStyledLabel_TemplatedLinkInherited() {
        IProductCmptLink productLink = createTemplatedLink();

        // The link inherits its cardinality, only the template's cardinality is displayed
        productLink.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        LinkViewItemLabelStyler styler = new LinkViewItemLabelStyler(new LinkViewItem(productLink));

        assertThat(styler.getStyledLabel().getString(), is("target [1..1, 1]"));
    }

    @Test
    public void testGetStyledLabel_TemplatedLinkDefinedOther() {
        IProductCmptLink productLink = createTemplatedLink();

        // The link overrides the template's cardinality, both the link's cardinality and the
        // template's cardinality are displayed
        productLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        productLink.setCardinality(new Cardinality(0, 1, 0));
        LinkViewItemLabelStyler styler = new LinkViewItemLabelStyler(new LinkViewItem(productLink));

        assertThat(styler.getStyledLabel().getString(), is("target [0..1, 0] [1..1, 1]"));
    }

    @Test
    public void testGetStyledLabel_TemplatedLinkDefinedSame() {
        IProductCmptLink productLink = createTemplatedLink();

        // The link overrides the template's cardinality, both the link's cardinality and the
        // template's cardinality are displayed
        productLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        productLink.setCardinality(new Cardinality(1, 1, 1));
        LinkViewItemLabelStyler styler = new LinkViewItemLabelStyler(new LinkViewItem(productLink));

        assertThat(styler.getStyledLabel().getString(), is("target [1..1, 1]"
                + LinkViewItemLabelStyler.OVERWRITE_EQUAL_SIGN));
    }

    protected IProductCmptLink createTemplatedLink() {
        IIpsProject project = newIpsProject();

        IPolicyCmptType policyType = newPolicyAndProductCmptType(project, "PolicyType", "ProductType");
        IProductCmptType type = policyType.findProductCmptType(project);

        IPolicyCmptTypeAssociation policyAssociation = policyType.newPolicyCmptTypeAssociation();
        policyAssociation.setTargetRoleSingular("PolicyAssociation");

        IProductCmptTypeAssociation association = type.newProductCmptTypeAssociation();
        association.setMatchingAssociationSource(policyType.getQualifiedName());
        association.setMatchingAssociationName(policyAssociation.getName());

        IProductCmpt template = newProductTemplate(type, "Template");
        IProductCmpt product = newProductCmpt(type, "Product");
        product.setTemplate(template.getQualifiedName());

        IProductCmptLink templateLink = template.newLink(association);
        templateLink.setTarget("target");
        templateLink.setCardinality(new Cardinality(1, 1, 1));

        IProductCmptLink productLink = product.newLink(association);
        productLink.setTarget("target");
        productLink.setCardinality(new Cardinality(0, 1, 0));
        return productLink;
    }

}
