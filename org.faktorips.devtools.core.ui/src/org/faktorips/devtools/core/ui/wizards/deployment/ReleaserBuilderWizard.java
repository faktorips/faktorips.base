/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.deployment;

import org.eclipse.jface.wizard.Wizard;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * The deployment wizard provides the basic ui for deployments of product definition projects. On
 * the first site you have to select a project
 * 
 * @author dirmeier
 */
public class ReleaserBuilderWizard extends Wizard {

    private ReleaserBuilderWizardSelectionPage selectionPage;

    public ReleaserBuilderWizard() {
        selectionPage = new ReleaserBuilderWizardSelectionPage();
    }

    @Override
    public void addPages() {
        addPage(selectionPage);
    }

    @Override
    public boolean performFinish() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setIpsProject(IIpsProject ipsProject) {
        selectionPage.setIpsProject(ipsProject);
    }

}
