/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bckfnn.reactstreams;

/**
 * A simple two value tuple.
 *
 * @param <T1> type of the first value.
 * @param <T2> type of the second value.
 */
public class Tuple<T1, T2> {
    private T1 value1;
    private T2 value2;

    /**
     * Construct a tuple with two values.
     * @param value1 the first value.
     * @param value2 the second value.
     */
    public Tuple(T1 value1, T2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    /**
     * @return the first value.
     */
    public T1 getValue1() {
        return value1;
    }

    /**
     * @return the second value.
     */
    public T2 getValue2() {
        return value2;
    }

    /**
     * @return the first value.
     */
    public T1 left() {
        return getValue1();
    }

    /**
     * @return the second value.
     */
    public T2 right() {
        return getValue2();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value1 == null) ? 0 : value1.hashCode());
        result = prime * result + ((value2 == null) ? 0 : value2.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tuple<?, ?> other = (Tuple<?, ?>) obj;
        if (value1 == null) {
            if (other.value1 != null)
                return false;
        } else if (!value1.equals(other.value1))
            return false;
        if (value2 == null) {
            if (other.value2 != null)
                return false;
        } else if (!value2.equals(other.value2))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Tuple[" + value1 + ", " + value2 + "]";
    }
}
