/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal;

import org.faktorips.runtime.CardinalityRange;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.w3c.dom.Element;

/**
 * Class that represents a link/relation between product components.
 * <p/>
 * As of FIPS 3.2 the cardinalities standard OPTIONAL, OBLIGATORY and FULL_RANGE are provided by the
 * {@link CardinalityRange} class.
 * 
 * @see CardinalityRange
 * 
 * @author Daniel Hohenberger
 */
public class ProductComponentLink<E extends IProductComponent> extends RuntimeObject implements
        IProductComponentLink<E> {

    private final IProductComponentGeneration source;
    private CardinalityRange cardinality;
    private String targetId;
    private String associationName;

    /**
     * Creates a new link for the given product component generation. Target and cardinality must be
     * set by invoking <code>initFromXml</code>.
     */
    public ProductComponentLink(IProductComponentGeneration source) {
        this.source = source;
    }

    /**
     * Creates a new link to the given target for the given product component generation using the
     * cardinality (0,*).
     * 
     * @throws NullPointerException if any of the parameters is {@code null}.
     */
    public ProductComponentLink(IProductComponentGeneration source, E target) {
        this(source, target, CardinalityRange.FULL_RANGE);
    }

    /**
     * Creates a new link with the given cardinality to the given target for the given product
     * component generation.
     * 
     * @throws NullPointerException if any of the parameters is {@code null}.
     */
    public ProductComponentLink(IProductComponentGeneration source, E target, CardinalityRange cardinality) {
        if (source == null) {
            throw new NullPointerException("The source for the ProductComponentLink may not be null.");
        }
        this.source = source;
        if (target == null) {
            throw new NullPointerException("The targetId for the ProductComponentLink may not be null.");
        }
        this.targetId = target.getId();
        if (cardinality == null) {
            throw new NullPointerException("The cardinality for the ProductComponentLink may not be null.");
        }
        this.cardinality = cardinality;
    }

    public CardinalityRange getCardinality() {
        return cardinality;
    }

    public void initFromXml(Element element) {
        associationName = element.getAttribute("association");
        targetId = element.getAttribute("targetRuntimeId");
        String maxStr = element.getAttribute("maxCardinality");
        Integer maxCardinality = null;
        if ("*".equals(maxStr) || "n".equals(maxStr.toLowerCase())) {
            maxCardinality = new Integer(Integer.MAX_VALUE);
        } else {
            maxCardinality = Integer.valueOf(maxStr);
        }

        Integer minCardinality = Integer.valueOf(element.getAttribute("minCardinality"));
        Integer defaultCardinality = Integer.valueOf(element.getAttribute("defaultCardinality"));
        cardinality = new CardinalityRange(minCardinality, maxCardinality, defaultCardinality);
        initExtensionPropertiesFromXml(element);
    }

    @SuppressWarnings("unchecked")
    public E getTarget() {
        try {
            return (E)source.getRepository().getExistingProductComponent(targetId);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public String getTargetId() {
        return targetId;
    }

    public String getAssociationName() {
        return associationName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(targetId);
        sb.append('(');
        sb.append(cardinality.getLowerBound());
        sb.append("..");
        sb.append(new Integer(Integer.MAX_VALUE).equals(cardinality.getUpperBound()) ? "*" : cardinality
                .getUpperBound());
        sb.append(", default:");
        sb.append(cardinality.getDefaultCardinality());
        sb.append(')');
        return sb.toString();
    }

    public IProductComponentGeneration getSource() {
        return source;
    }

}
