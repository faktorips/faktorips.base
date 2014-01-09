/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public interface AnnotationGeneratorFactory {

    /**
     * Returns <code>true</code> if the factory is required for the given project, as the
     * appropriate annotations have to be generated.
     * <p>
     * Note: This method is called during initialization of the builder set. Hence you cannot ask
     * the IpsProject for its builder set!
     * 
     * @param ipsProject The {@link IIpsProject} for which the {@link AnnotationGeneratorFactory}
     *            may be required.
     */
    public boolean isRequiredFor(IIpsProject ipsProject);

    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) throws CoreException;

}
