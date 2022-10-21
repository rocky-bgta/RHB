package com.rhbgroup.dcpbo.user.search;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.info.model.bo.User;

@RestController
@RequestMapping(path = "/bo/user")
public class SearchController {
	private SearchService searchService;
	
	private static Logger logger = LogManager.getLogger(SearchController.class);
	
	public SearchController(SearchService searchService) {
		this.searchService = searchService;
	}

	@BoControllerAudit(eventCode = "20021")
    @GetMapping(value = "/search",
    		produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity search(
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "department", required = false) String department,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "userGroup", required = false) String userGroup,
			@RequestParam(value = "fromDate", required = false, defaultValue = "") String fromDate,
			@RequestParam(value = "toDate", required = false, defaultValue = "") String toDate,
			@RequestParam(value = "sortOrder", required = false, defaultValue = "") String sortOrder

	) {
		logger.debug("search()");
		logger.debug("    searchService: " + searchService);
		logger.debug("    keyword: " + keyword);
		logger.debug("    pageNum: " + pageNum);
		logger.debug("    department: " + department);
		logger.debug("    status: " + status);
		logger.debug("    userGroup: " + userGroup);
		logger.debug("    fromDate: " + fromDate);
		logger.debug("    toDate: " + toDate);
		
		Timestamp fromTimestamp = null;
		if (fromDate.length() > 0)
			fromTimestamp = convertToTimestamp(fromDate, "fromDate");
		logger.debug("    fromTimestamp: " + fromTimestamp);
		
		Timestamp toTimestamp = null;
		if (toDate.length() > 0)
			toTimestamp = convertToTimestamp(toDate, "toDate");
		logger.debug("    toTimestamp: " + toTimestamp);

    	SearchResult searchResult = new SearchResult();
    	searchResult = (SearchResult) searchService.search(keyword, pageNum, department, status, userGroup, fromTimestamp, toTimestamp, sortOrder);
    	logger.debug("    searchResult: " + searchResult);

    	HttpStatus httpStatus = HttpStatus.OK;
    	if (searchResult.getUser().size() == 0)
    		httpStatus = HttpStatus.NOT_FOUND;

    	return new ResponseEntity(searchResult, httpStatus);
	}
    
    private Timestamp convertToTimestamp(String dateString, String fieldName) {
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(User.TIMESTAMP_FORMAT);

    	Date date = null;
    	try {
    		date = simpleDateFormat.parse(dateString);
		} catch (ParseException e) {
			logger.warn("Invalid value for " + fieldName + ": " + dateString);
			throw new CommonException("80000", "Invalid value for " + fieldName + ": " + dateString, HttpStatus.INTERNAL_SERVER_ERROR);
    	}

    	return new Timestamp(date.getTime());
    }
}
