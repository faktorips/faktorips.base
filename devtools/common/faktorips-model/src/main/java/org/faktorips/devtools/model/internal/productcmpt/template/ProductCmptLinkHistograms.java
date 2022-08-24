/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.template;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;

import com.google.common.collect.Maps;

import org.faktorips.devtools.model.internal.util.Histogram;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink.LinkIdentifier;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.values.Decimal;

/**
 * A ProductCmptLinkHistograms holds information about histograms for product component link
 * cardinalities for all links from all {@code IProductCmptLinkContainer} objects in a collection.
 */
public class ProductCmptLinkHistograms {

    private final Map<LinkIdentifier, Histogram<Cardinality, IProductCmptLinkContainer>> linkHistograms = new LinkedHashMap<>();

    private ProductCmptLinkHistograms(Collection<LinkIdentifier> links,
            Collection<? extends IProductCmptLinkContainer> container) {
        for (LinkIdentifier linkIdentifier : links) {
            Histogram<Cardinality, IProductCmptLinkContainer> histogram = new LinkContainerHistogram(
                    getCardinality(linkIdentifier), Collections.unmodifiableCollection(container));
            linkHistograms.put(linkIdentifier, histogram);
        }
    }

    /** Returns the histogram for the given link identifier. */
    public Histogram<Cardinality, IProductCmptLinkContainer> getHistogram(LinkIdentifier linkIdentifier) {
        return linkHistograms.get(linkIdentifier);
    }

    /** Returns the entries for all histograms. */
    public Set<Entry<LinkIdentifier, Histogram<Cardinality, IProductCmptLinkContainer>>> getEntries() {
        return linkHistograms.entrySet();
    }

    /** Returns the number of histograms. */
    public int size() {
        return linkHistograms.size();
    }

    /**
     * A function to obtain a link's cardinality.
     */
    private Function<IProductCmptLinkContainer, Cardinality> getCardinality(final LinkIdentifier linkIdentifier) {
        return linkContainer -> {
            if (linkContainer == null) {
                return null;
            } else {
                IProductCmptLink link = linkIdentifier.getValueFrom(linkContainer);
                return link == null ? Cardinality.UNDEFINED : link.getCardinality();
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
    public static ProductCmptLinkHistograms createFor(Collection<? extends IProductCmptLinkContainer> containers) {
        Set<LinkIdentifier> links = new LinkedHashSet<>();
        for (IProductCmptLinkContainer container : containers) {
            for (IProductCmptLink link : container.getLinksAsList()) {
                links.add(new LinkIdentifier(link));
            }
        }
        return new ProductCmptLinkHistograms(links, containers);
    }

    /**
     * A specialized histogram for link containers. Overrides the {@link #getBestValue(Decimal)}
     * method to handle the special (0..0, 0) cardinality in a container.
     */
    static class LinkContainerHistogram extends Histogram<Cardinality, IProductCmptLinkContainer> {

        LinkContainerHistogram(Function<IProductCmptLinkContainer, Cardinality> elementToValueFunction,
                Collection<IProductCmptLinkContainer> elements) {
            super(elementToValueFunction, elements);
        }

        /**
         * Returns the best value, ignoring the special (0..0, 0) cardinality for missing links: if
         * the best value would be (0..0, 0), the second best value is returned if it still is above
         * the given threshold. Otherwise {@code BestValue.missingValue()} is returned.
         */
        @Override
        public BestValue<Cardinality> getBestValue(Decimal threshold) {
            SortedMap<Cardinality, Decimal> relativeDistribution = Maps.newTreeMap(super.getRelativeDistribution());
            relativeDistribution.remove(Cardinality.UNDEFINED);
            Cardinality candidateValue = relativeDistribution.firstKey();
            return getBestValue(threshold, relativeDistribution, candidateValue);
        }
    }
}
