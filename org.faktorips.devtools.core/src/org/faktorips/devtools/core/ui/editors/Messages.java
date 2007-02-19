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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String IpsObjectEditor_fileHasChangesOnDiskMessage;
    public static String IpsObjectEditor_fileHasChangesOnDiskNoButton;
    public static String IpsObjectEditor_fileHasChangesOnDiskTitle;
    public static String IpsObjectEditor_fileHasChangesOnDiskYesButton;
    public static String IpsObjectEditorPage_ErrorsTitle;
    public static String IpsObjectEditorPage_ErrorTitle;
    public static String IpsObjectEditorPage_WarningsTitle;
    public static String IpsObjectEditorPage_WarningTitle;
    public static String IpsPartEditDialog_description;
	public static String IpsPartsComposite_buttonNew;
	public static String IpsPartsComposite_buttonEdit;
    public static String IpsPartsComposite_buttonShow;
	public static String IpsPartsComposite_buttonDelete;
	public static String IpsPartsComposite_buttonUp;
	public static String IpsPartsComposite_buttonDown;
	public static String DescriptionPage_description;
	public static String DescriptionSection_description;
	public static String IpsObjectEditor_msgEditorWillBeClosed;
    public static String UnparsableFilePage_fileContentIsNotParsable;

}
