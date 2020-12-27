package org.alter.eco.api.service.auth;

import com.google.firebase.auth.FirebaseAuth;
import org.springframework.stereotype.Component;

@Component
public class AuthService {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public String getUuidFromToken(String token) {
        try {
            return firebaseAuth.verifyIdToken(token).getUid();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
