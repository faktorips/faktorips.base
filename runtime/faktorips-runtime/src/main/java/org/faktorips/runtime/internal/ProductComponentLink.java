/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import org.faktorips.runtime.CardinalityRange;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IProductComponentLinkSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class that represents a link/relation between product components.
 * <p>
 * As of FIPS 3.2 the cardinalities standard OPTIONAL, OBLIGATORY and FULL_RANGE are provided by the
 * {@link CardinalityRange} class.
 *
 * @see CardinalityRange
 */
public class ProductComponentLink<T extends IProductComponent> extends RuntimeObject
        implements IProductComponentLink<T>, IXmlPersistenceSupport {

    private final IProductComponentLinkSource source;
    private CardinalityRange cardinality;
    private String targetName;
    private String targetId;
    private String associationName;

    /**
     * Creates a new link for the given product component generation. Target and cardinality must be
     * set by invoking <code>initFromXml</code>.
     */
    public ProductComponentLink(IProductComponentGeneration source) {
        this((IProductComponentLinkSource)source);
    }

    /**
     * Creates a new link to the given target for the given product component generation using the
     * cardinality (0,*).
     *
     * @throws NullPointerException if any of the parameters is {@code null}.
     *
     */
    public ProductComponentLink(IProductComponentGeneration source, T target) {
        this((IProductComponentLinkSource)source, target, CardinalityRange.FULL_RANGE);
    }

    /**
     * Creates a new link with the given cardinality to the given target for the given product
     * component generation.
     *
     * @throws NullPointerException if any of the parameters is {@code null}.
     */
    public ProductComponentLink(IProductComponentGeneration source, T target, CardinalityRange cardinality) {
        this((IProductComponentLinkSource)source, target, cardinality);
    }

    /**
     * Creates a new link for the given product component/generation. Target and cardinality must be
     * set by invoking <code>initFromXml</code>.
     */
    public ProductComponentLink(IProductComponentLinkSource source) {
        this.source = source;
    }

    /**
     * Creates a new link to the given target for the given product component/generation using the
     * cardinality (0,*).
     *
     * @throws NullPointerException if any of the parameters is {@code null}.
     *
     */
    public ProductComponentLink(IProductComponentLinkSource source, T target) {
        this(source, target, CardinalityRange.FULL_RANGE);
    }

    /**
     * Creates a new link to the given target and association name for the given product
     * component/generation using the cardinality (0,*).
     *
     * @throws NullPointerException if any of the parameters is {@code null}.
     *
     */
    public ProductComponentLink(IProductComponentLinkSource source, T target, String associationName) {
        this(source, target);

        if (associationName == null) {
            throw new NullPointerException("The associationName for the ProductComponentLink may not be null.");
        }
        this.associationName = associationName;
    }

    /**
     * Creates a new link with the given cardinality to the given target for the given product
     * component/generation.
     *
     * @throws NullPointerException if any of the parameters is {@code null}.
     */
    public ProductComponentLink(IProductComponentLinkSource source, T target, CardinalityRange cardinality) {
        if (source == null) {
            throw new NullPointerException("The source for the ProductComponentLink may not be null.");
        }
        this.source = source;
        if (target == null) {
            throw new NullPointerException("The targetId for the ProductComponentLink may not be null.");
        }
        targetId = target.getId();
        targetName = target.getQualifiedName();
        if (cardinality == null) {
            throw new NullPointerException("The cardinality for the ProductComponentLink may not be null.");
        }
        this.cardinality = cardinality;
    }

    /**
     * Creates a new link with the cardinality and association name to the given target for the
     * given product component/generation.
     *
     * @throws NullPointerException if any of the parameters is {@code null}.
     */
    public ProductComponentLink(IProductComponentLinkSource source, T target, CardinalityRange cardinality,
            String associationName) {
        this(source, target, cardinality);

        if (associationName == null) {
            throw new NullPointerException("The associationName for the ProductComponentLink may not be null.");
        }
        this.associationName = associationName;
    }

    @Override
    public CardinalityRange getCardinality() {
        return cardinality;
    }

    @Override
    public void initFromXml(Element element) {
        associationName = element.getAttribute("association");
        targetId = element.getAttribute("targetRuntimeId");
        targetName = element.getAttribute("target");
        String maxStr = element.getAttribute("maxCardinality");
        Integer maxCardinality = null;
        if ("*".equals(maxStr) || "n".equals(maxStr.toLowerCase())) {
            maxCardinality = Integer.valueOf(Integer.MAX_VALUE);
        } else {
            maxCardinality = Integer.valueOf(maxStr);
        }

        Integer minCardinality = Integer.valueOf(element.getAttribute("minCardinality"));
        Integer defaultCardinality = Integer.valueOf(element.getAttribute("defaultCardinality"));
        if (maxCardinality == null || Integer.valueOf(0).equals(maxCardinality)) {
            cardinality = CardinalityRange.EXCLUDED;
        } else {
            cardinality = new CardinalityRange(minCardinality, maxCardinality, defaultCardinality);
        }
        initExtensionPropertiesFromXml(element);
    }

    @Override
    public Element toXml(Document document) {
        Integer upperBound = getCardinality().getUpperBound();
        Element linkElement = document.createElement("Link");
        linkElement.setAttribute("association", getAssociationName());
        linkElement.setAttribute("defaultCardinality", Integer.toString(getCardinality().getDefaultCardinality()));
        linkElement.setAttribute("maxCardinality",
                upperBound == Integer.MAX_VALUE ? "*" : Integer.toString(upperBound));
        linkElement.setAttribute("minCardinality", Integer.toString(getCardinality().getLowerBound()));
        linkElement.setAttribute("target", targetName);
        linkElement.setAttribute("targetRuntimeId", getTargetId());
        writeExtensionPropertiesToXml(linkElement);
        return linkElement;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getTarget() {
        try {
            return (T)source.getRepository().getExistingProductComponent(targetId);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public String getTargetId() {
        return targetId;
    }

    @Override
    public String getAssociationName() {
        return associationName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(targetId);
        sb.append('(');
        sb.append(cardinality.getLowerBound());
        sb.append("..");
        sb.append(Integer.valueOf(Integer.MAX_VALUE).equals(cardinality.getUpperBound()) ? "*"
                : cardinality.getUpperBound());
        sb.append(", default:");
        sb.append(cardinality.getDefaultCardinality());
        sb.append(')');
        return sb.toString();
    }

    @Override
    public IProductComponentLinkSource getSource() {
        return source;
    }

}
