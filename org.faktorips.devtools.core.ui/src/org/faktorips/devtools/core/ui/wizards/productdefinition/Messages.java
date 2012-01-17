/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.productdefinition.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String FolderAndPackagePage_check_openInEditor;
    public static String FolderAndPackagePage_label_package;
    public static String FolderAndPackagePage_label_rootFolder;
    public static String FolderAndPackagePage_title;
    public static String TypeSelectionComposite_label_description;
    public static String TypeSelectionComposite_label_noDescriptionAvailable;
    public static String NewProductDefinitionValidator_msg_invalidPackageRoot;
    public static String NewProductDefinitionValidator_msg_invalidEffectiveDate;
    public static String NewProductDefinitionValidator_msg_invalidPackage;

}
