package com.cantinho_emocoes.dto;

public record DashboardStatsDTO(
    long totalUsuarios,
    long totalDiarios,
    long totalAtividades
) {}