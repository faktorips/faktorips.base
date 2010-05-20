/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 *  * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  *
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community
 * Lizenzvereinbarung - Version 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der
 * Auslieferung ist und auch unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen
 * werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 *******************************************************************************/

package org.faktorips.devtools.core.model.pctype;

import org.eclipse.osgi.util.NLS;

public class Messages {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.pctype.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AttributeType_changeable;
    public static String AttributeType_derived_by_explicit_method_call;
    public static String AttributeType_derived_on_the_fly;
    public static String AttributeType_constant;

}
