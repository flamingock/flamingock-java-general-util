/*
 * Copyright 2023 Flamingock (https://www.flamingock.io)
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
package io.flamingock.internal.util.id;

import java.util.Objects;

public abstract class Id<T> {

    protected final T value;

    protected Id(T value) {
        if(value == null) {
            String name = this.getClass().getSimpleName();
            throw new RuntimeException(name + " cannot be null");
        }
        this.value = value;
    }

    @Override
    abstract public String toString();


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Id<?> id = (Id<?>) o;
        return Objects.equals(value, id.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
