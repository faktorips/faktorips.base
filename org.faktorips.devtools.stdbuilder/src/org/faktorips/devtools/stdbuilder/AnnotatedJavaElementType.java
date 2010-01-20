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

package org.faktorips.devtools.stdbuilder;

/**
 * Java element types that can be annotated.
 * 
 * @author Roman Grutza
 */
public enum AnnotatedJavaElementType {

    POLICY_CMPT_IMPL_CLASS,

    POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD,

    POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_GETTER,

    POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_SETTER,

    POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD;
}
