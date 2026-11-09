package com.palavaca.back.controller;

import com.palavaca.back.dto.SyncDTO;
import com.palavaca.back.service.OfflineSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class OfflineSyncController {

    private final OfflineSyncService offlineSyncService;

    @GetMapping("/bootstrap")
    public SyncDTO.BootstrapResponse bootstrap(Authentication authentication) {
        return offlineSyncService.bootstrap(authentication.getName());
    }

    @GetMapping("/changes")
    public SyncDTO.ChangePage changes(Authentication authentication,
                                      @RequestParam long cursor,
                                      @RequestParam(required = false) Long upperBound,
                                      @RequestParam(defaultValue = "100") int limit) {
        return offlineSyncService.changes(authentication.getName(), cursor, upperBound, limit);
    }
}
