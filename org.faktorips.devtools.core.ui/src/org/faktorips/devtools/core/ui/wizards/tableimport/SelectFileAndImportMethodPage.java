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

package org.faktorips.devtools.core.ui.wizards.tableimport;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Wizard page to import external data into an IPS Table Content where one can select a source file,
 * the table format and some other properties.
 * 
 * @author Roman Grutza
 */
public class SelectFileAndImportMethodPage extends
        org.faktorips.devtools.core.ui.wizards.ipsimport.SelectFileAndImportMethodPage {

    public SelectFileAndImportMethodPage(IStructuredSelection selection) {
        super(selection);
    }

    @Override
    protected String getLabelForImportIntoNewIpsObject() {
        return Messages.SelectFileAndImportMethodPage_labelImportNew;
    }

    @Override
    protected String getLabelForImportIntoExistingIpsObject() {
        return Messages.SelectFileAndImportMethodPage_labelImportExisting;
    }
}
