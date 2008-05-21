/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn AG und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 * Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.table;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.JetJavaContentGenerator;

import org.faktorips.devtools.core.model.tablestructure.*;
import org.apache.commons.lang.StringUtils;

public class TableRowGenerator extends JetJavaContentGenerator
{
  protected static String nl;
  public static synchronized TableRowGenerator create(String lineSeparator)
  {
    nl = lineSeparator;
    TableRowGenerator result = new TableRowGenerator();
    nl = null;
    return result;
  }

  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "package ";
  protected final String TEXT_2 = ";";
  protected final String TEXT_3 = NL + " ";
  protected final String TEXT_4 = NL + NL + "/**" + NL + " * ";
  protected final String TEXT_5 = NL + " */ " + NL + "public class ";
  protected final String TEXT_6 = "{" + NL;
  protected final String TEXT_7 = NL + NL + "\tprivate ";
  protected final String TEXT_8 = " ";
  protected final String TEXT_9 = ";            " + NL + "\t\t\t";
  protected final String TEXT_10 = NL + NL + "\t/**" + NL + "\t * ";
  protected final String TEXT_11 = NL + "\t */" + NL + "\tpublic ";
  protected final String TEXT_12 = "(";
  protected final String TEXT_13 = " ";
  protected final String TEXT_14 = "){" + NL + "\t  ";
  protected final String TEXT_15 = NL + "          this.";
  protected final String TEXT_16 = " = ";
  protected final String TEXT_17 = ";   ";
  protected final String TEXT_18 = NL + "\t}" + NL + " ";
  protected final String TEXT_19 = NL + "\t/**" + NL + "\t * ";
  protected final String TEXT_20 = NL + "\t */            " + NL + "\tpublic ";
  protected final String TEXT_21 = " get";
  protected final String TEXT_22 = "(){" + NL + "\t\treturn ";
  protected final String TEXT_23 = ";" + NL + "\t}";
  protected final String TEXT_24 = NL + NL + "\tpublic String toString() {" + NL + "\t\treturn \"\"";
  protected final String TEXT_25 = " + ";
  protected final String TEXT_26 = " + \"|\" + ";
  protected final String TEXT_27 = ";" + NL + "\t}" + NL + "}";

    public String generate(IIpsSrcFile ipsSrcFile) throws CoreException
  {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
    stringBuffer.append(getJavaSourceFileBuilder().getPackage(ipsSrcFile));
    stringBuffer.append(TEXT_2);
    ITableStructure tableStructure = (ITableStructure)ipsSrcFile.getIpsObject();
    IColumn[] columns = tableStructure.getColumns();
    boolean validationStatus = true;
    for (int i = 0; i < columns.length; i++) {
        if(!columns[i].validate(ipsSrcFile.getIpsProject()).isEmpty()){
            validationStatus = false;
            break;
        }
    }

    stringBuffer.append(TEXT_3);
    markImportLocation(stringBuffer);

	String CONSTRUCTOR_JAVADOC = "TABLE_ROW_BUILDER_CONSTRUCTOR_JAVADOC";
	String GET_FIELD_JAVADOC = "TABLE_ROW_BUILDER_GET_FIELD_JAVADOC";
	String CLASS_JAVADOC = "TABLE_ROW_BUILDER_CLASS_JAVADOC";

    stringBuffer.append(TEXT_4);
    stringBuffer.append(getLocalizedText(ipsSrcFile, CLASS_JAVADOC));
    stringBuffer.append(TEXT_5);
    stringBuffer.append(getJavaSourceFileBuilder().getUnqualifiedClassName(ipsSrcFile));
    stringBuffer.append(TEXT_6);
    			for (int i = 0; validationStatus && i < columns.length; i++) {
            String columnName = columns[i].getName();
            String qualifiedDatatypeName = tableStructure.getIpsProject().findDatatype(columns[i].getDatatype()).getJavaClassName();

    stringBuffer.append(TEXT_7);
    stringBuffer.append(appendClassName(qualifiedDatatypeName));
    stringBuffer.append(TEXT_8);
    stringBuffer.append(StringUtils.uncapitalize(columnName));
    stringBuffer.append(TEXT_9);
    }
    stringBuffer.append(TEXT_10);
    stringBuffer.append(getLocalizedText(ipsSrcFile, CONSTRUCTOR_JAVADOC));
    stringBuffer.append(TEXT_11);
    stringBuffer.append(getJavaSourceFileBuilder().getUnqualifiedClassName(ipsSrcFile));
    stringBuffer.append(TEXT_12);
    
    for (int i = 0; validationStatus && i < columns.length; i++) {
	    String columnName = StringUtils.uncapitalize(columns[i].getName());
	    String qualifiedDatatypeName = tableStructure.getIpsProject().findDatatype(columns[i].getDatatype()).getJavaClassName();
	    if(i > 0){
    stringBuffer.append(", ");
    }
    stringBuffer.append(appendClassName(qualifiedDatatypeName));
    stringBuffer.append(TEXT_13);
    stringBuffer.append(columnName);
    
	   }
    stringBuffer.append(TEXT_14);
    
        for (int i = 0; validationStatus && i < columns.length; i++) {
					String columnName = StringUtils.uncapitalize(columns[i].getName());
    stringBuffer.append(TEXT_15);
    stringBuffer.append(columnName);
    stringBuffer.append(TEXT_16);
    stringBuffer.append(columnName);
    stringBuffer.append(TEXT_17);
    }
	  
    stringBuffer.append(TEXT_18);
          for (int i = 0; validationStatus && i < columns.length; i++) {
            String columnName = StringUtils.uncapitalize(columns[i].getName());
            String qualifiedDatatypeName = tableStructure.getIpsProject().findDatatype(columns[i].getDatatype()).getJavaClassName();

    stringBuffer.append(TEXT_19);
    stringBuffer.append(getLocalizedText(ipsSrcFile, GET_FIELD_JAVADOC));
    stringBuffer.append(TEXT_20);
    stringBuffer.append(appendClassName(qualifiedDatatypeName));
    stringBuffer.append(TEXT_21);
    stringBuffer.append(StringUtils.capitalize(columnName));
    stringBuffer.append(TEXT_22);
    stringBuffer.append(columnName);
    stringBuffer.append(TEXT_23);
            }
    stringBuffer.append(TEXT_24);
     
			for (int i = 0; i < columns.length; i++) {
			    if (i == 0) {
			        
    stringBuffer.append(TEXT_25);
    
			    }
				if (i != 0) {
					
    stringBuffer.append(TEXT_26);
    
				}
				
    stringBuffer.append(StringUtils.uncapitalize(columns[i].getName()));
    
			}
    stringBuffer.append(TEXT_27);
    addImports(stringBuffer);
    return stringBuffer.toString();
  }
}
