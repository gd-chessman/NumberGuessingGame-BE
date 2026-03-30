package org.example.numberguessinggame.repositories;

import java.util.Optional;
import org.example.numberguessinggame.entities.VnpayPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VnpayPaymentRepository extends JpaRepository<VnpayPayment, Long> {

    Optional<VnpayPayment> findByTxnRef(String txnRef);
}
