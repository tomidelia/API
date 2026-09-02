package com.uade.tpo.demo.entity;

/**
 * Roles disponibles en la aplicacion.
 * Al ser un enum, SecurityConfig puede referenciarlos con
 * Role.ADMIN.name() sin necesidad de un repositorio ni una tabla adicional.
 *
 * USER  = comprador. Es el rol con el que se registra cualquier persona.
 * ADMIN = la tienda. Es el unico que puede publicar y administrar productos.
 */
public enum Role {
    USER,
    ADMIN
}
