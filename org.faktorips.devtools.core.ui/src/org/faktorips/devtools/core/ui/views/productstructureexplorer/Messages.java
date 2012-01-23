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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.productstructureexplorer.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AddLinkAction_add;
    public static String AddLinkAction_selectDialogTitle;
    public static String ProductStructureExplorerContributionFactory_addNewProductCmpt_for;
    public static String ProductStructureExplorerContributionFactory_label_addNewProductCmpt;
    public static String ProductStructureExplorerContributionFactory_label_newTableContent;
    public static String ProductStructureExplorerContributionFactory_label_newTableContent_for;
    public static String ProductStructureExplorerContributionFactory_label_selectTable;
    public static String ProductStructureExplorerContributionFactory_label_selectTableFor;
    public static String ProductStructureExplorerContributionFactory_addExistingProductCmpt_for;
    public static String ProductStructureExplorer_collectingAdjustmentDates;
    public static String ProductStructureExplorer_infoMessageEmptyView_1;
    public static String ProductStructureExplorer_label_NoGenerationForDate;
    public static String ProductStructureExplorer_menuShowReferencedTables_name;
    public static String ProductStructureExplorer_menuShowReferencedTables_tooltip;
    public static String ProductStructureExplorer_menuShowAssociationNodes_name;
    public static String ProductStructureExplorer_menuShowCardinalities_name;
    public static String ProductStructureExplorer_menuShowTableRoleName_name;
    public static String ProductStructureExplorer_menuShowTableRoleName_tooltip;
    public static String ProductStructureLabelProvider_inactiveDecoration;
    public static String ProductStructureLabelProvider_undefined;
    public static String ProductStructureExplorer_labelCircleRelation;
    public static String ProductStructureExplorer_nextAdjustmentToolTip;
    public static String ProductStructureExplorer_prevAdjustmentToolTip;
    public static String ProductStructureExplorer_selectAdjustmentToolTip;
    public static String ProductStructureExplorer_tooltipToggleRelationTypeNodes;
    public static String ProductStructureExplorer_tooltipToggleCardinalities;
    public static String ProductStructureExplorer_tooltipClear;
    public static String ProductStructureExplorer_tooltipRefreshContents;
    public static String ProductStructureExplorer_menuShowAssociatedCmpts_name;
    public static String ProductStructureExplorer_ShowRulesActionLabel;
    public static String ProductStructureExplorer_ShowRulesActionTooltip;
    public static String ProductStructureExplorer_tooltipToggleAssociatedCmptsNodes;
    public static String SelectExistingTableContentsHandler_selectionDialogTitle;
    public static String ToggleRuleAction_Label_activate;
    public static String ToggleRuleAction_Label_deactivate;
    public static String ToggleRuleAction_TooltipActivate;
    public static String ToggleRuleAction_TooltipDeactivate;

}
