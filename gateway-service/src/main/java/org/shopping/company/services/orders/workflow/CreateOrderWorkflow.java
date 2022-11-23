package org.shopping.company.services.orders.workflow;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.workflow.Context;

import java.util.concurrent.CompletableFuture;

public class CreateOrderWorkflow {

    private final Logger logger = LoggerFactory.getLogger(CreateOrderWorkflow.class);

    Context context = null;

    public CreateOrderWorkflow(Context context) {
        this.context = context;
    }

    public CompletableFuture<Context> execute() {
        logger.info("Workflow Started");
        CompletableFuture<Context> workflowFuture = new CompletableFuture();

        step1(context).thenCompose(this::step2).thenCompose(this::step3).thenCompose(this::step4).thenAccept(context1 -> {
            logger.info("Workflow Completed");

            workflowFuture.complete(context);
        });

        return workflowFuture;
    }


    private CompletableFuture<Context> step1(Context context) {
        logger.info("Workflow Step1 Executed");
        CompletableFuture<Context> step1Future = new CompletableFuture();
        step1Future.complete(context);
        return step1Future;
    }

    private CompletableFuture<Context> step2(Context context) {
        logger.info("Workflow Step2 Executed");
        CompletableFuture<Context> step2Future = new CompletableFuture();
        step2Future.complete(context);
        return step2Future;
    }

    private CompletableFuture<Context> step3(Context context) {
        logger.info("Workflow Step3 Executed");
        CompletableFuture<Context> step3Future = new CompletableFuture();
        step3Future.complete(context);
        return step3Future;
    }

    private CompletableFuture<Context> step4(Context context) {
        logger.info("Workflow Step4 Executed");
        CompletableFuture<Context> step4Future = new CompletableFuture();
        step4Future.complete(context);
        return step4Future;
    }

}
