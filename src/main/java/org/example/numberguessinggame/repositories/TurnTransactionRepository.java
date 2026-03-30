package org.example.numberguessinggame.repositories;

import org.example.numberguessinggame.entities.TurnTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurnTransactionRepository extends JpaRepository<TurnTransaction, Long> {
}
