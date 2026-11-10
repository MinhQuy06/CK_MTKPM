package com.datlichkhambenh.backend.factory;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Looks up the concrete creator for a role among every {@link AccountFactory} Spring knows. */
@Component
public class AccountFactories {
    private final Map<String, AccountFactory> byRole;

    public AccountFactories(List<AccountFactory> factories) {
        this.byRole = factories.stream().collect(Collectors.toUnmodifiableMap(AccountFactory::role, Function.identity()));
    }

    public Optional<AccountFactory> forRole(String role) {
        return Optional.ofNullable(role == null ? null : byRole.get(role));
    }
}
