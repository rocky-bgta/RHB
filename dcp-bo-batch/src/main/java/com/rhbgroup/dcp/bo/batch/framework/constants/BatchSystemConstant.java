package com.rhbgroup.dcp.bo.batch.framework.constants;

public  final class BatchSystemConstant {

    private static final String CONSTANT_CLASS = "Constant Class";
	
    public final class ExitCode
    {
    	private ExitCode(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
        public static final int SUCCESS=0;
        public static final int FAILED=1;
    }

    public final class Statistics
    {
    	private Statistics(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
        public static final String READ_COUNT_KEY = "batch.read.count";
        public static final String WRITE_COUNT_KEY = "batch.write.count";
        public static final String SKIP_COUNT_KEY = "batch.skip.count";
        public static final String COMMIT_COUNT_KEY = "batch.commit.count";

    }

    public final class SystemFolder {
    	private SystemFolder(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
        public static final String BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY = "${dcp.bo.batch.inputfolder.path}";
        public static final String BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY = "${dcp.bo.batch.outputfolder.path}";
        public static final String BATCH_SYSTEM_FOLDER_SUCCESS_DIRECTORY = "${dcp.bo.batch.successfolder.path}";
        public static final String BATCH_SYSTEM_FOLDER_FAILED_DIRECTORY = "${dcp.bo.batch.failedfolder.path}";
    }
    
    public final class ReportFolder {
    	private ReportFolder(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
    	public static final String REPORT_FOLDER_EXPORT_DIRECTORY = "${dcp.bo.report.exportfolder.path}";
    }
    
    public final class ReportJobContextParameter {
    	private ReportJobContextParameter(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
        public static final String REPORT_JOB_PARAMETER_REPORT_FILE_PATH_KEY = "report.file.path";
        public static final String REPORT_JOB_PARAMETER_REPORT_FILES_PATH_KEY = "report.files.path";
        public static final String REPORT_JOB_PARAMETER_REPORT_TARGET_PATH_KEY = "report.target.path";

        public static final String REPORT_JOB_PARAMETER_REPORT_FILE_SOURCE_PATH_KEY = "report.source.path";
        public static final String REPORT_UNIT_URI = "report.unit.uri";
        public static final String REPORT_BILLER_ACCOUNT_NUMBER = "report.biller.account.no";
        public static final String REPORT_BILLER_CODE = "report.biller.code";


    }

    public final class BatchJobParameter {
    	private BatchJobParameter(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
    	public static final String BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY = "batch.system.date";
        public static final String BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT = "yyyy-MM-dd";
        public static final String BATCH_JOB_PARAMETER_DB_IBG_REJECT_LAST_PROCESSED_SUCCESS_JOB_EXECUTION_ID = "ibg.reject.last.processed.success.job.execution.id";
        public static final String BATCH_JOB_PARAMETER_DB_DAYS_TO_INACTIVE_USER = "days_to_inactive_user";
        public static final String BATCH_JOB_PARAMETER_DB_DAYS_TO_DELETE_USER= "days_to_delete_user";
        public static final String BATCH_JOB_PARAMETER_JOB_NAME_KEY = "jobname";
        public static final String BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY = "jobexecutionid";
        public static final String BATCH_JOB_PARAMETER_REPORT_ID_KEY = "reportid";
        public static final String BATCH_JOB_PARAMETER_BATCH_ID_KEY = "batchid";
        public static final String BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY = "jobprocessdate";
        public static final String BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_START_DATE_KEY = "startdate";
        public static final String BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_END_DATE_KEY = "enddate";
        public static final String BATCH_JOB_PARAMETER_JOB_BATCH_OFFSET_DAY_KEY = "offsetday";
        public static final String BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_FROM_TO_DATE_KEY = "jobprocessfromdatetodate";
        public static final String BATCH_JOB_PARAMETER_PAYMENT_METHOD_SAVINGS="SAVINGS";
        public static final String BATCH_JOB_PARAMETER_PAYMENT_METHOD_CURRENT="CURRENT";
        public static final String BATCH_JOB_PARAMETER_PAYMENT_METHOD_DEBIT_CARD="DEBIT_CARD";
        public static final String BATCH_JOB_PARAMETER_PAYMENT_METHOD_CREDIT_CARD="CREDIT_CARD";
        public static final String BATCH_JOB_PARAMETER_RUN_REPORT_JOB="RunReportJob";
        public static final String BATCH_JOB_PARAMETER_RUN_REPORT_WITH_DATE_RANGE_JOB="RunReportWithDateRangeJob";
        public static final String BATCH_JOB_PARAMETER_AUDIT_LOG_REPORT_JOB="AuditLogReportJob";
        public static final String BATCH_JOB_PARAMETER_EXTERNAL_SYSTEM_DATE="ProcessDate";
        public static final String BATCH_JOB_PARAMETER_KEY = "parameterkey";
        public static final String BATCH_JOB_PARAMETER_EXTRACT_MONTHLY_SUBS_JOB="ExtractMonthlySubsByStateJob";
    }

    public final class BatchJobPropertyFile {
    	private BatchJobPropertyFile(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
        public static final String BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER = "{#date}";
    }
    
    public final class BillerPaymentOutbound{
    	private BillerPaymentOutbound(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
    	public static final String BILLER_TXT_FILE = "biller.outbound.txt.file";
    	public static final String STEP_EXECUTION_STATUS = "step.status";
    	public static final String JOB_EXECUTION_STATUS = "job.status";
    }

    public final class BatchJobContextParameter {
    	private BatchJobContextParameter(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
        public static final String BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY ="ftp.to.input.file.fullpath";
        public static final String BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY ="output.to.ftp.file.fullpath";
        public static final String BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID ="last.processed.job.execution.id";

        public static final String LOADIBKBILLERPAYMENT_BILLER_TEMPLATE_NAME = "loadibkbillerpayment.biller.template.name";
        public static final String LOADIBKBILLERPAYMENT_INPUT_FILEPATH = "loadibkbillerpayment.input.file.path";
        public static final String LOADIBKBILLERPAYMENT_BILLER_PROCESS_DATE = "loadibkbillerpayment.biller.process.date";
        public static final String LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NO = "loadibkbillerpayment.biller.account.no";
        public static final String LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NAME = "loadibkbillerpayment.biller.account.name";
        public static final String LOADIBKBILLERPAYMENT_BILLER_CODE = "loadibkbillerpayment.biller.code";
    }

    public final class FTP {
    	private FTP() {
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
    	public static final String FTP_SEPARATOR = "/";
    	public static final int MAX_RETRY = 3;
    	public static final int SLEEP_TIME = 200;
    }

    public final class General {
    	private General(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
        public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
        public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
        public static final String DEFAULT_JOB_PARAMETER_DATE_FORMAT = "yyyyMMdd";
        public static final String DEFAULT_JOB_PARAMETER_FROMDATE_TODATE_REGEX = "^\\((\\d{8}),(\\d{8})\\)$";
        public static final String COMMON_DATE_DATA_FORMAT = "yyyyMMdd";
        public static final String COMMON_DATE_HEADER_FORMAT = "dd/MM/yyyy";
        public static final String RECORD_COUNT = "record.count";
    }
    
    public final class BatchIBGRejectStatusParameter{
    	private BatchIBGRejectStatusParameter(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
    	public static final String BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM = "runwindow";
    	public static final String BATCH_IBG_REJECT_STATUS_EXEC_FILE_NAME = "ibg.reject.file.path";
    	public static final String BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS = "ibg.reject.validate.status";

    }

    public final class GSTCentralizedFileUpdateParameter{
        private GSTCentralizedFileUpdateParameter(){
            throw new IllegalStateException(CONSTANT_CLASS);
        }
        public static final String BATCH_GST_CENTRALIZED_FILE_UPDATE_VALIDATING_STATUS = "gst.validate.status";
    }

    public final class JompayEmatchingParameter{
    	private JompayEmatchingParameter(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
    	public static final String JOMPAY_OUTBOUND_TXT_FILE = "jompay.outbound.txt.file";
    	public static final String JOMPAY_OUTBOUND_APP_ID_SAVINGS = "ST";
    	public static final String JOMPAY_OUTBOUND_APP_ID_CURRENT = "IM";
    	public static final String JOMPAY_OUTBOUND_APP_ID_CARD = "CC";

    }
    
    public final class MergeCISParameter{
    	private MergeCISParameter(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
    	public static final String MERGE_CIS_EXEC_FILE_NAME = "merge.cis.file.path";
    	public static final String MERGE_CIS_VALIDATING_STATUS = "merge.cis.validate.status";
    }
    
    public final class EMUnitTrustParameter{
    	private EMUnitTrustParameter(){
    		throw new IllegalStateException(CONSTANT_CLASS);
    	}
    	public static final String TARGET_DATA_SET = "ut.target.data.set";
    	public static final int STATUS_FAILED=-1;
    	public static final int STATUS_INITIAL=0;
    	public static final int STATUS_SUCCESS=1;
    }
    
    public final class ExtractDailyDataParameter {
        private ExtractDailyDataParameter(){
            throw new IllegalStateException(CONSTANT_CLASS);
        }
    	public static final String OUTPUT_FILE_LIST_FIRST_TIME_LOGIN = "output.file.list.first.time.login";
    	public static final String OUTPUT_FILE_LIST_LOGIN = "output.file.list.login";
    	public static final String OUTPUT_FILE_LIST_POD = "output.file.list.pod";
    }

    public final class ExtractSmsOtpNotificationParameter {
        private ExtractSmsOtpNotificationParameter(){
            throw new IllegalStateException(CONSTANT_CLASS);
        }
    	public static final String OUTPUT_FILE_LIST = "output.file.list";
    }

    public final class ASNBReconSettlementJobParameter {
        private ASNBReconSettlementJobParameter(){
            throw new IllegalStateException(CONSTANT_CLASS);
        }
        public static final String ASNB_OUTPUT_FILE_LIST_RECON = "asnb.output.file.list.recon";
        public static final String ASNB_OUTPUT_FILE_LIST_SETTLEMENT = "asnb.output.file.list.settlement";
        public static final String DIB_FILE_NAME_ENDING_PREFIX = "_DIB";
        public static final String ASNB_RECON_MBK = "DCP";
        public static final String ASNB_RECON_IBK = "DIB";
        public static final String ASNB_MOBILE_BANKING = "DMB";
        public static final String ASNB_INTERNET_BANKING = "DIB";
    }
}