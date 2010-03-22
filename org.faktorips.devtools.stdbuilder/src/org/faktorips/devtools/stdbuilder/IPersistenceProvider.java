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

public interface IPersistenceProvider {

    public boolean isSuppotingOrphanRemoval();

    public boolean isSupportingJoinFetchType();

    public boolean isSupportingConverter();

    public void addAnnotationOrphanRemoval(JavaCodeFragment javaCodeFragment);

    public void addAnnotationJoinFetchType(JavaCodeFragment javaCodeFragment);

    public void addAnnotationConverter(JavaCodeFragment javaCodeFragment,
            IPersistentAttributeInfo persistentAttributeInfo);

}
