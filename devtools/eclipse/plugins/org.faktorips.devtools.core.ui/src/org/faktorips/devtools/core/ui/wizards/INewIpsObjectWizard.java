/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

/**
 * Interface for new ips object creating wizards.
 * 
 * @author Joerg Ortmann
 */
public interface INewIpsObjectWizard extends INewWizard {

    /**
     * Returns the type of the object created by the wizard.
     */
    IpsObjectType getIpsObjectType();
}
