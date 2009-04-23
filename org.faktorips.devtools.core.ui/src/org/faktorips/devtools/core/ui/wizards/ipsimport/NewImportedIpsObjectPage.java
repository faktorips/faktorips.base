/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.ipsimport;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

/**
 * Base wizard page to specify project, package and custom additional information like 
 * a name (or a table structure/enum type) for an imported IPS Object type.
 * 
 * @author Roman Grutza
 */
public abstract class NewImportedIpsObjectPage extends WizardPage {

    public NewImportedIpsObjectPage(String pageName) {
        super(pageName);
    }

    public NewImportedIpsObjectPage(String pageName, String title, ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }
    
    

}