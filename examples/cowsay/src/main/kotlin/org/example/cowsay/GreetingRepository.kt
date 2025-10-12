package org.example.cowsay

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GreetingRepository : JpaRepository<Greeting, Long> {
    @Query(value = "SELECT * FROM greetings ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    fun findRandom(): Greeting?
}