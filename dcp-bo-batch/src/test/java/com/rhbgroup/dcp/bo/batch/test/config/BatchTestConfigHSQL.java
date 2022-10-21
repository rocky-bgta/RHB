package com.rhbgroup.dcp.bo.batch.test.config;

import javax.sql.DataSource;

import com.rhbgroup.dcp.bo.batch.test.job.*;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import com.rhbgroup.dcp.bo.batch.test.job.DailyDeltaProfileSMSJobTests;
import com.rhbgroup.dcp.bo.batch.test.job.ExtractAndUpdateExchangeRateJobTest;
import com.rhbgroup.dcp.bo.batch.test.job.IBGRejectedNotificationJobTests;
import com.rhbgroup.dcp.bo.batch.test.job.JompayValidationFailureReportJobTests;
import com.rhbgroup.dcp.bo.batch.test.job.LoadIBKBillerPaymentJobTests;
import com.rhbgroup.dcp.bo.batch.test.job.MergeCISJobTests;

@Configuration
@EnableBatchProcessing
@ComponentScan(basePackages = {
	"com.rhbgroup.dcp.bo.batch.framework",
	"com.rhbgroup.dcp.bo.batch.job"})
public class BatchTestConfigHSQL extends DefaultBatchConfigurer {

	@Autowired
	@Qualifier("dataSource")
    private DataSource dataSource;

//    @Value("${spring.batch.table-prefix}")
//    private String datasourceTablePrefix;

	@Bean
    @Lazy
    @Qualifier("SampleFTPFileToDBJobJobLauncherTestUtils")
    public JobLauncherTestUtils getSampleFTPFileToDBJobJobLauncherTestUtils() {
       return new JobLauncherTestUtils() {
           @Override
           @Autowired
           public void setJob(@Qualifier("SampleFTPFileToDBJob") Job job) {
               super.setJob(job);
           }
       };
    }

    @Bean
    @Lazy
    @Qualifier("SampleDBToFTPFileJobJobLauncherTestUtils")
    public JobLauncherTestUtils getSampleDBToFTPFileJobJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier("SampleDBToFTPFileJob") Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
    @Qualifier("SampleRunReportJobJobLauncherTestUtils")
    public JobLauncherTestUtils getSampleRunReportJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier("SampleRunReportJob") Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
	@Qualifier(LoadIBKBillerPaymentJobTests.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getLoadIBKBillerPaymentJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(LoadIBKBillerPaymentJobTests.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}

    @Bean
    @Lazy
    @Qualifier("InitialBatchConfigJobJobLauncherTestUtils")
    public JobLauncherTestUtils getInitialBatchConfigJobJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier("InitialBatchConfigJob") Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
    @Qualifier("PrepaidReloadExtractionJobJobLauncherTestUtils")
    public JobLauncherTestUtils getPrepaidReloadExtractionJobJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier("PrepaidReloadExtractionJob") Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
    @Qualifier("BillerPaymentFileJobJobLauncherTestUtils")
    public JobLauncherTestUtils getBillerPaymentFileJobJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier("BillerPaymentFileJob") Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
	@Qualifier("UpdateIBGRejectStatusJobLauncherTestUtils")
	public JobLauncherTestUtils getUpdateIBGRejectStatusJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier("UpdateIBGRejectedStatusJob") Job job) {
				super.setJob(job);
			}
		};
	}

    @Bean
    @Lazy
    @Qualifier("ExtractCustomerProfileJobLauncherTestUtils")
    public JobLauncherTestUtils getExtractCustomerProfileJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier("ExtractCustomerProfileJob") Job job) {
                super.setJob(job);
            }
        };
    }


    @Bean
    @Lazy
    @Qualifier("NADDeregistrationRequestsbyParticipantsJobTestsUtils")
    public JobLauncherTestUtils getNADDeregistrationRequestsbyParticipantsJobTestsUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier("NADDeregistrationRequestsbyParticipantsJob") Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
    @Qualifier("JompayEmatchingReportJobJobLauncherTestUtils")
    public JobLauncherTestUtils getJompayEmatchingReportJobJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier("JompayEmatchingReportJob") Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
	@Qualifier(IBGRejectedNotificationJobTests.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getIBGRejectedNotificationJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(IBGRejectedNotificationJobTests.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}

    @Bean
    @Lazy
	@Qualifier(JompayValidationFailureReportJobTests.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getJompayValidationFailureReportJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(JompayValidationFailureReportJobTests.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}

	@Bean
	@Lazy
	@Qualifier(ExtractAndUpdateExchangeRateJobTest.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getExtractAndUpdateExchangeRateJobTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(ExtractAndUpdateExchangeRateJobTest.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}

    @Bean
    @Lazy
    @Qualifier("UpdateCustomerProfileJobLauncherTestUtils")
    public JobLauncherTestUtils getUpdateCustomerProfileJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier("UpdateCustomerProfileJob") Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
    @Qualifier(RunReportJobTests.JOB_LAUNCHER_UTILS)
    public JobLauncherTestUtils getRunReportJobLaucherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier(RunReportJobTests.JOB_NAME) Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
    @Qualifier(RunReportWithDateRangeJobTests.JOB_LAUNCHER_UTILS)
    public JobLauncherTestUtils getRRunReportWithDateRangeJobLaucherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier(RunReportWithDateRangeJobTests.JOB_NAME) Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
    @Qualifier(GSTCentralizedFileUpdateJobTests.JOB_LAUNCHER_UTILS)
    public JobLauncherTestUtils getGSTCentralizedFileUpdateJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier(GSTCentralizedFileUpdateJobTests.JOB_NAME) Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
	@Qualifier(DailyDeltaProfileSMSJobTests.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getDailyDeltaProfileSMSJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(DailyDeltaProfileSMSJobTests.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}

    @Bean
    @Lazy
	@Qualifier(MergeCISJobTests.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getMergeCISJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(MergeCISJobTests.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}

    @Bean
    @Lazy
    @Qualifier(BranchCodeUpdateJobTests.JOB_LAUNCHER_UTILS)
    public JobLauncherTestUtils getBranchCodeUpdateJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier(BranchCodeUpdateJobTests.JOB_NAME) Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
    @Qualifier(BillerPaymentOutboundFileJobTests.JOB_LAUNCHER_UTILS)
    public JobLauncherTestUtils getBillerPaymentOutboundFileJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier(BranchCodeUpdateJobTests.JOB_NAME) Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
    @Qualifier(LoadIBKJompayEmatchJobTest.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getLoadIBKJompayEmatchJobTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(LoadIBKJompayEmatchJobTest.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}

    @Bean
    @Lazy
    @Qualifier(PrepaidReloadFileFromIBKJobTests.JOB_LAUNCHER_UTILS)
    public JobLauncherTestUtils getPrepaidReloadFileFromIBKJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier(PrepaidReloadFileFromIBKJobTests.JOB_NAME) Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
    @Qualifier(PremierCustomerInfoandRMCodeTaggingJobTest.JOB_LAUNCHER_UTILS)
    public JobLauncherTestUtils getPremierCustomerInfoandRMCodeTaggingJobTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier(PremierCustomerInfoandRMCodeTaggingJobTest.JOB_NAME) Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
    @Qualifier(UserMaintenanceAutoAgingJobTests.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getUserMaintenanceAutoAgingJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(UserMaintenanceAutoAgingJobTests.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}

    @Bean
    @Lazy
	@Qualifier(LoadIBKJompayFailureValidationExtractionJobTests.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getLoadIBKJompayFailureValidationExtractionJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(LoadIBKJompayFailureValidationExtractionJobTests.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}

    @Bean
    @Lazy
    @Qualifier(SnapshotBoUsersGroupJobTests.JOB_LAUNCHER_UTILS)
    public JobLauncherTestUtils getSnapshotBoUsersGroupJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier(SnapshotBoUsersGroupJobTests.JOB_NAME) Job job) {
                super.setJob(job);
            }
        };
    }

    @Bean
    @Lazy
	@Qualifier(PushCardlinkNotificationsProcessorJobTests.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getPushCardlinkNotificationsProcessorJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(PushCardlinkNotificationsProcessorJobTests.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}

	@Bean
	@Lazy
	@Qualifier(LoadCardlinkNotificationsJobTest.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getLoadCardlinkNotificationsJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(LoadCardlinkNotificationsJobTest.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}
	
    @Bean
    @Lazy
	@Qualifier(LoadEMUnitTrustJobTest.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getLoadEMUnitTrustJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(LoadEMUnitTrustJobTest.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}
    
    @Bean
    @Lazy
	@Qualifier(ExtractCardlinkNotificationsProcessorJobTests.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getExtractCardlinkNotificationsProcessorJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(ExtractCardlinkNotificationsProcessorJobTests.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}
    
    @Bean
    @Lazy
    @Qualifier(ExternalInterfacesCheckJobTest.JOB_LAUNCHER_UTILS)
    public JobLauncherTestUtils getExternalInterfacesCheckJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier(ExternalInterfacesCheckJobTest.JOB_NAME) Job job) {
                super.setJob(job);
            }
        };
    }
    
    @Bean
	@Lazy
	@Qualifier(LoadMassNotificationsJobTests.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getLoadMassNotificationsJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(LoadMassNotificationsJobTests.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
    }
			
    @Bean
    @Lazy
	@Qualifier(PushMassNotificationsProcessorJobTests.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getPushMassNotificationsProcessorJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(PushMassNotificationsProcessorJobTests.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}
    
	@Bean
	@Qualifier("dataSource")
    protected DataSource dataSource() {
		EmbeddedDatabaseBuilder embeddedDatabaseBuilder = new EmbeddedDatabaseBuilder();
		return embeddedDatabaseBuilder
			.addScript("classpath:org/springframework/batch/core/schema-drop-hsqldb.sql")
			.addScript("classpath:org/springframework/batch/core/schema-hsqldb.sql")
			.addScript("classpath:sql/create_test_tables.sql")
			.addScript("classpath:sql/insert_default_data.sql")
			.setType(EmbeddedDatabaseType.HSQL)
			.build();
    }

	@Bean
	@Qualifier("dataSourceDCP")
    protected DataSource dataSourceDCP() {
		return dataSource;
    }

	@Bean
    public JdbcTemplate jdbcTemplate(){
        return new JdbcTemplate(dataSource);
    }

	@Autowired
    private PlatformTransactionManager transactionManager;

    @Override
    protected JobRepository createJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
//        factory.setTablePrefix(datasourceTablePrefix);
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
