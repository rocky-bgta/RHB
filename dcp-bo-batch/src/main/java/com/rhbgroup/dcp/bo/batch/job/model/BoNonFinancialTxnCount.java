package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoNonFinancialTxnCount{
	 private int id;
	 private int userId;
	 private int cashxcessCount;
	 private String cashxcessAmt;
	 private int ola_casa_count;
	 private int update_profile;
	 private int forgot_username;
	 private int forgot_password;
	 private int change_password;
	 private int change_secure_word;
	 private int change_challenge_que;
	 private int favourite_intra_bank;
	 private int del_favourite_intrabank;
	 private int favourite_instant;
	 private int del_favourite_instant;
	 private int favourite_duitnow;
	 private int del_favourite_duitnow;
	 private int favourite_ibg;
	 private int del_favourite_ibg;
	 private int favourite_jompay;
	 private int del_favourite_jompay;
	 private int favourite_other_biller;
	 private int del_favourite_other_biller;
	 private int favourite_top_up;
	 private int del_favourite_top_up;

}