/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.Locale;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;

/**
 * 
 * @author Jan Ortmann
 */
public class PolicyCmptImplClassBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    
    
    
    /**
     * @param name
     */
    public PolicyCmptImplClassBuilderTest(String name) {
        super(name);
    }

    /*
     * @see IpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        IIpsProjectProperties props = project.getProperties();
        props.setJavaSrcLanguage(Locale.GERMAN);
        project.setProperties(props);
    }
    
    public void testBuild() throws CoreException {
        
        //the supertype of this subtype is missing on purpose for this testcase.
        //this subtype overrides an attribute that doesn't exist.
        //there was a bug that caused a NPE for this scenario.
        //This testcase makes sure that this bug will be found fast if it comes up again.
        PolicyCmptType b = newPolicyCmptType(project, "SubType");
        IPolicyCmptTypeAttribute bAttr = b.newPolicyCmptTypeAttribute();
        bAttr.setAttributeType(AttributeType.CHANGEABLE);
        bAttr.setDatatype(Datatype.INTEGER.getQualifiedName());
        bAttr.setOverwrites(true);
        bAttr.setName("age");

        try{
            project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        }
        catch(CoreException e){
            printOriginalStatus(e.getStatus());
            fail();
        }
    }
}
