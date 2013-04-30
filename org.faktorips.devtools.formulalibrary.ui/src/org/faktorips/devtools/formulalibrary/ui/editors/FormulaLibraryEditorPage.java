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

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;

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
public class FormulaLibraryEditorPage extends IpsObjectEditorPage implements ContentsChangeListener {

    /**
     * Creates a new <tt>FormulaLibraryEditorPage</tt>.
     * 
     * @param editor The <tt>FormulaLibraryContentEditor</tt> this page belongs to.
     */
    public FormulaLibraryEditorPage(FormulaLibraryContentEditor editor) {
        super(editor, "FormulaLibraryContentEditorPage", Messages.FormulaLibraryContentPage_title); //$NON-NLS-1$

    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(formBody);
        toolkit.createLabel(composite, getFormulaLibraryString());
    }

    
    private FormulaLibraryContentEditor getContentEditor() {
    	return (FormulaLibraryContentEditor)getEditor();
    }
    
    /**
     * BaseMethod just temporarily until editor ready
     */
    private String getFormulaLibraryString() {
    	IFormulaLibrary formulaLibrary = getContentEditor().getFormulaLibrary();
    	StringBuilder builder = new StringBuilder();
        builder.append("Name: " + formulaLibrary.getQualifiedName()); //$NON-NLS-1$
        List<IFormulaFunction> iFormulaFunctions = formulaLibrary.getFormulaFunctions();
        for (IFormulaFunction formulaFunction : iFormulaFunctions) {
            builder.append("\nFormulaFunction ["); //$NON-NLS-1$
            builder.append(formulaFunction.getFormulaMethod());
            builder.append(" - "); //$NON-NLS-1$
            builder.append(formulaFunction.getExpression());
            builder.append("]"); //$NON-NLS-1$
            builder.append(formulaFunction);
        }
        
        return builder.toString();
    }
    
    @Override
    public void contentsChanged(ContentChangeEvent event) {
        // Event
    }

}
