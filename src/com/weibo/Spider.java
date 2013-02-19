package com.weibo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Spider {
	public static String WeiboUrl = 
			"http://weibo.com/aj/mblog/mbloglist?_wv=5&page=%d&uid=%s";
	public static String WeiboUrlSecond = 
			"http://weibo.com/aj/mblog/mbloglist?_wv=5&pre_page=%d&page=%d&uid=%s";
	public static String WeiboUrlThird = 
			"http://weibo.com/aj/mblog/mbloglist?_wv=5&pre_page=%d&page=%d&pagebar=1&uid=%s";
	public static String CommentUrl = 
			"http://weibo.com/aj/comment/big?_wv=5&id=%s&page=%d";
	public static String UA = 
			"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.27 (KHTML, like Gecko) Chrome/26.0.1384.2 Safari/537.27";
	
	private HttpClient client;
	private String cookie;
	private int total, delay; 
	
	public Spider(String cookie) {
		this.cookie = cookie;
		client = new DefaultHttpClient();
		delay = 1000;
	}
	
	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	public Document crawl(String targetUID) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			return null;
		}
		
		Document doc = docBuilder.newDocument();
		Element root = doc.createElement("UID_" + targetUID);
		doc.appendChild(root);
		System.out.println("[INFO] Spider launched.");

		total = 0;
		HttpResponse resp;
		for(int i = 1; i <= 128; i++) {
			System.out.println("[INFO] Processing page " + i + " part 1...");
			resp = connect(String.format(WeiboUrl, i, targetUID));
			if(resp != null) 
				if(!processResponse(resp, doc, root))
					return doc;
			System.out.println("[INFO] Processing page " + i + " part 2...");
			resp = connect(String.format(WeiboUrlSecond, i, i, targetUID));
			if(resp != null) 
				if(!processResponse(resp, doc, root))
					return doc;
			System.out.println("[INFO] Processing page " + i + " part 3...");
			resp = connect(String.format(WeiboUrlThird, i, i, targetUID));
			if(resp != null) 
				if(!processResponse(resp, doc, root))
					return doc;
			System.out.println("[INFO] Sleep " + delay + "ms...");
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return doc;
	}
	
	private HttpResponse connect(String url) {
		HttpParams params = new BasicHttpParams();
		params.setParameter("http.protocol.handle-redirects", false);
		
		HttpResponse resp;
		HttpGet request = new HttpGet(url);
		request.setParams(params);
		request.setHeader("Cookie", cookie);
		request.setHeader("User-Agent", UA);
		try {
			System.out.println("[INFO] Fetching data from " + url + "...");
			resp = client.execute(request);
			System.out.println("[INFO] " + resp.getEntity().getContentLength() + "B fetched.");
			return resp;
		} catch (IOException e) {
			System.out.println("[ERROR] Fetcher failed.");
			e.printStackTrace();
			return null;
		}
	}
	
	private boolean processResponse(HttpResponse resp, Document doc, Element root) {
		if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			System.out.println("[INFO] HTTP Status OK.");
			System.out.println("[INFO] Extracting html page...");
			String html = extractHtml(resp);
			if(html == null) return false;
			System.out.println("[INFO] " + html.length() + "B html page extracted.");
			if(html.length() < 500) {
				System.out.println("[INFO] EOF reached, task completed.");
				return false;
			} else {
				System.out.println("[INFO] Parsing html page...");
				try {
					Parser parser = new Parser(html);
					NodeList weibo_list = parser.extractAllNodesThatMatch(
							new HasAttributeFilter("action-type", "feed_list_item"));
					System.out.println("[INFO] " + weibo_list.size() + " entries detected.");
					SimpleNodeIterator iter = weibo_list.elements();
					while(iter.hasMoreNodes()) {
						System.out.println("[INFO] processing entry #" + (++total) + "...");
						Element elem = extractContent(iter.nextNode(), doc);
						if(elem == null) {
							System.out.println("[ERROR] Data extraction failed.");
							return false;
						}
						root.appendChild(elem);
					}
					if(weibo_list.size() != 15) return false;
				} catch (ParserException e) {
					System.out.println("[ERROR] Parser failed.");
					e.printStackTrace();
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}
	
	private String extractHtml(HttpResponse response) {
		try {
			String raw = "", line;
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));
			while((line = reader.readLine()) != null) {
				raw += line;
			}
			
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(raw);
			return (String)json.get("data");
		} catch (IllegalStateException | IOException | ParseException e) {
			System.out.println("Failed to extract html.");
			e.printStackTrace();
			return null;
		}
	}
	
	private Element extractContent(Node n, Document doc) {
		String mid = ((TagNode)n).getAttribute("mid");
		if(mid == null) {
			System.out.println("[ERROR] MID tag not found.");
			return doc.createElement("MID_NOT_FOUND");
		}
		
		NodeList text = n.getChildren().extractAllNodesThatMatch(
				new HasAttributeFilter("class", "WB_text"), true);
		NodeList time = n.getChildren().extractAllNodesThatMatch(
				new HasAttributeFilter("class", "S_link2 WB_time"), true);
		if(time.size() == 0 || text.size() == 0) {
			System.out.println("[ERROR] No identifiers found for Weibo No." + mid + ".");
			return doc.createElement("UNRECOGNIZED_" + mid);
		}
		
		Element elem = doc.createElement("MID_" + mid);
		Attr attr = doc.createAttribute("time");
		attr.setNodeValue(time.elementAt(0).getChildren().asString());
		elem.setAttributeNode(attr);
		Element content = doc.createElement("content");
		content.setTextContent(text.elementAt(0).getChildren().asString());
		elem.appendChild(content);
		
		if(text.size() > 1) {
			Element retweet = doc.createElement("retweet");
			Attr from = doc.createAttribute("from");
			from.setNodeValue(text.elementAt(1).getParent().getFirstChild().getNextSibling().getFirstChild().getNextSibling().toPlainTextString());
			retweet.setAttributeNode(from);
			retweet.setTextContent(text.elementAt(1).getChildren().asString());
			elem.appendChild(retweet);
		}
		fetchComment(mid, doc, elem);
		
		System.out.println("[INFO] Weibo No." + mid + " processed.");
		return elem;
	}
	
	private void fetchComment(String mid, Document doc, Element parent) {
		int page = 0;
		while(++page > 0) {
			System.out.println("[INFO] Fetching comment of W" + mid + " page " + page + "...");
			String url = String.format(CommentUrl, mid, page);
			HttpResponse resp = connect(url);
			if(resp == null) return ;
			BufferedReader reader;
			try {
				reader = new BufferedReader(new InputStreamReader(
						resp.getEntity().getContent()));
				String raw = "", line;
				while((line = reader.readLine()) != null) {
					raw += line;
				}
				
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject)parser.parse(raw);
				
				Parser htmlparser = new Parser((String)((JSONObject)json.get("data")).get("html"));
				NodeList list = htmlparser.extractAllNodesThatMatch(new HasAttributeFilter("class", "S_txt2"));
				SimpleNodeIterator iter = list.elements();
				
				while(iter.hasMoreNodes()) {
					Node n = iter.nextNode();
					Node p = n.getPreviousSibling(), s = n;
					while(p != null && !s.toPlainTextString().startsWith("£º")) {
						s = p;
						p = p.getPreviousSibling();
					}
					String comment = "";
					while(s != n) {
						comment += s.toPlainTextString();
						s = s.getNextSibling();
					}
					Node name = n.getParent().getFirstChild().getNextSibling();
					
					Element cmt = doc.createElement("comment");
					cmt.setAttribute("by", name.getChildren().asString());
					cmt.setAttribute("on", n.getChildren().asString());
					cmt.setTextContent(comment.substring(1));
					parent.appendChild(cmt);
				}
				if(list.size() < 20) return ;
			} catch (IllegalStateException | IOException | ParseException | ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}