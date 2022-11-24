package org.shopping.company.services.orders.steps;


import org.shopping.company.services.orders.workflow.Context;

import java.util.concurrent.CompletableFuture;

public interface WorkflowStep {
    CompletableFuture<Context> execute(Context context);
}
