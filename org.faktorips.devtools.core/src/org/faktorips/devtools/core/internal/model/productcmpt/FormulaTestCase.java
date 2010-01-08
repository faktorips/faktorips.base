/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestCase;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestInputValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.fl.DefaultIdentifierResolver;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.ExprEvaluator;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FormulaTestCase extends IpsObjectPart implements IFormulaTestCase {

    /** Tags */
    final static String TAG_NAME = "FormulaTestCase"; //$NON-NLS-1$

    private String expectedResult = ""; //$NON-NLS-1$

    private List<IFormulaTestInputValue> formulaTestInputValues = new ArrayList<IFormulaTestInputValue>(0);

    public FormulaTestCase(Formula parent, int id) {
        super(parent, id);
    }

    /**
     * {@inheritDoc}
     */
    public IFormula getFormula() {
        return (IFormula)getParent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reinitPartCollections() {
        formulaTestInputValues.clear();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class<?> partType) {
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IIpsObjectPart newPart(Element partEl, int id) {
        String xmlTagName = partEl.getNodeName();
        if (FormulaTestInputValue.TAG_NAME.equals(xmlTagName)) {
            return newFormulaTestInputValueInternal(id);
        } else if (PROPERTY_EXPECTED_RESULT.equalsIgnoreCase(xmlTagName)) {
            // ignore expected result nodes, will be parsed in the this#initPropertiesFromXml method
            return null;
        }
        throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPart(IIpsObjectPart part) {
        if (part instanceof IFormulaTestInputValue) {
            formulaTestInputValues.add((IFormulaTestInputValue)part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof IFormulaTestInputValue) {
            formulaTestInputValues.remove(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsElement[] getChildren() {
        return formulaTestInputValues.toArray(new IIpsElement[formulaTestInputValues.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        expectedResult = ValueToXmlHelper
                .getValueFromElement(element, StringUtils.capitalize(PROPERTY_EXPECTED_RESULT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        ValueToXmlHelper.addValueToElement(expectedResult, element, StringUtils.capitalize(PROPERTY_EXPECTED_RESULT));
    }

    /*
     * Returns the expression compiler used to compile the formula preview result.
     */
    private ExprCompiler getPreviewExprCompiler(IIpsProject ipsProject) throws CoreException {
        ExprCompiler compiler = ipsProject.newExpressionCompiler();

        // add the table functions based on the table usages defined in the product cmpt type
        IProductCmptGeneration gen = getFormula().getProductCmptGeneration();
        if (gen != null) {
            compiler.add(new TableFunctionsFormulaTestResolver(getIpsProject(), gen.getTableContentUsages(), this));
        }

        IFormulaTestInputValue[] input = getFormulaTestInputValues();
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        for (int i = 0; i < input.length; i++) {
            String storedValue = input[i].getValue();
            // get the datatype and the helper for generating the code fragment of the formula
            Datatype datatype = input[i].findDatatypeOfFormulaParameter(ipsProject);
            DatatypeHelper dataTypeHelper = getIpsProject().getDatatypeHelper(datatype);
            if (dataTypeHelper == null) {
                throw new CoreException(new IpsStatus(NLS.bind(
                        Messages.FormulaTestCase_CoreException_DatatypeNotFoundOrWrongConfigured, datatype, input[i]
                                .getIdentifier())));
            }
            resolver.register(input[i].getIdentifier(), dataTypeHelper.newInstance(storedValue), datatype);
        }

        compileAndAddAllEnumDatatypeValueIdentifier(resolver);

        compiler.setIdentifierResolver(resolver);

        return compiler;
    }

    /*
     * Add all identifier for enum values
     */
    private void compileAndAddAllEnumDatatypeValueIdentifier(DefaultIdentifierResolver resolver) {
        try {
            EnumDatatype[] enumTypes = getIpsProject().findEnumDatatypes();
            for (int i = 0; i < enumTypes.length; i++) {
                String valueName = enumTypes[i].getName();
                List<String> valueIds = Arrays.asList(enumTypes[i].getAllValueIds(true));
                for (String id : valueIds) {
                    JavaCodeFragment frag = new JavaCodeFragment();
                    frag.getImportDeclaration().add(enumTypes[i].getJavaClassName());
                    DatatypeHelper helper = getIpsProject().getDatatypeHelper(enumTypes[i]);
                    frag.append(helper.newInstance(id));
                    String enumValueName = id;
                    resolver.register(valueName + "." + enumValueName, frag, enumTypes[i]); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object execute(IIpsProject ipsProject) throws Exception {
        ExprEvaluator processor = getExprEvaluatorInternal(ipsProject.getClassLoaderForJavaProject(), ipsProject);
        return processor.evaluate(getFormula().getExpression());
    }

    /**
     * Executes the given java code fragment.
     */
    public Object execute(JavaCodeFragment javaCodeFragment, ClassLoader classLoader, IIpsProject ipsProject)
            throws Exception {
        ExprEvaluator processor = getExprEvaluatorInternal(classLoader, ipsProject);
        return processor.evaluate(javaCodeFragment);
    }

    private ExprEvaluator getExprEvaluatorInternal(ClassLoader classLoader, IIpsProject ipsProject)
            throws CoreException {
        ExprCompiler compiler = getPreviewExprCompiler(ipsProject);
        return new ExprEvaluator(compiler, classLoader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        valueChanged(oldName, name);
    }

    /**
     * {@inheritDoc}
     */
    public IFormulaTestInputValue getFormulaTestInputValue(String identifier) {
        for (IFormulaTestInputValue v : formulaTestInputValues) {
            if (v.getIdentifier().equals(identifier)) {
                return v;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IFormulaTestInputValue[] getFormulaTestInputValues() {
        return formulaTestInputValues.toArray(new IFormulaTestInputValue[0]);
    }

    /**
     * {@inheritDoc}
     */
    public IFormulaTestInputValue newFormulaTestInputValue() {
        IFormulaTestInputValue v = newFormulaTestInputValueInternal(getNextPartId());
        objectHasChanged();
        return v;
    }

    /*
     * Creates a new formula test input value without updating the source file.
     */
    private IFormulaTestInputValue newFormulaTestInputValueInternal(int nextPartId) {
        IFormulaTestInputValue v = new FormulaTestInputValue(this, nextPartId);
        formulaTestInputValues.add(v);
        return v;
    }

    /**
     * {@inheritDoc}
     */
    public String getExpectedResult() {
        return expectedResult;
    }

    /**
     * {@inheritDoc}
     */
    public void setExpectedResult(String expectedResult) {
        String oldExpectedResult = this.expectedResult;
        this.expectedResult = expectedResult;
        valueChanged(oldExpectedResult, expectedResult);
    }

    /**
     * {@inheritDoc}
     */
    public boolean addOrDeleteFormulaTestInputValues(String[] newIdentifiers, IIpsProject ipsProject) {
        boolean changed = false;

        // add new or existing value on the given position
        List<IFormulaTestInputValue> newListOfInputValues = new ArrayList<IFormulaTestInputValue>();
        changed = updateWithAllIdentifiers(newIdentifiers, newListOfInputValues, ipsProject);

        // store new list
        formulaTestInputValues = newListOfInputValues;

        if (changed) {
            objectHasChanged();
        }

        return changed;
    }

    /**
     * Adds all of input values to the given list, returns <code>true</code> if there were changes.
     */
    private boolean updateWithAllIdentifiers(String[] newIdentifiers,
            List<IFormulaTestInputValue> newListOfInputValues,
            IIpsProject ipsProject) {
        List<IFormulaTestInputValue> oldInputValues = new ArrayList<IFormulaTestInputValue>();
        oldInputValues.addAll(formulaTestInputValues);

        FormulaUpdater formulaUpdater = new FormulaUpdater(newIdentifiers, oldInputValues, newListOfInputValues,
                ipsProject);
        try {
            getIpsModel().runAndQueueChangeEvents(formulaUpdater, null);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        return formulaUpdater.isFormulaTestCaseChanged();
    }

    private class FormulaUpdater implements IWorkspaceRunnable {
        private boolean formulaTestCaseChanged;
        private String[] newIdentifiers;
        private List<IFormulaTestInputValue> oldInputValues;
        private List<IFormulaTestInputValue> newListOfInputValues;
        private IIpsProject ipsProject;

        public FormulaUpdater(String[] newIdentifiers, List<IFormulaTestInputValue> oldInputValues,
                List<IFormulaTestInputValue> newListOfInputValues, IIpsProject ipsProject) {
            this.newIdentifiers = newIdentifiers;
            this.oldInputValues = oldInputValues;
            this.newListOfInputValues = newListOfInputValues;
            this.ipsProject = ipsProject;
        }

        public void run(IProgressMonitor monitor) throws CoreException {
            for (int i = 0; i < newIdentifiers.length; i++) {
                IFormulaTestInputValue inputValue = getFormulaTestInputValue(newIdentifiers[i]);
                if (inputValue == null) {
                    inputValue = newFormulaTestInputValue();
                    inputValue.setIdentifier(newIdentifiers[i]);
                    // try to set the default value depending on the corresponding value datatype
                    try {
                        Datatype datatype = inputValue.findDatatypeOfFormulaParameter(ipsProject);
                        if (datatype instanceof ValueDatatype) {
                            inputValue.setValue(((ValueDatatype)datatype).getDefaultValue());
                        }
                        // ignore if the datatype is not value datatype
                        // this is a validation error see FormulaTestInputValue#validateThis method
                    } catch (CoreException e) {
                        // ignore exception if the datatype wasn't found, this error will be handled
                        // as validation error
                        // see FormulaTestInputValue#validateThis method
                    }
                    formulaTestCaseChanged = true;
                } else {
                    int idxOld = formulaTestInputValues.indexOf(inputValue);
                    oldInputValues.remove(inputValue);
                    if (idxOld != i) {
                        formulaTestCaseChanged = true;
                    }
                }
                newListOfInputValues.add(inputValue);
            }
            // delete old input value
            for (IFormulaTestInputValue oldInputValue : oldInputValues) {
                oldInputValue.delete();
                formulaTestCaseChanged = true;
            }
        }

        public boolean isFormulaTestCaseChanged() {
            return formulaTestCaseChanged;
        }

    }

    /**
     * {@inheritDoc}
     */
    public boolean isFormulaTestCaseEmpty() {
        if (formulaTestInputValues.size() == 0) {
            return true;
        }
        for (IFormulaTestInputValue element : formulaTestInputValues) {
            if (StringUtils.isNotEmpty(element.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String generateUniqueNameForFormulaTestCase(String nameProposal) {
        String uniqueName = nameProposal;

        int idx = 2;
        IFormulaTestCase[] ftcs = getFormula().getFormulaTestCases();
        for (int i = 0; i < ftcs.length; i++) {
            if (!(ftcs[i] == this) && ftcs[i].getName().equals(uniqueName)) {
                uniqueName = nameProposal + " (" + idx++ + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                i = -1;
            }
        }
        return uniqueName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IFormula formula = getFormula();

        // check for duplicase formula test case names
        IFormulaTestCase[] ftcs = getFormula().getFormulaTestCases();
        // v2 - this code has to be moved to formula
        for (int i = 0; i < ftcs.length; i++) {
            if (!(ftcs[i] == this) && ftcs[i].getName().equals(name)) {
                String text = NLS.bind(Messages.FormulaTestCase_ValidationMessage_DuplicateFormulaTestCaseName, name);
                list.add(new Message(MSGCODE_DUPLICATE_NAME, text, Message.ERROR, this, PROPERTY_NAME));
                break;
            }
        }

        // check that the formula test input values matches the identifier in the formula
        boolean isIdentifierMismatch = false;
        String[] identifierInFormula = formula.getParameterIdentifiersUsedInFormula(ipsProject);
        if (identifierInFormula.length != formulaTestInputValues.size()) {
            isIdentifierMismatch = true;
        }
        for (int i = 0; i < identifierInFormula.length; i++) {
            if (getFormulaTestInputValue(identifierInFormula[i]) == null) {
                isIdentifierMismatch = true;
                break;
            }
        }
        if (isIdentifierMismatch) {
            String text = NLS.bind(
                    Messages.FormulaTestCase_ValidationMessage_MismatchBetweenFormulaInputValuesAndIdentifierInFormula,
                    name, getFormula().getName());
            list.add(new Message(MSGCODE_IDENTIFIER_MISMATCH, text, Message.WARNING, this, PROPERTY_NAME));
        }
    }

}
