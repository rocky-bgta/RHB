package com.rhbgroup.dcpbo.customer.audit.collector;

import lombok.Getter;
import lombok.Setter;
import org.springframework.jms.core.JmsTemplate;

/**
 * We should reuse from Audit Helper, AuditLogger class.
 * TODO:Reason why we came with out with own implementation because the current design not extensible
 */
@Setter
@Getter
public class BoAuditLogger implements BoAuditLogQueue {

    private JmsTemplate jmsTemplate;
    private String auditQueueName;

    public BoAuditLogger(JmsTemplate jmsTemplate, String auditQueueName){
        this.jmsTemplate = jmsTemplate;
        this.auditQueueName = auditQueueName;
    }

    @Override
    public void send(String jsonStr) {
        jmsTemplate.convertAndSend(auditQueueName, jsonStr);
    }
}
