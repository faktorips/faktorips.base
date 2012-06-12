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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class XpandBuilderTest {

    @Mock
    private IIpsProject project;

    @Mock
    private IJavaProject javaProject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(project.getJavaProject()).thenReturn(javaProject);
    }

    @Test
    public void testBeforeBuild() throws Exception {
        XpandBuilder<?> xpandBuilder = mock(XpandBuilder.class, Mockito.CALLS_REAL_METHODS);
        xpandBuilder.beforeBuildProcess(project, 0);
    }

}
