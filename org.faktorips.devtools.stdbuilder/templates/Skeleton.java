/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.JetJavaContentGenerator;

public class CLASS extends JetJavaContentGenerator {
    
    @Override
    public String generate(IIpsSrcFile ipsSrcFile) throws CoreException;
}
