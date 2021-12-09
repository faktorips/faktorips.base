/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
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
    public void test() throws CoreRuntimeException {
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

        targetType.getIpsSrcFile().getCorrespondingFile().delete(null);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
    }

}
