package org.faktorips.codegen;

import java.util.StringTokenizer;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.util.StringUtil;


/**
 * The class represents a Java sourcecode fragment. A sourcecode fragment consists
 * of the sourcecode text and the import statements needed to compile the text.
 * 
 * @see JavaCodeFragmentBuilder
 * @see ImportDeclaration
 * 
 * @author Jan Ortmann
 */
public class JavaCodeFragment {
    
    private final static String INDENT_HELPER = "                                                         "; 

    // buffer holding the sourcecode text
	private StringBuffer sourcecode_;
	
	// import declaration needed to compile the sourcecode
	private ImportDeclaration importDecl_;
	
	// number of blanks used for indentation
	private int indentation = 4;
	
	// the indentation level at the end of the sourcecode
	private int indentLevel = 0;
	
	/**
	 * Creates a new empty JavaCodeFragment.
	 */
	public JavaCodeFragment() {
		this("", new ImportDeclaration());	
	}

	/**
	 * Creates a new JavaCodeFragment with the indicated sourcecode and no 
	 * import statements.
	 */
	public JavaCodeFragment(String sourcecode) {
		this(sourcecode, new ImportDeclaration());	
	}
	
	/**
	 * Creates a new JavaCodeFragment with the indicated sourcecode and import declaration.
	 */
	public JavaCodeFragment(String sourcecode, ImportDeclaration importDecl) {
		this.sourcecode_ = new StringBuffer(sourcecode);
		this.importDecl_ = new ImportDeclaration(importDecl);
	}
	
	/**
	 * Constructs a JavaCodeFragment that has the same sourcecode as the given
	 * fragement with all import statements removed that are obsolete because of 
	 * the indicated package.
	 */
	public JavaCodeFragment(JavaCodeFragment fragment, String packageName) {
		sourcecode_ = fragment.sourcecode_;
		importDecl_ = new ImportDeclaration(fragment.importDecl_, packageName);
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
		return new ImportDeclaration(importDecl_); // defensive copy
	}

	/**
	 * Returns the sourcecode.
	 */
	public String getSourcecode() {
		return sourcecode_.toString();
	}
	
    /**
     * Increases the indentation level used for appending sourcecode.
     */
    public void incIndentationLevel() {
        indentLevel++;
    }

	/**
	 * Decreases the indentation level used for appending sourcecode.
	 * @throws RuntimeException if the level is 0.
	 */
    public void decIndentationLevel() {
        if (indentLevel == 0) {
            throw new RuntimeException("IndentationLevel can't be lesser than 0.");
        }
        indentLevel--;
    }
    
    /**
	 * Returns the current indentation level at the end of the sourcecode.
     */
    public int getIndentationLevel() {
        return indentLevel;
    }
    
    /**
     * Appends '{' to the sourcecode and increases the indentation level.
     */
    public void appendOpenBracket() {
        appendln('{');
        incIndentationLevel();
    }
    
    /**
     * Appends '}' to the sourcecode and dereases the indentation level.
     */
    public void appendCloseBracket() {
        appendln('}');
        decIndentationLevel();
    }
    
	/**
	 * Appends the given String to the sourcecode.
	 */
	public void append(String s) {
	    indentIfBol();
	    sourcecode_.append(s);
	}
	
	/**
	 * Encloses the given String with doublequotes (") and appends it to the sourcecode.
	 */
    public void appendQuoted(String s)
    {
        append("\"" + s + "\"");
    }
    
	/**
	 * Appends the given char to the sourcecode.
	 */
	public void append(char c) {
	    indentIfBol();
	    sourcecode_.append(c);
	}
	
	/**
	 * Transform the given int into a String and appends it to the sourcecode.
	 */
	public void append(int i) {
	    append("" + i);
	}
	
	/**
	 * Transform the given boolean into a String and appends it to the sourcecode.
	 */
	public void append(boolean b) {
	    append("" + b);
	}

	/**
	 * Appends the unqualified class name to the sourcecode and updates the import 
	 * declaration (if neccessary).
	 * 
	 * @throws NullPointerException if clazz is null.
	 */
	public void appendClassName(Class clazz) {
	    appendClassName(clazz.getName());
	}
	
	/**
	 * Appends the unqualified class name to the sourcecode and updates the import 
	 * declaration (if neccessary).
	 * 
	 * @throws NullPointerException if qualifiedClassName is null.
	 */
	public void appendClassName(String qualifiedClassName) {
	    append(StringUtil.unqualifiedName(qualifiedClassName));
	    importDecl_.add(qualifiedClassName);
	}
	
	/**
	 * Appends a line seperator to the sourcecode. 
	 */
	public void appendln() {
	    sourcecode_.append(SystemUtils.LINE_SEPARATOR);
	}
	
	/**
	 * Appends the given String and a line separator to the sourcecode.
	 */
	public void appendln(String s) {
	    indentIfBol();
	    sourcecode_.append(s);
	    sourcecode_.append(SystemUtils.LINE_SEPARATOR);
	}
	
	/**
	 * Appends the given String as is to the sourcecode without indenting it.
	 */
	public void appendlnUnindented(String arg) {
		sourcecode_.append(arg);
		sourcecode_.append(SystemUtils.LINE_SEPARATOR);
	}

	/**
	 * Appends the given char to the sourcecode.
	 */
	public void appendln(char c) {
	    indentIfBol();
	    sourcecode_.append(c);
	    sourcecode_.append(SystemUtils.LINE_SEPARATOR);
	}
	
	/**
	 * Appends the given fragment to his fragment and idents it properly.
	 */
	public void append(JavaCodeFragment fragment) {
		importDecl_.add(fragment.getImportDeclaration());
		String sourcecode = fragment.getSourcecode();
		StringTokenizer tokenizer = new StringTokenizer(sourcecode, SystemUtils.LINE_SEPARATOR);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (tokenizer.hasMoreTokens()) {
				appendln(token);
			} else {
				append(token);
			}
		}
		if (sourcecode.endsWith(SystemUtils.LINE_SEPARATOR)) {
			appendln("");
		}
	}
	
	/**
	 * Two fragments are equal if they contain the same sourcecode and have the same 
	 * import declaration.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof JavaCodeFragment)) {
			return false;
		}
		JavaCodeFragment other = (JavaCodeFragment)o;
		return importDecl_.equals(other.importDecl_)
			 && sourcecode_.length()==other.sourcecode_.length() 
			 && sourcecode_.toString().equals(other.sourcecode_.toString()); 
	}
	
	/**
	 * Returns true if the last character in the sourcecode is a line separator and
	 * so any text appended to the sourcecode goes to a new line (bol = begin of line).
	 */
	public boolean bol() {
	   return sourcecode_.length()==0 
	   	|| sourcecode_.toString().endsWith(SystemUtils.LINE_SEPARATOR); 
	}
	
	/**
	 * Returns the CodeFragment as String in "normal" Java style, that means
	 * first all import statements, new line, than the sourcecode.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return importDecl_.toString() 
			+ SystemUtils.LINE_SEPARATOR
			+ sourcecode_;
	}
	
	/*
	 * Appends the proper indentation if the sourcecode ens with a line separator.
	 */
	private void indentIfBol() {
	    if (bol()) {
	        sourcecode_.append(INDENT_HELPER.substring(0, indentation * indentLevel));
	    }
	}

}
