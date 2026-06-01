package ma.atos.billing.payment.billing_payment.dtos;

import java.math.BigDecimal;

public record PaymentDashboardDto(
        long totalTransactions,
        long successfulTransactions,
        long failedTransactions,
        long pendingTransactions,
        long cardTransactions,
        long cashTransactions,
        BigDecimal totalCollected
) {
}
