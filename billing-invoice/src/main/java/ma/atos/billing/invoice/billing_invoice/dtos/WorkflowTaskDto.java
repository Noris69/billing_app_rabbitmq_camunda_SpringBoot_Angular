package ma.atos.billing.invoice.billing_invoice.dtos;

import java.time.LocalDateTime;

public class WorkflowTaskDto {

    private final String id;
    private final String name;
    private final String processInstanceId;
    private final LocalDateTime createdDate;

    public WorkflowTaskDto(String id, String name, String processInstanceId, LocalDateTime createdDate) {
        this.id = id;
        this.name = name;
        this.processInstanceId = processInstanceId;
        this.createdDate = createdDate;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
}
