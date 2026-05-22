package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    // текущее время сервера
    private Instant serverTime;

    // время жизни тикета
    private Long ttl;

    // дата активации
    private Instant activatedAt;

    // дата окончания лицензии
    private Instant expiresAt;

    // ID пользователя
    private Long userId;

    // ID устройства
    private String deviceId;

    // флаг блокировки
    private boolean blocked;
}