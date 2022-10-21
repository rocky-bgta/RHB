package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcp.data.entity.PaginatedResult;
import com.rhbgroup.dcp.data.entity.cards.CardProfile;
import com.rhbgroup.dcp.data.entity.deposits.DepositProfile;
import com.rhbgroup.dcp.data.entity.loans.LoanProfile;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.data.util.PageUtil;
import io.ebean.Ebean;
import io.ebean.PagedList;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BoSearchRepository extends ProfileRepository {

    public static final String CUST_SEARCH_SQL_TYPE_COUNT = "count";
    public static final String CUST_SEARCH_SQL_TYPE_COLUMNS = "columns";

    public List<UserProfile> searchUserProfileByValue(String value) {
        value = value.trim();

        List<UserProfile> userProfile = new ArrayList<>();
        userProfile.addAll(Ebean.find(UserProfile.class).where().eq("username", value).findList());
        userProfile.addAll(Ebean.find(UserProfile.class).where().eq("idNo", value).findList());
        userProfile.addAll(Ebean.find(UserProfile.class).where().eq("cisNo", value).findList());
        userProfile.addAll(Ebean.find(UserProfile.class).where().eq("mobileNo", value).findList());
        userProfile.addAll(Ebean.find(UserProfile.class).where().eq("email", value).findList());

        return userProfile;
    }

    public List<UserProfile> searchUserProfileByAccountInfoValue(String value) {
        value = value.trim();

        List<UserProfile> userProfile = new ArrayList<>();
        List<DepositProfile> depositProfile = new ArrayList<>();
        List<CardProfile> cardProfile = new ArrayList<>();
        List<LoanProfile> loanProfile = new ArrayList<>();

        depositProfile = Ebean.find(DepositProfile.class).where().eq("accountNo", value).findList();
        if (depositProfile.size() > 0) {
            List<Integer> userIds = new ArrayList<>();

            for (DepositProfile singleProfile : depositProfile) {
                userIds.add(singleProfile.getUserId());
            }

            userProfile.addAll(Ebean.find(UserProfile.class).where().in("id", userIds).findList());
        }


        cardProfile = Ebean.find(CardProfile.class).where().eq("cardNo", value).findList();
        if (cardProfile.size() > 0) {
            List<Integer> userIds = new ArrayList<>();

            for (CardProfile singleProfile : cardProfile) {
                userIds.add(singleProfile.getUserId());
            }

            userProfile.addAll(Ebean.find(UserProfile.class).where().in("id", userIds).findList());
        }


        loanProfile = Ebean.find(LoanProfile.class).where().eq("accountNo", value).findList();
        if (loanProfile.size() > 0) {
            List<Integer> userIds = new ArrayList<>();

            for (LoanProfile singleProfile : loanProfile) {
                userIds.add(singleProfile.getUserId());
            }

            userProfile.addAll(Ebean.find(UserProfile.class).where().in("id", userIds).findList());
        }

        return userProfile;
    }

    /**
     * Retrieves list of customer that matches one or more filtering criteria(s).
     * <p>Filtering criteria(s) supported:
     * 2) Keyword in name or mobile no or id no or cis no </p>
     * <p>Database Table: TBL_USER_PROFILE</p>
     *
     * @param pageNo   Result page number to be retrieved. Starts with 1.
     * @param pageSize Result page size to be retrieved.
     * @param keyword  Search keyword with space delimiter
     * @return SearchedCustomerPagination list of customer
     */
    public PaginatedResult<UserProfile> searchUserProfileByValueWithPagination(Integer pageNo, Integer pageSize, Optional<String> keyword) {

        int totalRowCount = 0;
        int totalPageCount = 0;
        List<UserProfile> userProfileList = new ArrayList<>();
        ;

        Optional<String> sql = getUserProfileByFilterSQL(keyword, CUST_SEARCH_SQL_TYPE_COLUMNS);

        if (sql.isPresent()) {
            RawSql rawSql = RawSqlBuilder.parse(sql.get())
                    .create();

            PagedList<UserProfile> pagedList = Ebean.find(UserProfile.class)
                    .setRawSql(rawSql)
                    .orderBy("id")
                    .setFirstRow(PageUtil.getPageStartIndex(pageNo, pageSize))
                    .setMaxRows(pageSize)
                    .findPagedList();

            pagedList.loadCount();
            userProfileList = pagedList.getList();

            totalRowCount = pagedList.getTotalCount();
            totalPageCount = pagedList.getTotalPageCount();
        }

        return new PaginatedResult<UserProfile>(userProfileList, totalRowCount, totalPageCount);
    }

    private Optional<String> getUserProfileByFilterSQL(Optional<String> keyword, String type) {

        String baseSql = "";

        // Construct Base Query Statement
        if (type.equals(CUST_SEARCH_SQL_TYPE_COUNT)) {
            baseSql = "SELECT count(1) FROM TBL_USER_PROFILE profile ";
        } else {
            baseSql = "SELECT id, username, name, email, mobile_no, cis_no, uuid, id_type, id_no, user_status, is_premier, last_login " +
                    "\nFROM TBL_USER_PROFILE profile ";
        }

        // Construct Where Expression Statement Based On Filter Mode
        List<String> whereClauses = new ArrayList<>();

        String searchString = keyword.map(
                key -> ("%" + key.trim().replaceAll(" +", "%").toUpperCase() + "%")
        ).orElse("%");

        searchString = "'" + searchString + "'";

        whereClauses.add("user_status = 'A'");
        whereClauses.add("username LIKE " + searchString + " OR " + "name LIKE " + searchString + " OR " + "mobile_no LIKE " + searchString + " OR " + "id_no LIKE " + searchString + " OR " + "cis_no LIKE " + searchString);

        // Construct Consolidated SQL Statement
        String consolidatedSql = null;
        consolidatedSql = baseSql + whereClauses.stream().collect(Collectors.joining(")\nAND (", "\nWHERE (", ")"));

        return Optional.ofNullable(consolidatedSql);
    }

    public List<UserProfile> searchByCisno(String value) {
        value = value.trim();

        List<UserProfile> userProfile = new ArrayList<>();
        userProfile.addAll(Ebean.find(UserProfile.class).where().eq("cisNo", value).findList());
        return userProfile;
    }
}
