/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.faktorips.codegen.JavaCodeFragment;

/**
 * Abstract base class for annotation generators.
 * 
 * @author Roman Grutza
 */
public abstract class AbstractAnnotationGenerator implements IAnnotationGenerator {

    private final StandardBuilderSet builderSet;

    public AbstractAnnotationGenerator(StandardBuilderSet builderSet) {
        this.builderSet = builderSet;
    }

    public StandardBuilderSet getStandardBuilderSet() {
        return builderSet;
    }

    protected JavaCodeFragment newJavaCodeFragment() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        return fragment;
    }

    /**
     * Appends a key/value pair divided by an assign ('=') literal to the given code fragment at the
     * current position. The key is the literal "name", the value is an arbitrary unquoted String
     * which will be quoted.
     * <p/>
     * Example:<br/>
     * appendName(f, "tableName") <br/>
     * yields<br/>
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
