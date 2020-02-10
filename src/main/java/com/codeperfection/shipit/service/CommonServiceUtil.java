package com.codeperfection.shipit.service;

import com.codeperfection.shipit.entity.Transporter;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.EntityNotFoundException;
import com.codeperfection.shipit.repository.TransporterRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@Component
public class CommonServiceUtil {

    private TransporterRepository transporterRepository;

    public CommonServiceUtil(TransporterRepository transporterRepository) {
        this.transporterRepository = transporterRepository;
    }

    <T> boolean applyChangeIfNeeded(T existingValue, T updateValue, Consumer<T> setter) {
        if (updateValue == null || Objects.equals(existingValue, updateValue)) {
            return false;
        }

        setter.accept(updateValue);
        return true;
    }

    public Transporter getTransporter(UUID transporterUuid, User user) {
        return transporterRepository.findByUuidAndUser(transporterUuid, user)
                .orElseThrow(() -> new EntityNotFoundException(transporterUuid));
    }
}
