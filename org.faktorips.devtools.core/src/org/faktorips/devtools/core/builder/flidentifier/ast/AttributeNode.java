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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;

public class AttributeNode extends IdentifierNode {

    private final IAttribute attribute;

    private final IIpsProject ipsProject;

    public AttributeNode(String name, IAttribute attribute, IIpsProject ipsProject) throws CoreException {
        super(name, attribute.findDatatype(ipsProject));
        this.attribute = attribute;
        this.ipsProject = ipsProject;
    }

    public IAttribute getAttribute() {
        return attribute;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

}
