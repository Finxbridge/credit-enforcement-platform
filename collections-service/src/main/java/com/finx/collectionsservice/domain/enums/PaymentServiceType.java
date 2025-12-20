package com.finx.collectionsservice.domain.enums;

/**
 * Payment service types supported by the unified payment API
 */
public enum PaymentServiceType {
    DYNAMIC_QR,      // Generate QR code for UPI payment
    PAYMENT_LINK,    // Generate payment link (SMS/WhatsApp)
    COLLECT_CALL     // UPI collect request to customer's VPA
}
