package org.alter.eco.api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class Application {

    private static final InputStream KEY = Thread.currentThread().getContextClassLoader().getResourceAsStream("service_account_key.json");

    public static void main(String[] args) throws IOException {
        var options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(KEY))
            .build();
        FirebaseApp.initializeApp(options);

        SpringApplication.run(Application.class);
    }
}
