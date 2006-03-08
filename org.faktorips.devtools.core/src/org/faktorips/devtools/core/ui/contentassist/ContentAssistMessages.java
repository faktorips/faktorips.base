/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.contentassist;


import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 *
 * @since 3.0
 */
final class ContentAssistMessages extends NLS {

	private static final String BUNDLE_NAME= ContentAssistMessages.class.getName();

	private ContentAssistMessages() {
		// Do not instantiate
	}

	public static String ContentAssistHandler_contentAssistAvailable;
	public static String ContentAssistHandler_contentAssistAvailableWithKeyBinding;

	static {
		NLS.initializeMessages(BUNDLE_NAME, ContentAssistMessages.class);
	}
}