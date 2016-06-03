package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;

//download & parse url, keeping a List of URLS visited from and including this URL
//traverse resulting DOM tree to find first valid link
//check for failure: no links, first link has already been seen (loop), page does not exist (red link)
//check for success: matches philosophy page url
//if not failure or success, go back to first step

public class WikiPhilosophy {
	
	final static WikiFetcher wf = new WikiFetcher();
	private List<String> urlPath = new ArrayList<>(); 
	private static final String PHILOSOPHY_LINK = "https://en.wikipedia.org/wiki/Philosophy";
	private static final String JAVA_LINK = "https://en.wikipedia.org/wiki/Java_(programming_language)";
	private static final String INVALID_LINK = "";
	private static final String BASE_WIKI = "https://en.wikipedia.org";
	private static final String RED_LINK = "redlink=1";
	private static final char OPEN_PARENTHESIS = '(';
	private static final char CLOSE_PARENTHESIS = ')';
	private static final String ANCHOR = "a";
	private static final String HYPER = "href";
	private static final String ITALICS_TAG1 = "i";
	private static final String ITALICS_TAG2 = "em";
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
        WikiPhilosophy wiki = new WikiPhilosophy();
		String url = JAVA_LINK;
		System.out.println(wiki.hasPhilosophyPath(url));
	}

	private boolean hasPhilosophyPath(String url) throws IOException{
		urlPath.add(url);
		Elements paragraphs = wf.fetchWikipedia(url);
		for(Element paragraph: paragraphs){
			Iterable<Node> iter = new WikiNodeIterable(paragraph);
			String link = getFirstValidLink(iter);
			if(!link.equals(INVALID_LINK)){
				if(isPhilosophy(link)){
					urlPath.add(link);
					System.out.println(urlPath);
					return true;
				} else return hasPhilosophyPath(link);
			} else{
				System.out.println(urlPath);
				return false;
			}
        }
		return false;
	}

	private String getFirstValidLink(Iterable<Node> iter){
		int opencount = 0;
		int closecount = 0;

		for (Node node: iter) {
			if (node instanceof TextNode) { //keep track of parentheses 
				for(char c: ((TextNode) node).text().toCharArray()){
					if(c == OPEN_PARENTHESIS) opencount++;
					else if(c == CLOSE_PARENTHESIS) closecount++;
				}
			}
			if (node instanceof Element && node.nodeName().equals(ANCHOR) && node.hasAttr(HYPER)) //get hyperlinks
				if(isValid(node) && closecount>=opencount){ 
					return BASE_WIKI + node.attr(HYPER);
				}
			}
		return INVALID_LINK;
	}

	private boolean isValid(Node node){
		if(isItalicized(node)) return false;
		if(isRedlink(node)) return false;
		if(isLoop(node)) return false;
		return true;
	}

	private boolean isItalicized(Node node){
		for(Element element: ((Element) node).parents()){
			if(element.tagName().equals(ITALICS_TAG1) || element.tagName().equals(ITALICS_TAG2)){
				return true;
			}
		}
		return false;
	}

	private boolean isRedlink(Node node){
		return node.attr(HYPER).substring(node.attr(HYPER).length()-9).equals(RED_LINK);
	}

	private boolean isLoop(Node node){
		return urlPath.contains(BASE_WIKI + node.attr(HYPER));
	}

	private boolean isPhilosophy(String link){
		return link.equals(PHILOSOPHY_LINK);
	}
}
