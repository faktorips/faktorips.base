/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.jface.wizard.IWizard;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;

/**
 * Interface for a wizard wich disables the next button if the editing object is not valid. If a
 * page is displayed the next button from the previous page is always enabled.
 * 
 * @author Joerg Ortmann
 */
public interface IBlockedValidationWizard extends IWizard {

    /**
     * Returns <code>true</code> if the page with the given number is valid or <code>false</code> if
     * the page isn't valid. The page is valid if the editing object is valid or a page after the
     * given page number was displayed.
     */
    public boolean isPageValid(int pageNumber);

    /**
     * Sets the maximum page number which was displayed.
     */
    public void setMaxPageShown(int pageNumber);

    /**
     * Starts given runnable in an asynchronous manner.
     */
    public void postAsyncRunnable(Runnable runnable);

    /**
     * Returns the ui controller.
     */
    public IpsObjectUIController getController();
}
