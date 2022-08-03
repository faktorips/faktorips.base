/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.jface.wizard.IWizard;
import org.faktorips.devtools.core.ui.binding.BindingContext;

/**
 * Interface for a wizard which disables the next button if the editing object is not valid. If a
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
    boolean isPageValid(int pageNumber);

    /**
     * Sets the maximum page number which was displayed.
     */
    void setMaxPageShown(int pageNumber);

    /**
     * Starts given runnable in an asynchronous manner.
     */
    void postAsyncRunnable(Runnable runnable);

    /**
     * Returns the binding context.
     */
    BindingContext getBindingContext();
}
