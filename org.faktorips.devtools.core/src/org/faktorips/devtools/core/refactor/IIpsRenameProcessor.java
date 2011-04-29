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

package org.faktorips.devtools.core.refactor;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;

/**
 * Describes a specific Faktor-IPS "Rename" refactoring.
 * 
 * @author Alexander Weickmann
 */
public interface IIpsRenameProcessor extends IIpsRefactoringProcessor {

    /**
     * Sets the new name for the {@link IIpsElement} to be refactored.
     * 
     * @param newName New name for the {@link IIpsElement} to be refactored.
     * 
     * @throws NullPointerException If the parameter is null
     */
    public void setNewName(String newName);

    /**
     * Sets the new plural name for the {@link IIpsElement} to be refactored.
     * 
     * @param newPluralName New plural name for the {@link IIpsElement} to be refactored
     * 
     * @throws NullPointerException If the parameter is null
     */
    public void setNewPluralName(String newPluralName);

    /**
     * Sets whether the runtime ID of {@link IProductCmpt} should be adapted.
     * 
     * @param adaptRuntimeId Flag indicating whether to adapt runtime IDs.
     */
    public void setAdaptRuntimeId(boolean adaptRuntimeId);

    /**
     * Returns the element's original name.
     */
    public String getOriginalName();

    /**
     * Returns the element's original plural name.
     */
    public String getOriginalPluralName();

    /**
     * Returns the element's new name.
     */
    public String getNewName();

    /**
     * Returns the element's new plural name.
     */
    public String getNewPluralName();

    /**
     * Returns whether a plural name refactoring is required.
     */
    public boolean isPluralNameRefactoringRequired();

    /**
     * Returns whether the runtime ID of {@link IProductCmpt} should be adapted.
     */
    public boolean isAdaptRuntimeId();

}
