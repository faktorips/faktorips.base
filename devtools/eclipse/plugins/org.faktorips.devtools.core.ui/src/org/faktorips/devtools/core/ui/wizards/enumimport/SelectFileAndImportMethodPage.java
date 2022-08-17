/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumimport;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Wizard page to import external data into an IPS Enum Type or Content where one can select a
 * source file, the table format and some other properties.
 * 
 * @author Roman Grutza
 */
public class SelectFileAndImportMethodPage extends
        org.faktorips.devtools.core.ui.wizards.ipsimport.SelectFileAndImportMethodPage {

    public SelectFileAndImportMethodPage(IStructuredSelection selection) {
        super(selection);
    }

    @Override
    protected String getLabelForImportIntoExistingIpsObject() {
        return Messages.SelectFileAndImportMethodPage_labelImportExisting;
    }

    @Override
    protected String getLabelForImportIntoNewIpsObject() {
        return Messages.SelectFileAndImportMethodPage_labelImportNew;
    }

}
