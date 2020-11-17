/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.ui.wizards.AbstractIpsObjectNewWizardPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;
import org.faktorips.devtools.model.enums.IEnumContent;

/**
 * A wizard responsible for the creation of a new {@link IEnumContent}.
 * 
 * @see IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class NewEnumContentWizard extends NewIpsObjectWizard {

    @Override
    protected AbstractIpsObjectNewWizardPage createFirstPage(IStructuredSelection selection) throws Exception {
        return new EnumContentPage(selection);
    }

}
