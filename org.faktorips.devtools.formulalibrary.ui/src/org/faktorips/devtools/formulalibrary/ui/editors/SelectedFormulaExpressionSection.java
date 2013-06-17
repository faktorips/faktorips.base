/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.formulalibrary.ui.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.editors.productcmpt.ContentProposalListener;
import org.faktorips.devtools.core.ui.editors.productcmpt.ExpressionProposalProvider;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.message.MessageList;

/**
 * The <tt>SelectedFormulaExpressionSection</tt> generates an <tt>IpsSection</tt>.
 * <p>
 * This shows the formula expression of the selected formula in <tt>FormulaFunctionListSection</tt>
 * 
 * @author HBaagil
 */
public class SelectedFormulaExpressionSection extends IpsSection {

    private static final String ID = "org.faktorips.devtools.formulalibrary.ui.editors.SelectedFormulaExpressionSection"; //$NON-NLS-1$

    private Composite selectedFormulaExpressionComposite;
    private FormulaFunctionPmo formulaFunctionPmo;
    private TextField textFieldForFormulaExpression;
    private ContentProposalAdapter contentProposalAdapter;
    private ContentProposalListener contentProposalListener;
    private ExpressionProposalProviderListener formulaFunctionChangedListener;
    private RefreshMessageListener formulaFunctionExpressionChangeListener;
    private MessageComposite messageComposite;

    /**
     * Creates a new <tt>SelectedFormulaExpressionSection</tt>.
     * 
     * @param parent <tt>Composite</tt> this <tt>IpsSection</tt> belongs to.
     * @param toolkit The <tt>UIToolkit</tt> for look and feel controls.
     * @param formulaFunctionPmo The <tt>IpsObjectPartPmo</tt> as presentation model object for
     *            <tt>IFormulaFunction</tt>.
     */
    protected SelectedFormulaExpressionSection(Composite parent, UIToolkit toolkit,
            FormulaFunctionPmo formulaFunctionPmo) {
        super(ID, parent, GridData.FILL_BOTH, toolkit);
        this.formulaFunctionPmo = formulaFunctionPmo;
        initControls();
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        createExpressionSection(client);
        formulaFunctionChangedListener = new ExpressionProposalProviderListener(formulaFunctionPmo,
                contentProposalAdapter, textFieldForFormulaExpression);
        formulaFunctionPmo.addPropertyChangeListener(formulaFunctionChangedListener);

        formulaFunctionExpressionChangeListener = new RefreshMessageListener(messageComposite, formulaFunctionPmo);
        formulaFunctionPmo.addPropertyChangeListener(formulaFunctionExpressionChangeListener);

        getBindingContext().updateUI();
    }

    private void createExpressionSection(Composite parent) {
        selectedFormulaExpressionComposite = getToolkit().createGridComposite(parent, 1, false, false);
        GridLayout middleRightGrid = createGridLayout();
        selectedFormulaExpressionComposite.setLayout(middleRightGrid);

        createExpressionWidged();
    }

    private GridLayout createGridLayout() {
        GridLayout middleRightGrid = new GridLayout();
        middleRightGrid.numColumns = 1;
        return middleRightGrid;
    }

    private void createExpressionWidged() {
        Text formulaText = createMultilineText();
        createContentProposalAdapterAndListener(formulaText);
        setTextField(formulaText);
        bindTextFieldToPmo();
        createMessageComposite();
    }

    private void createMessageComposite() {
        messageComposite = new MessageComposite(selectedFormulaExpressionComposite);
    }

    private Text createMultilineText() {
        Text formulaText = getToolkit().createMultilineText(selectedFormulaExpressionComposite);
        GridData gridData = (GridData)formulaText.getLayoutData();
        gridData.horizontalIndent = 10;
        return formulaText;
    }

    private void createContentProposalAdapterAndListener(Text formulaText) {
        final char[] autoActivationCharacters = createAutoActivationCharacters();
        KeyStroke keyStroke = createKeyStrokeForAutocompletion();
        contentProposalAdapter = new ContentProposalAdapter(formulaText, new TextContentAdapter(), null, keyStroke,
                autoActivationCharacters);
        contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
        contentProposalListener = new ContentProposalListener(contentProposalAdapter);
        contentProposalAdapter.addContentProposalListener(contentProposalListener);
    }

    private void setTextField(Text formulaText) {
        textFieldForFormulaExpression = new TextField(formulaText);
        if (formulaFunctionPmo.getFormulaFunction() == null) {
            textFieldForFormulaExpression.getTextControl().setEditable(false);
        }
    }

    private KeyStroke createKeyStrokeForAutocompletion() {
        KeyStroke keyStroke = null;
        try {
            keyStroke = KeyStroke.getInstance("Ctrl+Space"); //$NON-NLS-1$
        } catch (final ParseException e) {
            throw new IllegalArgumentException("KeyStroke \"Ctrl+Space\" could not be parsed.", e); //$NON-NLS-1$
        }
        return keyStroke;
    }

    private char[] createAutoActivationCharacters() {
        final char[] autoActivationCharacters = new char[] { '.' };
        return autoActivationCharacters;
    }

    private void bindTextFieldToPmo() {
        getBindingContext().bindContent(textFieldForFormulaExpression, formulaFunctionPmo,
                FormulaFunctionPmo.PROPERTY_FORMULA_EXPRESSION);
    }

    @Override
    public void widgetDisposed(DisposeEvent e) {
        super.widgetDisposed(e);
        formulaFunctionPmo.removePropertyChangeListener(formulaFunctionChangedListener);
        formulaFunctionPmo.removePropertyChangeListener(formulaFunctionExpressionChangeListener);
    }

    private static final class ExpressionProposalProviderListener implements PropertyChangeListener {

        private final FormulaFunctionPmo formulaFunctionPmo;
        private final ContentProposalAdapter proposalAdapter;
        private final TextField textFieldForFormulaExpression;

        public ExpressionProposalProviderListener(FormulaFunctionPmo formulaFunctionPmo,
                ContentProposalAdapter contentProposalAdapter, TextField textFieldForFormulaExpression) {
            this.formulaFunctionPmo = formulaFunctionPmo;
            proposalAdapter = contentProposalAdapter;
            this.textFieldForFormulaExpression = textFieldForFormulaExpression;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (FormulaFunctionPmo.PROPERTY_IPS_OBJECT_PART_CONTAINER.equals(evt.getPropertyName())) {
                if (formulaFunctionPmo.getFormulaFunction() != null) {
                    IExpression formulaExpression = formulaFunctionPmo.getFormulaFunction().getExpression();
                    ExpressionProposalProvider proposalProvider = new ExpressionProposalProvider(formulaExpression);
                    proposalAdapter.setContentProposalProvider(proposalProvider);
                    textFieldForFormulaExpression.getTextControl().setEditable(true);
                } else {
                    textFieldForFormulaExpression.getTextControl().setEditable(false);
                }
            }
        }
    }

    private static final class RefreshMessageListener implements PropertyChangeListener {
        private final MessageComposite showErrorMessage;
        private final FormulaFunctionPmo formulaFunctionPmo;

        public RefreshMessageListener(MessageComposite showErrorMessage, FormulaFunctionPmo formulaFunctionPmo) {
            this.showErrorMessage = showErrorMessage;
            this.formulaFunctionPmo = formulaFunctionPmo;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (formulaFunctionPmo.getFormulaFunction() != null) {
                try {
                    showErrorMessage.setMessage(formulaFunctionPmo.getFormulaFunction().getExpression()
                            .validate(formulaFunctionPmo.getIpsProject()));
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            } else {
                showErrorMessage.setMessage(new MessageList());
            }
        }
    }
}
