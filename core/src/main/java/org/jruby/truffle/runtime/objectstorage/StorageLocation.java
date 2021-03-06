/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.objectstorage;

/**
 * A storage location that abstracts the method for reading and writing values.
 */
public abstract class StorageLocation {

    private ObjectLayout objectLayout;

    protected StorageLocation(ObjectLayout objectLayout) {
        this.objectLayout = objectLayout;
    }

    public abstract boolean isSet(ObjectStorage object);

    public abstract Object read(ObjectStorage object, boolean condition);

    public abstract void write(ObjectStorage object, Object value) throws GeneralizeStorageLocationException;

    public abstract Class getStoredClass();

    public ObjectLayout getObjectLayout() {
        return objectLayout;
    }

}
