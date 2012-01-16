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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public class AddNewTableContentCommand extends AbstractHandler {

    public static final String COMMAND_ID = "org.faktorips.devtools.core.ui.wizards.tablecontents.newTableContent"; //$NON-NLS-1$

    public static final String PARAMETER_TABLE_USAGE = "org.faktorips.devtools.core.ui.wizards.tablecontents.newTableContent.tableUsage"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        IStructuredSelection structuredSelection = null;
        if (currentSelection instanceof IStructuredSelection) {
            structuredSelection = (IStructuredSelection)currentSelection;
        }
        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        String tableUsageName = event.getParameter(PARAMETER_TABLE_USAGE);
        if (structuredSelection != null && structuredSelection.getFirstElement() instanceof IProductCmptReference
                && tableUsageName != null) {
            IProductCmptReference selectedReference = (IProductCmptReference)structuredSelection.getFirstElement();
            IProductCmptGeneration activeGeneration = selectedReference.getProductCmpt().getGenerationByEffectiveDate(
                    selectedReference.getStructure().getValidAt());
            ITableContentUsage tableContentUsage = activeGeneration.getTableContentUsage(tableUsageName);
            initWizard(tableContentUsage, shell);
        }
        return null;
    }

    private void initWizard(ITableContentUsage addToUsage, Shell shell) {
        NewTableContentsWizard newTableContentsWizard = new NewTableContentsWizard();
        newTableContentsWizard.initDefaults(addToUsage.getIpsSrcFile().getIpsPackageFragment(), null);
        newTableContentsWizard.setAddToTableUsage(addToUsage);
        WizardDialog dialog = new WizardDialog(shell, newTableContentsWizard);
        dialog.open();
    }

    @Override
    public void setEnabled(Object evaluationContext) {
        ISelection selection = IpsUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService()
                .getSelection();
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
