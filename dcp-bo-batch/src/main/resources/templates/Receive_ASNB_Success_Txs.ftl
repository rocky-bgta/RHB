<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html  xmlns:th="http://www.thymeleaf.org" xmlns="http://www.w3.org/1999/xhtml">
 
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <title>RHB Bank</title>
  <style type="text/css">
  body {margin: 0; padding: 0; min-width: 100%!important;}
  img {height: auto;}
  .content {width: 100%; max-width: 600px;}
  .header {padding: 40px 30px 20px 30px;}
  .innerpadding {padding: 30px 30px 30px 30px;}
  .borderbottom {border-bottom: 1px solid #f2eeed;}
  .subhead {font-size: 15px; color: #ffffff; font-family: sans-serif; letter-spacing: 10px;}
  .h1, .h2, .bodycopy {color: #153643; font-family: sans-serif;}
  .h1 {font-size: 33px; line-height: 38px; font-weight: bold;}
  .h2 {padding: 0 0 15px 0; font-size: 24px; line-height: 28px; font-weight: bold;}
  .bodycopy {font-size: 16px; line-height: 22px;}
  .button {text-align: center; font-size: 18px; font-family: sans-serif; font-weight: bold; padding: 0 30px 0 30px;}
  .button a {color: #ffffff; text-decoration: none;}
  .footer {padding: 20px 30px 15px 30px;}
  .footercopy {font-family: sans-serif; font-size: 14px; color: #ffffff;}
  .footercopy a {color: #ffffff; text-decoration: underline;}

  @media only screen and (max-width: 550px), screen and (max-device-width: 550px) {
  body[yahoo] .hide {display: none!important;}
  body[yahoo] .buttonwrapper {background-color: transparent!important;}
  body[yahoo] .button {padding: 0px!important;}
  body[yahoo] .button a {background-color: #e05443; padding: 15px 15px 13px!important;}
  body[yahoo] .unsubscribe {display: block; margin-top: 20px; padding: 10px 50px; background: #2f3942; border-radius: 5px; text-decoration: none!important; font-weight: bold;}
  }

  /*@media only screen and (min-device-width: 601px) {
    .content {width: 600px !important;}
    .col425 {width: 425px!important;}
    .col380 {width: 380px!important;}
    }*/

  </style>
</head>

<body yahoo bgcolor="#f6f8f1">
<table width="100%" bgcolor="#f6f8f1" border="0" cellpadding="0" cellspacing="0">
<tr>
  <td>    
    <table bgcolor="#ffffff" class="content" align="center" cellpadding="0" cellspacing="0" border="0">
      <tr>
        <td class="innerpadding borderbottom">
		 
		  <table>
		  	    <tr>
			<td style="padding-bottom: 25px;  class="innerpadding bodycopy">
			Hi Ops & Settlement team, 
			</td>
		  </tr>
		  <tr><td></td></tr>
			<tr>
			<td style="padding-bottom: 25px;  class="innerpadding bodycopy">
			Please be informed that Successful Transaction Listing file was received from ASNB today ${time}.
			</td>
		  </tr>
		  <tr><td></td></tr>
		  <tr>
			<td style="padding-bottom: 25px;  class="innerpadding bodycopy">
			ASNB successful transaction <u>is match </u> with DCP transaction.
			</td>
		  </tr>
		   <tr><td></td></tr>  
		   <tr>
			<td class="innerpadding bodycopy">
			Below is the summary and variance for the Subscription:
			</td>
		  </tr>
		  <tr>
			<td></td>
			</tr>
		  </table>
        </td>
      </tr>
      <tr>
        <td class="innerpadding borderbottom">
          
           <table class="col380" align="center" border="1" cellpadding="0" cellspacing="0" style="width: 100%; max-width: 380px;">
		  <h1 class="innerpadding borderbottom" style="font-weight: normal;">Summary:</h1>
		  <tr>
		  <th align="center">Fund</th>
		  <th align="center">Number of Transaction (RHB)</th>
		  <th align="center">Amount (MYR)</th>
		  <th align="center">Number of Transaction (ASNB)</th>
		  <th align="center">Amount (MYR)</th>
		  </tr>
		  <#list summary as funds>
		   <tr align="center">
		  
		  <td style="width: 125px;text-align: center;padding-right: 5px;"> ${funds.fund}</td>
		  <td align="right" style="width: 125px;text-align: right;padding-right: 5px;"> ${funds.tran} </td>
   		  <td align="right" style="width: 125px;text-align: right;padding-right: 5px;"> ${funds.totalAmount}</td>
   		  <td align="right" style="width: 125px;text-align: right;padding-right: 5px;"> ${funds.asnbTran} </td>
   		  <td align="right" style="width: 125px;text-align: right;padding-right: 5px;"> ${funds.asnbTotalAmount} </td>
   		  
		  </tr>
		  </#list>
		  </tr>
		  </td>
		  </tr>
		  
          </table>
         
        </td>
      </tr>
      
       <tr>
        <td class="innerpadding borderbottom">
         <table class="col380" align="center" border="1" cellpadding="0" cellspacing="0" style="width: 100%; max-width: 380px;">
		  <h1 class="innerpadding borderbottom" style="font-weight: normal;">Variance:</h1>
		  <tr>
		  <th align="center">UH/Beneficiary ASNB ID</th>
		  <th align="center">Fund</th>
		  <th align="center">Date</th>
		  <th align="center">Time</th>
		  <th align="center">Bank Ref No</th>
		  <th align="center">Asnb Ref No</th>
		  <th align="center">Amount</th>
		  </tr>
		  <#list variance as funds>
		   <tr align="center">
		  
		  <td> ${funds.beneficiaryAhnbId}</td>
		  <td> ${funds.fund} </td>
   		  <td> ${funds.date}</td>
   		  <td> ${funds.time} </td>
   		  <td> ${funds.bankRefNo} </td>
		  <td> ${funds.asnbRefNo} </td>
		  <td align="right" style="padding-right: 5px;"> ${funds.amount} </td>
   		  
		  </tr>
		  </#list>
		  </tr>
		  </td>
		  </tr>
		  
          </table>
          
        </td>
      </tr>
 
    </table>
    
    </td>
  </tr>
</table>
</body>
</html>
