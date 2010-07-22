/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.bf;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ActionBFE extends MethodCallBFE implements IActionBFE {

    public ActionBFE(IIpsObject parent, String id) {
        super(parent, id);
    }

    @Override
    public String getDisplayString() {
        if (BFElementType.ACTION_METHODCALL.equals(getType())) {
            return super.getDisplayString();
        }
        if (BFElementType.ACTION_BUSINESSFUNCTIONCALL.equals(getType())) {
            return getTarget();
        }
        return getName();
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IActionBFE.XML_TAG);
    }

    @Override
    public String getReferencedBfQualifiedName() {
        if (getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
            return getTarget();
        }
        return null;
    }

    @Override
    public String getReferencedBfUnqualifedName() {
        if (getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
            int index = StringUtils.lastIndexOf(getTarget(), '.');
            if (index == -1) {
                return getTarget();
            }
            return getTarget().substring(index + 1, getTarget().length());
        }
        return null;
    }

    @Override
    public IBusinessFunction findReferencedBusinessFunction() throws CoreException {
        return (IBusinessFunction)getIpsProject().findIpsObject(BusinessFunctionIpsObjectType.getInstance(),
                getTarget());
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        if (getType().equals(BFElementType.ACTION_METHODCALL)) {
            validateMethodCall(list, ipsProject);
        }
        validateBusinessFunctionCallAction(list);
    }

    private void validateBusinessFunctionCallAction(MessageList msgList) throws CoreException {
        if (getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
            // business function has to be specified
            if (StringUtils.isEmpty(getTarget())) {
                msgList.add(new Message(MSGCODE_TARGET_NOT_SPECIFIED, Messages.ActionBFE_bfMustBeSpecified,
                        Message.ERROR, this));
            }
            validateNotAllowedNames(getTarget(), Messages.ActionBFE_bfName, msgList);
            // business function exists
            IBusinessFunction refBf = findReferencedBusinessFunction();
            if (refBf == null) {
                msgList.add(new Message(MSGCODE_TARGET_DOES_NOT_EXIST, Messages.ActionBFE_bfDoesNotExist,
                        Message.ERROR, this));
            }
        }
    }

    @Override
    public RenameRefactoring getRenameRefactoring() {
        return null;
    }

    public boolean isRenameRefactoringSupported() {
        return false;
    }

}
