/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
