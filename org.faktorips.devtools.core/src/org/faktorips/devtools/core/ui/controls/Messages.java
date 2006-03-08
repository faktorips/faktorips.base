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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.controls.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String RangeEditControl_titleRange;

	public static String RangeEditControl_labelMinimum;

	public static String RangeEditControl_labelMaximum;

	public static String RangeEditControl_labelStep;

	public static String IpsPckFragmentRefControl_titleBrowse;

	public static String EnumValueSetEditControl_titleValues;

	public static String EnumValueSetEditControl_colName_1;

	public static String EnumValueSetEditControl_colName_2;

	public static String PcTypeRefControl_title;

	public static String PcTypeRefControl_description;

	public static String ValueSetEditControl_labelType;

	public static String TableStructureRefControl_title;

	public static String TableStructureRefControl_description;

	public static String IpsPckFragmentRootRefControl_title;

	public static String DatatypeRefControl_title;

	public static String DescriptionControl_title;

	public static String IpsObjectCompletionProcessor_msgNoProject;

	public static String IpsObjectCompletionProcessor_msgInternalError;

	public static String ProductCmptRefControl_title;

	public static String ProductCmptRefControl_description;

	public static String IpsObjectRefControl_title;
}
