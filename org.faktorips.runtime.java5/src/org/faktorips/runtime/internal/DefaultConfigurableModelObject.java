/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IUnresolvedReference;
import org.w3c.dom.Element;

/**
 * Default implementation that can be used for all policy components.
 */
public class DefaultConfigurableModelObject extends AbstractConfigurableModelObject {

    private Calendar effectiveFrom = new GregorianCalendar();

    /**
     * Creates a new policy component effective from now on.
     */
    protected DefaultConfigurableModelObject() {
        // nothing to do
    }

    /**
     * Creates a new policy component based on the given product component that is effective from
     * now on.
     * 
     * @throws NullPointerException if productCmpt is <code>null</code>.
     */
    protected DefaultConfigurableModelObject(IProductComponent productCmpt) {
        this(productCmpt, new GregorianCalendar());
    }

    /**
     * Creates a new policy component that is effective from the given date.
     * 
     * @throws NullPointerException if productCmpt or effectiveDate is <code>null</code>.
     */
    protected DefaultConfigurableModelObject(Calendar effectiveFrom) {
        this(null, effectiveFrom);
    }

    /**
     * Creates a new policy component based on the given product component that is effective from
     * the given date.
     * 
     * @throws NullPointerException if productCmpt or effectiveDate is <code>null</code>.
     */
    protected DefaultConfigurableModelObject(IProductComponent productCmpt, Calendar effectiveFrom) {
        super(productCmpt);
        if (effectiveFrom == null) {
            throw new NullPointerException("EffectiveFrom was null!");
        }
        this.effectiveFrom = effectiveFrom;
    }

    /**
     * Overridden.
     */
    public Calendar getEffectiveFromAsCalendar() {
        return effectiveFrom;
    }

    /**
     * Sets the new effective date.
     * 
     * @throws NullPointerException if effectice date is <code>null</code>.
     */
    public void setEffectiveFrom(Calendar effectiveDate) {
        if (effectiveDate == null) {
            throw new NullPointerException("EffectiveDate was null!");
        }
        this.effectiveFrom = effectiveDate;
    }

    /**
     * @param propMap The property map
     */
    protected void initPropertiesFromXml(Map<String, String> propMap) {
        // empty default implementation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractModelObject createChildFromXml(Element childEl) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IUnresolvedReference createUnresolvedReference(Object objectId, String targetRole, String targetId)
            throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeChildModelObjectInternal(IModelObject child) {
        // empty default implementation
    }

}
