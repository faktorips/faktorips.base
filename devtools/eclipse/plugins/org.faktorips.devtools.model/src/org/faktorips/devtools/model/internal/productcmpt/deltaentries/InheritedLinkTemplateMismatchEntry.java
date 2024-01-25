/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import java.text.MessageFormat;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * A link should inherit its configuration from a template but the configuration differs.
 *
 * @since 24.1.1
 */
public class InheritedLinkTemplateMismatchEntry extends AbstractDeltaEntryForLinks {

    private IProductCmptTypeAssociation association;
    private IProductCmptLink link;
    private Cardinality internalCardinality;
    private Cardinality templateCardinality;

    public InheritedLinkTemplateMismatchEntry(IProductCmptTypeAssociation association, IProductCmptLink link,
            Cardinality internalCardinality, Cardinality templateCardinality) {
        super(link);
        this.association = association;
        this.link = link;
        this.internalCardinality = internalCardinality;
        this.templateCardinality = templateCardinality;
    }

    @Override
    public void fix() {
        link.setCardinality(templateCardinality);
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.INHERITED_TEMPLATE_MISMATCH;
    }

    @Override
    public String getDescription() {
        return MessageFormat.format(Messages.InheritedTemplateMismatchEntry_desc,
                IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(association),
                internalCardinality,
                templateCardinality);
    }

}
