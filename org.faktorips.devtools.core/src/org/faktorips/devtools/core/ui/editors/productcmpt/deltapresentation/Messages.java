/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String ProductCmptDeltaContentProvider_msgTypeMismatch;

	public static String ProductCmptDeltaContentProvider_msgNoAttribute;

	public static String ProductCmptDeltaContentProvider_msgNoConfigElement;

	public static String ProductCmptDeltaContentProvider_msgNoRelation;

	public static String ProductCmptDeltaContentProvider_msgValuesetMismatch;

    public static String ProductCmptDeltaDialog_messageWarningRecentGenerationCouldBeChanged;

	public static String ProductCmptDeltaType_missingAttribute;

	public static String ProductCmptDeltaType_missingCfgElement;

	public static String ProductCmptDeltaType_valuesetMismatch;

	public static String ProductCmptDeltaType_cfgElementTypeMismatch;

	public static String ProductCmptDeltaType_missingRelationDefinition;

	public static String ProductCmptDeltaLabelProvider_label_undefined;

	public static String ProductCmptDeltaDialog_labelSelectGeneration;

	public static String ProductCmptDeltaDialog_labelSelectedDifferences;

	public static String ProductCmptDeltaDialog_title;

	public static String ProductCmptDeltaDialog_message;

	public static String ProductCmptDeltaDialog_fix;

	public static String ProductCmptDeltaDialog_ignore;

    public static String ProductCmptDeltaContentProvider_msgMissingContentUsage;

    public static String ProductCmptDeltaContentProvider_msgMissingStructureUsage;

    public static String ProductCmptDeltaType_missingTableStructureUsage;

    public static String ProductCmptDeltaType_missingTableContentUsage;
}
