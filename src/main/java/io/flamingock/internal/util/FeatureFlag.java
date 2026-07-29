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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Simple, internal, in-memory feature flag registry.
 *
 * <p>Flags are identified by name and are disabled unless explicitly enabled. There is no external
 * configuration source: flags are only turned on and off from code, so they cannot be overridden by
 * the user of the library.
 *
 * <p>Flags can be queried, to be used in a regular condition:
 *
 * <pre>{@code
 * FeatureFlag.enable("my-feature");
 *
 * if (FeatureFlag.isEnabled("my-feature")) {
 *     ...
 * }
 * }</pre>
 *
 * <p>or used to guard an action directly:
 *
 * <pre>{@code
 * FeatureFlag.ifEnabled("my-feature", () -> service.doNewThing());
 * FeatureFlag.ifEnabledOrElse("my-feature", () -> newWay(), () -> oldWay());
 *
 * String value = FeatureFlag.getIfEnabled("my-feature", () -> newValue(), "fallback");
 * }</pre>
 *
 * <p>The state is global and shared by the whole JVM. This class is thread-safe.
 */
public final class FeatureFlag {

    private static final Map<String, Boolean> FLAGS = new ConcurrentHashMap<>();

    private FeatureFlag() {
    }

    /**
     * Enables the given feature.
     *
     * @param feature feature name
     * @return whether the feature was enabled before this call
     */
    public static boolean enable(String feature) {
        return set(feature, true);
    }

    /**
     * Disables the given feature.
     *
     * @param feature feature name
     * @return whether the feature was enabled before this call
     */
    public static boolean disable(String feature) {
        return set(feature, false);
    }

    /**
     * Sets the state of the given feature.
     *
     * @param feature feature name
     * @param enabled new state
     * @return whether the feature was enabled before this call
     */
    public static boolean set(String feature, boolean enabled) {
        return isEnabled(FLAGS.put(validateFeature(feature), enabled));
    }

    /**
     * Removes the given feature from the registry, so it goes back to its default state(disabled).
     *
     * @param feature feature name
     * @return whether the feature was enabled before this call
     */
    public static boolean remove(String feature) {
        return isEnabled(FLAGS.remove(validateFeature(feature)));
    }

    /**
     * Removes every feature from the registry. Mainly intended for tests.
     */
    public static void clear() {
        FLAGS.clear();
    }

    /**
     * Returns whether the given feature is enabled. Unknown features are considered disabled.
     *
     * @param feature feature name
     * @return true if the feature is enabled
     */
    public static boolean isEnabled(String feature) {
        return isEnabled(feature, false);
    }

    /**
     * Returns whether the given feature is enabled, providing the value to be used when the feature
     * hasn't been explicitly set.
     *
     * @param feature      feature name
     * @param defaultValue value returned when the feature is not registered
     * @return true if the feature is enabled
     */
    public static boolean isEnabled(String feature, boolean defaultValue) {
        Boolean state = FLAGS.get(validateFeature(feature));
        return state != null ? state : defaultValue;
    }

    /**
     * Returns whether the given feature is disabled. Unknown features are considered disabled.
     *
     * @param feature feature name
     * @return true if the feature is disabled
     */
    public static boolean isDisabled(String feature) {
        return !isEnabled(feature);
    }

    /**
     * Returns whether the given feature is disabled, providing the value to be used when the feature
     * hasn't been explicitly set.
     *
     * @param feature      feature name
     * @param defaultValue state assumed when the feature is not registered
     * @return true if the feature is disabled
     */
    public static boolean isDisabled(String feature, boolean defaultValue) {
        return !isEnabled(feature, defaultValue);
    }

    /**
     * Runs the given action only if the feature is enabled.
     *
     * @param feature feature name
     * @param action  action to run when the feature is enabled
     */
    public static void ifEnabled(String feature, Runnable action) {
        Objects.requireNonNull(action, "action must not be null");
        if (isEnabled(feature)) {
            action.run();
        }
    }

    /**
     * Runs the given action only if the feature is disabled.
     *
     * @param feature feature name
     * @param action  action to run when the feature is disabled
     */
    public static void ifDisabled(String feature, Runnable action) {
        Objects.requireNonNull(action, "action must not be null");
        if (isDisabled(feature)) {
            action.run();
        }
    }

    /**
     * Runs one action or the other, depending on the state of the feature.
     *
     * @param feature       feature name
     * @param action        action to run when the feature is enabled
     * @param fallbackAction action to run when the feature is disabled
     */
    public static void ifEnabledOrElse(String feature, Runnable action, Runnable fallbackAction) {
        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(fallbackAction, "fallbackAction must not be null");
        if (isEnabled(feature)) {
            action.run();
        } else {
            fallbackAction.run();
        }
    }

    /**
     * Returns the value provided by the supplier if the feature is enabled, or the fallback value
     * otherwise. The supplier is only invoked when the feature is enabled.
     *
     * @param feature  feature name
     * @param supplier supplier invoked when the feature is enabled
     * @param fallback value returned when the feature is disabled
     * @param <T>      returned type
     * @return the supplied value or the fallback
     */
    public static <T> T getIfEnabled(String feature, Supplier<T> supplier, T fallback) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return isEnabled(feature) ? supplier.get() : fallback;
    }

    private static boolean isEnabled(Boolean state) {
        return state != null && state;
    }

    private static String validateFeature(String feature) {
        if (StringUtil.isEmpty(feature)) {
            throw new IllegalArgumentException("feature must not be null or empty");
        }
        return feature;
    }
}
