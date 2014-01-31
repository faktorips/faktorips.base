/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.editors.type.ParametersEditControl;
import org.faktorips.util.ArgumentCheck;

public class FormulaEditDialog extends IpsPartEditDialog2 {

    /** the formula configuration element being edited */
    private IFormula formula;

    /** the formulas method signature */
    private IBaseMethod signature;

    private IIpsProject ipsProject;

    /** control to display & edit the formula parameters */
    private ParametersEditControl parametersControl;

    /** edit fields */
    private TextField formulaField;

    private ContentProposalAdapter contentProposalAdapter;
    private ContentProposalListener contentProposalListener;

    /**
     * Creates a new dialog which allows to edit a formula.
     * 
     * @param parentShell The shell as parent for the dialog.
     * 
     * @throws CoreException if the config element is invalid (e.g. no datatype can be found for
     *             it).
     */
    public FormulaEditDialog(IFormula formula, Shell parentShell) throws CoreException {
        super(formula, parentShell, Messages.FormulaEditDialog_editFormula, true);
        ArgumentCheck.notNull(formula);
        this.formula = formula;
        ipsProject = formula.getIpsProject();
        signature = formula.findFormulaSignature(ipsProject);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.minimumHeight = 400;
        folder.setLayoutData(layoutData);

        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.FormulaEditDialog_Formula);
        firstPage.setControl(createFirstPage(folder));

        bindContent();

        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        GridLayout layout = (GridLayout)c.getLayout();
        layout.verticalSpacing = 20;

        parametersControl = new ParametersEditControl(c, getToolkit(), SWT.NONE,
                Messages.FormulaEditDialog_availableParameters, ipsProject);

        GridData parameterControlLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        parameterControlLayoutData.heightHint = 150;
        parametersControl.setLayoutData(parameterControlLayoutData);

        parametersControl.setCanAddParameters(false);
        parametersControl.setCanChangeParameterTypes(false);
        parametersControl.setCanChangeParameterNames(false);
        parametersControl.setCanMoveParameters(false);
        parametersControl.setTableStyle(SWT.BORDER);
        parametersControl.initControl();

        Text formulaText = getToolkit().createMultilineText(c);
        final char[] autoActivationCharacters = new char[] { '.' };
        KeyStroke keyStroke = null;
        try {
            keyStroke = KeyStroke.getInstance("Ctrl+Space"); //$NON-NLS-1$
        } catch (final ParseException e) {
            throw new IllegalArgumentException("KeyStroke \"Ctrl+Space\" could not be parsed.", e); //$NON-NLS-1$
        }
        contentProposalAdapter = new ContentProposalAdapter(formulaText, new TextContentAdapter(),
                new ExpressionProposalProvider(formula), keyStroke, autoActivationCharacters);

        contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
        contentProposalListener = new ContentProposalListener(contentProposalAdapter);
        contentProposalAdapter.addContentProposalListener(contentProposalListener);

        // create fields
        formulaField = new TextField(formulaText);
        return c;
    }

    @Override
    protected String buildTitle() {
        String localizedCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(formula);
        return localizedCaption + (signature != null ? " - " + signature.getDatatype() : ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void bindContent() {
        getBindingContext().bindContent(formulaField, formula, IFormula.PROPERTY_EXPRESSION);
        if (signature == null) {
            return;
        }
        parametersControl.setInput(signature);
        getBindingContext().updateUI();
    }

    @Override
    public boolean close() {
        if (contentProposalAdapter != null) {
            contentProposalAdapter.removeContentProposalListener(contentProposalListener);
        }
        return super.close();
    }
}
