package ma.atos.billing.invoice.billing_invoice.workflow;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceWorkflowRequest;
import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceWorkflowResponse;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvoiceWorkflowService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public InvoiceWorkflowService(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    public InvoiceWorkflowResponse startInvoicePaymentProcess(InvoiceWorkflowRequest request) {
        Map<String, Object> variables = toVariables(request);
        String businessKey = request.getReference();

        ProcessInstanceWithVariables processInstance = runtimeService
                .createProcessInstanceByKey(InvoiceWorkflowVariables.PROCESS_KEY)
                .businessKey(businessKey)
                .setVariables(variables)
                .executeWithVariablesInReturn();

        Long invoiceId = readInvoiceId(processInstance);

        return new InvoiceWorkflowResponse(processInstance.getProcessInstanceId(), invoiceId, businessKey);
    }

    public List<Task> findPendingValidationTasks() {
        return taskService.createTaskQuery()
                .processDefinitionKey(InvoiceWorkflowVariables.PROCESS_KEY)
                .taskName("Valider facture")
                .active()
                .list();
    }

    public void completeValidationTask(String taskId, Boolean paymentSuccess) {
        taskService.complete(taskId);
    }

    private Long readInvoiceId(ProcessInstanceWithVariables processInstance) {
        Object invoiceId = processInstance.getVariables().get(InvoiceWorkflowVariables.INVOICE_ID);
        if (invoiceId instanceof Number number) {
            return number.longValue();
        }
        return invoiceId != null ? Long.valueOf(invoiceId.toString()) : null;
    }

    private Map<String, Object> toVariables(InvoiceWorkflowRequest request) {
        Map<String, Object> variables = new HashMap<>();
        variables.put(InvoiceWorkflowVariables.REFERENCE, request.getReference());
        variables.put(InvoiceWorkflowVariables.DATE_INVOICE, request.getDateInvoice() != null ? request.getDateInvoice().toString() : null);
        variables.put(InvoiceWorkflowVariables.DATE_DUE, request.getDateDue() != null ? request.getDateDue().toString() : null);
        variables.put(InvoiceWorkflowVariables.MONTANT_HT, request.getMontantHt());
        variables.put(InvoiceWorkflowVariables.MONTANT_TVA, request.getMontantTva());
        variables.put(InvoiceWorkflowVariables.MONTANT_TTC, request.getMontantTtc());
        variables.put(InvoiceWorkflowVariables.MODE_REGLEMENT, request.getModeReglement() != null ? request.getModeReglement().name() : null);
        variables.put(InvoiceWorkflowVariables.DESCRIPTION, request.getDescription());
        variables.put(InvoiceWorkflowVariables.CUSTOMER_ID, request.getCustomerId());
        variables.put(InvoiceWorkflowVariables.CREANCIER_ID, request.getCreancierId());
        variables.put(InvoiceWorkflowVariables.POINT_DE_VENTE_ID, request.getPointDeVenteId());
        return variables;
    }
}
