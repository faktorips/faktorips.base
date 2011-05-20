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

package org.faktorips.devtools.core.model.businessfct;

import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.pctype.IValidationRule;

/**
 * These kind of business functions are used to specify that validation rules are only applicable in
 * a specific business context. The only have a name like 'Offer' oder 'Proposal' or 'NewBusiness'.
 * There is no user interface in Faktor-IPS to create this kind of business functions as we want to
 * re-think how validation rules can be applied in different business contexts. These kind of
 * business functions will be removed in future versions.
 * <p>
 * There is another business function concept which is defined by {@link IBusinessFunction}.
 * 
 * @see IValidationRule#getBusinessFunctions()
 */
public interface BusinessFunction extends IIpsObject {
    // see documentation above
}
