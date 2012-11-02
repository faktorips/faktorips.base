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

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

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
 * {@link #fix()} moves the link from a product component to all its generations or from the latest
 * generation to the product component. In the first case a link instance is created in each
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

    public LinkChangingOverTimeMismatchEntry(IProductCmptTypeAssociation association, IProductCmptLink link) {
        this.association = association;
        this.link = link;
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.LINK_CHANGING_OVER_TIME_MISMATCH;
    }

    @Override
    public String getDescription() {
        String linkCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(link);
        if (getAssociation().isChangingOverTime()) {
            return NLS.bind("\"{0}\" will be moved to every generation.", linkCaption);
        } else {
            if (isLatestGeneration(getLink().getProductCmptLinkContainer())) {
                return NLS.bind("\"{0}\" will be moved to the product component.", linkCaption);
            } else {
                return NLS
                        .bind("\"{0}\" will be removed (only links from the latest generation can be moved to the product component).",
                                linkCaption);
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
        IProductCmptLinkContainer generation = getLink().getProductCmptLinkContainer();
        if (isLatestGeneration(generation)) {
            IProductCmptLink newLink = generation.getProductCmpt().newLink(getAssociation());
            newLink.copyFrom(getLink());
        } else {
            // nothing to do: link is part of an "older" generation
        }
    }

    private boolean isLatestGeneration(IProductCmptLinkContainer container) {
        IProductCmptGeneration latestGeneration = getLink().getProductCmpt().getLatestProductCmptGeneration();
        return container.equals(latestGeneration);
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

}
