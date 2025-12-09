package com.finx.strategyengineservice.domain.enums;

/**
 * Available text filter fields for strategy rules
 * Maps to entity field paths for text-based filtering
 * Updated to match unified CSV format
 */
public enum TextFilterField {

    // ==================== CUSTOMER/LOCATION (via loan.primaryCustomer) ====================
    LANGUAGE("Language", "loan.primaryCustomer.languagePreference"),
    STATE("State", "loan.primaryCustomer.state"),
    CITY("City", "loan.primaryCustomer.city"),
    PINCODE("Pincode", "loan.primaryCustomer.pincode"),

    // ==================== PRODUCT ====================
    PRODUCT("Product", "loan.productType"),
    PRODUCT_CODE("Product Code", "loan.productCode"),
    SCHEME_CODE("Scheme Code", "loan.schemeCode"),
    PRODUCT_SOURCING_TYPE("Product Sourcing Type", "loan.productSourcingType"),

    // ==================== LENDER ====================
    LENDER("Lender", "loan.lender"),
    CO_LENDER("Co-Lender", "loan.coLender"),
    REFERENCE_LENDER("Reference Lender", "loan.referenceLender"),

    // ==================== BUCKET ====================
    BUCKET("Bucket", "loan.bucket"),
    RISK_BUCKET("Risk Bucket", "loan.riskBucket"),
    SOM_BUCKET("SOM Bucket", "loan.somBucket"),
    CYCLE_DUE("Cycle Due", "loan.cycleDue"),

    // ==================== CREDIT CARD ====================
    CARD_STATUS("Card Status", "loan.cardStatus"),
    STATEMENT_MONTH("Statement Month", "loan.statementMonth"),

    // ==================== PAYMENT ====================
    LAST_PAYMENT_MODE("Last Payment Mode", "loan.lastPaymentMode"),

    // ==================== BLOCK STATUS ====================
    BLOCK_1("Block 1", "loan.block1"),
    BLOCK_2("Block 2", "loan.block2"),

    // ==================== CASE/GEOGRAPHY ====================
    LOCATION("Location", "location"),
    ZONE("Zone", "zone"),
    CASE_STATUS("Case Status", "caseStatus"),
    CASE_PRIORITY("Case Priority", "casePriority"),

    // ==================== ALLOCATION ====================
    PRIMARY_AGENT("Primary Agent", "primaryAgent"),
    SECONDARY_AGENT("Secondary Agent", "secondaryAgent"),
    AGENCY_NAME("Agency Name", "agencyName"),

    // ==================== ASSET ====================
    MODEL_MAKE("Model/Make", "modelMake"),
    REVIEW_FLAG("Review Flag", "reviewFlag");

    private final String displayName;
    private final String fieldPath;

    TextFilterField(String displayName, String fieldPath) {
        this.displayName = displayName;
        this.fieldPath = fieldPath;
    }

    TextFilterField(String displayName) {
        this.displayName = displayName;
        this.fieldPath = null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFieldPath() {
        return fieldPath;
    }
}
