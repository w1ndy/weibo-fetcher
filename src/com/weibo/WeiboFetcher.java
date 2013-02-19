package com.weibo;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class WeiboFetcher {
	private static int delay = 1000;
	private static String outputFileName = "data.xml";
	private static String cookie = null;
	private static String uid = null;
	
	public static boolean parseArguments(String[] args) {
		boolean d = false, o = false;
		int filled = 0;
		for(int i = 0; i < args.length; i++) {
			if(d) {
				delay = Integer.valueOf(args[i]);
				d = false;
			} else if(o) {
				outputFileName = args[i];
				o = false;
			} else {
				if(args[i].equals("-o") || args[i].equals("--output")) {
					o = true;
				} else if(args[i].equals("-d") || args[i].equals("--delay")) {
					d = true;
				} else {
					switch(filled) {
					case 0:
						uid = args[i];
						break;
					case 1:
						cookie = args[i];
						break;
					default:
						return false;
					}
					filled++;
				}
			}
		}
		if(filled != 2) return false;
		return true;
	}
	
	public static void main(String[] args) {
		if(!parseArguments(args)) {
			System.out.println("USAGE: java -jar WeiboFetcher UID COOKIE [-o XML_FILE_NAME] [-d DELAY]");
			return ;
		}
		
		Spider spider = new Spider(cookie);
		spider.setDelay(delay);
		Document output = spider.crawl(uid);
		if(output != null) {
			try {
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(output);
				StreamResult result = new StreamResult(outputFileName);
				transformer.transform(source, result);
				System.out.println("Saved to " + outputFileName);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		}
	}
}