package org.example.cowsay

import jakarta.persistence.*

@Entity
@Table(name = "greetings")
class Greeting(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var text: String = ""
) {
    constructor() : this(null, "")
}