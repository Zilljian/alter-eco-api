package org.alter.eco.api.controller;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.service.auth.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerHelper {

    private final static Logger log = LoggerFactory.getLogger(ControllerHelper.class);

    private final AuthService authService;

    public void obtainToken(String header) {
        var token = header.replace("Bearer ", "").trim();
        try {
            var uuid = authService.getUuidFromToken(token);
            MDC.put("user", uuid);
            log.info("ControllerHelper.obtainToken Request by user with uuid = {}", uuid);
        } catch (Exception e) {
            log.error("ControllerHelper.obtainToken.thrown Error while obtaining token", e);
        }
    }
}
