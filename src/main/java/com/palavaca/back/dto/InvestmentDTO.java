package com.palavaca.back.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class InvestmentDTO {

    /** Dinero invertido en un animal y su punto mínimo de venta. */
    public record AnimalInvestment(
            UUID animalId,
            String tagNumber,
            String name,
            UUID lotId,
            String lotName,
            BigDecimal acquisitionCost,
            BigDecimal assignedCosts,
            BigDecimal feedingCost,
            BigDecimal farmOverhead,
            BigDecimal totalInvestment,
            BigDecimal breakEvenPrice) {}

    /** Resumen de inversión de una finca: total y desglose por animal. */
    public record FarmInvestment(
            UUID farmId,
            int activeAnimalCount,
            BigDecimal totalInvestment,
            BigDecimal averageInvestment,
            List<AnimalInvestment> animals) {}
}
