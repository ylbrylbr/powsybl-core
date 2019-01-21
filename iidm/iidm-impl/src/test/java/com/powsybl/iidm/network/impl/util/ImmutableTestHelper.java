/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableTestHelper {

    static final Set<String> RXGB_SETTERS = new HashSet<>(Arrays.asList("setR", "setX", "setG", "setB"));

    static final Set<String> NEW_REACTIVE = new HashSet<>(Arrays.asList("newReactiveCapabilityCurve", "newMinMaxReactiveLimits"));

    private static final String EXPECTED_MESSAGE = "Unmodifiable identifiable";

    private static Stream<Method> getMutableMethods(Stream<Method> allMethods, Set<String> names) {
        return allMethods.filter(m -> {
            return isMutableMethods(m) || names.contains(m.getName());
        });
    }

    public static void testInvalidMethods(Object sut, Set<String> expectedInvalidMethods) {
        testInvalidMethods(sut, expectedInvalidMethods, Collections.EMPTY_SET);
    }

    public static void testInvalidMethods(Object sut, Set<String> expectedInvalidMethods, Set<String> nonStandardMutableMethods) {
        Set<String> testedInvalidMethods = new HashSet<>();
        Stream<Method> allMethods = Arrays.asList(sut.getClass().getMethods()).stream();
        Stream<Method> mutableMethods = getMutableMethods(allMethods, nonStandardMutableMethods);
        mutableMethods
                .forEach(m -> {
                    try {
                        testedInvalidMethods.add(m.getName());
                        m.setAccessible(true);
                        Class[] parameterTypes = m.getParameterTypes();
                        if (m.getParameterCount() == 0) {
                            m.invoke(sut);
                        } else {
                            Object[] mocks = new Object[parameterTypes.length];
                            for (int i = 0; i < parameterTypes.length; i++) {
                                String clazz = m.getParameterTypes()[i].getSimpleName();
                                if (clazz.equals("double")) {
                                    mocks[i] = 1.0;
                                } else if (clazz.equals("int")) {
                                    mocks[i] = 1;
                                } else if (clazz.equals("float")) {
                                    mocks[i] = 1.0f;
                                } else if (clazz.equals("boolean")) {
                                    mocks[i] = true;
                                } else {
                                    // implicitly set as null
                                }
                            }
                            m.invoke(sut, mocks);
                        }
                        fail(m.getName() + " should throw exception.");
                    } catch (InvocationTargetException e) {
                        if (!e.getCause().getMessage().equals("deprecated")) {
                            assertEquals(EXPECTED_MESSAGE, e.getCause().getMessage());
                        } else {
                            testedInvalidMethods.remove(m.getName());
                        }
                    } catch (IllegalAccessException e) {
                        fail(m.getName() + " not tested");
                    }
                });
        assertEquals(expectedInvalidMethods, testedInvalidMethods);
    }

    private static boolean isMutableMethods(Method m) {
        String name = m.getName();
        return name.startsWith("set") || name.startsWith("new") || name.equals("remove")
                || name.equals("connect") || name.equals("disconnect");
    }

    private ImmutableTestHelper() {
    }
}
