import java.io.IOException;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupPractice {
    private static final String SCHOOL_URL = "https://www.hue.edu.cn/";

    public static void main(String[] args) {
        try {
            Document document = Jsoup.connect(SCHOOL_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .referrer("https://www.baidu.com")
                    .get();

            System.out.println("网页标题：" + document.title());

            String html = document.html();
            System.out.println("\nHTML 前 500 个字符：");
            System.out.println(html.substring(0, Math.min(500, html.length())));

            Elements links = document.select("a[href]");

            System.out.println("\n页面中的前 20 个非空文本链接：");
            int count = 0;
            for (Element link : links) {
                String text = link.text().trim();
                String absoluteUrl = link.absUrl("href");
                String lowerUrl = absoluteUrl.toLowerCase();

                if (text.isEmpty()) {
                    continue;
                }

                if (!text.matches(".*[\\u4e00-\\u9fa5].*")) {
                    continue;
                }

                if (!absoluteUrl.startsWith("https://www.hue.edu.cn/")) {
                    continue;
                }

                if (lowerUrl.contains("english") || lowerUrl.contains("/en/")) {
                    continue;
                }

                System.out.println("链接文本：" + text);
                System.out.println("原始 href：" + link.attr("href"));
                System.out.println("绝对 URL：" + absoluteUrl);
                System.out.println();

                count++;
                if (count >= 20) {
                    break;
                }
            }
        } catch (HttpStatusException e) {
            System.out.println("请求失败：服务器返回 HTTP 状态码 " + e.getStatusCode());
            System.out.println("请检查网址是否正确，或稍后再试。");
        } catch (IOException e) {
            System.out.println("网络连接失败：" + e.getMessage());
            System.out.println("请检查网络连接、网址或网站是否允许访问。");
        }
    }
}
