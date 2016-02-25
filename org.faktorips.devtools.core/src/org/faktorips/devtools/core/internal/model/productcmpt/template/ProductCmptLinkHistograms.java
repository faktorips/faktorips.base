/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt.template;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import org.faktorips.devtools.core.internal.model.productcmpt.Cardinality;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink.LinkIdentifier;
import org.faktorips.devtools.core.util.Histogram;

/**
 * A ProductCmptLinkHistograms holds information about histograms for product component link
 * cardinalities for all links from all {@code IProductCmptLinkContainer} objects in a collection.
 */
public class ProductCmptLinkHistograms {

    private final Map<LinkIdentifier, Histogram<Cardinality, IProductCmptLink>> linkHistograms = new LinkedHashMap<LinkIdentifier, Histogram<Cardinality, IProductCmptLink>>();

    private ProductCmptLinkHistograms(Multimap<LinkIdentifier, IProductCmptLink> links) {
        for (LinkIdentifier linkIdentifier : links.keySet()) {
            Histogram<Cardinality, IProductCmptLink> histogram = new Histogram<Cardinality, IProductCmptLink>(
                    getCardinality(), links.get(linkIdentifier));
            linkHistograms.put(linkIdentifier, histogram);
        }
    }

    /** Returns the histogram for the given link identifier. */
    public Histogram<Cardinality, IProductCmptLink> getHistogram(LinkIdentifier linkIdentifier) {
        return linkHistograms.get(linkIdentifier);
    }

    /** Returns the entries for all histograms. */
    public Set<Entry<LinkIdentifier, Histogram<Cardinality, IProductCmptLink>>> getEntries() {
        return linkHistograms.entrySet();
    }

    /** Returns the number of histograms. */
    public int size() {
        return linkHistograms.size();
    }

    /** A function to obtain a link's cardinality. */
    private Function<IProductCmptLink, Cardinality> getCardinality() {
        return new Function<IProductCmptLink, Cardinality>() {
            @Override
            public Cardinality apply(IProductCmptLink link) {
                if (link == null) {
                    return null;
                } else {
                    return link.getCardinality();
                }
            }
        };
    }

    /**
     * Creates a new {@code ProductCmptLinkHistograms} that is filled with the histograms for all
     * links in the given containers.
     * <p>
     * Note that the histograms will <em>only</em> contain links from the containers themselves. If
     * the containers are {@link IProductCmpt product components} the links from their
     * {@link IProductCmptGeneration generations} are <em>not</em> considered in the histograms.
     */
    public static ProductCmptLinkHistograms createFor(Iterable<? extends IProductCmptLinkContainer> containers) {
        Multimap<LinkIdentifier, IProductCmptLink> links = LinkedHashMultimap.create();
        for (IProductCmptLinkContainer container : containers) {
            for (IProductCmptLink link : container.getLinksAsList()) {
                links.put(new LinkIdentifier(link), link);
            }
        }
        return new ProductCmptLinkHistograms(links);
    }

}
