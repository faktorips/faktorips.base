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

    private final boolean defaultValueAccess;

    private final IIpsProject ipsProject;

    public AttributeNode(IAttribute attribute, boolean defaultValueAccess, boolean listOfTypes, IIpsProject ipsProject)
            throws CoreException {
        super(attribute.findDatatype(ipsProject), listOfTypes);
        this.attribute = attribute;
        this.defaultValueAccess = defaultValueAccess;
        this.ipsProject = ipsProject;
    }

    public IAttribute getAttribute() {
        return attribute;
    }

    public boolean isDefaultValueAccess() {
        return defaultValueAccess;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

}
