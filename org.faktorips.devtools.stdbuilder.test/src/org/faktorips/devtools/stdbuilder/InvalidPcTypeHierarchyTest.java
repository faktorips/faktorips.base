/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * 
 * 
 * @author Jan Ortmann
 */
public class InvalidPcTypeHierarchyTest extends AbstractIpsPluginTest {

    public void test() throws CoreException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType type1 = newPolicyCmptType(project, "Type1");
        IPolicyCmptType type2 = newPolicyCmptType(project, "Type2");
        IPolicyCmptType type3 = newPolicyCmptType(project, "Type3");

        type3.setSupertype(type2.getQualifiedName());
        type2.setSupertype(type1.getQualifiedName());
        type1.setSupertype(type3.getQualifiedName());

        type1.getIpsSrcFile().save(true, null);
        type2.getIpsSrcFile().save(true, null);
        type3.getIpsSrcFile().save(true, null);

        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

}
