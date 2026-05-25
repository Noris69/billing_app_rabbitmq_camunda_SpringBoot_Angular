package ma.atos.billing.invoice.billing_invoice.controllers;

import jakarta.validation.Valid;
import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceWorkflowRequest;
import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceWorkflowResponse;
import ma.atos.billing.invoice.billing_invoice.dtos.WorkflowTaskDto;
import ma.atos.billing.invoice.billing_invoice.workflow.InvoiceWorkflowService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/workflows/invoice-payment")
public class InvoiceWorkflowController {

    private final InvoiceWorkflowService workflowService;

    public InvoiceWorkflowController(InvoiceWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/start")
    public ResponseEntity<InvoiceWorkflowResponse> start(@Valid @RequestBody InvoiceWorkflowRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(workflowService.startInvoicePaymentProcess(request));
    }

    @GetMapping("/validation-tasks")
    public ResponseEntity<List<WorkflowTaskDto>> validationTasks() {
        List<WorkflowTaskDto> tasks = workflowService.findPendingValidationTasks()
                .stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/validation-tasks/{taskId}/complete")
    public ResponseEntity<Void> completeValidationTask(
            @PathVariable String taskId,
            @RequestParam(required = false) Boolean paymentSuccess
    ) {
        workflowService.completeValidationTask(taskId, paymentSuccess);
        return ResponseEntity.noContent().build();
    }

    private WorkflowTaskDto toDto(Task task) {
        return new WorkflowTaskDto(
                task.getId(),
                task.getName(),
                task.getProcessInstanceId(),
                task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
    }
}
