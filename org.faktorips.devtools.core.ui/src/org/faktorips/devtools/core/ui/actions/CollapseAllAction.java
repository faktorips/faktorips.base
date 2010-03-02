package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * Collapses all elements in a tree viewer.
 */
public class CollapseAllAction extends Action {
    private static final String COLLAPSE_ALL_ICON = "CollapseAll.gif"; //$NON-NLS-1$

    private final AbstractTreeViewer treeViewer;

    /**
     * Creates a new instance of {@link CollapseAllAction}.
     * 
     * @param treeViewer the viewer to collapse
     */
    public CollapseAllAction(final AbstractTreeViewer treeViewer) {
        super(null, IpsUIPlugin.getImageHandling().createImageDescriptor(COLLAPSE_ALL_ICON));

        ArgumentCheck.notNull(treeViewer);
        this.treeViewer = treeViewer;

        setToolTipText(Messages.CollapseAllAction_Description);
        setActionDefinitionId(CollapseAllHandler.COMMAND_ID);
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        treeViewer.collapseAll();
    }
}
