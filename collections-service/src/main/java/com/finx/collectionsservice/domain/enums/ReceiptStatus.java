package com.finx.collectionsservice.domain.enums;

public enum ReceiptStatus {
    PENDING,      // Receipt generation initiated
    GENERATED,    // Receipt PDF generated
    VERIFIED,     // Receipt verified by supervisor
    SENT,         // Receipt sent to customer
    DOWNLOADED,   // Receipt downloaded
    CANCELLED,    // Receipt cancelled
    VOID          // Receipt voided (after verification found invalid)
}
