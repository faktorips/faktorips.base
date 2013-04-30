package org.faktorips.devtools.formulalibrary.internal.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;
import org.faktorips.fl.FlFunction;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
    protected void validateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        super.validateThis(result, ipsProject);
        validateSameFormulaFunctions(result);
    }

    private void validateSameFormulaFunctions(MessageList result) {
        Set<String> signatures = new HashSet<String>();
        Set<String> formulaNames = new HashSet<String>();

        for (IFormulaFunction formulaFunction : getFormulaFunctions()) {
            String formulaName = formulaFunction.getFormulaMethod().getFormulaName();
            String signatureString = formulaFunction.getFormulaMethod().getSignatureString();
            if (formulaNames.contains(formulaName)) {
                result.add(new Message(MSGCODE_DUPLICATE_FUNCTION, Messages.FormulaLibrary_msgDuplicateFormulaName, Message.ERROR,
                        this, formulaFunction.getFormulaMethod().getFormulaName()));
                break;
            }
            if (signatures.contains(signatureString)) {
                result.add(new Message(MSGCODE_DUPLICATE_SIGNATURE, Messages.FormulaLibrary_msgDuplicateSignature,
                        Message.ERROR, this, formulaFunction.getFormulaMethod().getFormulaName()));
                break;
            }
            signatures.add(signatureString);
            formulaNames.add(formulaName);
        }
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
