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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.DefaultJavaGeneratorForIpsPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

public abstract class GenProductCmptTypePart extends DefaultJavaGeneratorForIpsPart {

    private GenProductCmptType genProductCmptType;

    public GenProductCmptTypePart(GenProductCmptType genProductCmptType, IIpsObjectPartContainer part,
            LocalizedStringsSet stringsSet) throws CoreException {
        super(part, stringsSet);
        ArgumentCheck.notNull(genProductCmptType, this);
        this.genProductCmptType = genProductCmptType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLanguageUsedInGeneratedSourceCode() {
        return genProductCmptType.getLanguageUsedInGeneratedSourceCode();
    }

    public IIpsProject getIpsProject() {
        return getGenProductCmptType().getIpsPart().getIpsProject();
    }

    public GenProductCmptType getGenProductCmptType() {
        return genProductCmptType;
    }

    protected boolean isUseTypesafeCollections() {
        return getGenProductCmptType().getBuilderSet().isUseTypesafeCollections();
    }
}
