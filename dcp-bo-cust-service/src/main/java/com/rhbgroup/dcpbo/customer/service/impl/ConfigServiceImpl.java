package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.enums.TransactionType;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.UserPerTxnDailyLimit;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.model.UserTxnMainFuncLimit;
import com.rhbgroup.dcpbo.customer.model.UserTxnMainLimit;
import com.rhbgroup.dcpbo.customer.repository.UserPerTransactionRepo;
import com.rhbgroup.dcpbo.customer.repository.UserProfileRepo;
import com.rhbgroup.dcpbo.customer.repository.UserTxnMainFuncLimitRepo;
import com.rhbgroup.dcpbo.customer.repository.UserTxnMainLimitRepo;
import com.rhbgroup.dcpbo.customer.service.ConfigService;
import com.rhbgroup.dcpbo.customer.vo.CustomerTrxLimitVo;
import com.rhbgroup.dcpbo.customer.vo.MainFunctionLimitsVo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigServiceImpl implements ConfigService {

    private final Logger log = LogManager.getLogger(ConfigServiceImpl.class);

    private static String passDailyLimit = "PASSWORD_DAILY_LIMIT";
    private static String wPassDailyLimit = "WPASSWORD_DAILY_LIMIT";
    private static String dayLimit = "PER_DAY_LIMIT";

    @Autowired
    UserTxnMainFuncLimitRepo userTxnMainFuncLimitRepo;

    @Autowired
    UserTxnMainLimitRepo userTxnMainLimitRepo;

    @Autowired
    UserProfileRepo userProfileRepo;

    @Autowired
    UserPerTransactionRepo dailyLimit;

    List<CustomerTrxLimitVo> custLimitList;


    public List<CustomerTrxLimitVo> getCustomerTrxLimits(String customerId) {
        custLimitList = new ArrayList<>();
        Integer customerIdInt;
        try {
            customerIdInt = Integer.parseInt(customerId);
        } catch (NumberFormatException e) {
            throw new CommonException("1");
        }

        UserProfile userProfile = userProfileRepo.findOneById(customerIdInt);

        if (userProfile == null) {
            throw new CommonException("2");
        }

        List<UserTxnMainFuncLimit> userTxnMainFuncLimitList = userTxnMainFuncLimitRepo.findByUserId(customerIdInt);
        log.info(userTxnMainFuncLimitList.size());

        if (userTxnMainFuncLimitList.size() > 0) {
            for (UserTxnMainFuncLimit userTxnMainFuncLimit : userTxnMainFuncLimitList) {

                MainFunctionLimitsVo mainFunc = new MainFunctionLimitsVo();
                mainFunc.setAmount(userTxnMainFuncLimit.getAmount());
                mainFunc.setMainFunction(userTxnMainFuncLimit.getMainFunction());

                CustomerTrxLimitVo custLimit = custLimitList.stream()
                        .filter(temp -> userTxnMainFuncLimit.getTxnType().equals(temp.getTxnType())).findAny()
                        .orElse(null);

                if (custLimit == null) {
                    custLimit = new CustomerTrxLimitVo();
                    custLimit.setTxnType(userTxnMainFuncLimit.getTxnType());
                    custLimitList.add(custLimit);

                    custLimit.setMainFunctionLimits(new ArrayList<>());

                }
                custLimit.getMainFunctionLimits().add(mainFunc);

            }
        }

        UserTxnMainLimit userTxnMainLimit = userTxnMainLimitRepo.findByUserIdAndTxnType(customerIdInt,
                TransactionType.TOPUP.name());

        if (userTxnMainLimit != null && TransactionType.TOPUP.name().equalsIgnoreCase(userTxnMainLimit.getTxnType())) {
            MainFunctionLimitsVo mainFunc = new MainFunctionLimitsVo();
            mainFunc.setAmount(userTxnMainLimit.getAmount());
            mainFunc.setMainFunction(TransactionType.TOPUP.name());

            CustomerTrxLimitVo custLimit = new CustomerTrxLimitVo();
            custLimit.setTxnType(userTxnMainLimit.getTxnType());
            custLimit.setMainFunctionLimits(new ArrayList<MainFunctionLimitsVo>());
            custLimit.getMainFunctionLimits().add(mainFunc);

            custLimitList.add(custLimit);
        }

        // for DUitNow QR
        List<UserTxnMainFuncLimit> userTxnMainFuncLimitListForDuitQr = userTxnMainFuncLimitRepo
                .findByUserIdAndMainFunction(customerIdInt);
        getMainFucntionLimitAndAmountQR(userTxnMainFuncLimitListForDuitQr, customerIdInt);

        getListInSequence(custLimitList);

        return custLimitList;
    }

    private void getListInSequence(List<CustomerTrxLimitVo> custLimitList2) {
        Map<String, List<MainFunctionLimitsVo>> tmpMap = new HashMap<String, List<MainFunctionLimitsVo>>();
        for (CustomerTrxLimitVo cs : custLimitList2) {
            tmpMap.put(cs.getTxnType(), cs.getMainFunctionLimits());
        }
        custLimitList = new ArrayList<CustomerTrxLimitVo>();
        for (TransactionType row : TransactionType.values()) {
            CustomerTrxLimitVo cst = new CustomerTrxLimitVo();
            if (tmpMap.get(row.toString()) != null) {
                List<MainFunctionLimitsVo> s = (List<MainFunctionLimitsVo>) tmpMap.get(row.toString());
                cst.setTxnType(row.toString());
                cst.setMainFunctionLimits(s);
                custLimitList.add(cst);
            }
        }

    }

    private void getMainFucntionLimitAndAmountQR(
            List<UserTxnMainFuncLimit> userTxnMainFuncLimitListForDuitQr,
            int customerIdInt) {

        MainFunctionLimitsVo mainFunc = null;
        MainFunctionLimitsVo mainFuncInvest = null;
        CustomerTrxLimitVo custLimit2 = new CustomerTrxLimitVo();
        List<MainFunctionLimitsVo> mainListVO = new ArrayList<>();

        if (userTxnMainFuncLimitListForDuitQr != null && !userTxnMainFuncLimitListForDuitQr.isEmpty()
                && userTxnMainFuncLimitListForDuitQr.get(0).isFullLogin()) {

            for (UserTxnMainFuncLimit userTxnMainFuncLimit : userTxnMainFuncLimitListForDuitQr) {
                custLimit2.setTxnType(userTxnMainFuncLimit.getMainFunction());
                mainFunc = new MainFunctionLimitsVo();
                mainFunc.setAmount(userTxnMainFuncLimit.getAmount());
                mainFunc.setMainFunction(userTxnMainFuncLimit.getMainFunction());

                UserPerTxnDailyLimit usrPr = dailyLimit.findByUserIdAndMainFunction(customerIdInt,
                        userTxnMainFuncLimit.getMainFunction());
                if (usrPr != null) {
                    mainFunc = new MainFunctionLimitsVo();
                    mainFunc.setMainFunction(passDailyLimit);
                    mainFunc.setAmount(userTxnMainFuncLimit.getAmount());
                    mainListVO.add(mainFunc);
                    if (userTxnMainFuncLimitListForDuitQr.get(0).isPreLogin()) {

                        mainFunc = new MainFunctionLimitsVo();
                        mainFunc.setMainFunction(wPassDailyLimit);
                        mainFunc.setAmount(usrPr.getAmountPerDay());
                        mainListVO.add(mainFunc);

                        mainFunc = new MainFunctionLimitsVo();
                        mainFunc.setMainFunction(dayLimit);
                        mainFunc.setAmount(usrPr.getAmountPerTxn());
                        mainListVO.add(mainFunc);
                    }
                }

            }
            custLimit2.setMainFunctionLimits(mainListVO);
            custLimitList.add(custLimit2);
        }
    }
}
