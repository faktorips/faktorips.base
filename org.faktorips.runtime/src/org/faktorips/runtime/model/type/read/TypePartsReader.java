/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type.read;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsAttributes;

/**
 * Reads the model parts from a {@link AnnotatedDeclaration}. After constructing the reader with the
 * necessary collectors you need to call {@link #init(AnnotatedDeclaration)}. This
 * {@link AnnotatedDeclaration} need to be annotated with the annotations that contains all names of
 * the pars (e.g. {@link IpsAttributes}). After initialization you could call
 * {@link #read(AnnotatedDeclaration)} multiple times with different declarations (e.g. first for
 * product component and second for generation).
 * <p>
 * At the end the collectors have collected all relevant methods and could be asked for the
 * requested parts.
 * 
 */
public class TypePartsReader {

    private List<TypePartCollector<?, ?>> collectors;

    public TypePartsReader(TypePartCollector<?, ?>... collectors) {
        this.collectors = Arrays.asList(collectors);
    }

    public void init(AnnotatedDeclaration annotatedDeclaration) {
        for (TypePartCollector<?, ?> typePartCollector : collectors) {
            typePartCollector.initDescriptors(annotatedDeclaration);
        }
    }

    public void read(AnnotatedDeclaration annotatedDeclaration) {
        for (AnnotatedElement annotatedElement : annotatedDeclaration.getDeclaredElements()) {
            for (TypePartCollector<?, ?> typePartCollector : collectors) {
                typePartCollector.readAnnotations(annotatedDeclaration, annotatedElement);
            }
        }
    }

}
