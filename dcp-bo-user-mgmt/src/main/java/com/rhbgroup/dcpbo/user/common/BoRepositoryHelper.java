package com.rhbgroup.dcpbo.user.common;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.user.search.SearchServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public final class BoRepositoryHelper {

    private static Logger logger = LogManager.getLogger(BoRepositoryHelper.class);

    private final static String DELIMITER_COMMA = ",";

    public static List<Integer> constructIdsByDelimitedString(String idsDelimitedStr) {
        return constructIdsByDelimitedString(idsDelimitedStr, DELIMITER_COMMA);
    }

        public static List<Integer> constructIdsByDelimitedString(String idsDelimitedStr, String delimiter) {
        String[] idsStr = idsDelimitedStr.split(delimiter);
        List<Integer> ids = new ArrayList<>();
        for(int i = 0; i < idsStr.length; i++) {
            ids.add(Integer.valueOf(idsStr[i]));
        }
        return ids;
    }

    public static List<String> constructStringsByDelimitedString(String idsDelimitedStr) {
        return constructStringsByDelimitedString(idsDelimitedStr, DELIMITER_COMMA);
    }

    public static List<String> constructStringsByDelimitedString(String idsDelimitedStr, String delimiter) {
        String[] idsStr = idsDelimitedStr.split(delimiter);
        List<String> string = new ArrayList<>();
        for(int i = 0; i < idsStr.length; i++) {
            string.add(idsStr[i]);
        }
        return string;
    }
}
