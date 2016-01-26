/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StyledString;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.editors.productcmpt.TemplateValueUiStatus;
import org.faktorips.devtools.core.ui.internal.IpsStyler;

public class LinkViewItemLabelStyler {

    static final String OVERWRITE_EQUAL_SIGN = " \u25CB"; //$NON-NLS-1$

    private static final StyledString EMPTY_STYLED_STRING = new StyledString();

    private final LinkViewItem viewItem;
    private final TemplateValueUiStatus status;

    public LinkViewItemLabelStyler(LinkViewItem viewItem) {
        super();
        this.viewItem = viewItem;
        this.status = TemplateValueUiStatus.mapStatus(viewItem.getLink());
    }

    public StyledString getStyledLabel() {
        return styledName().append(styledLinkCardinality()).append(styledOverwrittenTemplateCardinality());
    }

    private StyledString styledName() {
        if (status == TemplateValueUiStatus.UNDEFINED) {
            return new StyledString(viewItem.getText(), IpsStyler.DISABLED_STYLER);
        } else {
            return new StyledString(viewItem.getText());
        }
    }

    /** Returns the styled string for link's text and (if necessary) its cardinality. */
    private StyledString styledLinkCardinality() {
        if (status == TemplateValueUiStatus.NEWLY_DEFINED) {
            return new StyledString(formattedCardinality(), IpsStyler.DEFAULT_CARDINALITY_STYLER);
        }
        if (status == TemplateValueUiStatus.OVERWRITE || status == TemplateValueUiStatus.OVERWRITE_EQUAL) {
            return new StyledString(formattedCardinality(), IpsStyler.DEFAULT_CARDINALITY_STYLER);
        }
        if (status == TemplateValueUiStatus.INHERITED) {
            return new StyledString(formattedCardinality(), IpsStyler.TEMPLATE_STYLER);
        }
        if (status == TemplateValueUiStatus.UNDEFINED) {
            return new StyledString(formattedTemplateCardinality(), IpsStyler.DISABLED_STYLER);
        }
        throw new IllegalStateException();
    }

    private String formattedCardinality() {
        if (displayCardinality()) {
            return formattedCardinality(viewItem.getLink());
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Returns the styles string with the link's template cardinality. Returns an empty styled
     * string if the link's association does not configure a policy association or if the template
     * cardinality should not be displayed (e.g. because it the link's cardinality is already the
     * inherited template cardinality).
     */
    private StyledString styledOverwrittenTemplateCardinality() {
        if (!displayOverwrittenTemplateCardinality()) {
            return EMPTY_STYLED_STRING;
        } else if (status == TemplateValueUiStatus.OVERWRITE_EQUAL) {
            return new StyledString(OVERWRITE_EQUAL_SIGN);
        } else {
            String formattedTemplateCardinality = formattedTemplateCardinality();
            return new StyledString(formattedTemplateCardinality, IpsStyler.OVERWRITE_TEMPLATE_STYLER);
        }
    }

    private String formattedTemplateCardinality() {
        IProductCmptLink templateLink = viewItem.getLink().findTemplateProperty(ipsProject());
        if (templateLink != null) {
            return formattedCardinality(templateLink);
        } else {
            return StringUtils.EMPTY;
        }
    }

    /** Returns the cardinality of the given link as a formatted string. */
    private String formattedCardinality(IProductCmptLink link) {
        return " " + link.getCardinality().format(); //$NON-NLS-1$
    }

    /**
     * Returns {@code true} if the link's template cardinality should be displayed, i.e. if the link
     * has a template and does not inherit the template'S cardinality.
     */
    private boolean displayCardinality() {
        return isConfiguringPolicyAssociation();
    }

    /** Returns {@code true} if the link configures a policy association. */
    private boolean isConfiguringPolicyAssociation() {
        try {
            IProductCmptTypeAssociation productAsssociation = viewItem.getLink().findAssociation(ipsProject());
            return productAsssociation.findMatchingPolicyCmptTypeAssociation(ipsProject()) != null;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns {@code true} if the link's template cardinality should be displayed, i.e. if the link
     * is configuring a policy association, has a template and does not inherit the template's
     * cardinality.
     */
    private boolean displayOverwrittenTemplateCardinality() {
        if (!isConfiguringPolicyAssociation()) {
            return false;
        }
        IProductCmptLink link = viewItem.getLink();
        return link.getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }

    private IIpsProject ipsProject() {
        return viewItem.getLink().getIpsProject();
    }
}
