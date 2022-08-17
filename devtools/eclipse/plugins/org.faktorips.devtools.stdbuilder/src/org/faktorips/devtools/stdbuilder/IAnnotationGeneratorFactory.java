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

import org.faktorips.devtools.model.ipsproject.IIpsProject;

public interface IAnnotationGeneratorFactory {

    /**
     * Returns <code>true</code> if the factory is required for the given project, as the
     * appropriate annotations have to be generated.
     * <p>
     * Note: This method is called during initialization of the builder set. Hence you cannot ask
     * the IpsProject for its builder set!
     * 
     * @param ipsProject The {@link IIpsProject} for which the {@link IAnnotationGeneratorFactory}
     *            may be required.
     */
    boolean isRequiredFor(IIpsProject ipsProject);

    IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type);

}
