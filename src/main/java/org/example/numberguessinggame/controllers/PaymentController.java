package org.example.numberguessinggame.controllers;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.example.numberguessinggame.dtos.CreateVnpayPaymentResponse;
import org.example.numberguessinggame.entities.User;
import org.example.numberguessinggame.repositories.UserRepository;
import org.example.numberguessinggame.services.VnpayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final VnpayService vnpayService;
    private final UserRepository userRepository;

    public PaymentController(VnpayService vnpayService, UserRepository userRepository) {
        this.vnpayService = vnpayService;
        this.userRepository = userRepository;
    }

    /** Creates a VNPay payment URL (user is redirected to VNPay). */
    @PostMapping("/buy-turns")
    public CreateVnpayPaymentResponse create(Authentication authentication, HttpServletRequest request) {
        User user = requireUser(authentication);
        return vnpayService.createPayment(user, request);
    }

    /** Browser return from VNPay (no JWT). */
    @GetMapping("/vnpay/return")
    public ResponseEntity<Void> returnFromVnpay(HttpServletRequest request) {
        URI location = vnpayService.handleReturn(request);
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    /** Server IPN callback from VNPay (no JWT). */
    @RequestMapping(value = "/vnpay/ipn", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> ipn(HttpServletRequest request) {
        String body = vnpayService.handleIpn(request);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    private User requireUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
