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

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.deepcopy.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String ProductStructureLabelProvider_undefined;
    public static String DeepCopyWizard_title;
    public static String SourcePage_title;
    public static String SourcePage_pageTitle;
    public static String SourcePage_description;
    public static String SourcePage_msgSelect;
	public static String ReferenceAndPreviewPage_title;
	public static String ReferenceAndPreviewPage_pageTitle;
	public static String ReferenceAndPreviewPage_description;
	public static String ReferenceAndPreviewPage_labelValidFrom;
	public static String ReferenceAndPreviewPage_labelTargetPackage;
	public static String ReferenceAndPreviewPage_labelSearchPattern;
	public static String ReferenceAndPreviewPage_labelReplacePattern;
	public static String ReferenceAndPreviewPage_msgCopyNotPossible;
	public static String ReferenceAndPreviewPage_msgCanNotCreateFile;
	public static String ReferenceAndPreviewPage_msgFileAllreadyExists;
	public static String ReferenceAndPreviewPage_msgNameCollision;
	public static String ReferenceAndPreviewPage_errorLabelInsert;
	public static String ReferenceAndPreviewPage_msgSelectAtLeastOneProduct;
	public static String SourcePage_msgCircleRelation;
	public static String SourcePage_msgCircleRelationShort;
	public static String ReferenceAndPreviewPage_msgCircleDetected;
}
