/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Represents a link that has the wrong parent, e.g. the parent is the generation but the link
 * should be part of a product component (or the other way round). This happens if the corresponding
 * association's changing over time property was changed from "changing over time" to "static" (or
 * the other way round). Instances (links) of static associations must be part of the product
 * component, links of changing associations must be part of product component generations.
 * <p/>
 * {@link #fix()} "moves" the link from a product component to all its generations or from the
 * latest generation to the product component. In the first case a link instance is created in each
 * generation with the same (original) target. In the latter case a link instance is created in the
 * product component with the original target. Links of "old" generations will not be transferred to
 * the product component. However all original link instances are deleted. This includes out-dated
 * link instances of "older" generations.
 * 
 * @author widmaier
 */
public class LinkChangingOverTimeMismatchEntry implements IDeltaEntry {

    private final IProductCmptLink link;
    private final IProductCmptTypeAssociation association;
    private String targetName;

    public LinkChangingOverTimeMismatchEntry(IProductCmptTypeAssociation association, IProductCmptLink link) {
        this.association = association;
        this.link = link;

        try {
            targetName = link.findTarget(link.getIpsProject()).getUnqualifiedName();
        } catch (CoreException e) {
            targetName = link.getTarget();
        }
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.LINK_CHANGING_OVER_TIME_MISMATCH;
    }

    @Override
    public String getDescription() {
        String linkCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(link);
        String generationLabelByNamingConvention = IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNameSingular(true);
        if (getAssociation().isChangingOverTime()) {
            return NLS.bind(Messages.LinkChangingOverTimeMismatchEntry_Description_GenToProdCmpt, new String[] {
                    linkCaption, targetName, generationLabelByNamingConvention });
        } else {
            if (isLinkPartOfLatestGeneration()) {
                return NLS.bind(Messages.LinkChangingOverTimeMismatchEntry_Description_ProdCmptToGen, linkCaption,
                        targetName);
            } else {
                return NLS.bind(Messages.LinkChangingOverTimeMismatchEntry_Description_RemoveOnly, new String[] {
                        linkCaption, targetName, generationLabelByNamingConvention });
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

    private boolean isLinkPartOfProductComponent() {
        return getLink().getProductCmptLinkContainer().equals(getLink().getProductCmpt());
    }

    protected IProductCmptTypeAssociation getAssociation() {
        return association;
    }

    /**
     * Returns the respective link.
     */
    public IProductCmptLink getLink() {
        return link;
    }

    /**
     * Returns <code>true</code> if fixing this delta entry moves the link from one parent to
     * another. That is if the link is part of the product component or part of the latest
     * generation. Returns <code>false</code> if {@link #fix()} only removes the link. The link is
     * part of an old generation in the latter case.
     */
    public boolean isMovingLink() {
        return isLinkPartOfProductComponent() || isLinkPartOfLatestGeneration();
    }

}
