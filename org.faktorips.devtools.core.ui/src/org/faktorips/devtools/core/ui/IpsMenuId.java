/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

/**
 * Enumerates all menu IDs, toolbar IDs and group IDs provided by Faktor-IPS.
 * 
 * @author Alexander Weickmann
 */
public enum IpsMenuId {

    TOOLBAR_ENUM_TYPE_EDITOR_PAGE("org.faktorips.devtools.core.ui.editors.enumtype.enumTypeEditorPage.toolbar"), //$NON-NLS-1$

    TOOLBAR_POLICY_CMPT_TYPE_EDITOR_PAGE(
            "org.faktorips.devtools.core.ui.editors.pctype.policyCmptTypeEditorPage.toolbar"), //$NON-NLS-1$

    TOOLBAR_PRODUCT_CMPT_TYPE_EDITOR_PAGE(
            "org.faktorips.devtools.core.ui.editors.productcmpttype.productCmptTypeEditorPage.toolbar"), //$NON-NLS-1$

    TOOLBAR_TEST_CASE_TYPE_EDITOR_PAGE(
            "org.faktorips.devtools.core.ui.editors.testcasetype.testCaseTypeEditorPage.toolbar"), //$NON-NLS-1$

    TOOLBAR_TABLE_STRUCTURE_EDITOR_PAGE(
            "org.faktorips.devtools.core.ui.editors.tablestructure.tableStructureEditorPage.toolbar"), //$NON-NLS-1$

    GROUP_JUMP_TO_SOURCE_CODE("jumpToSourceCode"); //$NON-NLS-1$

    private final String id;

    private IpsMenuId(String id) {
        this.id = id;
    }

    /**
     * Returns the ID of this Faktor-IPS menu, toolbar or group.
     */
    public String getId() {
        return id;
    }

}
