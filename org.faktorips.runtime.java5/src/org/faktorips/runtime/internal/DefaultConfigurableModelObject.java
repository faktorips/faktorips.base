/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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

    @Override
    protected AbstractModelObject createChildFromXml(Element childEl) {
        return null;
    }

    @Override
    protected IUnresolvedReference createUnresolvedReference(Object objectId, String targetRole, String targetId)
            throws Exception {
        return null;
    }

    @Override
    public void removeChildModelObjectInternal(IModelObject child) {
        // empty default implementation
    }

}
