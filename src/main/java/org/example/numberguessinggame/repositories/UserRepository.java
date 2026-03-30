package org.example.numberguessinggame.repositories;

import java.util.List;
import java.util.Optional;
import org.example.numberguessinggame.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findTop10ByOrderByScoreDesc();

    /**
     * Atomic update to prevent concurrent guesses from consuming the same "turns" value.
     *
     * <p>Decrements {@code turns} by 1 and increments {@code score} by {@code delta} in one SQL UPDATE,
     * only if current {@code turns > 0}.
     *
     * @return number of rows updated (0 means "no turns left")
     */
    @Modifying
    @Query(
            "update User u set u.turns = u.turns - 1, u.score = u.score + :delta where u.id = :id and u.turns > 0")
    int decrementTurnsAndUpdateScore(
            @Param("id") Long id, @Param("delta") int delta);
}
