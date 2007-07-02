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

package org.faktorips.devtools.core.ui.internal.text;


import java.io.IOException;
import java.io.Reader;


/**
 * NOTE: This class is a copy of the corresponding internal Eclipse class.
 * It is copied as the class' package has changed from Eclipse version 3.2 to 3.3.
 */
public abstract class SingleCharReader extends Reader {

    /**
     * @see Reader#read()
     */
    public abstract int read() throws IOException;

    /**
     * @see Reader#read(char[],int,int)
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        int end= off + len;
        for (int i= off; i < end; i++) {
            int ch= read();
            if (ch == -1) {
                if (i == off)
                    return -1;
                return i - off;
            }
            cbuf[i]= (char)ch;
        }
        return len;
    }

    /**
     * @see Reader#ready()
     */
    public boolean ready() throws IOException {
        return true;
    }

    /**
     * Returns the readable content as string.
     * @return the readable content as string
     * @exception IOException in case reading fails
     */
    public String getString() throws IOException {
        StringBuffer buf= new StringBuffer();
        int ch;
        while ((ch= read()) != -1) {
            buf.append((char)ch);
        }
        return buf.toString();
    }
}
