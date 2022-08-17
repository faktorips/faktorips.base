/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import org.eclipse.core.runtime.IProgressMonitor;

public class TestPlainJavaMonitor implements IProgressMonitor {

    private String name;
    private String subTaskName;
    private boolean done = false;
    private boolean canceled = false;
    private int totalWork = 0;
    private int work = 0;
    private double internalWork = 0.0;

    public double getInternalWork() {
        return internalWork;
    }

    public int getWork() {
        return work;
    }

    public int getTotalWork() {
        return totalWork;
    }

    public boolean isDone() {
        return done;
    }

    public String getSubTaskName() {
        return subTaskName;
    }

    public String getName() {
        return name;
    }

    @Override
    public void beginTask(String name, int totalWork) {
        this.name = name;
        this.totalWork = totalWork;
    }

    @Override
    public void done() {
        done = true;
    }

    @Override
    public void internalWorked(double work) {
        internalWork = work;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean value) {
        canceled = value;
    }

    @Override
    public void setTaskName(String name) {
        this.name = name;
    }

    @Override
    public void subTask(String name) {
        subTaskName = name;
    }

    @Override
    public void worked(int work) {
        this.work += work;
    }

}
