/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsproject;

/**
 * This interface identifies the kind of a builder.
 * <p>
 * A builder could be used to generate different kind of output. For example a builder for policy
 * component types could be instantiated to generate interfaces or implementations. Different kind
 * IDs identifies the different builders.
 * <p>
 * This interface is intended to be implemented by an java enum that specifies your different
 * builders.
 * 
 * @author dirmeier
 */
public interface IBuilderKindId {

    public String getId();

}
