/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.faktorips.runtime.util.StringBuilderJoiner;
import org.faktorips.util.StringUtil;

/**
 * The class represents a Java source code fragment. A source code fragment consists of the source
 * code text and the import statements needed to compile the text.
 * 
 * @see JavaCodeFragmentBuilder
 * @see ImportDeclaration
 */
public class JavaCodeFragment extends CodeFragment {

    // import declaration needed to compile the source code
    private ImportDeclaration importDecl;

    /**
     * Creates a new empty JavaCodeFragment.
     */
    public JavaCodeFragment() {
        this(false);
    }

    public JavaCodeFragment(boolean indent) {
        super(indent);
        importDecl = new ImportDeclaration();
    }

    /**
     * Creates a new JavaCodeFragment with the indicated source code and no import statements.
     */
    public JavaCodeFragment(String sourcecode) {
        this(sourcecode, new ImportDeclaration());
    }

    /**
     * Creates a new JavaCodeFragment with the indicated source code and import declaration.
     */
    public JavaCodeFragment(String sourcecode, ImportDeclaration importDecl) {
        super(sourcecode);
        this.importDecl = new ImportDeclaration(importDecl);
    }

    /**
     * Constructs a JavaCodeFragment that has the same source code as the given fragment with all
     * import statements removed that are obsolete because of the indicated package.
     */
    public JavaCodeFragment(JavaCodeFragment fragment, String packageName) {
        super(fragment);
        importDecl = new ImportDeclaration(fragment.importDecl, packageName);
    }

    /**
     * Copy constructor.
     */
    public JavaCodeFragment(JavaCodeFragment fragment) {
        this(fragment.getSourcecode(), new ImportDeclaration(fragment.getImportDeclaration()));
    }

    /**
     * Returns the import declaration needed to compile the source code.
     */
    public ImportDeclaration getImportDeclaration() {
        // defensive copy
        return new ImportDeclaration(importDecl);
    }

    /**
     * Returns the import declaration needed to compile the source code. The returned import
     * declaration does not contain any import statements that refer to the indicated packageName.
     * The method is useful to avoid unnecessary import statements.
     */
    public ImportDeclaration getImportDeclaration(String packageName) {
        return new ImportDeclaration(importDecl, packageName);
    }

    /**
     * Appends '{' to the source code and increases the indentation level.
     */
    public JavaCodeFragment appendOpenBracket() {
        appendln('{');
        incIndentationLevel();
        return this;
    }

    /**
     * Appends '}' to the source code and decreases the indentation level.
     */
    public JavaCodeFragment appendCloseBracket() {
        appendln('}');
        decIndentationLevel();
        return this;
    }

    @Override
    public JavaCodeFragment append(String s) {
        return (JavaCodeFragment)super.append(s);
    }

    @Override
    public JavaCodeFragment appendJoined(Iterable<?> iterable) {
        return (JavaCodeFragment)super.appendJoined(iterable);
    }

    @Override
    public JavaCodeFragment appendJoined(Object[] array) {
        return (JavaCodeFragment)super.appendJoined(array);
    }

    public JavaCodeFragment appendJoined(JavaCodeFragment[] javaCodeFragments) {
        boolean first = true;
        for (JavaCodeFragment javaCodeFragment : javaCodeFragments) {
            if (!first) {
                append(StringBuilderJoiner.DEFAULT_SEPARATOR);
            }
            append(javaCodeFragment);
            first = false;
        }
        return this;
    }

    @Override
    public JavaCodeFragment appendQuoted(String s) {
        return (JavaCodeFragment)super.appendQuoted(s);
    }

    @Override
    public JavaCodeFragment append(char c) {
        return (JavaCodeFragment)super.append(c);
    }

    @Override
    public JavaCodeFragment appendln() {
        return (JavaCodeFragment)super.appendln();
    }

    @Override
    public JavaCodeFragment appendln(String s) {
        return (JavaCodeFragment)super.appendln(s);
    }

    @Override
    public JavaCodeFragment appendlnUnindented(String arg) {
        return (JavaCodeFragment)super.appendlnUnindented(arg);
    }

    @Override
    public JavaCodeFragment appendln(char c) {
        return (JavaCodeFragment)super.appendln(c);
    }

    /**
     * Transform the given int into a String and appends it to the source code.
     */
    public JavaCodeFragment append(int i) {
        append("" + i); //$NON-NLS-1$
        return this;
    }

    /**
     * Transform the given boolean into a String and appends it to the source code.
     */
    public JavaCodeFragment append(boolean b) {
        append("" + b); //$NON-NLS-1$
        return this;
    }

    /**
     * Appends the unqualified class name to the source code and updates the import declaration (if
     * necessary).
     * 
     * @throws NullPointerException if clazz is null.
     */
    public JavaCodeFragment appendClassName(Class<?> clazz) {
        if (clazz.isArray()) {
            appendClassName(clazz.getComponentType());
            append("[]"); //$NON-NLS-1$
            return this;
        }
        appendClassName(clazz.getName());
        return this;
    }

    /**
     * Appends the unqualified class name of an public inner class to the source code and updates
     * the import declaration (if necessary).
     * 
     * @throws NullPointerException if clazz is null.
     */
    public JavaCodeFragment appendInnerClassName(Class<?> clazz) {
        appendInnerClassName(clazz.getName());
        return this;
    }

    /**
     * Appends the unqualified class name of an public inner class to the source code and updates
     * the import declaration (if necessary).
     * 
     * @throws NullPointerException if qualifiedClassName is null.
     */
    public JavaCodeFragment appendInnerClassName(String qualifiedClassName) {
        appendClassName(qualifiedClassName.replace('$', '.'));
        return this;
    }

    /**
     * Appends the unqualified class name to the source code and updates the import declaration (if
     * necessary). A [] appended at the end of the qualified class name indicates the array of this
     * type. The brackets are added correctly at the end of the class name in the source code.
     * 
     * @throws NullPointerException if qualifiedClassName is null.
     */
    public JavaCodeFragment appendClassName(String qualifiedClassName) {
        if (qualifiedClassName.contains("<")) { //$NON-NLS-1$
            return appendClassNameAndParseGenerics(qualifiedClassName);
        } else {
            return appendClassNameWithoutGenerics(qualifiedClassName);
        }
    }

    private JavaCodeFragment appendClassNameAndParseGenerics(String qualifiedClassName) {
        appendClassNameWithoutGenerics(qualifiedClassName.substring(0, qualifiedClassName.indexOf('<')));
        parseGenerics(qualifiedClassName);
        return this;
    }

    private void parseGenerics(String qualifiedClassName) {
        String generics = qualifiedClassName.substring(qualifiedClassName.indexOf('<') + 1,
                qualifiedClassName.lastIndexOf('>'));
        List<String> toplevelGenericTypes = splitToplevelGenericTypesByColon(generics);
        append('<');
        for (Iterator<String> iterator = toplevelGenericTypes.iterator(); iterator.hasNext();) {
            String nextGenericType = iterator.next();
            // Recursion: also handles nested generics.
            appendClassName(nextGenericType);
            if (iterator.hasNext()) {
                append(", "); //$NON-NLS-1$
            }
        }
        append('>');
    }

    List<String> splitToplevelGenericTypesByColon(String genericsDefinition) {
        LinkedList<String> result = new LinkedList<>();
        int depth = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < genericsDefinition.length(); i++) {
            char c = genericsDefinition.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            } else if (c == ',' && depth == 0) {
                result.add(sb.toString().trim());
                sb = new StringBuilder();
                continue;
            }
            sb.append(c);
        }
        result.add(sb.toString().trim());
        return result;
    }

    private JavaCodeFragment appendClassNameWithoutGenerics(final String className) {
        // Consider "$" for inner classes
        String qualifiedClassName = className.replace('$', '.');
        String unqualifiedClassName = StringUtil.unqualifiedName(qualifiedClassName);
        // don't add two imports for the same unqualified name
        for (Iterator<String> iterator = importDecl.iterator(); iterator.hasNext();) {
            String imp = iterator.next();
            if (imp.substring(imp.lastIndexOf('.') + 1).equals(unqualifiedClassName)
                    && !imp.equals(qualifiedClassName)) {
                append(qualifiedClassName);
                return this;
            }
        }
        append(unqualifiedClassName);
        if (qualifiedClassName.indexOf('.') < 0) {
            return this;
        }
        int bracketIndex = qualifiedClassName.indexOf("[]"); //$NON-NLS-1$
        if (bracketIndex > -1) {
            importDecl.add(qualifiedClassName.substring(0, bracketIndex));
        } else {
            importDecl.add(qualifiedClassName);
        }
        return this;
    }

    /**
     * Adds an import entry to this code fragment.
     * 
     * @param qualifiedClassName the java class that is added to the import declaration
     */
    public JavaCodeFragment addImport(String qualifiedClassName) {
        importDecl.add(qualifiedClassName);
        return this;
    }

    /**
     * Appends the given fragment to his fragment and indents it properly.
     */
    @Override
    public JavaCodeFragment append(CodeFragment fragment) {
        return append((JavaCodeFragment)fragment);
    }

    /**
     * Appends the given fragment to his fragment and indents it properly.
     */
    public JavaCodeFragment append(JavaCodeFragment fragment) {
        importDecl.add(fragment.getImportDeclaration());
        super.append(fragment);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return prime * result + ((importDecl == null) ? 0 : importDecl.hashCode());
    }

    /**
     * Two fragments are equal if they contain the same source code and have the same import
     * declaration.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        JavaCodeFragment other = (JavaCodeFragment)obj;
        if (!super.equals(obj)) {
            return false;
        }
        return Objects.equals(importDecl, other.importDecl);
    }

    /**
     * Returns the CodeFragment as String in "normal" Java style, that means first all import
     * statements, new line, than the source code.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return importDecl.toString() + System.lineSeparator() + super.toString();
    }
}
