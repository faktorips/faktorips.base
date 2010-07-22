/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.table;

/**
 * {@link TraversalStrategy} that can be added to different {@link IpsCellEditor}s during its
 * lifetime (the {@link TableTraversalStrategy} in contrast is instantiated for every
 * {@link IpsCellEditor} separately). The {@link AbstractPermanentTraversalStrategy} is designed to
 * be used with an {@link CellTrackingEditingSupport} object. The {@link CellTrackingEditingSupport}
 * should hold an instance of the {@link AbstractPermanentTraversalStrategy} and register it with
 * each {@link IpsCellEditor} after it is created.
 * 
 * @author Stefan Widmaier
 */
public abstract class AbstractPermanentTraversalStrategy implements TraversalStrategy {

    private CellTrackingEditingSupport editingSupport;

    public AbstractPermanentTraversalStrategy(CellTrackingEditingSupport editingSupport) {
        super();
        this.editingSupport = editingSupport;
    }

    protected Object getCurrentViewItem() {
        return getEditingSupport().getCurrentViewItem();
    }

    protected IpsCellEditor getCurrentCellEditor() {
        return getEditingSupport().getCurrentCellEditor();
    }

    protected void fireApplyEditorValue() {
        if (getCurrentCellEditor() != null) {
            getCurrentCellEditor().fireApplyEditorValue();
        }
    }

    protected CellTrackingEditingSupport getEditingSupport() {
        return editingSupport;
    }

}
