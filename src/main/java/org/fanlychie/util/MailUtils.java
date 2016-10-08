package org.fanlychie.util;

import java.io.PrintStream;
import java.net.ConnectException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * 邮件发送工具类
 * 
 * @author fanlychie
 */
public class MailUtils {

	// 账户
	private static String username = "javamail2016@163.com";

	// 密码
	private static String password = "java2016123456";

	// smtp.163.com, smtp.126.com, smtp.yeah.net, smtp.qq.com ...
	private static String host = "smtp.163.com";

	// 会话
	private static Session session;

	static {
		Properties config = new Properties();
		config.put("mail.smtp.auth", true);
		config.put("mail.smtp.host", host);
		config.put("mail.smtp.port", 25);
		session = Session.getInstance(config, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
	}

	/**
	 * 发送电子邮件
	 * 
	 * @param subject
	 *            邮件标题
	 * @param content
	 *            邮件内容
	 * @param to
	 *            收件人
	 */
	public static void send(String subject, String content, String to) {
		send(subject, content, new String[] { to });
	}

	/**
	 * 发送电子邮件
	 * 
	 * @param subject
	 *            邮件标题
	 * @param content
	 *            邮件内容
	 * @param to
	 *            收件人列表
	 */
	public static void send(String subject, String content, String[] to) {
		send(subject, content, to, new DefaultRetryHandler(3));
	}

	/**
	 * 发送电子邮件
	 * 
	 * @param subject
	 *            邮件标题
	 * @param content
	 *            邮件内容
	 * @param to
	 *            收件人列表
	 * @param retryHandler
	 *            重试处理器
	 */
	@SuppressWarnings("serial")
	public static void send(String subject, String content, String[] to, RetryHandler retryHandler) {
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			Address[] addresses = new InternetAddress[to.length];
			for (int i = 0; i < to.length; i++) {
				addresses[i] = new InternetAddress(to[i]);
			}
			message.setRecipients(Message.RecipientType.TO, addresses);
			message.setSubject(subject);
			Multipart multipart = new MimeMultipart();
			MimeBodyPart body = new MimeBodyPart();
			body.setContent(content, "text/html;charset=utf-8");
			multipart.addBodyPart(body);
			message.setContent(multipart);
			message.setSentDate(new Date());
			Transport.send(message);
		} catch (MessagingException e) {
			if (retryHandler.retryRequest(e.getCause())) {
				send(subject, content, to, retryHandler);
			} else {
				throw new RuntimeException(e) {
					public void printStackTrace(PrintStream s) {
						e.printStackTrace(s);
					}
				};
			}
		}
	}

	/**
	 * 重试处理器
	 * 
	 * @author fanlychie
	 */
	public static interface RetryHandler {

		/**
		 * 重试请求
		 * 
		 * @param e
		 *            异常对象
		 * @return true: 重新发起请求; false: 不发送请求
		 */
		boolean retryRequest(Throwable e);

	}

	/**
	 * 默认的重试处理器
	 * 
	 * @author fanlychie
	 */
	public static class DefaultRetryHandler implements RetryHandler {

		protected int retryTimes;

		public DefaultRetryHandler(int retryTimes) {
			this.retryTimes = retryTimes;
		}

		public boolean retryRequest(Throwable e) {
			if (retryTimes-- <= 0) {
				return false;
			}
			if (e instanceof ConnectException) {
				return true;
			}
			return false;
		}

	}

}