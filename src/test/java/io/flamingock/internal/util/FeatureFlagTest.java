/*
 * Copyright 2025 Flamingock (https://www.flamingock.io)
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureFlagTest {

    private static final String FEATURE = "my-feature";

    @AfterEach
    void tearDown() {
        FeatureFlag.clear();
    }

    @Test
    void shouldBeDisabledWhenNotRegistered() {
        assertFalse(FeatureFlag.isEnabled(FEATURE));
        assertTrue(FeatureFlag.isDisabled(FEATURE));
    }

    @Test
    void shouldUseProvidedDefaultWhenNotRegistered() {
        assertTrue(FeatureFlag.isEnabled(FEATURE, true));
        assertFalse(FeatureFlag.isDisabled(FEATURE, true));
    }

    @Test
    void shouldIgnoreProvidedDefaultWhenRegistered() {
        FeatureFlag.disable(FEATURE);

        assertFalse(FeatureFlag.isEnabled(FEATURE, true));
        assertTrue(FeatureFlag.isDisabled(FEATURE, true));
    }

    @Test
    void shouldEnableAndDisableFeature() {
        FeatureFlag.enable(FEATURE);
        assertTrue(FeatureFlag.isEnabled(FEATURE));
        assertFalse(FeatureFlag.isDisabled(FEATURE));

        FeatureFlag.disable(FEATURE);
        assertFalse(FeatureFlag.isEnabled(FEATURE));
        assertTrue(FeatureFlag.isDisabled(FEATURE));
    }

    @Test
    void shouldSetFeatureFromBooleanValue() {
        FeatureFlag.set(FEATURE, true);
        assertTrue(FeatureFlag.isEnabled(FEATURE));

        FeatureFlag.set(FEATURE, false);
        assertFalse(FeatureFlag.isEnabled(FEATURE));
    }

    @Test
    void shouldReturnPreviousStateOnMutation() {
        assertFalse(FeatureFlag.enable(FEATURE));
        assertTrue(FeatureFlag.enable(FEATURE));
        assertTrue(FeatureFlag.disable(FEATURE));
        assertFalse(FeatureFlag.set(FEATURE, true));
        assertTrue(FeatureFlag.remove(FEATURE));
        assertFalse(FeatureFlag.remove(FEATURE));
    }

    @Test
    void shouldGoBackToDefaultWhenRemoved() {
        FeatureFlag.enable(FEATURE);
        FeatureFlag.remove(FEATURE);

        assertFalse(FeatureFlag.isEnabled(FEATURE));
        assertTrue(FeatureFlag.isEnabled(FEATURE, true));
    }

    @Test
    void shouldClearEveryFeature() {
        FeatureFlag.enable(FEATURE);
        FeatureFlag.enable("another-feature");

        FeatureFlag.clear();

        assertFalse(FeatureFlag.isEnabled(FEATURE));
        assertFalse(FeatureFlag.isEnabled("another-feature"));
    }

    @Test
    void shouldNotMixFeatures() {
        FeatureFlag.enable(FEATURE);

        assertTrue(FeatureFlag.isEnabled(FEATURE));
        assertFalse(FeatureFlag.isEnabled("another-feature"));
    }

    @Test
    void shouldRunActionOnlyWhenEnabled() {
        AtomicInteger executions = new AtomicInteger(0);

        FeatureFlag.ifEnabled(FEATURE, executions::incrementAndGet);
        assertEquals(0, executions.get());

        FeatureFlag.enable(FEATURE);
        FeatureFlag.ifEnabled(FEATURE, executions::incrementAndGet);
        assertEquals(1, executions.get());
    }

    @Test
    void shouldRunActionOnlyWhenDisabled() {
        AtomicInteger executions = new AtomicInteger(0);

        FeatureFlag.ifDisabled(FEATURE, executions::incrementAndGet);
        assertEquals(1, executions.get());

        FeatureFlag.enable(FEATURE);
        FeatureFlag.ifDisabled(FEATURE, executions::incrementAndGet);
        assertEquals(1, executions.get());
    }

    @Test
    void shouldRunFallbackActionWhenDisabled() {
        AtomicInteger actionExecutions = new AtomicInteger(0);
        AtomicInteger fallbackExecutions = new AtomicInteger(0);

        FeatureFlag.ifEnabledOrElse(FEATURE, actionExecutions::incrementAndGet, fallbackExecutions::incrementAndGet);
        assertEquals(0, actionExecutions.get());
        assertEquals(1, fallbackExecutions.get());

        FeatureFlag.enable(FEATURE);
        FeatureFlag.ifEnabledOrElse(FEATURE, actionExecutions::incrementAndGet, fallbackExecutions::incrementAndGet);
        assertEquals(1, actionExecutions.get());
        assertEquals(1, fallbackExecutions.get());
    }

    @Test
    void shouldReturnFallbackValueWhenDisabled() {
        assertEquals("fallback", FeatureFlag.getIfEnabled(FEATURE, () -> "supplied", "fallback"));

        FeatureFlag.enable(FEATURE);
        assertEquals("supplied", FeatureFlag.getIfEnabled(FEATURE, () -> "supplied", "fallback"));
    }

    @Test
    void shouldNotInvokeSupplierWhenDisabled() {
        AtomicInteger invocations = new AtomicInteger(0);

        FeatureFlag.getIfEnabled(FEATURE, invocations::incrementAndGet, -1);

        assertEquals(0, invocations.get());
    }

    @Test
    void shouldRejectInvalidFeatureName() {
        assertThrows(IllegalArgumentException.class, () -> FeatureFlag.isEnabled(null));
        assertThrows(IllegalArgumentException.class, () -> FeatureFlag.isEnabled(""));
        assertThrows(IllegalArgumentException.class, () -> FeatureFlag.enable(null));
        assertThrows(IllegalArgumentException.class, () -> FeatureFlag.enable(""));
    }

    @Test
    void shouldRejectNullAction() {
        FeatureFlag.enable(FEATURE);

        assertThrows(NullPointerException.class, () -> FeatureFlag.ifEnabled(FEATURE, null));
        assertThrows(NullPointerException.class, () -> FeatureFlag.ifDisabled(FEATURE, null));
        assertThrows(NullPointerException.class, () -> FeatureFlag.ifEnabledOrElse(FEATURE, null, () -> {}));
        assertThrows(NullPointerException.class, () -> FeatureFlag.getIfEnabled(FEATURE, null, "fallback"));
    }
}
