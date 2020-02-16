package com.codeperfection.shipit.service;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CommonServiceUtilTest {

    @SuppressWarnings("unchecked")
    @Test
    public void applyChangeIfNeededOnNullUpdateValueDoesNotApplyAndReturnsFalse() {
        final Integer existingValue = 5;
        final Integer updateValue = null;
        final Consumer<Integer> setter = mock(Consumer.class);

        assertThat(CommonServiceUtil.applyChangeIfNeeded(existingValue, updateValue, setter)).isFalse();

        verifyNoMoreInteractions(setter);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void applyChangeIfNeededOnEqualValuesDoesNothingAndReturnsFalse() {
        final Integer existingValue = 5;
        final Integer updateValue = 5;
        final Consumer<Integer> setter = mock(Consumer.class);

        assertThat(CommonServiceUtil.applyChangeIfNeeded(existingValue, updateValue, setter)).isFalse();

        verifyNoMoreInteractions(setter);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void applyChangeIfNeededOnNotEqualValuesApplyTheChangeAndReturnTrue() {
        final Integer existingValue = 5;
        final Integer updateValue = 4;
        final Consumer<Integer> setter = mock(Consumer.class);

        assertThat(CommonServiceUtil.applyChangeIfNeeded(existingValue, updateValue, setter)).isTrue();

        verify(setter).accept(updateValue);
        verifyNoMoreInteractions(setter);
    }
}
