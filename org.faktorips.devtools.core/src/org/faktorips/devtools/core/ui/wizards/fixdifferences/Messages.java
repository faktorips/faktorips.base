/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende:� Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de �
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.wizards.fixdifferences;

import org.eclipse.osgi.util.NLS;

/**
 * @author Daniel Hohenberger
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.fixdifferences.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

    public static String ElementSelectionPage_SelectElementsMessage;
    public static String FixDifferencesToModelWizard_beginTask;
    public static String FixDifferencesToModelWizard_Title;
    public static String OpenFixDifferencesToModelWizardAction_fixingNotPossibleReason;
    public static String OpenFixDifferencesToModelWizardAction_fixingNotPossibleTitle;

}
