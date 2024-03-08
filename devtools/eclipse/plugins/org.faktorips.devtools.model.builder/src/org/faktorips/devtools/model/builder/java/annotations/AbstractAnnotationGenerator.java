/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Abstract base class for annotation generators.
 * 
 * @author Roman Grutza
 */
public abstract class AbstractAnnotationGenerator implements IAnnotationGenerator {

    public AbstractAnnotationGenerator() {
    }

    public IIpsArtefactBuilderSet getBuilderSet(IIpsProject ipsProject) {
        return ipsProject.getIpsArtefactBuilderSet();
    }

    protected JavaCodeFragment newJavaCodeFragment() {
        return new JavaCodeFragment();
    }

    /**
     * Appends a key/value pair divided by an assign ('=') literal to the given code fragment at the
     * current position. The key is the literal "name", the value is an arbitrary unquoted String
     * which will be quoted.
     * <p>
     * Example:<br>
     * appendName(f, "tableName") <br>
     * yields<br>
     * name = "tableName"
     * 
     * @param fragment A <code>JavaCodeFragment</code> instance.
     * @param value The value which will be assigned to, must not be quoted
     * 
     * @return The same code fragment given as parameter 1, allowing method chaining
     */
    protected JavaCodeFragment appendName(JavaCodeFragment fragment, String value) {
        return appendKeyValuePairDividedByAssignLiteral(fragment, "name", '"' + value + '"');
    }

    /**
     * Appends the String "key = value" to fragment (without the quotes).
     */
    protected JavaCodeFragment appendKeyValuePairDividedByAssignLiteral(JavaCodeFragment fragment,
            String key,
            String value) {
        return fragment.append(key).append(" = ").append(value);
    }
}
