package com.logicore.shared.domain.valueobject;

public record Address(
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        String country
) {
    public Address {
        if (street == null || street.isBlank()) throw new IllegalArgumentException("Rua é obrigatória");
        if (number == null || number.isBlank()) throw new IllegalArgumentException("Número é obrigatório");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("Cidade é obrigatória");
        if (state == null || state.isBlank()) throw new IllegalArgumentException("Estado é obrigatório");
        if (zipCode == null || zipCode.isBlank()) throw new IllegalArgumentException("CEP é obrigatório");
        country = (country == null || country.isBlank()) ? "Brasil" : country;
    }

    public String formatted() {
        StringBuilder sb = new StringBuilder(street).append(", ").append(number);
        if (complement != null && !complement.isBlank()) sb.append(", ").append(complement);
        if (neighborhood != null && !neighborhood.isBlank()) sb.append(" - ").append(neighborhood);
        return sb.append(", ").append(city).append("/").append(state)
                .append(" - ").append(zipCode).toString();
    }
}
