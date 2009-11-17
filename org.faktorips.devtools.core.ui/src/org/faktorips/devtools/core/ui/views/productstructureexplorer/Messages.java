/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.productstructureexplorer.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String ProductStructureContentProvider_treeNodeText_GenerationCurrentWorkingDate;
    public static String ProductStructureExplorer_adjustmentDate;
    public static String ProductStructureExplorer_collectingAdjustmentDates;
    public static String ProductStructureExplorer_infoMessageEmptyView_1;
    public static String ProductStructureExplorer_infoMessageEmptyView_2;
    public static String ProductStructureExplorer_label_NoGenerationForCurrentWorkingDate;
    public static String ProductStructureExplorer_menuCollapseAll_toolkit;
    public static String ProductStructureExplorer_menuShowReferencedTables_name;
    public static String ProductStructureExplorer_menuShowReferencedTables_tooltip;
    public static String ProductStructureExplorer_menuShowAssociationNodes_name;
    public static String ProductStructureExplorer_menuShowTableRoleName_name;
    public static String ProductStructureExplorer_menuShowTableRoleName_tooltip;
    public static String ProductStructureLabelProvider_undefined;
	public static String ProductStructureExplorer_labelCircleRelation;
	public static String ProductStructureExplorer_tooltipToggleRelationTypeNodes;
	public static String ProductStructureExplorer_tooltipClear;
	public static String ProductStructureExplorer_tooltipRefreshContents;
}
