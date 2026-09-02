package com.uade.tpo.demo.entity;

/**
 * Roles de la aplicacion, modelados como enum igual que en el ejemplo de la
 * catedra. Al ser un enum, SecurityConfig puede referenciarlos con
 * Role.ADMIN.name() y no hace falta ni repositorio ni tabla aparte.
 *
 * USER  = comprador. Es el rol con el que se registra cualquier persona.
 * ADMIN = la tienda. Es el unico que puede publicar y administrar productos.
 */
public enum Role {
    USER,
    ADMIN
}
