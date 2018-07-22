package com.logreposit.logrepositapi.rest.filters.clientinfo;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

// Source: https://gist.github.com/c0rp-aubakirov/a4349cbd187b33138969
public class ClientInfoFactory
{
    private ClientInfoFactory()
    {
    }

    public static ClientInfo extract(HttpServletRequest request)
    {
        final String referer         = getReferer(request);
        final String fullUrl         = getFullURL(request);
        final String ipAddress       = getClientIpAddr(request);
        final String operatingSystem = getClientOS(request);
        final String browser         = getClientBrowser(request);
        final String userAgent       = getUserAgent(request);

        ClientInfo clientInfo = ClientInfo.builder()
                                          .referer(referer)
                                          .fullUrl(fullUrl)
                                          .ipAddress(ipAddress)
                                          .operatingSystem(operatingSystem)
                                          .browser(browser)
                                          .userAgent(userAgent)
                                          .build();

        return clientInfo;
    }

    private static String getReferer(HttpServletRequest request)
    {
        final String referer = request.getHeader("referer");

        return referer;
    }

    private static String getFullURL(HttpServletRequest request)
    {
        final StringBuffer requestURL  = request.getRequestURL();
        final String       queryString = request.getQueryString();

        final String result = queryString == null ? requestURL.toString() : requestURL.append('?')
                                                                                      .append(queryString)
                                                                                      .toString();

        return result;
    }

    //http://stackoverflow.com/a/18030465/1845894
    private static String getClientIpAddr(HttpServletRequest request)
    {
        String ip = request.getHeader("X-Forwarded-For");

        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    //http://stackoverflow.com/a/18030465/1845894
    private static String getClientOS(HttpServletRequest request)
    {
        final String browserDetails   = request.getHeader("User-Agent");
        final String lowerCaseBrowser = browserDetails.toLowerCase();

        if (lowerCaseBrowser.contains("windows"))
        {
            return "Windows";
        }
        else if (lowerCaseBrowser.contains("mac"))
        {
            return "Mac";
        }
        else if (lowerCaseBrowser.contains("x11"))
        {
            return "Unix";
        }
        else if (lowerCaseBrowser.contains("android"))
        {
            return "Android";
        }
        else if (lowerCaseBrowser.contains("iphone"))
        {
            return "IPhone";
        }
        else
        {
            return browserDetails;
        }
    }

    //http://stackoverflow.com/a/18030465/1845894
    private static String getClientBrowser(HttpServletRequest request)
    {
        final String browserDetails = request.getHeader("User-Agent");
        final String user           = browserDetails.toLowerCase();

        String browser = "";

        //===============Browser===========================
        if (user.contains("msie"))
        {
            String substring = browserDetails.substring(browserDetails.indexOf("MSIE")).split(";")[0];
            browser = substring.split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
        }
        else if (user.contains("safari") && user.contains("version"))
        {
            browser = (browserDetails.substring(browserDetails.indexOf("Safari")).split(" ")[0]).split(
                    "/")[0] + "-" + (browserDetails.substring(
                    browserDetails.indexOf("Version")).split(" ")[0]).split("/")[1];
        }
        else if (user.contains("opr") || user.contains("opera"))
        {
            if (user.contains("opera"))
                browser = (browserDetails.substring(browserDetails.indexOf("Opera")).split(" ")[0]).split(
                        "/")[0] + "-" + (browserDetails.substring(
                        browserDetails.indexOf("Version")).split(" ")[0]).split("/")[1];
            else if (user.contains("opr"))
                browser = ((browserDetails.substring(browserDetails.indexOf("OPR")).split(" ")[0]).replace(
                        "/",
                        "-"
                )).replace(
                        "OPR", "Opera");
        }
        else if (user.contains("chrome"))
        {
            browser = (browserDetails.substring(browserDetails.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
        }
        else if ((user.contains("mozilla/7.0")) || (user.contains("netscape6")) || (user.contains("mozilla/4.7")) || (user.contains("mozilla/4.78")) || (user.contains("mozilla/4.08")) || (user.contains("mozilla/3")))
        {
            browser = "Netscape-?";
        }
        else if (user.contains("firefox"))
        {
            browser = (browserDetails.substring(browserDetails.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
        }
        else if (user.contains("rv"))
        {
            browser = "IE";
        }
        else
        {
            browser = "UnKnown, More-Info: " + browserDetails;
        }

        return browser;
    }

    private static String getUserAgent(HttpServletRequest request)
    {
        return request.getHeader("User-Agent");
    }
}
