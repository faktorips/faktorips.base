/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.faktorips.codegen.JavaCodeFragment;

/**
 * A class that represents a text of a validation message. The original text string might contain parameters. A
 * parameter is indicated with <code>[]</code> within the string. A MessageFragment instance is a disassembled 
 * representation of the message string. In contains a JavaCodeFragment for the message. The parameters are
 * part of the code represented by the code fragment. The names, the types and the values of the parameters can be
 * requested from this MessageFragment. 
 *  
 * @author Peter Erzberger
 */
public class MessageFragment{
	
	
	private JavaCodeFragment frag;
	private String[] parameterNames;
	private String[] parameterValues;
	private String msgTextExpression;
	
	/**
	 * @param frag
	 * @param parameterNames
	 * @param parameterValues
	 */
	private MessageFragment(JavaCodeFragment frag, String[] parameterNames, String[] parameterValues, String msgTextExpr) {
		super();
		// TODO Auto-generated constructor stub
		this.frag = frag;
		this.parameterNames = parameterNames;
		this.parameterValues = parameterValues;
		this.msgTextExpression = msgTextExpr;
	}

	/**
	 * Returns the <code>JavaCodeFragment</code> that contains the generated code for the message.
	 */
	public JavaCodeFragment getFrag() {
		return frag;
	}
	
	/**
	 * Returns the generated expression that can be used to access the message string within the generated code. 
	 */
	public String getMsgTextExpression() {
		return msgTextExpression;
	}

	/**
	 * Returns the number of paramters.
	 */
	public int getNumberOfParameters(){
		return parameterNames.length;
	}
	
	/**
	 * Returns the qualified java names for the classes of the parametes.
	 */
	public String[] getParameterClasses(){
		String[] parameterClasses = new String[parameterNames.length];
		for (int i = 0; i < parameterNames.length; i++) {
			parameterClasses[i] = String.class.getName();
		}
		return parameterClasses;
	}
	
	/**
	 * Returns the names of the parameters which are used within the code fragment of the message.
	 */
	public String[] getParameterNames() {
		return parameterNames;
	}

	/**
	 * Returns the values of the parameters which are used within the code fragment of the message.
	 */
	public String[] getParameterValues() {
		return parameterValues;
	}

	/**
	 * Tries to convert the parameter at the indexed position into an integer and returns it. If the
	 * conversion is not successful -1 is returned.
	 * 
	 * @throws ArrayIndexOutOfBoundsException if the provided index is out of the range of the parameter
	 * 		   array hold by this message fragment.
	 */
    public int considerParameterAsIndex(int index){
        try{
            Integer value = Integer.valueOf(parameterValues[index]);
            return value.intValue();
        }
        catch(NumberFormatException e){
            return -1;
        }
    }

    /**
     * Creates a MessageFragment from the provided message string.
     */
	public final static MessageFragment createMessageFragment(String messageText){
		
        Pattern p = Pattern.compile("\\[[^\\]]*\\]");
        Matcher m = p.matcher(messageText);
		JavaCodeFragment frag = new JavaCodeFragment();
		frag.appendClassName(StringBuffer.class);
		frag.append(" text = ");
		frag.append("new ");
		frag.appendClassName(StringBuffer.class);
		frag.appendln("();");
		int beginIndex = 0;
		int numberOfParams = 0;
		ArrayList paraNames = new ArrayList();
		ArrayList paraValues = new ArrayList();
		while (m.find()){
			frag.append("text.append(\"");
			frag.append(messageText.subSequence(beginIndex, m.start()).toString());
			frag.appendln("\");");
			frag.append("text.append(");
			String paraName = "param" + numberOfParams++;
			frag.append(paraName);
			frag.appendln(");");
			paraNames.add(paraName);
			paraValues.add(messageText.subSequence(m.start() + 1, m.end() - 1).toString());
		    beginIndex = m.end();
		}
		if(beginIndex < messageText.length()){
			frag.append("text.append(\"");
			frag.append(messageText.subSequence(beginIndex, messageText.length()).toString());
			frag.appendln("\");");
		}
		return new MessageFragment(frag, (String[])paraNames.toArray(new String[paraNames.size()]), 
				(String[])paraValues.toArray(new String[paraValues.size()]), "text.toString()");
	}
}