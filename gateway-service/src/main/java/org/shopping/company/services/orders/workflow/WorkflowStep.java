package org.shopping.company.services.orders.workflow;


import java.util.concurrent.CompletableFuture;

public interface WorkflowStep {
    CompletableFuture<Context> execute(Context context);
}
