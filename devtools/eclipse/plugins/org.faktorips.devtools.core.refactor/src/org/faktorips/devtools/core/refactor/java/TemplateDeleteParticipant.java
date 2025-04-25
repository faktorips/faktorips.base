/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor.java;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.util.Tree;
import org.faktorips.devtools.model.util.Tree.Node;

public class TemplateDeleteParticipant extends DeleteParticipant {

    private IProductCmpt template;

    public TemplateDeleteParticipant() {
    }

    @Override
    protected boolean initialize(Object element) {
        if (element instanceof IResource resource) {
            IIpsElement ipsElement = IIpsModel.get().findIpsElement(Wrappers.wrap(resource).as(AResource.class));
            if (ipsElement instanceof IIpsSrcFile ipsSrcFile) {
                if (IpsObjectType.PRODUCT_TEMPLATE.equals(ipsSrcFile.getIpsObjectType())) {
                    template = (IProductCmpt)ipsSrcFile.getIpsObject();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws OperationCanceledException {
        return new RefactoringStatus();
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        if (template == null) {
            return null;
        }
        Tree<IIpsSrcFile> templateHierarchy = template.getIpsProject().findTemplateHierarchy(template);
        List<Node<IIpsSrcFile>> directlyReferencingSrcFiles = templateHierarchy.getRoot().getChildren();
        if (directlyReferencingSrcFiles.isEmpty()) {
            return null;
        }
        CompositeChange compositeChange = new CompositeChange(Messages.TemplateDeleteParticipant_CompositeChange);
        directlyReferencingSrcFiles.stream()
                .map(Node::getElement)
                .map(RemoveTemplateUsageChange::new)
                .forEach(compositeChange::add);
        return compositeChange;
    }
}
