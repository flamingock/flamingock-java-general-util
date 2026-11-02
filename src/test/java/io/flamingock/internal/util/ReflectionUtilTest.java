/*
 * Copyright 2026 Flamingock (https://www.flamingock.io)
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
package io.flamingock.internal.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReflectionUtilTest {

    @Test
    void shouldInvokeInheritedMethodThroughMultipleLevels() throws Exception {
        Method method = ReflectionUtil.getMethodFromParameterTypeNames(
                PlainLeaf.class, "apply", Collections.singletonList("java.lang.String"), true);

        assertEquals(Base.class, method.getDeclaringClass());
        assertEquals("base:value", method.invoke(new PlainLeaf(), "value"));
    }

    @Test
    void shouldUseNearestClassNameHeuristicBeforeSearchingAncestors() throws Exception {
        for (Class<?> source : Arrays.asList(Overloaded.class, Leaf.class)) {
            Method method = ReflectionUtil.getMethodFromParameterTypeNames(
                    source, "apply", Collections.singletonList("java.lang.String"), true);

            assertEquals(Overloaded.class, method.getDeclaringClass());
            assertEquals("integer:7", method.invoke(source.newInstance(), 7));
        }
    }

    @Test
    void shouldPreferNearestMatchingDeclaration() throws Exception {
        Method method = ReflectionUtil.getMethodFromParameterTypeNames(
                Override.class, "apply", Collections.singletonList("java.lang.String"), true);

        assertEquals(Override.class, method.getDeclaringClass());
        assertEquals("override:value", method.invoke(new Override(), "value"));
    }

    @Test
    void shouldInvokeDirectOverloadMatchingRequestedSignature() throws Exception {
        Method method = ReflectionUtil.getMethodFromParameterTypeNames(
                Leaf.class, "apply", Collections.singletonList("java.lang.Integer"), true);

        assertEquals(Overloaded.class, method.getDeclaringClass());
        assertEquals("integer:7", method.invoke(new Leaf(), 7));
    }

    @Test
    void shouldMatchPrimitiveArrayAndNestedTypeNames() throws Exception {
        for (List<String> names : Arrays.asList(
                Arrays.asList("int", "int[]", "java.lang.String[]", Value.class.getCanonicalName()),
                Arrays.asList("int", "[I", "[Ljava.lang.String;", Value.class.getName()))) {
            Method method = ReflectionUtil.getMethodFromParameterTypeNames(Leaf.class, "arrays", names, true);

            assertEquals(Base.class, method.getDeclaringClass());
            assertEquals("value:7:8", method.invoke(new Leaf(), 7, new int[]{8},
                    new String[]{"value"}, new Value()));
        }
    }

    @Test
    void shouldPreserveDeclaredOnlyNameFallback() throws Exception {
        Method method = ReflectionUtil.getMethodFromParameterTypeNames(
                Overloaded.class, "apply", Collections.singletonList("java.lang.String"), false);

        assertEquals(Overloaded.class, method.getDeclaringClass());
        assertEquals("integer:7", method.invoke(new Overloaded(), 7));
    }

    @Test
    void shouldUseDeclaredFallbackWhenSignatureDoesNotMatch() throws Exception {
        Method method = ReflectionUtil.getMethodFromParameterTypeNames(
                Unique.class, "apply", Collections.emptyList(), true);

        assertEquals("unique:value", method.invoke(new Unique(), "value"));
    }

    @Test
    void shouldReportMissingMethodWithDeclaredLookupDiagnostic() {
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> ReflectionUtil.getMethodFromParameterTypeNames(
                        Leaf.class, "missing", Collections.emptyList(), true));

        assertEquals("No declared method named 'missing' found in " + Leaf.class.getName(), error.getMessage());
    }

    @Test
    void shouldResolveDeclaredOverloadsByArityAndExactTypes() throws Exception {
        Method byArity = ReflectionUtil.getMethodFromParameterTypeNames(
                DeclaredOverloads.class, "apply", Collections.emptyList(), false);
        Method byTypes = ReflectionUtil.getMethodFromParameterTypeNames(
                DeclaredOverloads.class, "apply", Collections.singletonList("java.lang.String"), false);

        assertEquals("empty", byArity.invoke(new DeclaredOverloads()));
        assertEquals("string:value", byTypes.invoke(new DeclaredOverloads(), "value"));
    }

    @Test
    void shouldReportMissingDeclaredMethodWithoutSearchingAncestors() {
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> ReflectionUtil.getMethodFromParameterTypeNames(
                        Leaf.class, "apply", Collections.singletonList("java.lang.String"), false));

        assertEquals("No declared method named 'apply' found in " + Leaf.class.getName(), error.getMessage());
    }

    @Test
    void shouldSearchAncestorsWhenDeclaredOverloadsCannotResolve() throws Exception {
        for (List<String> names : Arrays.asList(Collections.<String>emptyList(),
                Collections.singletonList("java.lang.String"))) {
            Method method = ReflectionUtil.getMethodFromParameterTypeNames(
                    UnmatchedOverloads.class, "apply", names, true);

            assertEquals(Base.class, method.getDeclaringClass());
            assertEquals("base:value", method.invoke(new UnmatchedOverloads(), "value"));
        }
    }

    @Test
    void shouldPropagateInvalidParameterTypeInsteadOfSearchingAncestors() {
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> ReflectionUtil.getMethodFromParameterTypeNames(
                        UnmatchedOverloads.class, "apply", Collections.singletonList("missing.Type"), true));

        assertEquals("Class not found for parameter type name: missing.Type", error.getMessage());
    }

    public static class UnmatchedOverloads extends Base {
        public String apply(Integer value) { return "integer:" + value; }
        public String apply(Long value) { return "long:" + value; }
    }

    public static class PlainIntermediate extends Base { }

    public static class PlainLeaf extends PlainIntermediate { }

    public static class DeclaredOverloads {
        public String apply() { return "empty"; }
        public String apply(String value) { return "string:" + value; }
        public String apply(Integer value) { return "integer:" + value; }
    }

    public static class Value { }

    public static class Base {
        public String apply(String value) {
            return "base:" + value;
        }

        public String arrays(int value, int[] numbers, String[] text, Value nested) {
            return text[0] + ":" + value + ":" + numbers[0];
        }
    }

    public static class Overloaded extends Base {
        public String apply(Integer value) {
            return "integer:" + value;
        }
    }

    public static class Leaf extends Overloaded { }

    public static class Override extends Base {
        @java.lang.Override
        public String apply(String value) {
            return "override:" + value;
        }
    }

    public static class Unique {
        public String apply(String value) {
            return "unique:" + value;
        }
    }
}
