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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.net.URL;

import org.eclipse.internal.xtend.expression.parser.SyntaxConstants;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.junit.Test;

public class PolicyCmptImplClassBuilderTest {

    @Test
    public void testGetTemplate_exists() throws Exception {
        PolicyCmptImplClassBuilder policyCmptImplClassBuilder = new PolicyCmptImplClassBuilder(false,
                mock(StandardBuilderSet.class), mock(GeneratorModelContext.class), null);
        String template = policyCmptImplClassBuilder.getTemplate();
        int lastIndexOf = template.lastIndexOf(SyntaxConstants.NS_DELIM);
        template = template.substring(0, lastIndexOf);
        String templatePath = template.replaceAll(SyntaxConstants.NS_DELIM, "/") + ".xpt";
        URL resource = PolicyCmptImplClassBuilder.class.getClassLoader().getResource(templatePath);
        assertNotNull(resource);
    }

}
