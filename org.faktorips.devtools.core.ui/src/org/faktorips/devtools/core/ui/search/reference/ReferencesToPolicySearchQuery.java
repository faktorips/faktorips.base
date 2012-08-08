/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * 
 * @author Stefan Widmaier
 */
public class ReferencesToPolicySearchQuery extends ReferenceSearchQuery {

    public ReferencesToPolicySearchQuery(IPolicyCmptType referenced) {
        super(referenced);
    }

    @Override
    protected IIpsElement[] findReferences() throws CoreException {
        List<IPolicyCmptType> referencingPolicyCmptTypes = new ArrayList<IPolicyCmptType>();
        
        IIpsProject[] referencingProjects = referenced.getIpsProject().findReferencingProjectLeavesOrSelf();
        
        for (IIpsProject referencingIpsProject : referencingProjects) {
            IPolicyCmptType[] findReferencingPolicyCmptTypes = referencingIpsProject
                    .findReferencingPolicyCmptTypes((IPolicyCmptType)referenced);
        
            referencingPolicyCmptTypes.addAll(Arrays.asList(findReferencingPolicyCmptTypes));
        }
        return referencingPolicyCmptTypes.toArray(new IIpsElement[referencingPolicyCmptTypes.size()]);
    }

    @Override
    protected Object[] getDataForResult(IIpsElement object) throws CoreException {
        return new Object[] { object };
    }
}
