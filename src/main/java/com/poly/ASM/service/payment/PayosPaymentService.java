package com.poly.ASM.service.payment;

import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

public interface PayosPaymentService {

    CreatePaymentLinkResponse createPaymentLink(long orderCode, long amount, String description, String returnUrl, String cancelUrl) throws PayOSException;

    PaymentLink getPaymentLink(long orderCode) throws PayOSException;

    void cancelPaymentLink(long orderCode, String reason) throws PayOSException;

    boolean cancelIfPending(long orderCode, String reason) throws PayOSException;

    WebhookData verifyWebhook(Webhook webhook) throws Exception;
}
