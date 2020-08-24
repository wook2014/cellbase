/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.core.api.queries;

import org.opencb.commons.datastore.mongodb.MongoDBIterator;

import java.io.Closeable;
import java.util.Iterator;

public class CellBaseIterator<E> implements Iterator<E>, Closeable {

    private MongoDBIterator<E> iterator;
    private long numMatches;

    public CellBaseIterator(MongoDBIterator<E> iterator) {
        this.iterator = iterator;
        this.numMatches = iterator.getNumMatches();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public E next() {
        return iterator.next();
    }

    @Override
    public void close() {
        iterator.close();
    }

    public long getNumMatches() {
        return numMatches;
    }
}