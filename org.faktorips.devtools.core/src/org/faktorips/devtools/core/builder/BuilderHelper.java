/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.pctype.Parameter;

/**
 * A collection of static helper methods.
 * 
 * @author Jan Ortmann
 */
public class BuilderHelper {
    
    public final static String[] extractParameterNames(Parameter[] params) {
	    String[] paramNames = new String[params.length];
	    for (int i=0; i<params.length; i++) {
	        paramNames[i] = params[i].getName();
	    }
        return paramNames;
    }
    
    public final static String[] extractMessageParameters(String message) {
    	ArrayList al = new ArrayList();
		while(true) {
			int start = message.indexOf('{');
			if(start > -1) {
				int end = message.indexOf('}', start + 2); // param darf kein Leerstring sein
				if(end > -1) {
					String param = message.substring(start + 1, end);
					param = param.trim();
					param = param.replace(' ', '_');	
					param = StringUtils.uncapitalise(param); 
					al.add(param);
					message = message.substring(end + 1); 
				}
				else {
					break;
				}
			}
			else {
				break;
			}
		}
		return (String[]) al.toArray(new String[al.size()]);    	
    }
    
    public final static String transformMessage(String message) {
    	int count = 0;
    	String transformedMessage = ""; //$NON-NLS-1$
		while(true) {
			int start = message.indexOf('{');
			if(start > -1) {
				int end = message.indexOf('}', start + 2); // param darf kein Leerstring sein
				if(end > -1) {
					transformedMessage += message.substring(0,start);
					transformedMessage += "{"; //$NON-NLS-1$
					transformedMessage += count;
					transformedMessage += "}"; //$NON-NLS-1$
					message = message.substring(end + 1); 
					count++;
				}
				else {
					transformedMessage += message;
					break;
				}
			}
			else {
				transformedMessage += message;
				break;
			}
		}
		return transformedMessage;    	
    }    

    private BuilderHelper() {
        super();
    }
}
