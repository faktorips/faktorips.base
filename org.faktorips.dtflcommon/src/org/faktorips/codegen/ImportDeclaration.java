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

package org.faktorips.codegen;

import java.util.*;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.util.StringUtil;


/**
 * An ImportDeclaration is an ordered set of import statements.
 * <p>
 * When adding new import statements it is checked that no unnecessary
 * statements are added, for example if you add <code>java.util.ArrayList</code>
 * and then <code>java.util.*</code> only the latter statement is kept.
 * Also import statements for classes residing in <code>java.lang</code> and
 * for primitive types are ignored.  
 * 
 * @author Jan Ortmann
 */
public class ImportDeclaration {

    /**
     * Returns true if this is a package import, e.g. <code>java.util.*</code>
     * Returns false if importSpec is null.
     */
    public final static boolean isPackageImport(String importSpec) {
    	if (importSpec == null) {
    		return false;
    	}
        return "*".equals(importSpec.substring(importSpec.length()-1));
    }
    
    private final static String JAVA_LANG_ASTERIX = "java.lang.*";

    // List that holds the class imports 
    private List<String> classes;

    // Set that holds the package imports (e.g. java.util.*) 
    private List<String> packages;

	/**
	 * Creates a new import declaration.
	 */
	public ImportDeclaration() {
        classes = new ArrayList<String>();
        packages = new ArrayList<String>();
	}
	
	/**
	 * Copy constructor.
	 */
	public ImportDeclaration(ImportDeclaration decl) {
		this();
		add(decl);
	}

	/**
	 * Constructs a new import declaration that contains all import statements
	 * from the given declaration that are not covered by the package.
	 */
	public ImportDeclaration(ImportDeclaration decl, String packageName) {
		this();
		String packageImport = packageName + ".*";
		ImportDeclaration temp = new ImportDeclaration(decl);
		temp.add(packageImport);
		for (Iterator<String> it=temp.iterator(); it.hasNext();) {
			String importSpec = (String)it.next();
			if (!importSpec.equals(packageImport)) {
				add(importSpec);
			}
		}
	}

    /**
     * Adds the class to the import list.
     */
    public void add(Class<?> clazz) {
        add(clazz.getName());
    }
    
	/**
	 * Adds all imports in the given import declaration to this declaration.
	 * Does nothing if the given import decl is null.
	 */
	public void add(ImportDeclaration decl) {
		if (decl == null) {
			return;
		}
		for (Iterator<String> it=decl.iterator(); it.hasNext();) {
			add((String)it.next());
		}
	}
    
    /**
     * Adds the import specifications to the list of imports.
	 * Does nothing if the given importSpecs is null.
     */
    public void add(String[] importSpecs) {
        if (importSpecs==null) {
            return;
        }
        for (int i=0; i<importSpecs.length; i++) {
            add(importSpecs[i]);
        }
    }

    /**
     * Adds the import specification to the list of imports.
	 * Does nothing if the given importSpecs is null.
     */
    public void add(String importSpec)
    {
        if (importSpec == null || isCovered(importSpec)) {
            return;
        }
        if (isPackageImport(importSpec)) {
            removeClassImports(importSpec);
            packages.add(importSpec);
        } else {
            classes.add(importSpec);
        }
    }
    
    /**
     * Removes the class imports that are covered by the package import.
     */
    private void removeClassImports(String packageImport) {
        for (Iterator<String> it=classes.iterator(); it.hasNext();) {
            String classImport = (String)it.next();
            if (classImportCoveredByPackageImport(classImport, packageImport)) {
                   it.remove();
            }
        }
    }
    
    /**
     * Returns true if the class is covered by the import declaration. That is, if
     * either an import for that class exists or the package the class resides in
     * is imported.
     * 
     * @throws NullPointerException if clazz is null.
     */
    public boolean isCovered(Class<?> clazz) {
        return isCovered(clazz.getName());
    }

    /**
     * Returns true if the import specification is covered by thisimport declaration.
     * 
     * @throws NullPointerException if importSpec is null.
     */
    public boolean isCovered(String importSpec)
    {
    	if (importSpec == null) {
    		throw new NullPointerException();
    	}
        if (importSpec.equals(Boolean.TYPE.getName())
            || importSpec.equals(Integer.TYPE.getName())
            || importSpec.equals(Double.TYPE.getName())
            || importSpec.equals(Long.TYPE.getName())
            || importSpec.equals("void")) {
            return true; // this is a primitive type
        }
        if (isPackageImport(importSpec)) {
            if (JAVA_LANG_ASTERIX.equals(importSpec)) {
                return true;
            }
            return packages.contains(importSpec);
        }
        if (classes.contains(importSpec)) {
            return true;
        }
        return isCovered(StringUtil.getPackageName(importSpec)+".*");
    }
    
    /**
     * Returns true if the import specification is already covered by an
     * existing import specification.
     */
    private boolean classImportCoveredByPackageImport(
        String classImport,
        String packageImport) {
        return packageImport.equals(StringUtil.getPackageName(classImport) + ".*");
    }
    
    /**
     * Returns an Iterator over the import statements as Strings.
     */
    public Iterator<String> iterator()
    {
    	List<String> allImports = new ArrayList<String>();
        allImports.addAll(packages);
		allImports.addAll(classes);
        return allImports.iterator();
    }
    
    /**
     * Returns the number of imports.
     */
    public int getNoOfImports() {
        return classes.size() + packages.size();
    }
    
    /**
     * Returns those imports in the <code>importsToTest>/code> declaration that are not
     * covered this one. Returns an empty import declaration if either all imports are covered
     * or importsToTest is <code>null</code>.
     */
    public ImportDeclaration getUncoveredImports(ImportDeclaration importsToTest) {
        ImportDeclaration uncovered = new ImportDeclaration();
        if (importsToTest==null) {
            return uncovered;
        }
        for (Iterator<String> it=importsToTest.iterator(); it.hasNext();) {
            String importToTest = (String)it.next();
            if (!isCovered(importToTest)) {
                uncovered.add(importToTest);
            }
        }
        return uncovered;
    }
    
    /**
     * Returns true if the indicated objects is an import specification with
     * the same import specifications in the same order.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
    	if (!(o instanceof ImportDeclaration)) {
    		return false;
    	}
    	ImportDeclaration other = (ImportDeclaration)o;
    	return classes.equals(other.classes) && packages.equals(other.packages);
    }
    
	/**
	 * Returns the import statements as a string. The import statements are
	 * separated by a line separator. Each line has a trailing "import "
	 * and ends with a semicolon (;).
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		String separator = SystemUtils.LINE_SEPARATOR;
		for (Iterator<String> it=iterator(); it.hasNext();)
		{
			sb.append(("import "));
			sb.append(it.next());
			sb.append(";");
			sb.append(separator);
		}
		return sb.toString();
	}
}
