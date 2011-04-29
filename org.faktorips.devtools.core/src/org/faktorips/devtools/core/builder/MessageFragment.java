/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.faktorips.codegen.JavaCodeFragment;

/**
 * A class that represents a text of a validation message. The original text string might contain
 * parameters. A parameter is indicated with <code>{}</code> within the string. A MessageFragment
 * instance is a disassembled representation of the message string. It contains a JavaCodeFragment
 * for the message. The parameters are part of the code represented by the code fragment. The names,
 * the types and the values of the parameters can be requested from this MessageFragment.
 * 
 * @author Peter Erzberger
 */
public class MessageFragment {

    /**
     * A parameter name type constant used for the creation of a MessageFragment to indicate that
     * default parameter names are used within the JavaCodeFragment to represent the parameters. A
     * default parameter name consists of the parts <i>param</i> followed by an integer number e.g.
     * param0
     */
    public final static int DEFAULT_PARAMETER_NAMES = 0;

    /**
     * A parameter name type constant used for the creation of a MessageFragment to indicate that
     * the parameter values are used within the JavaCodeFragment to represent the parameters.
     */
    public final static int VALUES_AS_PARAMETER_NAMES = 1;

    private JavaCodeFragment frag;
    private String[] parameterNames;
    private String[] parameterValues;
    private String msgTextExpression;

    private MessageFragment(JavaCodeFragment frag, String[] parameterNames, String[] parameterValues, String msgTextExpr) {
        super();
        this.frag = frag;
        this.parameterNames = parameterNames;
        this.parameterValues = parameterValues;
        msgTextExpression = msgTextExpr;
    }

    /**
     * Returns the <code>JavaCodeFragment</code> that contains the generated code for the message.
     */
    public JavaCodeFragment getFrag() {
        return frag;
    }

    /**
     * Returns the generated expression that can be used to access the message string within the
     * generated code.
     */
    public String getMsgTextExpression() {
        return msgTextExpression;
    }

    /**
     * Returns the number of parameters.
     */
    public int getNumberOfParameters() {
        return parameterNames.length;
    }

    /**
     * Returns true if this message fragment contains any parameters.
     */
    public boolean hasParameters() {
        return parameterNames.length > 0;
    }

    /**
     * Returns the qualified java names for the classes of the parameters.
     */
    public String[] getParameterClasses() {
        String[] parameterClasses = new String[parameterNames.length];
        for (int i = 0; i < parameterNames.length; i++) {
            parameterClasses[i] = Object.class.getName();
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
     * Tries to convert the parameter value at the indexed position into an integer and returns it.
     * If the conversion is not successful -1 is returned.
     * 
     * @throws ArrayIndexOutOfBoundsException if the provided index is out of the range of the
     *             parameter array hold by this message fragment.
     */
    public int considerParameterAsIndex(int index) {
        return considerParameterAsIndex(parameterValues[index]);
    }

    private static int considerParameterAsIndex(String parameter) {
        try {
            int value = Integer.valueOf(parameter).intValue();
            if (value < 0) {
                return -1;
            }
            return value;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Creates a MessageFragment from the provided message string with default parameter names.
     */
    public final static MessageFragment createMessageFragment(String messageText) {
        return createMessageFragment(messageText, DEFAULT_PARAMETER_NAMES);
    }

    /**
     * Creates a MessageFragment from the provided message string.
     * 
     * @param messageText the original message text containing parameters marked by braces
     * @param parameterNameType the type of parameter name contained in the JavaCodeFragment for the
     *            parameters extracted in the original message. Two kinds of parameter names are
     *            available expressed by the two available constants.
     * @throws IllegalArgumentException if the value of the parameter parameterNameType is different
     *             from one of the constant values.
     * 
     * @see #DEFAULT_PARAMETER_NAMES
     * @see #VALUES_AS_PARAMETER_NAMES
     */
    public final static MessageFragment createMessageFragment(String messageText, int parameterNameType) {
        if (parameterNameType != DEFAULT_PARAMETER_NAMES && parameterNameType != VALUES_AS_PARAMETER_NAMES) {
            throw new IllegalArgumentException(
                    "The value of the parameter parameterNameType must be one of the constant values" + //$NON-NLS-1$
                            "DEFAULT_PARAMETER_NAMES, VALUES_AS_PARAMETER_NAMES"); //$NON-NLS-1$
        }

        messageText = StringEscapeUtils.escapeJava(messageText);
        Pattern p = Pattern.compile("\\{[^\\}]*\\}"); //$NON-NLS-1$
        Matcher m = p.matcher(messageText);
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendClassName(StringBuffer.class);
        frag.append(" text = "); //$NON-NLS-1$
        frag.append("new "); //$NON-NLS-1$
        frag.appendClassName(StringBuffer.class);
        frag.appendln("();"); //$NON-NLS-1$
        int beginIndex = 0;
        int numberOfParams = 0;
        List<String> paraNames = new ArrayList<String>();
        List<String> paraValues = new ArrayList<String>();
        while (m.find()) {
            frag.append("text.append(\""); //$NON-NLS-1$
            frag.append(messageText.subSequence(beginIndex, m.start()).toString());
            frag.appendln("\");"); //$NON-NLS-1$
            frag.append("text.append("); //$NON-NLS-1$
            String pValue = messageText.subSequence(m.start() + 1, m.end() - 1).toString();
            paraValues.add(pValue);
            beginIndex = m.end();
            String paraName = null;
            if (parameterNameType == VALUES_AS_PARAMETER_NAMES) {
                int parameterAsIndex = considerParameterAsIndex(pValue);
                if (parameterAsIndex != -1) {
                    paraName = "p" + String.valueOf(parameterAsIndex); //$NON-NLS-1$
                } else {
                    paraName = pValue;
                }
            } else if (parameterNameType == DEFAULT_PARAMETER_NAMES) {
                paraName = "param" + numberOfParams++; //$NON-NLS-1$    
            }
            paraNames.add(paraName);
            frag.append(paraName);
            frag.appendln(");"); //$NON-NLS-1$
        }
        if (beginIndex < messageText.length()) {
            frag.append("text.append(\""); //$NON-NLS-1$
            frag.append(messageText.subSequence(beginIndex, messageText.length()).toString());
            frag.appendln("\");"); //$NON-NLS-1$
        }
        return new MessageFragment(frag, paraNames.toArray(new String[paraNames.size()]),
                paraValues.toArray(new String[paraValues.size()]), "text.toString()"); //$NON-NLS-1$
    }

}
