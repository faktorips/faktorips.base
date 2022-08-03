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
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.util.StringUtil;

/**
 * Represents a link that has the wrong parent, e.g. the parent is the generation but the link
 * should be part of a product component (or the other way round). This happens if the corresponding
 * association's changing over time property was changed from "changing over time" to "static" (or
 * the other way round). Instances (links) of static associations must be part of the product
 * component, links of changing associations must be part of product component generations.
 * <p>
 * {@link #fix()} "moves" the link from a product component to all its generations or from the
 * latest generation to the product component. In the first case a link instance is created in each
 * generation with the same (original) target. In the latter case a link instance is created in the
 * product component with the original target. Links of "old" generations will not be transferred to
 * the product component. However all original link instances are deleted. This includes out-dated
 * link instances of "older" generations.
 * 
 * @author widmaier
 */
public class LinkChangingOverTimeMismatchEntry extends AbstractDeltaEntryForLinks {

    private final IProductCmptTypeAssociation association;
    private final String unqualifiedTargetName;

    public LinkChangingOverTimeMismatchEntry(IProductCmptTypeAssociation association, IProductCmptLink link) {
        super(link);
        this.association = association;
        unqualifiedTargetName = StringUtil.unqualifiedName(link.getTarget());
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.LINK_CHANGING_OVER_TIME_MISMATCH;
    }

    @Override
    public String getDescription() {
        String linkCaption = IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(getLink());
        String generationLabelByNamingConvention = IIpsModelExtensions.get().getModelPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNameSingular(true);
        if (getAssociation().isChangingOverTime()) {
            return MessageFormat.format(Messages.LinkChangingOverTimeMismatchEntry_Description_GenToProdCmpt,
                    linkCaption, unqualifiedTargetName, generationLabelByNamingConvention);
        } else {
            if (isLinkPartOfLatestGeneration()) {
                return MessageFormat.format(Messages.LinkChangingOverTimeMismatchEntry_Description_ProdCmptToGen,
                        linkCaption,
                        unqualifiedTargetName);
            } else {
                return MessageFormat.format(Messages.LinkChangingOverTimeMismatchEntry_Description_RemoveOnly,
                        linkCaption, unqualifiedTargetName, generationLabelByNamingConvention);
            }
        }
    }

    /**
     * "Moves" the link from a product component to all its generations or from the latest
     * generation to the product component. In the first case a link instance is created in each
     * generation with the same (original) target. In the latter case a link instance is created in
     * the product component with the original target. Links of "old" generations will not be
     * transferred to the product component. However all original link instances are deleted. This
     * includes out-dated link instances of "older" generations. {@inheritDoc}
     */
    @Override
    public void fix() {
        if (getAssociation().isChangingOverTime()) {
            moveLinkToAllGenerations();
        } else {
            moveLatestGenerationLinksToProdCmpt();
        }
        // always delete out-dated link
        getLink().delete();
    }

    private void moveLinkToAllGenerations() {
        IProductCmptLinkContainer productCmpt = getLink().getProductCmptLinkContainer();
        for (IProductCmptGeneration gen : productCmpt.getProductCmpt().getProductCmptGenerations()) {
            IProductCmptLink newLink = gen.newLink(getAssociation());
            newLink.copyFrom(getLink());
        }
    }

    private void moveLatestGenerationLinksToProdCmpt() {
        if (isLinkPartOfLatestGeneration()) {
            IProductCmptLink newLink = getLink().getProductCmpt().newLink(getAssociation());
            newLink.copyFrom(getLink());
        } else {
            // nothing to do: link is part of an "older" generation
        }
    }

    private boolean isLinkPartOfLatestGeneration() {
        IProductCmptGeneration latestGeneration = getLink().getProductCmpt().getLatestProductCmptGeneration();
        return getLink().getProductCmptLinkContainer().equals(latestGeneration);
    }

    protected IProductCmptTypeAssociation getAssociation() {
        return association;
    }

}
