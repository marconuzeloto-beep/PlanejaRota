package com.planejarota.domain.valueobject;

/**
 * Endereço completo como Value Object imutável.
 * A geocodificação (conversão para GeoCoordinate) é responsabilidade
 * da camada de infraestrutura via GeocodingPort.
 */
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
        if (city == null || city.isBlank()) throw new IllegalArgumentException("Cidade é obrigatória");
        if (state == null || state.isBlank()) throw new IllegalArgumentException("Estado é obrigatório");
        if (zipCode == null || zipCode.isBlank()) throw new IllegalArgumentException("CEP é obrigatório");
        country = (country == null || country.isBlank()) ? "Brasil" : country;
    }

    public String formatted() {
        StringBuilder sb = new StringBuilder();
        sb.append(street).append(", ").append(number);
        if (complement != null && !complement.isBlank()) sb.append(", ").append(complement);
        sb.append(" - ").append(neighborhood);
        sb.append(", ").append(city).append(" - ").append(state);
        sb.append(", ").append(zipCode);
        return sb.toString();
    }
}
