/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.IType;

public class InheritedTypeMethodsPageElement extends AbstractInheritedIpsObjectPartsPageElement<IType, IMethod> {

    private List<IMethod> overriddenMethods;

    public InheritedTypeMethodsPageElement(DocumentationContext context, IType element, List<IType> superElements) {
        super(context, element, superElements);
    }

    @Override
    protected boolean showObjectPart(IMethod objectPart) {
        registerOverriddenMethod(objectPart);
        return !overriddenMethods.contains(objectPart);
    }

    @Override
    protected void buildInternal() {
        initializeOverriddenMethodList();
        super.buildInternal();
    }

    private void initializeOverriddenMethodList() {
        overriddenMethods = new ArrayList<>();
        for (IMethod iMethod : getIpsObjectParts(getParentIpsElement())) {
            registerOverriddenMethod(iMethod);
        }
    }

    private void registerOverriddenMethod(IMethod objectPart) {
        try {
            IMethod overriddenMethod = objectPart.findOverriddenMethod(objectPart.getIpsProject());

            if (overriddenMethod == null) {
                return;
            }

            overriddenMethods.add(overriddenMethod);
        } catch (IpsException e) {
            getContext().addStatus(new IpsStatus(IStatus.WARNING, "Error finding overridden Method", e)); //$NON-NLS-1$
        }
    }

    @Override
    protected String createHeadline(IType superElement) {
        return getContext().getMessage(HtmlExportMessages.InheritedTypeMethodsPageElement_inheritedMethods)
                + " " + superElement.getQualifiedName(); //$NON-NLS-1$
    }

    @Override
    protected List<IMethod> getIpsObjectParts(IType ipsElement) {
        return ipsElement.getMethods();
    }
}
