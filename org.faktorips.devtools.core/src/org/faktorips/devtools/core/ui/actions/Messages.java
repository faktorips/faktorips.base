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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.actions.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String AddIpsNatureAction_defaultRuntimeIdPrefix;
    public static String AddIpsNatureAction_defaultSourceFolderName;
    public static String AddIpsNatureAction_dialogMessage;
    public static String AddIpsNatureAction_dialogTitle;
    public static String AddIpsNatureAction_ErrorNoSourceFolderName;
    public static String AddIpsNatureAction_fullProject;
    public static String AddIpsNatureAction_modelProject;
    public static String AddIpsNatureAction_productDefinitionProject;
    public static String AddIpsNatureAction_ProjectType;
    public static String AddIpsNatureAction_runtimeIdPrefix;
    public static String AddIpsNatureAction_sourceFolderName;
    public static String FindProductReferencesAction_description;
    public static String FindProductReferencesAction_name;
    public static String FindPolicyReferencesAction_description;
    public static String FindPolicyReferencesAction_name;
    public static String ShowAttributesAction_description;
    public static String ShowAttributesAction_name;
    public static String ShowStructureAction_description;
    public static String ShowStructureAction_name;
    public static String OpenEditorAction_name;
    public static String OpenEditorAction_description;
    public static String OpenEditorAction_tooltip;
	public static String IpsAction_msgUnsupportedSelection;
	public static String IpsDeepCopyAction_name;
	public static String IpsPasteAction_errorTitle;
	public static String IpsPasteAction_msgSrcAndTargetSame;
	public static String IpsPasteAction_suggestedNamePrefixSimple;
	public static String IpsPasteAction_suggestedNamePrefixComplex;
	public static String IpsPasteAction_titleNamingConflict;
	public static String IpsPasteAction_msgNamingConflict;
	public static String IpsPasteAction_msgFileAllreadyExists;
	public static String AddIpsNatureAction_noJavaProject;
	public static String AddIpsNatureAction_errorTitle;
	public static String AddIpsNatureAction_msgIPSNatureAlreadySet;
	public static String AddIpsNatureAction_msgSourceInProjectImpossible;
	public static String AddIpsNatureAction_msgErrorCreatingIPSProject;
	public static String AddIpsNatureAction_titleAddFaktorIpsNature;
	public static String NewProductComponentAction_name;
	public static String RenameAction_name;
	public static String MoveAction_name;
	public static String NewProductCmptRelationAction_name;
	public static String NewPolicyComponentTypeAction_name;
	public static String ChangeWorkingDateAction_title;
	public static String ChangeWorkingDateAction_description;
	public static String ChangeWorkingDateAction_errorFallbackMessageParameter;
	public static String ChangeWorkingDateAction_errorPrefix;
	public static String IpsDeepCopyAction_nameNewVersion;
	public static String IpsDeepCopyAction_titleNoVersion;
	public static String IpsDeepCopyAction_msgNoVersion;
	public static String NewTableContentAction_name;
	public static String NewFolderAction_titleNewFolder;
	public static String NewFolderAction_descriptionNewFolder;
	public static String NewFolderAction_valueNewFolder;
	public static String NewFolderAction_msgNoParentFound;
	public static String NewFolderAction_msgFolderAllreadyExists;
	public static String NewFolderAction_name;
	public static String IpsTestCaseAction_name;
	public static String IpsTestCaseAction_description;
	public static String IpsTestCaseAction_tooltip;
	public static String NewTestCaseAction_name;
	public static String NewFileResourceAction_name;
	public static String NewFileResourceAction_description;
    public static String NewFolderAction_FoldernameMustNotContainBlanks;
    public static String NewFolderAction_InvalidFoldername;
    public static String IpsPropertiesAction_name;
    public static String IpsTestAction_titleCantRunTest;
    public static String IpsTestAction_msgCantRunTest;
}
