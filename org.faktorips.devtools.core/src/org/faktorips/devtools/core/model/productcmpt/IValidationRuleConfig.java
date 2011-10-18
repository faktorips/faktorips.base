/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IValidationRule;

public interface IValidationRuleConfig extends IPropertyValue {

    public static final String PROPERTY_ACTIVE = "active"; //$NON-NLS-1$

    /**
     * Returns whether the configured {@link IValidationRule} is active.
     * 
     * @return <code>true</code> if the rule is active, <code>false</code> otherwise.
     */
    public boolean isActive();

    /**
     * Defines whether the {@link IValidationRule} configured by this {@link IValidationRuleConfig}
     * is active.
     */
    public void setActive(boolean active);

    /**
     * Returns the {@link IValidationRule} which is configured by this {@link IValidationRuleConfig}
     * . Returns <code>null</code> if no validation rule can be found.
     * <p>
     * This method searches the super type hierarchy.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IValidationRule findValidationRule(IIpsProject ipsProject) throws CoreException;

}
