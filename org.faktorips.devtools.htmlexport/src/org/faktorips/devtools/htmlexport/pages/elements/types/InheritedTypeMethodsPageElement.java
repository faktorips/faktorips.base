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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;

public class InheritedTypeMethodsPageElement extends AbstractInheritedIpsObjectPartsPageElement<IType, IMethod> {

    public InheritedTypeMethodsPageElement(DocumentationContext context, IType element, List<IType> superElements) {
        super(context, element, superElements);
    }

    private List<IMethod> overriddenMethods;

    @Override
    protected boolean showObjectPart(IMethod objectPart) {
        registerOverriddenMethod(objectPart);
        return !overriddenMethods.contains(objectPart);
    }

    @Override
    public void build() {
        initializeOverriddenMethodList();
        super.build();
    }

    private void initializeOverriddenMethodList() {
        overriddenMethods = new ArrayList<IMethod>();
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
        } catch (CoreException e) {
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
