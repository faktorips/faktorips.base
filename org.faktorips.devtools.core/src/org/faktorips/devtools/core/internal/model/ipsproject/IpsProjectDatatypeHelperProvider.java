/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsproject;

import org.faktorips.codegen.BaseDatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.fl.DatatypeHelperProvider;

class IpsProjectDatatypeHelperProvider implements DatatypeHelperProvider<JavaCodeFragment> {
    private final IIpsProject ipsProject;

    IpsProjectDatatypeHelperProvider(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    @Override
    public BaseDatatypeHelper<JavaCodeFragment> getDatatypeHelper(Datatype datatype) {
        return ipsProject.findDatatypeHelper(datatype.getQualifiedName());
    }
}