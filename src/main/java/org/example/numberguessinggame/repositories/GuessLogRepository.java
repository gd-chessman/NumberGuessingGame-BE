package org.example.numberguessinggame.repositories;

import org.example.numberguessinggame.entities.GuessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuessLogRepository extends JpaRepository<GuessLog, Long> {
}
