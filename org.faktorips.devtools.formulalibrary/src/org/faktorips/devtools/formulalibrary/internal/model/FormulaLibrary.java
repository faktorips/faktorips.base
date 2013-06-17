package org.faktorips.devtools.formulalibrary.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;
import org.faktorips.fl.FlFunction;

/**
 * Implementation of {@link IFormulaLibrary}. It holds the {@link IFormulaFunction}-Parts
 * 
 * @author frank
 */
public class FormulaLibrary extends BaseIpsObject implements IFormulaLibrary {

    private final IpsObjectPartCollection<IFormulaFunction> formulaFunctions = new IpsObjectPartCollection<IFormulaFunction>(
            this, FormulaFunction.class, IFormulaFunction.class, FormulaFunction.TAG_NAME);

    protected FormulaLibrary(IIpsSrcFile file) {
        super(file);
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return FormulaLibraryIpsObjectType.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFormulaFunction newFormulaFunction() {
        return formulaFunctions.newPart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IFormulaFunction> getFormulaFunctions() {
        return formulaFunctions.asList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeFormulaFunction(IFormulaFunction formulaFunction) {
        return formulaFunctions.removePart(formulaFunction);
    }

    @Override
    public List<FlFunction> getFlFunctions() {
        List<FlFunction> functions = new ArrayList<FlFunction>();
        for (IFormulaFunction formulaFunction : getFormulaFunctions()) {
            functions.add(formulaFunction.getFlFunction());
        }
        return functions;
    }
}
