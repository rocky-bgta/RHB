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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@ComponentScan(basePackages = {
	"com.rhbgroup.dcp.bo.batch.framework",
	"com.rhbgroup.dcp.bo.batch.job"})
public class BatchTestConfig extends DefaultBatchConfigurer {
    @Autowired
    private DataSource dataSource;

    @Value("${spring.batch.table-prefix}")
    private String datasourceTablePrefix;

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
	@Qualifier(ExtractAndUpdateExchangeRateJobTest.JOB_LAUNCHER_UTILS)
	public JobLauncherTestUtils getExtractAndUpdateExchangeRateJobJobLauncherTestUtils() {
		return new JobLauncherTestUtils() {
			@Override
			@Autowired
			public void setJob(@Qualifier(ExtractAndUpdateExchangeRateJobTest.JOB_NAME) Job job) {
				super.setJob(job);
			}
		};
	}

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
    @Qualifier(PremierCustomerInfoandRMCodeTaggingJobTest.JOB_LAUNCHER_UTILS)
    public JobLauncherTestUtils getPremierCustomerinfoandRMCodeTaggingJobLauncherTestUtils() {
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
    @Qualifier(ExtractDebitCardDeliveryJobTests.JOB_LAUNCHER_UTILS)
    public JobLauncherTestUtils getExtractDebitCardDeliveryJobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier(ExtractDebitCardDeliveryJobTests.JOB_NAME) Job job) {
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
    protected DataSource dataSource(
            @Value("${spring.datasource.driver-class-name}") String datasourceDriverClassName,
            @Value("${spring.datasource.url}") String datasourceUrl,
            @Value("${spring.datasource.username}") String datasourceUserName,
            @Value("${spring.datasource.password}") String datasourcePassword){
        return  DataSourceBuilder.create()
                .url(datasourceUrl)
                .driverClassName(datasourceDriverClassName)
                .username(datasourceUserName)
                .password(datasourcePassword)
                .build();
    }

//    @Bean
//	public DataSource dataSource() {
//		return new EmbeddedDatabaseBuilder()
//				.setType(EmbeddedDatabaseType.HSQL)
//				.addScript("/org/springframework/batch/core/schema-drop-hsqldb.sql")
//				.addScript("/org/springframework/batch/core/schema-hsqldb.sql")
//				.addScript("sql/a.sql")
//				.addScript("sql/b.sql")
//				.addScript("sql/c.sql")
//				.addScript("sql/d.sql")
//				.addScript("sql/y.sql")
//				.addScript("sql/z.sql")
//				.build();
//	}

    @Bean(name = "dataSourceDCP")
    protected DataSource dataSourceDCP(
            @Value("${spring.datasource-dcp.driver-class-name}") String datasourceDriverClassName,
            @Value("${spring.datasource-dcp.url}") String datasourceUrl,
            @Value("${spring.datasource-dcp.username}") String datasourceUserName,
            @Value("${spring.datasource-dcp.password}") String datasourcePassword){
        return  DataSourceBuilder.create()
                .url(datasourceUrl)
                .driverClassName(datasourceDriverClassName)
                .username(datasourceUserName)
                .password(datasourcePassword)
                .build();
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
        factory.setTablePrefix(datasourceTablePrefix);
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
