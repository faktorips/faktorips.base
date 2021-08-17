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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.StyledString;
import org.faktorips.devtools.core.ui.editors.productcmpt.TemplateValueUiStatus;
import org.faktorips.devtools.core.ui.internal.IpsStyler;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.util.StringUtil;

public class LinkViewItemLabelStyler {

    static final String OVERWRITE_EQUAL_SIGN = " \u25CB"; //$NON-NLS-1$
    static final String INHERITED_SIGN = "   \u25B3"; //$NON-NLS-1$

    private static final StyledString EMPTY_STYLED_STRING = new StyledString();

    private final LinkViewItem viewItem;
    private final TemplateValueUiStatus status;

    public LinkViewItemLabelStyler(LinkViewItem viewItem) {
        super();
        this.viewItem = viewItem;
        this.status = TemplateValueUiStatus.mapStatus(viewItem.getLink());
    }

    public StyledString getStyledLabel() {
        if (displayCardinality()) {
            return styledName().append(styledLinkCardinality()).append(styledTemplateCardinality());
        } else {
            return styledName().append(styledInheritedTemplateSign());
        }
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

    /**
     * Returns the styled string with the link's template cardinality (if the link overwrites the
     * template cardinality) or {@link #OVERWRITE_EQUAL_SIGN} (if the links inherits its cardinality
     * from the template. Returns an empty styled string if the template cardinality should not be
     * displayed.
     */
    private StyledString styledTemplateCardinality() {
        if (!displayTemplateCardinality()) {
            return EMPTY_STYLED_STRING;
        } else if (status == TemplateValueUiStatus.OVERWRITE_EQUAL) {
            return new StyledString(OVERWRITE_EQUAL_SIGN);
        } else {
            String formattedTemplateCardinality = formattedTemplateCardinality();
            return new StyledString(formattedTemplateCardinality, IpsStyler.OVERWRITE_TEMPLATE_STYLER);
        }
    }

    /**
     * Returns the styled string with the {@link #INHERITED_SIGN} if the link is inherited. Returns
     * an empty styled string if the link is not inherited.
     */
    private StyledString styledInheritedTemplateSign() {
        if (status == TemplateValueUiStatus.INHERITED) {
            return new StyledString(INHERITED_SIGN, IpsStyler.TEMPLATE_STYLER);
        } else {
            return EMPTY_STYLED_STRING;
        }
    }

    private String formattedCardinality() {
        return formattedCardinality(viewItem.getLink());
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
        return StringUtil.BLANK + link.getCardinality().format();
    }

    /**
     * Returns {@code true} if cardinalities should be displayed, i.e. if the link is configuring a
     * policy association.
     */
    private boolean displayCardinality() {
        return viewItem.getLink().isConfiguringPolicyAssociation();
    }

    /**
     * Returns {@code true} if the link's template cardinality for a policy configuring link should
     * be displayed, i.e. if the link has a template and does not inherit the template's
     * cardinality.
     */
    private boolean displayTemplateCardinality() {
        IProductCmptLink link = viewItem.getLink();
        return link.getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }

    private IIpsProject ipsProject() {
        return viewItem.getLink().getIpsProject();
    }
}
