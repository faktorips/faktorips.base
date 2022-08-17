/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
    void store(IDialogSettings settings);

    /**
     * reads the dialog setting and uses the values for the actual search
     */
    void read(IDialogSettings settings);

    /**
     * returns, if this part of a search is valid and so the search is executable
     * 
     */
    boolean isValid();

}
