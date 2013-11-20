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

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.IKey;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.TableStructureRefControl;

/**
 * A dialog to edit foreign key.
 */
public class KeyEditDialogForeignKey extends KeyEditDialog {

    private IKey key;

    /** completion processor for a table structure's indices. */
    private IndexCompletionProcessor completionProcessor;
    private TextButtonField tableStructureRefField;
    private TextField uniqueKeyRefField;

    public KeyEditDialogForeignKey(IKey key, Shell parentShell) {
        super(key, parentShell, Messages.KeyEditDialogForeignKey_titleText);
        this.key = key;
    }

    @Override
    protected void addPageTopControls(Composite pageComposite) {
        createStructureAndKeyReferenceControls(pageComposite);
        bind();
    }

    protected void createStructureAndKeyReferenceControls(Composite pageComposite) {
        Composite refTableComposite = getToolkit().createLabelEditColumnComposite(pageComposite);
        GridLayout layout = (GridLayout)refTableComposite.getLayout();
        layout.marginHeight = 12;
        createLabelReferenceStructure(refTableComposite);
        createLabelUniqueKey(refTableComposite);
    }

    private void createLabelReferenceStructure(Composite refTableComposite) {
        getToolkit().createFormLabel(refTableComposite, Messages.KeyEditDialog_labelReferenceStructure);
        TableStructureRefControl refControl = getToolkit().createTableStructureRefControl(key.getIpsProject(),
                refTableComposite);
        refControl.setFocus();
        tableStructureRefField = new TextButtonField(refControl);
    }

    private void createLabelUniqueKey(Composite refTableComposite) {
        getToolkit().createFormLabel(refTableComposite, Messages.KeyEditDialog_labelReferenceUniqueKey);
        Text ukRefControl = getToolkit().createText(refTableComposite);
        uniqueKeyRefField = new TextField(ukRefControl);
        setupContentAssist(ukRefControl);
    }

    /**
     * @param ukRefControl
     */
    protected void setupContentAssist(Text ukRefControl) {
        completionProcessor = new IndexCompletionProcessor();

        // Der ContentAssistHandler ist deprecated. Genutzt werden kann sattdessen der
        // contentProposalAdapter?
        // ContentProposalAdapter contentProposalAdapter = new ContentProposalAdapter(ukRefControl,
        // new TextContentAdapter(),
        // new ExpressionProposalProvider(formula), keyStroke, autoActivationCharacters);

        ContentAssistHandler.createHandlerForText(ukRefControl,
                CompletionUtil.createContentAssistant(completionProcessor));
    }

    private void bind() {
        getBindingContext().bindContent(tableStructureRefField, key, IForeignKey.PROPERTY_REF_TABLE_STRUCTURE);
        getBindingContext().bindContent(uniqueKeyRefField, key, IForeignKey.PROPERTY_REF_UNIQUE_KEY);
    }

}
