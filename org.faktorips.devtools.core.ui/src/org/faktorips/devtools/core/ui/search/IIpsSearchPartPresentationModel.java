/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search;

import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * 
 * The interface provides methods for presentation models for the model and product search
 * <p>
 * It contains methods for storing and reading {@link IDialogSettings} and checking the validity of
 * the data of the presentation model.
 * 
 * 
 * @author dicker
 */
public interface IIpsSearchPartPresentationModel {

    /**
     * stores the actual values into the dialog settings
     */
    public void store(IDialogSettings settings);

    /**
     * reads the dialog setting and uses the values for the actual search
     */
    public void read(IDialogSettings settings);

    /**
     * returns, if this part of a search is valid and so the search is executable
     * 
     */
    public boolean isValid();

}