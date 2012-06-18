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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.util.Locale;

import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

public class ProductCmptGenJavaClassNameProvider extends DefaultJavaClassNameProvider {

    private final Locale locale;

    public ProductCmptGenJavaClassNameProvider(Locale locale) {
        this.locale = locale;
    }

    @Override
    public String getImplClassName(IIpsSrcFile ipsSrcFile) {
        String generationAbb = getAbbreviationForGenerationConcept(ipsSrcFile);
        return ipsSrcFile.getIpsProject().getJavaNamingConvention()
                .getImplementationClassName(ipsSrcFile.getIpsObjectName() + generationAbb);
    }

    @Override
    public String getInterfaceName(IIpsSrcFile ipsSrcFile) {
        String name = ipsSrcFile.getIpsObjectName() + getAbbreviationForGenerationConcept(ipsSrcFile);
        return ipsSrcFile.getIpsProject().getJavaNamingConvention().getPublishedInterfaceName(name);
    }

    /**
     * Returns the abbreviation for the generation (changes over time) concept.
     * 
     * @param element An <tt>IIpsElement</tt> needed to access the <tt>IIpsProject</tt> where the
     *            necessary configuration information is stored.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention
     */
    public String getAbbreviationForGenerationConcept(IIpsElement element) {
        return JavaSourceFileBuilder.getChangesInTimeNamingConvention(element).getGenerationConceptNameAbbreviation(
                locale);
    }

}
