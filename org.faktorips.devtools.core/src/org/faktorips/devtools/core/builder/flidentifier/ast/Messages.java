/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.builder.flidentifier.ast.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String AssociationNode_ListDatatypeDescriptionPrefix;

    public static String AttributeNode_defaultOfName;

    public static String ParameterNode_description;

    public static String EnumClassNode_description;

    private Messages() {
        // do not instantiate
    }
}
