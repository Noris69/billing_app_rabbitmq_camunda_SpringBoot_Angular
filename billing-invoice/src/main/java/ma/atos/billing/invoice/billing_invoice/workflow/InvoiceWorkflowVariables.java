package ma.atos.billing.invoice.billing_invoice.workflow;

public final class InvoiceWorkflowVariables {

    public static final String PROCESS_KEY = "invoice-payment-process";
    public static final String INVOICE_ID = "invoiceId";
    public static final String REFERENCE = "reference";
    public static final String DATE_INVOICE = "dateInvoice";
    public static final String DATE_DUE = "dateDue";
    public static final String MONTANT_HT = "montantHt";
    public static final String MONTANT_TVA = "montantTva";
    public static final String MONTANT_TTC = "montantTtc";
    public static final String MODE_REGLEMENT = "modeReglement";
    public static final String DESCRIPTION = "description";
    public static final String CUSTOMER_ID = "customerId";
    public static final String CREANCIER_ID = "creancierId";
    public static final String POINT_DE_VENTE_ID = "pointDeVenteId";
    public static final String PAYMENT_SUCCESS = "paymentSuccess";

    private InvoiceWorkflowVariables() {
    }
}
