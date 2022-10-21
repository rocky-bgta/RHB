package com.rhbgroup.dcpbo.customer.repository;

public interface BoAuditQueryList {

    String MAIN_QUERY = "select am.id as audit_id,\n" +
            "am.event_id as event_id,\n" +
            "ec.details_table_name as details_table_name,\n" +
            "ec.function_id as function_id,\n" +
            "ec.activity_name as activity_name,\n" +
            "am.username as username,\n" +
            "am.timestamp as current_ts\n" +
            "from tbl_bo_audit_main am\n" +
            "inner join tbl_bo_audit_event_config ec on ec.id = am.event_id\n" +
            "where \n" +
            "(am.username = :username or 1 = :switchUsername)\n" +
            "and (datediff( day , am.timestamp , :selectedDate) = 0 or 1 = :switchDate)\n" +
            "and (ec.function_id in (:functionList) or (ec.function_id is null and 1=:switchFunction))\n" +
            "and am.status_code in ('200','10000') " +
            "order by am.timestamp asc \n-- #pageable\n"; // pageable is to fix spring old version native query bug

    String COUNT_QUERY = "select count(*) \n" +
            "from tbl_bo_audit_main am\n" +
            "inner join tbl_bo_audit_event_config ec on ec.id = am.event_id\n" +
            "where ec.id = am.event_id \n" +
            "and (am.username = :username or 1 = :switchUsername)\n" +
            "and (datediff( day , am.timestamp , :selectedDate) = 0 or 1 = :switchDate)\n" +
            "and (ec.function_id in (:functionList) or (ec.function_id is null and 1=:switchFunction))\n" +
            "and am.status_code in ('200','10000')";

}
