/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.businessfct;

import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.runtime.ValidationContext;

/**
 * These kind of business functions are used to specify that validation rules are only applicable in
 * a specific business context. They only have a name like 'Offer' or 'Proposal' or 'NewBusiness'.
 * There is no user interface in Faktor-IPS to create this kind of business functions as we want to
 * re-design how validation rules can be applied in different business contexts. These kind of
 * business functions will be removed in future versions.
 * <p>
 * There is another business function concept which is defined by {@code IBusinessFunction}.
 * 
 * @see IValidationRule#getBusinessFunctions()
 * @deprecated for removal since 21.6; You can use any object type as a value set to
 *             {@link ValidationContext#setValue(String, Object)} to control execution of your
 *             validation rules.
 */
@Deprecated
public interface BusinessFunction extends IIpsObject {
    /**
     * Validation message code to indicate that business functions are a deprecated concept.
     */
    public static final String MSGCODE_DEPRECATED = "BUSINESS_FUNCTION-DEPRECATED"; //$NON-NLS-1$

}
