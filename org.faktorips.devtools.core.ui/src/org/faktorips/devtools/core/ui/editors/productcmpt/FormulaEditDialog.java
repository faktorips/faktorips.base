/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.IMessageProvider;
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
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.parametertable.ChangeParametersControl;
import org.faktorips.devtools.core.ui.controls.parametertable.ParameterInfo;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class FormulaEditDialog extends IpsPartEditDialog {

    /** the formula configuration element being edited */
    private IFormula formula;

    /** the formulas method signature */
    private IMethod signature;

    private IIpsProject ipsProject;

    /** control to display & edit the formula parameters */
    private ChangeParametersControl parametersControl;

    /** edit fields */
    private TextField formulaField;

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
    protected IpsObjectUIController createUIController(IIpsObjectPart part) {
        IpsObjectUIController controller = new IpsObjectUIController(part) {

            @Override
            protected MessageList validatePartContainerAndUpdateUI() {
                MessageList messages = super.validatePartContainerAndUpdateUI();
                return validateAndUpdateDialogUI(messages);
            }
        };
        return controller;
    }

    /**
     * Validates and updates the dialog
     */
    private MessageList validateAndUpdateDialogUI(MessageList messages) {
        MessageList relevantMessages = new MessageList();
        if (messages.size() > 0) {
            for (Message msg : messages) {
                relevantMessages.add(msg);
            }
            if (!relevantMessages.isEmpty()) {
                Message firstMessage = relevantMessages.getMessage(0);
                setMessage(firstMessage.getText(), getJFaceMessageType(firstMessage.getSeverity()));
            } else {
                setMessage(""); //$NON-NLS-1$
            }
        } else {
            setMessage(""); //$NON-NLS-1$
        }
        return relevantMessages;
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

        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        GridLayout layout = (GridLayout)c.getLayout();
        layout.verticalSpacing = 20;

        parametersControl = new ChangeParametersControl(c, getToolkit(), SWT.NONE,
                Messages.FormulaEditDialog_availableParameters, ipsProject) {

            @Override
            public MessageList validate(int paramIndex) throws CoreException {
                return new MessageList();
            }

        };
        GridData parameterControlLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        parameterControlLayoutData.heightHint = 150;
        parametersControl.setLayoutData(parameterControlLayoutData);

        parametersControl.setCanAddParameters(false);
        parametersControl.setCanChangeParameterTypes(false);
        parametersControl.setCanChangeParameterNames(false);
        parametersControl.setCanMoveParameters(false);
        parametersControl.setTableStyle(SWT.BORDER);
        parametersControl.setCanChangeParameterNames(isDataChangeable());
        parametersControl.initControl();

        Text formulaText = getToolkit().createMultilineText(c);
        final char[] autoActivationCharacters = new char[] { '.' };
        KeyStroke keyStroke = null;
        try {
            keyStroke = KeyStroke.getInstance("Ctrl+Space"); //$NON-NLS-1$
        } catch (final ParseException e) {
            throw new IllegalArgumentException("KeyStroke \"Ctrl+Space\" could not be parsed.", e); //$NON-NLS-1$
        }
        new ContentProposalAdapter(formulaText, new TextContentAdapter(), new ExpressionProposalProvider(formula),
                keyStroke, autoActivationCharacters);

        // create fields
        formulaField = new TextField(formulaText);
        return c;
    }

    private int getJFaceMessageType(int severity) {

        if (Message.ERROR == severity) {
            return IMessageProvider.ERROR;
        }
        if (Message.INFO == severity) {
            return IMessageProvider.INFORMATION;
        }
        if (Message.WARNING == severity) {
            return IMessageProvider.WARNING;
        }

        return IMessageProvider.NONE;
    }

    @Override
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(formulaField, IFormula.PROPERTY_EXPRESSION);
        if (signature == null) {
            return;
        }
        List<ParameterInfo> infos = ParameterInfo.createInfosAsList(signature.getParameters());
        parametersControl.setInput(infos);
    }

    @Override
    protected String buildTitle() {
        String localizedCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(formula);
        return localizedCaption + (signature != null ? " - " + signature.getDatatype() : ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
