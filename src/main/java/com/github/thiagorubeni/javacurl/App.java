package com.github.thiagorubeni.javacurl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

/**
 * Main class
 *
 */
public class App {
	public static void main(String[] args) {
		Options options = new Options();

		Option option = Option.builder("?").longOpt("help").desc("Prints help").build();
		options.addOption(option);
		option = Option.builder("m").longOpt("method").hasArg().desc("Method. Default is " + HttpGet.METHOD_NAME)
				.build();
		options.addOption(option);
		option = Option.builder("c").longOpt("content").hasArg().desc("Content to send; overwrites \"f\"").build();
		options.addOption(option);
		option = Option.builder("f").longOpt("file").hasArg().desc("File with content to send").build();
		options.addOption(option);
		option = Option.builder("H").hasArgs().desc("Header name-value pair parameters (\"-Hname=value\")").build();
		options.addOption(option);
		option = Option.builder("s").longOpt("status").desc("Print HTTP status code").build();
		options.addOption(option);
		option = Option.builder("keystore").hasArg().desc("Key store path").build();
		options.addOption(option);
		option = Option.builder("keystoretype").hasArg().desc("Key store type; default is " + KeyStore.getDefaultType())
				.build();
		options.addOption(option);
		option = Option.builder("keystorepass").hasArg().desc("Key store password").build();
		options.addOption(option);
		option = Option.builder("truststore").hasArg().desc("Trust store path").build();
		options.addOption(option);
		option = Option.builder("truststoretype").hasArg()
				.desc("Trust store type; default is " + KeyStore.getDefaultType()).build();
		options.addOption(option);
		option = Option.builder("truststorepass").hasArg().desc("Trust store password").build();
		options.addOption(option);
		option = Option.builder("debug").desc("Debug mode").build();
		options.addOption(option);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);

			if (cmd.getArgList().size() != 1) {
				throw new ParseException("One and only one URL must to be provided");
			}
			String url = cmd.getArgList().get(0);

			String method = cmd.hasOption("m") ? cmd.getOptionValue("m").toUpperCase() : "GET";
			Properties header = cmd.getOptionProperties("H");
			String file = cmd.hasOption("f") ? cmd.getOptionValue("f") : null;
			String content = cmd.hasOption("c") ? cmd.getOptionValue("c") : null;
			String keyStore = cmd.hasOption("keystore") ? cmd.getOptionValue("keystore") : null;
			String keyStoreType = cmd.hasOption("keystoretype") ? cmd.getOptionValue("keystoretype")
					: KeyStore.getDefaultType();
			char[] keyStorePass = cmd.hasOption("keystorepass") ? cmd.getOptionValue("keystorepass").toCharArray()
					: null;
			if (keyStore != null && keyStorePass == null) {
				throw new ParseException("keystore parameter depends keystorepass parameter");
			}
			if (keyStore != null && keyStorePass == null) {
				throw new ParseException("keystorepass parameter depends keystore parameter");
			}
			String trustStore = cmd.hasOption("truststore") ? cmd.getOptionValue("truststore") : null;
			String trustStoreType = cmd.hasOption("truststoretype") ? cmd.getOptionValue("truststoretype")
					: KeyStore.getDefaultType();
			char[] trustStorePass = cmd.hasOption("truststorepass") ? cmd.getOptionValue("truststorepass").toCharArray()
					: null;
			if (trustStore != null && trustStorePass == null) {
				throw new ParseException("truststore parameter depends truststorepass parameter");
			}
			if (trustStore != null && trustStorePass == null) {
				throw new ParseException("truststorepass parameter depends truststore parameter");
			}
			if (cmd.hasOption("debug")) {
				System.getProperties().setProperty("javax.net.debug", "all");
			}

			HttpUriRequest httpRequest = null;
			if (method.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
				httpRequest = new HttpDelete(url);
			} else if (method.equalsIgnoreCase(HttpGet.METHOD_NAME)) {
				httpRequest = new HttpGet(url);
			} else if (method.equalsIgnoreCase(HttpHead.METHOD_NAME)) {
				httpRequest = new HttpHead(url);
			} else if (method.equalsIgnoreCase(HttpOptions.METHOD_NAME)) {
				httpRequest = new HttpOptions(url);
			} else if (method.equalsIgnoreCase(HttpPatch.METHOD_NAME)) {
				HttpPatch httpPatch = new HttpPatch(url);
				if (file != null) {
					httpPatch.setEntity(new FileEntity(new File(file)));
				}
				if (content != null) {
					httpPatch.setEntity(new ByteArrayEntity(content.getBytes()));
				}
				httpRequest = httpPatch;
			} else if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
				HttpPost httpPost = new HttpPost(url);
				if (file != null) {
					httpPost.setEntity(new FileEntity(new File(file)));
				}
				if (content != null) {
					httpPost.setEntity(new ByteArrayEntity(content.getBytes()));
				}
				httpRequest = httpPost;
			} else if (method.equalsIgnoreCase(HttpPut.METHOD_NAME)) {
				HttpPut httpPut = new HttpPut(url);
				if (file != null) {
					httpPut.setEntity(new FileEntity(new File(file)));
				}
				if (content != null) {
					httpPut.setEntity(new ByteArrayEntity(content.getBytes()));
				}
				httpRequest = httpPut;
			} else if (method.equalsIgnoreCase(HttpTrace.METHOD_NAME)) {
				httpRequest = new HttpTrace(url);
			} else {
				throw new ParseException("Invalid HTTP method");
			}
			// Default content type; will be overwritten if corresponding header
			// is set
			httpRequest.setHeader("Content-type", "application/x-www-form-urlencoded");
			// Write custom headers
			for (String headerKey : header.stringPropertyNames()) {
				httpRequest.addHeader(headerKey, header.getProperty(headerKey));
			}
			CloseableHttpClient httpClient;
			if (!httpRequest.getURI().getScheme().equalsIgnoreCase("https")) {
				httpClient = HttpClients.createDefault();
			} else if (keyStore != null || trustStore != null) {
				if (keyStore != null) {
					System.setProperty("javax.net.ssl.keyStore", keyStore);
					System.setProperty("javax.net.ssl.keyStoreType", keyStoreType);
					System.setProperty("javax.net.ssl.keyStorePassword", new String(keyStorePass));
				}
				if (trustStore != null) {
					System.setProperty("javax.net.ssl.trustStore", trustStore);
					System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
					System.setProperty("javax.net.ssl.trustStorePassword", new String(trustStorePass));
				}

				SSLContextBuilder sslContextBuilder = SSLContexts.custom();
				if (keyStore != null) {
					KeyStore ks = KeyStore.getInstance(keyStoreType);
					ks.load(new FileInputStream(keyStore), keyStorePass);
					sslContextBuilder = sslContextBuilder.loadKeyMaterial(ks, keyStorePass);
				}
				if (trustStore != null) {
					KeyStore ks = KeyStore.getInstance(trustStoreType);
					ks.load(new FileInputStream(trustStore), trustStorePass);
					sslContextBuilder = sslContextBuilder.loadTrustMaterial(ks, TrustSelfSignedStrategy.INSTANCE);
				}
				SSLContext sslContext = sslContextBuilder.build();
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
						new DefaultHostnameVerifier());
				httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
			} else {
				SSLContext sslcontext = SSLContexts.createDefault();
				sslcontext.init(null, new X509TrustManager[] { new X509TrustManager() {

					public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
						// TODO Auto-generated method stub
					}

					public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
						// TODO Auto-generated method stub
					}

					public X509Certificate[] getAcceptedIssuers() {
						// TODO Auto-generated method stub
						return new X509Certificate[] {};
					}

				} }, new SecureRandom());
				SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext,
						new DefaultHostnameVerifier());
				httpClient = HttpClients.custom().setSSLSocketFactory(factory).build();
			}

			CloseableHttpResponse httpResponse = httpClient.execute(httpRequest);
			// Print status code
			if (cmd.hasOption("s")) {
				System.out.println("Status: " + httpResponse.getStatusLine().getStatusCode());
			}
			try {
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					System.out.println(EntityUtils.toString(entity));
				}
			} finally {
				httpResponse.close();
			}
		} catch (ParseException e) {
			if (cmd != null && !cmd.hasOption("?")) {
				e.printStackTrace();
			}
			HelpFormatter formater = new HelpFormatter();
			formater.printHelp("curl [OPTIONS] <URL>", options);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			System.out.println("Socket exception: " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
