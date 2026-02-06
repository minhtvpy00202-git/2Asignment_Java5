package com.poly.ASM.service.payment.impl;

import com.poly.ASM.service.payment.PayosPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

@Service
@RequiredArgsConstructor
public class PayosPaymentServiceImpl implements PayosPaymentService {

    private final PayOS payOS;

    @Override
    public CreatePaymentLinkResponse createPaymentLink(long orderCode, long amount, String description, String returnUrl, String cancelUrl) throws PayOSException {
        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();
        return payOS.paymentRequests().create(request);
    }

    @Override
    public PaymentLink getPaymentLink(long orderCode) throws PayOSException {
        return payOS.paymentRequests().get(orderCode);
    }

    @Override
    public void cancelPaymentLink(long orderCode, String reason) throws PayOSException {
        if (reason != null && !reason.isBlank()) {
            payOS.paymentRequests().cancel(orderCode, reason);
            return;
        }
        payOS.paymentRequests().cancel(orderCode);
    }

    @Override
    public boolean cancelIfPending(long orderCode, String reason) throws PayOSException {
        PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
        if (paymentLink == null || paymentLink.getStatus() == null) {
            return false;
        }
        String status = paymentLink.getStatus().getValue();
        if (status == null || !"PENDING".equalsIgnoreCase(status)) {
            return false;
        }
        if (reason != null && !reason.isBlank()) {
            payOS.paymentRequests().cancel(orderCode, reason);
            return true;
        }
        payOS.paymentRequests().cancel(orderCode);
        return true;
    }

    @Override
    public WebhookData verifyWebhook(Webhook webhook) throws Exception {
        return payOS.webhooks().verify(webhook);
    }

}
