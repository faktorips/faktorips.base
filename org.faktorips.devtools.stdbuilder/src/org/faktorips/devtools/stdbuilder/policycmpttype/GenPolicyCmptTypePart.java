/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.DefaultJavaGeneratorForIpsPart2;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

public abstract class GenPolicyCmptTypePart extends DefaultJavaGeneratorForIpsPart2{

    private GenPolicyCmptType genPolicyCmptType;
    
    public GenPolicyCmptTypePart(GenPolicyCmptType genPolicyCmptType, IIpsObjectPartContainer part, LocalizedStringsSet stringsSet) throws CoreException {
        super(part, stringsSet);
        ArgumentCheck.notNull(genPolicyCmptType, this);
        this.genPolicyCmptType = genPolicyCmptType;
    }


    public GenPolicyCmptType getGenPolicyCmptType(){
        return genPolicyCmptType;
    }
}
