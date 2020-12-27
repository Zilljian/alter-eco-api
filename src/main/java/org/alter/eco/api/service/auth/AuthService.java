package org.alter.eco.api.service.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.stereotype.Component;

@Component
public class AuthService {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public String getUuidFromToken(String token) {
        try {
            return firebaseAuth.verifyIdToken(token).getUid();
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
    }
}
