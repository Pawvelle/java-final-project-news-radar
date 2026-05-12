package crawler;

import model.Article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 爬取湖北第二师范学院官网“通知公告”列表。
 */
public class NoticeCrawler {
    // 学校官网“通知公告”栏目地址。
    private static final String NOTICE_URL = "https://www.hue.edu.cn/14857/list.htm";
    // 匹配公告列表中的发布日期，例如：2026-05-12。
    private static final Pattern DATE_PATTERN = Pattern.compile("(20\\d{2}-\\d{2}-\\d{2})");

    /**
     * 爬取通知公告文章列表。
     */
    public List<Article> crawl() {
        // 用于保存本次爬取到的公告文章。
        List<Article> articles = new ArrayList<>();

        try {
            // 使用 Jsoup 请求通知公告页面，超时时间设置为 10 秒。
            Document document = Jsoup.connect(NOTICE_URL)
                    .timeout(10000)
                    .get();

            // 官网公告列表中的记录通常放在 li 标签中。
            Elements items = document.select("li");
            // 记录本次爬取发生的时间，方便后续保存或展示。
            String crawlTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            for (Element item : items) {
                String itemText = item.text();
                Matcher matcher = DATE_PATTERN.matcher(itemText);

                // 通知列表中的每一项一般都包含标题链接和发布日期。
                if (!matcher.find()) {
                    continue;
                }

                // 提取公告标题链接，过滤掉没有链接的列表项。
                Element link = item.selectFirst("a[href]");
                if (link == null) {
                    continue;
                }

                // 优先使用 a 标签的 title 属性作为标题，没有则使用链接文本。
                String title = link.attr("title").trim();
                if (title.isEmpty()) {
                    title = link.text().trim();
                }

                // absUrl 会把相对路径转换成完整链接。
                String url = link.absUrl("href");
                String publishDate = matcher.group(1);

                // 标题和链接都有效时，封装为 Article 对象。
                if (!title.isEmpty() && !url.isEmpty()) {
                    Article article = new Article(title, url, "通知公告", publishDate, "", crawlTime);
                    articles.add(article);
                }
            }
        } catch (Exception e) {
            System.out.println("爬取通知公告失败，请检查网络或稍后重试。错误信息：" + e.getMessage());
        }

        return articles;
    }
}
