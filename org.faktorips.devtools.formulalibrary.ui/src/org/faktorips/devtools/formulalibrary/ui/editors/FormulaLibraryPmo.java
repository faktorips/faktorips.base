package org.faktorips.devtools.formulalibrary.ui.editors;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;

/**
 * The <tt>FormulaLibraryPmo</tt> is a presentation object model for <tt>IFormulaLibrary</tt>.
 * <p>
 * 
 * @see FormulaFunctionListSection
 * 
 * 
 * @author HBaagil
 */
public class FormulaLibraryPmo extends IpsObjectPartPmo {

    public static final String PROPERTY_SELECTED_FORMULA = "selectedFormula"; //$NON-NLS-1$
    private final FormulaFunctionPmo selectedFormulaPmo;

    /**
     * Creates a new <tt>FomulaLibraryPmo</tt>.
     * 
     * @param formulaLibrary The <tt>IFormulaLibrary</tt> this represents.
     */
    public FormulaLibraryPmo(IFormulaLibrary formulaLibrary) {
        super(formulaLibrary);
        selectedFormulaPmo = new FormulaFunctionPmo();
    }

    /**
     * Returns list of <tt>IFormulaFunction</tt> of this presented <tt>IFomulaLibrary</tt>.
     * 
     * @return List<IFormulaFunction>
     */
    public List<IFormulaFunction> getFormulaFunctions() {
        List<IFormulaFunction> functions = getFormulaLibrary().getFormulaFunctions();
        return functions;
    }

    /**
     * Returns <tt>IFormulaFunction</tt> of selected <tt>FormulaFunctionPmo</tt>.
     * 
     * @see FormulaFunctionPmo
     * 
     * @return IFormulaFunction
     */
    public IFormulaFunction getSelectedFormula() {
        return selectedFormulaPmo.getFormulaFunction();
    }

    /**
     * Sets a new selected <tt>FormulaFunctionPmo</tt>.
     * 
     * @see FormulaFunctionPmo
     * 
     * @param selectedFormula The new selected <tt>IFormulaFunction</tt>
     */
    public void setSelectedFormula(IFormulaFunction selectedFormula) {
        IFormulaFunction oldFormulaFunction = this.selectedFormulaPmo.getFormulaFunction();
        selectedFormulaPmo.setFormulaFunction(selectedFormula);
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SELECTED_FORMULA, oldFormulaFunction, selectedFormula));
    }

    /**
     * Returns the selected <tt>FormulaFunctionPmo</tt>
     * 
     * @return FormulaFunctionPmo
     */
    public FormulaFunctionPmo getFormulaFunctionPmo() {
        return selectedFormulaPmo;
    }

    /**
     * Returns <tt>IFormulaLibrary</tt> this represents.
     * 
     * @see IFormulaLibrary
     * 
     * @return IFormulaLibrary
     */
    public IFormulaLibrary getFormulaLibrary() {
        return (IFormulaLibrary)getIpsObjectPartContainer();
    }

    /**
     * Adds a new <tt>IFormulaFunction</tt> to the <tt>IFormulaLibrary</tt> represented by this.
     * 
     * @see IFormulaLibrary
     */
    public void addNewFormulaFunction() {
        setSelectedFormula(getFormulaLibrary().newFormulaFunction());
    }

    /**
     * Removes the selected <tt>IFormulaFunction</tt> from <tt>IFormulaLibrary</tt> represented by
     * this.
     * 
     */
    public void removeSelectedFormulaFunction() {
        if (getSelectedFormula() != null) {
            int index = getFormulaLibrary().getFormulaFunctions().indexOf(getSelectedFormula());
            getSelectedFormula().delete();
            selectNextFormula(index);
        }

    }

    private void selectNextFormula(int indexOfDeletedFormula) {
        int nextSelectionIndex = getNextSelectionIndex(indexOfDeletedFormula);
        selectNextFormulaIfPossible(nextSelectionIndex);
    }

    private void selectNextFormulaIfPossible(int nextSelectionIndex) {
        IFormulaFunction nextSelected = null;
        if (getFormulaLibrary().getFormulaFunctions().size() > 0) {
            nextSelected = getFormulaLibrary().getFormulaFunctions().get(nextSelectionIndex);
        }
        setSelectedFormula(nextSelected);
    }

    private int getNextSelectionIndex(int indexOfDeletedFormula) {
        int nextSelectionIndex = indexOfDeletedFormula - 1;
        if (nextSelectionIndex < 0) {
            nextSelectionIndex = 0;
        }
        return nextSelectionIndex;
    }
}
