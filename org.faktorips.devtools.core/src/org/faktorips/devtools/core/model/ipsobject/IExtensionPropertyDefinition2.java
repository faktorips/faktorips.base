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

package org.faktorips.devtools.core.model.ipsobject;


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
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer part} for which the
     *            extension property should be active or inactive
     * @return <code>true</code> if this extension property is active for the given part
     */
    public boolean isApplicableFor(IIpsObjectPartContainer ipsObjectPartContainer);

}
