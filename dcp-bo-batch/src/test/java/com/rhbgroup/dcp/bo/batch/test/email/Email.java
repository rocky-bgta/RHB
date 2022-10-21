package com.rhbgroup.dcp.bo.batch.test.email;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Email {

    public static void main(String[] args) throws MessagingException {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("172.30.81.113");
        mailSender.setPort(25);
        mailSender.setUsername("bgiuatdom/DCPBO2");
        mailSender.setPassword("Outlook1");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "TRUE");
        props.put("mail.smtp.starttls.enable", "TRUE");
        props.put("mail.smtp.starttls.required", "TRUE");
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.ssl.trust", "*");
//        props.put("mail.smtp.host", "172.30.81.113");
//        props.put("mail.smtp.auth", "false");
//        props.put("mail.smtp.port", "25");
//        props.put("mail.smtp.auth.mechanisms","NTLM");
//        props.put("mail.smtp.auth.ntlm.domain","bgiuatdom");
//        props.put("mail.smtp.socketFactory.port", "465");
//        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
//                StandardCharsets.UTF_8.name());
////        helper.setFrom("DCPBO2@bgiuatdom.rhbgroup.com");
//        helper.setFrom("mvp5@bgiuatdom.rhbgroup.com");
////        helper.setTo("DCPBO1@bgiuatdom.rhbgroup.com");
//        helper.setTo("Mvp1@bgiuatdom.rhbgroup.com");
//        helper.setSubject("hello");
//        helper.setText("tatabalalu");
//        mailSender.send(message);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html  xmlns:th=\"http://www.thymeleaf.org\" xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                " \n" +
                "<head>\n" +
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "  <title>RHB Bank</title>\n" +
                "  <style type=\"text/css\">\n" +
                "  body {margin: 0; padding: 0; min-width: 100%!important;}\n" +
                "  img {height: auto;}\n" +
                "  .content {width: 100%; max-width: 600px;}\n" +
                "  .header {padding: 40px 30px 20px 30px;}\n" +
                "  .innerpadding {padding: 30px 30px 30px 30px;}\n" +
                "  .borderbottom {border-bottom: 1px solid #f2eeed;}\n" +
                "  .subhead {font-size: 15px; color: #ffffff; font-family: sans-serif; letter-spacing: 10px;}\n" +
                "  .h1, .h2, .bodycopy {color: #153643; font-family: sans-serif;}\n" +
                "  .h1 {font-size: 33px; line-height: 38px; font-weight: bold;}\n" +
                "  .h2 {padding: 0 0 15px 0; font-size: 24px; line-height: 28px; font-weight: bold;}\n" +
                "  .bodycopy {font-size: 16px; line-height: 22px;}\n" +
                "  .button {text-align: center; font-size: 18px; font-family: sans-serif; font-weight: bold; padding: 0 30px 0 30px;}\n" +
                "  .button a {color: #ffffff; text-decoration: none;}\n" +
                "  .footer {padding: 20px 30px 15px 30px;}\n" +
                "  .footercopy {font-family: sans-serif; font-size: 14px; color: #ffffff;}\n" +
                "  .footercopy a {color: #ffffff; text-decoration: underline;}\n" +
                "\n" +
                "  @media only screen and (max-width: 550px), screen and (max-device-width: 550px) {\n" +
                "  body[yahoo] .hide {display: none!important;}\n" +
                "  body[yahoo] .buttonwrapper {background-color: transparent!important;}\n" +
                "  body[yahoo] .button {padding: 0px!important;}\n" +
                "  body[yahoo] .button a {background-color: #e05443; padding: 15px 15px 13px!important;}\n" +
                "  body[yahoo] .unsubscribe {display: block; margin-top: 20px; padding: 10px 50px; background: #2f3942; border-radius: 5px; text-decoration: none!important; font-weight: bold;}\n" +
                "  }\n" +
                "\n" +
                "  /*@media only screen and (min-device-width: 601px) {\n" +
                "    .content {width: 600px !important;}\n" +
                "    .col425 {width: 425px!important;}\n" +
                "    .col380 {width: 380px!important;}\n" +
                "    }*/\n" +
                "\n" +
                "  </style>\n" +
                "</head>\n" +
                "\n" +
                "<body yahoo bgcolor=\"#f6f8f1\">\n" +
                "<table width=\"100%\" bgcolor=\"#f6f8f1\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "<tr>\n" +
                "  <td>    \n" +
                "    <table bgcolor=\"#ffffff\" class=\"content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "      <tr>\n" +
                "        <td class=\"innerpadding borderbottom\">\n" +
                "\t\t \n" +
                "\t\t  <table>\n" +
                "\t\t  \t    <tr>\n" +
                "\t\t\t<td  style=\"padding-bottom: 25px;  class=\"innerpadding bodycopy\">\n" +
                "\t\t\tHi Ops & Settlement team, \n" +
                "\t\t\t</td>\n" +
                "\t\t  </tr>\n" +
                "\t\t<tr><td></td></tr>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t<td  style=\"padding-bottom: 25px;  class=\"innerpadding bodycopy\">\n" +
                "\t\t\tPlease be informed that empty Successful Transaction Listing file being received from ASNB today ${time}. \n" +
                "\t\t\t</td>\n" +
                "\t\t  </tr>\n" +
                "\t\t<tr><td></td></tr>\n" +
                "\t\t<tr>\n" +
                "\t\t<td class=\"innerpadding bodycopy\">\n" +
                "\t\tThere is none successful transaction at bank side on the same day.\n" +
                "Hence no settlement has been done for the day.\n" +
                "\n" +
                "\t\t</td>\n" +
                "\t  </tr>\n" +
                "\t  <tr>\n" +
                "\t\t\t<td></td>\n" +
                "\t\t\t</tr>\n" +
                "     \n" +
                "                              </table>\n" +
                "              </td>\n" +
                "            </tr>\n" +
                "          </table>\n" +
                "        </td>\n" +
                "      </tr>\n" +
                "    </table>   </td>\n" +
                " </tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>\n";
        helper.setTo("DCPBO1@bgiuatdom.rhbgroup.com");
        helper.setText(html, true);
        helper.setSubject("SUBJECT");
        helper.setFrom("DCPBO2@bgiuatdom.rhbgroup.com");



//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo("DCPBO1@bgiuatdom.rhbgroup.com");
//        message.setFrom("DCPBO2@bgiuatdom.rhbgroup.com");
//        message.setSubject("hello3");
//        message.setText("tatabalal3");
        mailSender.send(message);

    }
}

