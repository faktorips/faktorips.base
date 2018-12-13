/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.productcmpt;

import java.util.Locale;

import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

public class ProductCmptGenJavaClassNameProvider extends DefaultJavaClassNameProvider {

    private final Locale locale;

    public ProductCmptGenJavaClassNameProvider(boolean isGeneratePublishedInterface, Locale locale) {
        super(isGeneratePublishedInterface);
        this.locale = locale;
    }

    @Override
    public String getImplClassName(IIpsSrcFile ipsSrcFile) {
        String generationAbb = getAbbreviationForGenerationConcept(ipsSrcFile);
        return ipsSrcFile.getIpsProject().getJavaNamingConvention()
                .getImplementationClassName(ipsSrcFile.getIpsObjectName() + generationAbb);
    }

    @Override
    public String getInterfaceNameInternal(IIpsSrcFile ipsSrcFile) {
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
