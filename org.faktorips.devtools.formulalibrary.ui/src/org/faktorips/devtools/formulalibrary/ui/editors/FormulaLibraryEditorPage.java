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

package org.faktorips.devtools.formulalibrary.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;
import org.faktorips.devtools.formulalibrary.ui.Messages;

/**
 * The <tt>FormulaLibraryEditorPage</tt> shows general information about an <tt>IFormulaLibrary</tt>
 * and provides controls to edit its values. It is intended to be used with the
 * <tt>FormulaLibraryEditor</tt>.
 * <p>
 * 
 * @see FormulaLibraryContentEditor
 * 
 * 
 * @since 3.10.
 */
public class FormulaLibraryEditorPage extends IpsObjectEditorPage {

    private static final String FORMULA_LIBRARY_CONTENT_EDITOR_PAGE = "FormulaLibraryContentEditorPage"; //$NON-NLS-1$
    private FormulaFunctionListSection functionListSection;
    private SelectedFormulaSignatureSection formulaSignatureSection;
    private SelectedFormulaExpressionSection formulaEquationSection;
    private SelectedFormulaDescriptionsSection formulaDescriptionsSection;
    private final FormulaLibraryPmo formulaLibraryPmo;
    private SashForm sashForm;

    /**
     * Creates a new <tt>FormulaLibraryEditorPage</tt>.
     * 
     * @param editor The <tt>FormulaLibraryContentEditor</tt> this page belongs to.
     */
    public FormulaLibraryEditorPage(FormulaLibraryContentEditor editor) {
        super(editor, FORMULA_LIBRARY_CONTENT_EDITOR_PAGE, Messages.FormulaLibraryContentPage_title);
        formulaLibraryPmo = new FormulaLibraryPmo((IFormulaLibrary)getIpsObject());
    }

    @Override
    public void dispose() {
        super.dispose();
        formulaLibraryPmo.dispose();
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(2, false));
        sashForm = new SashForm(formBody, SWT.HORIZONTAL);
        GridData baseGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        baseGD.heightHint = 500;
        baseGD.widthHint = 1000;
        sashForm.setLayoutData(baseGD);
        createTableOfFormulaFunctionsSection(sashForm, toolkit);
        createPropertiesOfFormulaFunctionSection(sashForm, toolkit);
        sashForm.setWeights(new int[] { 40, 60 });
        setDefaults();
    }

    private void createTableOfFormulaFunctionsSection(Composite formBody, UIToolkit toolkit) {
        Composite leftComposite = toolkit.createGridComposite(formBody, 1, false, false);
        functionListSection = new FormulaFunctionListSection(leftComposite, toolkit, formulaLibraryPmo);
        functionListSection.setText(Messages.FormulaFunctionListSection_title);
    }

    private void createPropertiesOfFormulaFunctionSection(Composite formBody, UIToolkit toolkit) {
        Composite rightComposite = toolkit.createGridComposite(formBody, 1, false, false);
        formulaSignatureSection = new SelectedFormulaSignatureSection(rightComposite, toolkit,
                formulaLibraryPmo.getFormulaFunctionPmo(), formulaLibraryPmo.getIpsObjectPartContainer()
                        .getIpsProject());
        formulaSignatureSection.setText(Messages.FormulaLibraryEditorPage_SignatureTitle);

        formulaEquationSection = new SelectedFormulaExpressionSection(rightComposite, toolkit,
                formulaLibraryPmo.getFormulaFunctionPmo());
        formulaEquationSection.setText(Messages.FormulaLibraryEditorPage_FormulaTitle);

        formulaDescriptionsSection = new SelectedFormulaDescriptionsSection(rightComposite, toolkit,
                formulaLibraryPmo.getFormulaFunctionPmo());
        formulaDescriptionsSection.setText(Messages.FormulaLibraryEditorPage_DescriptionsTitle);
    }

    private void setDefaults() {
        if (!formulaLibraryPmo.getFormulaFunctions().isEmpty()) {
            IFormulaFunction formulaFunction = formulaLibraryPmo.getFormulaFunctions().get(0);
            formulaLibraryPmo.setSelectedFormula(formulaFunction);
        }
    }
}
