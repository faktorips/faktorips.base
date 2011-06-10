/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.model.scope;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.search.model.scope.messages"; //$NON-NLS-1$
    public static String ModelSearchProjectsScope_scopeTypeLabelPlural;
    public static String ModelSearchProjectsScope_scopeTypeLabelSingular;
    public static String ModelSearchScope_scopeWithMoreThanTwoSelectedElements;
    public static String ModelSearchScope_scopeWithOneSelectedElement;
    public static String ModelSearchScope_scopeWithTwoSelectedElements;
    public static String ModelSearchScope_undefinedScope;
    public static String ModelSearchWorkingSetScope_scopeTypeLabelPlural;
    public static String ModelSearchWorkingSetScope_scopeTypeLabelSingular;
    public static String ModelSearchWorkspaceScope_scopeTypeLabel;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        //
    }
}
