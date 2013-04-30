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

package org.faktorips.devtools.formulalibrary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaLibrary;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class FormulaLibraryResolverTest extends AbstractIpsPluginTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IpsSrcFile ipsSrcFile;

    @Mock
    private FormulaLibrary formulaLibrary;

    @Mock
    private FlFunction flFunction;

    @Override
    @Before
    public void setUp() throws CoreException {
        MockitoAnnotations.initMocks(this);
        IpsSrcFile[] ipsSrcFiles = new IpsSrcFile[] { ipsSrcFile };
        when(ipsProject.findIpsSrcFiles((IpsObjectType)Mockito.anyObject())).thenReturn(ipsSrcFiles);
        List<FlFunction> flfunctions = new ArrayList<FlFunction>();
        flfunctions.add(flFunction);
        when(formulaLibrary.getFlFunctions()).thenReturn(flfunctions);
        when(ipsSrcFile.getIpsObject()).thenReturn(formulaLibrary);
    }

    @Test
    public void testGetFunctions() throws Exception {
        FunctionResolver resolver = new FormulaLibraryResolver(ipsProject);
        FlFunction[] functions = resolver.getFunctions();
        assertNotNull(functions);
        assertEquals(1, functions.length);
    }

}
