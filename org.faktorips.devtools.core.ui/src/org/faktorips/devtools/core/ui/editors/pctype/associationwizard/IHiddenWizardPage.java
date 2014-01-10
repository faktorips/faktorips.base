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

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

/**
 * An instance of this interface can be used by wizard pages to become invisible if the page is not
 * necessary.
 * 
 * @author Joerg Ortmann
 */
public interface IHiddenWizardPage {

    /**
     * Returns <code>true</code> if the given page is visible
     */
    public boolean isPageVisible();
}
