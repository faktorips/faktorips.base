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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class AssociationTargetDoesNotExistTest extends AbstractStdBuilderTest {

    public AssociationTargetDoesNotExistTest() {
        super();
    }

    @Test
    public void test() throws CoreException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType sourceType = newPolicyCmptTypeWithoutProductCmptType(project, "Source");
        IPolicyCmptType targetType = newPolicyCmptTypeWithoutProductCmptType(project, "target");

        IPolicyCmptTypeAssociation fromSourceToTarget = sourceType.newPolicyCmptTypeAssociation();
        fromSourceToTarget.setTarget(targetType.getQualifiedName());
        fromSourceToTarget.setTargetRoleSingular("Target");
        fromSourceToTarget.setTargetRolePlural("Targets");

        IPolicyCmptTypeAssociation fromTargetToSource = targetType.newPolicyCmptTypeAssociation();
        fromTargetToSource.setTarget(sourceType.getQualifiedName());
        fromTargetToSource.setTargetRoleSingular("Source");
        fromTargetToSource.setTargetRolePlural("Sources");

        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        targetType.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
    }

}
