/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.commands;

 import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;

 /**
  * A handler to set the template value status of property values to
  * {@link TemplateValueStatus#DEFINED}.
  */
 public class SetDefinedTemplateValueStatusHandler extends SetTemplateValueStatusHandler {

     public SetDefinedTemplateValueStatusHandler() {
         super(TemplateValueStatus.DEFINED);
     }
 }