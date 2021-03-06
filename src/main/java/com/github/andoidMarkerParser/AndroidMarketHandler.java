/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.andoidMarkerParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles action with Android Market
 *
 * @author blackbass
 */
public class AndroidMarketHandler {

    public static final String MARKET_URL_SEARCH_PAGE = "https://play.google.com/store/search";
    public static final String MARKET_URL_DETAILS_PAGE = "https://play.google.com/store/apps/details?id=";
    private static final String MARKET_EN_LOCALE = "en";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:10.0.2) Gecko/20100101 Firefox/10.0.2";
    private static final String SEARCH_RESULT_SECTION = "div.results-section-set";

    private static final int TIMEOUT = 10000;
    private static final boolean IGNORE_HTTP_ERRORS = true;
    private static final boolean FOLLOW_REDIRECTS = true;

    private static final String DETAILS_PAGE_CONTENT = "div.page-content";
    private static final String SEARCH_NUM_PAGINATION_LIST = "div.num-pagination-page";
    private static final String SEARCH_UL = "ul";
    private static final String SEARCH_LI = "li";


    @Deprecated
    private static final String SEARCH_RESULT_LIST = "ul.search-results-list";
    @Deprecated
    private static final String SEARCH_ITEMS_SELECTOR = "li";
    @Deprecated
    private static final String DETAILS_IMAGE_ELEMENT = "doc-banner-icon img";
    @Deprecated
    private static final String SEARCH_TYPE_PACKAGE_NAME = "pname:";
    @Deprecated
    private static final String SEARCH_TYPE_PUBLISHER_NAME = "pub:";


    /**
     * public static methods for private makeRequest
     *
     * @param request    search request
     * @param searchType search type
     *                   [SEARCH_TYPE_PACKAGE_NAME,SEARCH_TYPE_PUBLISHER_NAME]
     * @return list of AndroidApplication
     */
    public static List<AndroidApplication> marketSearch(String request, String searchType) {
        request = searchType + request;
        Elements applications = makeRequest(request);
        if (applications.size() > 0) {
            return getApplications(applications);
        } else {
            return null;
        }
    }

    /**
     * public static methods for private makeRequest
     *
     * @param request String of request to Android Play
     * @return list of AndroidApplication
     */
    public static List<AndroidApplication> marketSearch(String request) {
        Elements applications;

        if (request.contains("pname:")) {
            applications = makeDetailsRequest(request);
        } else {
            applications = makeRequest(request);
        }
        if (applications == null) {
            return new ArrayList<AndroidApplication>();
        }
        if (applications.size() > 0) {
            return getApplications(applications);
        } else {
            return new ArrayList<AndroidApplication>();
        }
    }

    /**
     * Method returns HTML after market.android/search request
     *
     * @param request String thing to search in Android market
     * @return Elements after request to Android Play
     */
    private static Elements makeRequest(String request) {
        try {
            request = request.replace(" ", "+");
            request = MARKET_URL_SEARCH_PAGE + "?q=" + request + "&c=apps"
                    + getLocaleUrl(MARKET_EN_LOCALE);
            Document doc;
            // try get
            doc = Jsoup
                    .connect(request)
                    .timeout(TIMEOUT)
                    .ignoreHttpErrors(IGNORE_HTTP_ERRORS)
                    .followRedirects(FOLLOW_REDIRECTS)
                    .userAgent(USER_AGENT).get();
            return doc.select(SEARCH_RESULT_SECTION)
                      .select(SEARCH_NUM_PAGINATION_LIST)
                      .select(SEARCH_UL)
                      .select(SEARCH_LI);
        } catch (IOException e) {
            System.out.println("makeRequest exception; " + e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param request string request
     * @return Elements
     */
    private static Elements makeDetailsRequest(String request) {
        try {
            request = request.replace(" ", "%20");
            request = request.replace("pname:", "");
            request = MARKET_URL_DETAILS_PAGE + request + getLocaleUrl(MARKET_EN_LOCALE);
            Document doc = Jsoup
                    .connect(request)
                    .timeout(TIMEOUT)
                    .ignoreHttpErrors(IGNORE_HTTP_ERRORS)
                    .followRedirects(FOLLOW_REDIRECTS)
                    .userAgent(USER_AGENT).get();
            return doc.select(DETAILS_PAGE_CONTENT);
        } catch (IOException e) {
            System.out.println("makeDetailsRequest exception; " + e.toString());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * add iterations application List
     *
     * @param items list
     */
    private static List<AndroidApplication> getApplications(Elements items) {
        List<AndroidApplication> applications = new ArrayList<AndroidApplication>();
        for (Element item : items) {
            applications.add(parseApplication(item));
        }
        return applications;
    }

    /**
     * Parse application for image caption, title
     *
     * @param item Jsoup Element; item of application
     */
    private static AndroidApplication parseApplication(Element item) {

        if (item.attr("class").equalsIgnoreCase("page-content")) {
            //is details

            Element docMetaDataList = item.select("dl.doc-metadata-list").first();

            Element imgElement = item.select("div.doc-banner-icon img").first();
            Element nameElement = item.select(".doc-banner-title").first();
            Element descriptionElement = item.select("div#doc-original-text").first();
            Element categoryElement = item.select("dd a").first();
            Element priceElement = item.select("div.buy-border a span.buy-offer").first();
            Element fileSizeElement = docMetaDataList.select("dd[itemprop=fileSize]").first();
            Element minVersionElement = docMetaDataList.select("dt[itemprop=operatingSystems]").first().nextElementSibling();

            String name = nameElement.html();
            String image = imgElement.attr("src");
            String packageName = priceElement.attr("data-docid");
            Long fileSize = getFileSizeFromAndroidMarketString(fileSizeElement.html());
            String minOSVersion = getMinVersionFromHtmlString(minVersionElement.html());

            String description = "";
            if (descriptionElement != null && descriptionElement.hasText()) {
                description = descriptionElement.html();
            }
            String category = "";
            if (categoryElement != null && categoryElement.hasText()) {
                category = categoryElement.html();
            }
            String currency = priceElement.attr("data-docCurrencyCode");

            Double price = (priceElement.attr("data-isFree").equals("true")) ? 0.0 : new Double(priceElement.attr("data-docPriceMicros")) / 1000000;

            return new AndroidApplication(name, image, packageName, description, category, currency, price, fileSize,
                                          minOSVersion);
        } else {
            Element imgElement = item.select("img").first();
            Element nameElement = item.select("a.title").first();
            Element descriptionElement = item.select(".description").first();
            Element categoryElement = item.select("span.category a").first();
            Element priceElement = item.select("div.buy-border a span.buy-offer").first();

            String name = nameElement.attr("title");
            String image = imgElement.attr("src");
            String packageName = item.attr("data-docid");
            String description = "";
            if (descriptionElement != null && descriptionElement.hasText()) {
                description = descriptionElement.html();
            }
            String category = "";
            if (categoryElement != null && categoryElement.hasText()) {
                category = categoryElement.html();
            }
            String currency = priceElement.attr("data-docCurrencyCode");

            Double price = (priceElement.attr("data-isFree").equals("true")) ? 0.0 : new Double(priceElement.attr("data-docPriceMicros")) / 1000000;

            return new AndroidApplication(name, image, packageName, description, category, currency, price, 0l, null);
        }
    }

    /**
     * @param html example: 2.2 and up
     * @return String of version: 2.2
     */
    private static String getMinVersionFromHtmlString(String html) {
        Pattern pattern = Pattern.compile("(\\d|\\.)+");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return null;
        }
    }

    /**
     * returns filesize from String html of dl.doc-metadata-list dd[itemprop=fileSize]
     *
     * @param html of filesize tag. Example: 25M, 100K
     * @return Long of bytes
     */
    private static Long getFileSizeFromAndroidMarketString(String html) {
        Pattern pattern = Pattern.compile("^([^a-zA-Z]+)(\\D+)$");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            Double size = Double.valueOf(matcher.group(1).replaceAll(",", "."));
            String measure = matcher.group(2);
            return calculateSize(size, measure);
        }
        return 0l;
    }

    /**
     * Calculates bytes from size and measure
     *
     * @param size     example: 100
     * @param measure: example M/B/K/G
     * @return Long of filesize
     */
    private static Long calculateSize(Double size, String measure) {
        Long targetSize = 0l;
        if (measure.equalsIgnoreCase("b")) {
            targetSize = size.longValue();
        } else if (measure.equalsIgnoreCase("k")) {
            targetSize = (long) (size * 1024);
        } else if (measure.equalsIgnoreCase("m")) {
            targetSize = (long) (size * 1048576); // 1024 * 1024;
        } else if (measure.equalsIgnoreCase("g")) {
            targetSize = (long) (size * 1073741824);  //1024 * 1024 * 1024;
        }
        return targetSize;
    }

    /**
     * return locale url param for market request
     *
     * @param locale example: en
     * @return request &param=value string
     */
    private static String getLocaleUrl(String locale) {
        return "&hl=" + locale;
    }
}
