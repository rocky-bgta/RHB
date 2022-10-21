package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.InvestigationAuditListCSVService;
import com.rhbgroup.dcpbo.customer.service.InvestigationAuditListService;
import org.apache.http.entity.ContentType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/bo/cs/customers")
public class InvestigationController {

	@Autowired
	private InvestigationAuditListService investigationAuditListService;
	@Autowired
	private InvestigationAuditListCSVService investigationAuditListCSVService;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;

	public final static String TEXT_CSV = "text/csv";
	public final static String CONTENT_DISPOSITION = "Content-Disposition";

	private static Logger logger = LogManager.getLogger(InvestigationController.class);

    @BoControllerAudit(eventCode = "60002")
	@GetMapping(path = "/audit/list")
	public BoData getInvestigationAuditListController(
						  @RequestParam(value = "eventCode", required = false, defaultValue = "0") String eventCodes,
						  @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
						  @RequestParam(value = "fromDate", required = false, defaultValue = "") String fromDate,
						  @RequestParam(value = "toDate", required = false, defaultValue = "") String toDate,
						  @RequestParam(value = "status", required = false, defaultValue = "all") String status) {

        return investigationAuditListService.listing(eventCodes, pageNum ,fromDate, toDate, status);

	}

	@BoControllerAudit(eventCode = "60003")
	@GetMapping(path = "/audit/list/csv")
	public void getCSVInvestigationAuditListController(
			@RequestParam(value = "eventCode", required = false, defaultValue = "0") String eventCodes,
			@RequestParam(value = "fromDate", required = false, defaultValue = "") String fromDate,
			@RequestParam(value = "toDate", required = false, defaultValue = "") String toDate,
			@RequestParam(value = "status", required = false, defaultValue = "all") String status) throws IOException {
		response.setContentType(TEXT_CSV);
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateNow = dateFormatter.format(new Date());
		String reportName = "auditlist" + dateNow + ".csv";
		response.setHeader(CONTENT_DISPOSITION, String.format("attachment; filename=" + reportName));
		investigationAuditListCSVService.listing(response, eventCodes ,fromDate, toDate, status);
	}
}
