/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.runtime.internal;

import org.faktorips.runtime.ILink;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.valueset.java5.IntegerRange;
import org.w3c.dom.Element;

/**
 * 
 * @author Daniel Hohenberger
 */
public class Link<E extends IProductComponent> extends RuntimeObject implements ILink<E> {

    private IProductComponentGeneration productComponentGeneration;
    private IntegerRange cardinality;
    private String targetId;
    private String associationName;

    /**
     * Creates a new link for the given product component generation. Target and cardinality must be
     * set by invoking <code>initFromXml</code>.
     */
    public Link(IProductComponentGeneration productComponentGeneration) {
        this.productComponentGeneration = productComponentGeneration;
    }

    /**
     * Creates a new link to the given target for the given product component generation using the
     * cardinality (0,*).
     */
    public Link(IProductComponentGeneration productComponentGeneration, E target) {
        this.productComponentGeneration = productComponentGeneration;
        this.targetId = target.getId();
        cardinality = new IntegerRange(0, Integer.MAX_VALUE);
    }

    /**
     * Creates a new link with the given cardinality to the given target for the given product
     * component generation.
     */
    public Link(IProductComponentGeneration productComponentGeneration, E target, IntegerRange cardinality) {
        this.productComponentGeneration = productComponentGeneration;
        this.targetId = target.getId();
        this.cardinality = cardinality;
    }

    /**
     * {@inheritDoc}
     */
    public IntegerRange getCardinality() {
        return cardinality;
    }

    /**
     * {@inheritDoc}
     */
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
        cardinality = new IntegerRange(minCardinality, maxCardinality);
        initExtensionPropertiesFromXml(element);
    }

    /**
     * {@inheritDoc}
     */
    public IRuntimeRepository getRepository() {
        return productComponentGeneration.getRepository();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public E getTarget() {
        try {
            return (E)getRepository().getExistingProductComponent(targetId);
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * {@inheritDoc}
     */
    public String getAssociationName() {
        return associationName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(targetId);
        sb.append('(');
        sb.append(cardinality.getLowerBound());
        sb.append("..");
        sb.append(cardinality.getUpperBound() == Integer.MAX_VALUE ? "*" : cardinality.getUpperBound());
        sb.append(')');
        return sb.toString();
    }

}
