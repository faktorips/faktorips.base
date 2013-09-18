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

package org.faktorips.devtools.stdbuilder.flidentifier;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.devtools.core.builder.flidentifier.AbstractIdentifierNodeGenerator;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;

public abstract class AbstractIdentifierGenerator<T extends CodeFragment> extends AbstractIdentifierNodeGenerator<T> {

    private final StandardBuilderSet builderSet;

    public AbstractIdentifierGenerator(IdentifierNodeGeneratorFactory<T> factory, StandardBuilderSet builderSet) {
        super(factory);
        this.builderSet = builderSet;
    }

    protected <X extends AbstractGeneratorModelNode> X getModelNode(IIpsObjectPartContainer container, Class<X> type) {
        return getBuilderSet().getModelNode(container, type);
    }

    private StandardBuilderSet getBuilderSet() {
        return builderSet;
    }

}
