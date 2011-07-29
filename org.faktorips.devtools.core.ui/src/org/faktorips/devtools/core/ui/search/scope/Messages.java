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

package org.faktorips.devtools.core.ui.search.scope;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.search.scope.messages"; //$NON-NLS-1$
    public static String IpsSearchProjectsScope_scopeTypeLabelPlural;
    public static String IpsSearchProjectsScope_scopeTypeLabelSingular;
    public static String IpsSearchScope_scopeWithMoreThanTwoSelectedElements;
    public static String IpsSearchScope_scopeWithOneSelectedElement;
    public static String IpsSearchScope_scopeWithTwoSelectedElements;
    public static String IpsSearchScope_undefinedScope;
    public static String IpsSearchWorkingSetScope_scopeTypeLabelPlural;
    public static String IpsSearchWorkingSetScope_scopeTypeLabelSingular;
    public static String IpsSearchWorkspaceScope_scopeTypeLabel;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        //
    }
}
