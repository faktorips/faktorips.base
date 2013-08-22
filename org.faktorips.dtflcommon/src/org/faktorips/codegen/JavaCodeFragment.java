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

package org.faktorips.codegen;

import java.util.Iterator;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.util.StringUtil;

/**
 * The class represents a Java sourcecode fragment. A sourcecode fragment consists of the sourcecode
 * text and the import statements needed to compile the text.
 * 
 * @see JavaCodeFragmentBuilder
 * @see ImportDeclaration
 * 
 * @author Jan Ortmann
 */
public class JavaCodeFragment extends CodeFragment {

    // import declaration needed to compile the sourcecode
    private ImportDeclaration importDecl;

    /**
     * Creates a new empty JavaCodeFragment.
     */
    public JavaCodeFragment() {
        this(false);
    }

    public JavaCodeFragment(boolean indent) {
        super(indent);
        this.importDecl = new ImportDeclaration(importDecl);
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
     * Returns the import declaration needed to compile the sourcecode.
     */
    public ImportDeclaration getImportDeclaration() {
        return new ImportDeclaration(importDecl); // defensive copy
    }

    /**
     * Returns the import declaration needed to compile the sourcecode. The returned import
     * declaration does not contain any import statements that refer to the indicated packageName.
     * The method is useful to avoid unnecessary import statements.
     */
    public ImportDeclaration getImportDeclaration(String packageName) {
        return new ImportDeclaration(importDecl, packageName);
    }

    /**
     * Appends '{' to the sourcecode and increases the indentation level.
     */
    public JavaCodeFragment appendOpenBracket() {
        appendln('{');
        incIndentationLevel();
        return this;
    }

    /**
     * Appends '}' to the sourcecode and dereases the indentation level.
     */
    public JavaCodeFragment appendCloseBracket() {
        appendln('}');
        decIndentationLevel();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment append(String s) {
        return (JavaCodeFragment)super.append(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment appendQuoted(String s) {
        return (JavaCodeFragment)super.appendQuoted(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment append(char c) {
        return (JavaCodeFragment)super.append(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment appendln() {
        return (JavaCodeFragment)super.appendln();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment appendln(String s) {
        return (JavaCodeFragment)super.appendln(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment appendlnUnindented(String arg) {
        return (JavaCodeFragment)super.appendlnUnindented(arg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment appendln(char c) {
        return (JavaCodeFragment)super.appendln(c);
    }

    /**
     * Transform the given int into a String and appends it to the sourcecode.
     */
    public JavaCodeFragment append(int i) {
        append("" + i); //$NON-NLS-1$
        return this;
    }

    /**
     * Transform the given boolean into a String and appends it to the sourcecode.
     */
    public JavaCodeFragment append(boolean b) {
        append("" + b); //$NON-NLS-1$
        return this;
    }

    /**
     * Appends the unqualified class name to the sourcecode and updates the import declaration (if
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
     * Appends the unqualified class name of an public inner class to the sourcecode and updates the
     * import declaration (if necessary).
     * 
     * @throws NullPointerException if clazz is null.
     */
    public JavaCodeFragment appendInnerClassName(Class<?> clazz) {
        appendInnerClassName(clazz.getName());
        return this;
    }

    /**
     * Appends the unqualified class name of an public inner class to the sourcecode and updates the
     * import declaration (if necessary).
     * 
     * @throws NullPointerException if clazz is null.
     */
    public JavaCodeFragment appendInnerClassName(String qualifiedClassName) {
        appendClassName(qualifiedClassName.replaceAll("\\$", "\\.")); //$NON-NLS-1$ //$NON-NLS-2$
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
        if (qualifiedClassName.indexOf('<') > 0) {
            appendClassName(qualifiedClassName.substring(0, qualifiedClassName.indexOf('<')));
            append('<');
            String[] classNames = qualifiedClassName.substring(qualifiedClassName.indexOf('<') + 1,
                    qualifiedClassName.lastIndexOf('>')).split(","); //$NON-NLS-1$
            for (int i = 0; i < classNames.length; i++) {
                String className = classNames[i].trim();
                if (className.indexOf("extends") > className.indexOf('>')) { //$NON-NLS-1$
                    String prefix = className.substring(0, className.indexOf("extends")).trim(); //$NON-NLS-1$
                    append(prefix);
                    append(" extends "); //$NON-NLS-1$
                    className = className.substring(className.indexOf("extends") + 8).trim(); //$NON-NLS-1$
                }
                appendClassName(className);
                if (i < classNames.length - 1) {
                    append(", "); //$NON-NLS-1$
                }
            }
            append('>');
            return this;
        }
        qualifiedClassName = qualifiedClassName.replace('$', '.'); // for inner classes.
        String unqualifiedClassName = StringUtil.unqualifiedName(qualifiedClassName);
        // don't add two imports for the same unqualified name
        for (Iterator<String> iterator = importDecl.iterator(); iterator.hasNext();) {
            String imp = iterator.next();
            if (imp.substring(imp.lastIndexOf('.') + 1).equals(unqualifiedClassName) && !imp.equals(qualifiedClassName)) {
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
        result = prime * result + ((importDecl == null) ? 0 : importDecl.hashCode());
        return result;
    }

    /**
     * Two fragments are equal if they contain the same sourcecode and have the same import
     * declaration.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JavaCodeFragment other = (JavaCodeFragment)obj;
        if (!super.equals(obj)) {
            return false;
        }
        if (importDecl == null) {
            if (other.importDecl != null) {
                return false;
            }
        } else if (!importDecl.equals(other.importDecl)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the CodeFragment as String in "normal" Java style, that means first all import
     * statements, new line, than the sourcecode.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return importDecl.toString() + SystemUtils.LINE_SEPARATOR + super.toString();
    }
}
