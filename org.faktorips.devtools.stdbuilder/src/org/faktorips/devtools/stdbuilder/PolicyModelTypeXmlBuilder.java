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

package org.faktorips.devtools.stdbuilder;

import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;

/**
 * The builder for the policy component type XML.
 * <p>
 * This class do not really much more than {@link ModelTypeXmlBuilder} but we have to use unique
 * builder classes for different aspects in the StandardBuilder.
 * 
 * @author dirmeier
 */
public class PolicyModelTypeXmlBuilder extends ModelTypeXmlBuilder {

    public PolicyModelTypeXmlBuilder(DefaultBuilderSet builderSet) {
        super(IpsObjectType.POLICY_CMPT_TYPE, builderSet);
    }

}
