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

package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.flidentifier.Messages;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;

public class AssociationNode extends IdentifierNode {

    private final IAssociation association;

    private final IIpsProject ipsProject;

    AssociationNode(IAssociation association, boolean listOfTypes, IIpsProject ipsProject) throws CoreException {
        super(association.findTarget(ipsProject), listOfTypes);
        this.association = association;
        this.ipsProject = ipsProject;
    }

    public IdentifierNode create(IAssociation association, boolean listOfTypes, IIpsProject ipsProject) {
        try {
            return new AssociationNode(association, listOfTypes, ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return new InvalidIdentifierNode(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, NLS.bind(
                    Messages.AbstractParameterIdentifierResolver_noAssociationTarget, association.getTarget(),
                    association.getName())));
        }
    }

    public IAssociation getAssociation() {
        return association;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

}
