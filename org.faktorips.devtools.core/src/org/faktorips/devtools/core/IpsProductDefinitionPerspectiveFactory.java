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

package org.faktorips.devtools.core;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Perspective for ProductDefinition.
 * 
 * @author Thorsten Guenther
 */
public class IpsProductDefinitionPerspectiveFactory implements IPerspectiveFactory {

    public final static String PRODUCTDEFINITIONPERSPECTIVE_ID = "org.faktorips.devtools.core.productDefinitionPerspective"; //$NON-NLS-1$

    @Override
    public void createInitialLayout(IPageLayout layout) {
        // nothing to do
    }

}
