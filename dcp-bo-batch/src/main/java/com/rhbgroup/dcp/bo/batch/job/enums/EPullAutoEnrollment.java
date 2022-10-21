package com.rhbgroup.dcp.bo.batch.job.enums;

public enum EPullAutoEnrollment {
    GET_DEPOSIT("select prd.deposit_type as accountType, prf.account_no as accountNo, prf.statement_type as statementType\n" +
            "from  dcp.dbo.tbl_deposit_profile prf\n" +
            "join dcp.dbo.tbl_deposit_product prd on prf.deposit_product_id = prd.id\n" +
            "where prd.deposit_type in ('SAVINGS', 'CURRENT', 'MCA', 'TERM_DEPOSIT') and user_id = ?\n" +
            "order by prd.deposit_type"),

    GET_LOAN("select prd.loan_type as accountType, prf.account_no as accountNo, prf.statement_type as statementType\n" +
            "from dcp.dbo.tbl_loan_profile prf\n" +
            "join dcp.dbo.tbl_loan_product prd on prf.loan_product_id = prd.id\n" +
            "where prd.loan_type in ('MORTGAGE','PERSONAL_FINANCE','HIRE_PURCHASE','ASB') and user_id = ?\n" +
            "order by prd.loan_type"),

    GET_CARD("select prd.category as accountType, prf.card_no as accountNo, prf.statement_type as statementType\n" +
            "from dcp.dbo.tbl_card_profile prf\n" +
            "join dcp.dbo.tbl_card_product prd on prf.card_product_id = prd.id\n" +
            "where prd.category in ('CREDIT_CARD','PREPAID_CARD') and user_id = ?\n" +
            "order by prd.card_type");

    public final String value;

    EPullAutoEnrollment(String value) {
        this.value = value;
    }
}
