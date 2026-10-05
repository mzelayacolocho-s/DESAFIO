package com.udb.eventos.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabla "user" de las guias anteriores (id_user, username, firstname,
 * lastname, age, password). Se agrega la columna "role" (nullable para no
 * romper filas existentes) y la relacion con las reservas.
 */
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Integer idUser;

    @Column(nullable = false, unique = true)
    private String username;

    private String firstname;

    private String lastname;

    private Integer age;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role = Role.USER;

    @OneToMany(mappedBy = "user")
    private List<Booking> bookings = new ArrayList<>();

    public User() {
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /** Usuarios antiguos (sin columna role) se tratan como USER. */
    public Role getRole() {
        return role == null ? Role.USER : role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}