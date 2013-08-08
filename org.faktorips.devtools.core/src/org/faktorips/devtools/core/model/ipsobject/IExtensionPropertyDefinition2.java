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

package org.faktorips.devtools.core.model.ipsobject;

import org.faktorips.devtools.core.model.IIpsModel;

/**
 * This interface extends the {@link IExtensionPropertyDefinition} interface by an additional method
 * to activate or deactivate the extension property depending on the concrete
 * {@link IIpsObjectPartContainer part}. This is especially interesting for extension properties
 * based on product definition parts because the implementer is able to decide whether the extension
 * is enabled or not depending on the concrete model object. For example an attribute value would
 * only need a special extension property if another property is set at the product component
 * attribute.
 * 
 * @see IExtensionPropertyDefinition
 * @since 3.10
 * @author dirmeier
 */
public interface IExtensionPropertyDefinition2 extends IExtensionPropertyDefinition {

    /**
     * This method is called by the extension property framework to decide whether this extension
     * property is applicable for the given {@link IIpsObjectPartContainer part} or not.
     * <p>
     * The active state is cached by the extension property framework. If the conditions have
     * changed you need to call {@link IIpsModel#clearExtensionPropertyCache()} to clear the cache.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer part} for which the
     *            extension property should be active or inactive
     * @return <code>true</code> if this extension property is active for the given part
     */
    public boolean isApplicableFor(IIpsObjectPartContainer ipsObjectPartContainer);

}
