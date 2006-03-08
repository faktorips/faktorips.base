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
    public static String FindReferenceAction_description;
    public static String FindReferenceAction_name;
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
}
