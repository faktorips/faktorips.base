/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;

/**
 * Persistence provider for standard generic JPA 2 support
 * 
 * @author Joerg Ortmann
 */
public class GenericJPA2PersistenceProvider implements IPersistenceProvider {

    public boolean isSupportingConverter() {
        return false;
    }

    public boolean isSuppotingOrphanRemoval() {
        return true;
    }

    public void addAnnotationOrphanRemoval(JavaCodeFragment javaCodeFragment) {
        // nothing to do
    }

    public String getRelationshipAnnotationAttributeOrphanRemoval() {
        return "orphanRemoval=true";
    }

    public void addAnnotationConverter(JavaCodeFragment javaCodeFragment,
            IPersistentAttributeInfo persistentAttributeInfo) {
        throw new UnsupportedOperationException();
    }

}
