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

package org.faktorips.devtools.formulalibrary.internal.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.method.BaseMethod;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation for {@link IFormulaMethod} and extend {@link BaseMethod} with formulaName
 * 
 * @author frank
 */
public class FormulaMethod extends BaseMethod implements IFormulaMethod {

    private String formulaName = StringUtils.EMPTY;

    public FormulaMethod(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    public String getFormulaName() {
        return formulaName;
    }

    @Override
    public void setFormulaName(String newFormulaName) {
        String oldFormulaName = getFormulaName();
        this.formulaName = newFormulaName;
        valueChanged(oldFormulaName, formulaName, IFormulaMethod.PROPERTY_FORMULA_NAME);
    }

    @Override
    public String getDefaultMethodName() {
        return name;
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        formulaNameToXml(element);
    }

    private void formulaNameToXml(Element element) {
        if (!StringUtils.isEmpty(getFormulaName())) {
            element.setAttribute(IFormulaMethod.PROPERTY_FORMULA_NAME, getFormulaName());
        }
    }

    @Override
    public void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        initFormulaName(element);
    }

    private void initFormulaName(Element element) {
        String formulaNameString = element.getAttribute(IFormulaMethod.PROPERTY_FORMULA_NAME);
        if (!StringUtils.isEmpty(formulaNameString)) {
            formulaName = formulaNameString;
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        validateFormulaName(list);
        validateDatatype(list, ipsProject);
    }

    private void validateFormulaName(MessageList list) {
        if (StringUtils.isEmpty(getFormulaName())) {
            String text = Messages.FormulaMethod_FormulaNameIsMissing;
            list.add(new Message(IFormulaMethod.MSGCODE_FORMULA_NAME_IS_EMPTY, text, Message.ERROR, this,
                    IFormulaMethod.PROPERTY_FORMULA_NAME));
        } else {
            IIpsProject ipsProject = getIpsProject();
            String complianceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
            String sourceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_SOURCE, true);
            IStatus status = JavaConventions.validateMethodName(getFormulaName(), sourceLevel, complianceLevel);
            if (!status.isOK()) {
                list.add(new Message(IFormulaMethod.MSGCODE_INVALID_FORMULA_NAME,
                        Messages.FormulaMethod_msgInvalidFormulaName, Message.ERROR, this, PROPERTY_NAME));
            }
        }
    }

    private void validateDatatype(MessageList list, IIpsProject ipsProject) throws CoreException {
        Datatype datatype = findDatatype(ipsProject);
        if (datatype != null) {
            if (datatype.isVoid() || !datatype.isValueDatatype()) {
                String text = Messages.FormulaMethod_FormulaSignatureDatatypeMustBeAValueDatatype;
                list.add(new Message(IFormulaMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES,
                        text, Message.ERROR, this, IBaseMethod.PROPERTY_DATATYPE));
            }
        }
    }
}
