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

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public abstract class AbstractAddTableContentsHandler extends AbstractHandler {

    public AbstractAddTableContentsHandler() {
        super();
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        IStructuredSelection structuredSelection = null;
        if (currentSelection instanceof IStructuredSelection) {
            structuredSelection = (IStructuredSelection)currentSelection;
        }
        String tableUsageName = event.getParameter(getTableUsageParameter());
        if (structuredSelection != null && structuredSelection.getFirstElement() instanceof IProductCmptReference
                && tableUsageName != null) {
            IProductCmptReference selectedReference = (IProductCmptReference)structuredSelection.getFirstElement();
            IProductCmptGeneration activeGeneration = selectedReference.getProductCmpt().getGenerationByEffectiveDate(
                    selectedReference.getStructure().getValidAt());
            ITableContentUsage tableContentUsage = activeGeneration.getTableContentUsage(tableUsageName);
            openDialog(tableContentUsage, shell, true);
        }
        return null;
    }

    protected abstract String getTableUsageParameter();

    /**
     * Opens the dialog to fulfill the command.
     * 
     * @param tableContentUsage The {@link ITableContentUsage} where the table content should be set
     *            to.
     * @param shell The shell to open the wizard dialog
     * @param autoSave true for automatically safe the product component where the table was added
     *            to, false if the wizard dialog is opened from editor and you do not want to safe
     *            automatically
     */
    protected abstract void openDialog(ITableContentUsage tableContentUsage, Shell shell, boolean autoSave);

    @Override
    public void setEnabled(Object evaluationContext) {
        IWorkbenchWindow activeWorkbenchWindow = IpsUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null) {
            return;
        }
        ISelection selection = activeWorkbenchWindow.getSelectionService().getSelection();
        TypedSelection<IProductCmptReference> typedSelection = new TypedSelection<IProductCmptReference>(
                IProductCmptReference.class, selection);
        if (typedSelection.isValid()) {
            IProductCmptReference cmptReference = typedSelection.getFirstElement();
            IProductCmptGeneration generation = cmptReference.getProductCmpt().getGenerationByEffectiveDate(
                    cmptReference.getStructure().getValidAt());
            setBaseEnabled(generation != null && generation.getTableContentUsages().length > 0);
        } else {
            setBaseEnabled(true);
        }
        super.setEnabled(evaluationContext);
    }

}