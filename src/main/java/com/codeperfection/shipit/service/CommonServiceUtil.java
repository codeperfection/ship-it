package com.codeperfection.shipit.service;

import java.util.Objects;
import java.util.function.Consumer;

public class CommonServiceUtil {

    static <T> boolean applyChangeIfNeeded(T existingValue, T updateValue, Consumer<T> setter) {
        if (updateValue == null || Objects.equals(existingValue, updateValue)) {
            return false;
        }

        setter.accept(updateValue);
        return true;
    }
}
