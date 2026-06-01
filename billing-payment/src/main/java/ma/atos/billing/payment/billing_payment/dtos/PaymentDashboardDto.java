package ma.atos.billing.payment.billing_payment.dtos;

public record PaymentDashboardDto(
        long totalTransactions,
        long successfulTransactions,
        long failedTransactions,
        long pendingTransactions,
        long cardTransactions,
        long cashTransactions,
        double totalCollected
) {
}
