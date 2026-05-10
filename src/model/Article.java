package model;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * 文章实体类，用来保存一条资讯或通知的基本信息。
 * 实现 Serializable 后，Article 对象可以被写入文件或从文件中读取。
 */
public class Article implements Serializable {
    // 序列化版本号，用于判断保存的数据和当前类结构是否兼容。
    private static final long serialVersionUID = 1L;

    // 文章标题
    private String title;
    // 文章链接，当前类中用它来判断两篇文章是否为同一篇。
    private String url;
    // 文章来源，例如新闻、通知或具体栏目名称。
    private String source;
    // 文章发布日期
    private String publishDate;
    // 文章摘要
    private String summary;
    // 爬虫抓取到这篇文章的时间
    private String crawlTime;

    /**
     * 无参构造方法。
     * 可以先创建一个空的 Article 对象，再通过 setter 方法逐个赋值。
     */
    public Article() {
    }

    /**
     * 带参构造方法。
     * 创建 Article 对象时，直接传入所有文章信息。
     */
    public Article(String title, String url, String source, String publishDate, String summary, String crawlTime) {
        this.title = title;
        this.url = url;
        this.source = source;
        this.publishDate = publishDate;
        this.summary = summary;
        this.crawlTime = crawlTime;
    }

    // 以下 getter 和 setter 方法用于读取、修改文章的各个字段。
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCrawlTime() {
        return crawlTime;
    }

    public void setCrawlTime(String crawlTime) {
        this.crawlTime = crawlTime;
    }

    /**
     * 判断文章是否包含某个关键词。
     * 会在标题、摘要和来源中查找，并且忽略大小写。
     */
    public boolean containsKeyword(String keyword) {
        // 关键词为空、为 null，或者只包含空格时，不进行匹配。
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }

        // 去掉关键词前后的空格，并统一转成小写，方便后面忽略大小写比较。
        String lowerKeyword = keyword.trim().toLowerCase(Locale.ROOT);

        // 只要标题、摘要或来源中任意一个包含关键词，就认为这篇文章匹配成功。
        return containsIgnoreCase(title, lowerKeyword)
                || containsIgnoreCase(summary, lowerKeyword)
                || containsIgnoreCase(source, lowerKeyword);
    }

    /**
     * 判断某段文本中是否包含关键词。
     * 这里要求 lowerKeyword 已经提前转换成小写。
     */
    private boolean containsIgnoreCase(String text, String lowerKeyword) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }

    /**
     * 判断两篇文章是否相同。
     * 这里认为链接 url 相同，就代表是同一篇文章。
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Article)) {
            return false;
        }

        Article other = (Article) obj;
        return Objects.equals(url, other.url);
    }

    /**
     * 根据 url 生成哈希值。
     * 需要和 equals 方法保持一致，方便在 Set、Map 等集合中去重。
     */
    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    /**
     * 将文章对象转换成适合打印显示的文本。
     */
    @Override
    public String toString() {
        return "标题：" + title + "\n"
                + "链接：" + url + "\n"
                + "来源：" + source + "\n"
                + "发布日期：" + publishDate + "\n"
                + "摘要：" + summary + "\n"
                + "抓取时间：" + crawlTime;
    }
}
