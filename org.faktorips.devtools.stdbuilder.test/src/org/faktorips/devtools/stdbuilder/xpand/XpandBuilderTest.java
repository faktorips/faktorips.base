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

package org.faktorips.devtools.stdbuilder.xpand;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyCmptImplClassBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class XpandBuilderTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IIpsProject ipsProject;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StandardBuilderSet builderSet;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testBeforeBuildProcess() throws Exception {
        // Using PolicyCmptImlClassBuilder as concrete instance knowing that this also tests this
        // other class
        PolicyCmptImplClassBuilder policyCmptImplClassBuilder = new PolicyCmptImplClassBuilder(false, builderSet,
                mock(GeneratorModelContext.class), null);
        policyCmptImplClassBuilder.beforeBuildProcess(ipsProject, 0);
        assertNotNull(policyCmptImplClassBuilder.getOut());
        assertNotNull(policyCmptImplClassBuilder.getTemplateDefinition());
    }
}
