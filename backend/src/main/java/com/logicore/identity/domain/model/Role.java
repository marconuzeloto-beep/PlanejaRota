package com.logicore.identity.domain.model;

public enum Role {
    ADMIN,    // gerencia usuários, tudo liberado
    MANAGER,  // cria rotas, clientes, veículos
    DRIVER,   // visualiza e executa próprias rotas
    VIEWER    // somente leitura
}
