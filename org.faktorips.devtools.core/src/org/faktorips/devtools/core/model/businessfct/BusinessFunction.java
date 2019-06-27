/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.businessfct;

import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.pctype.IValidationRule;

/**
 * These kind of business functions are used to specify that validation rules are only applicable in
 * a specific business context. They only have a name like 'Offer' or 'Proposal' or 'NewBusiness'.
 * There is no user interface in Faktor-IPS to create this kind of business functions as we want to
 * re-design how validation rules can be applied in different business contexts. These kind of
 * business functions will be removed in future versions.
 * <p>
 * There is another business function concept which is defined by {@link IBusinessFunction}.
 * 
 * @see IValidationRule#getBusinessFunctions()
 */
public interface BusinessFunction extends IIpsObject {
    // see documentation above
}
