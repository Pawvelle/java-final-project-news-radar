package service;

import crawler.NoticeCrawler;
import model.Article;
import storage.FileStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文章业务类。
 * 负责把“爬取文章、读取历史、判断新增、保存文章”这些步骤组织起来。
 *
 * 可以把这个类理解成“调度员”：
 * - NoticeCrawler 只负责去网页上抓文章；
 * - FileStorage 只负责把数据存到文件、从文件读出来；
 * - ArticleService 负责把这些功能按正确顺序串起来，形成一个完整业务流程。
 */
public class ArticleService {
    // 爬虫对象：负责访问学校官网，并把网页中的通知公告转换成 Article 对象。
    private NoticeCrawler noticeCrawler = new NoticeCrawler();

    // 存储对象：负责读取和保存本地 data 目录下的文章数据。
    private FileStorage fileStorage = new FileStorage();

    /**
     * 爬取官网通知公告，并把新增文章保存到本地历史文件中。
     *
     * 返回值 CrawlResult 里会包含：
     * 1. 本次从官网一共爬到了多少篇文章；
     * 2. 其中有多少篇是以前没有保存过的新文章；
     * 3. 新文章的具体列表，方便 Main 打印给用户看。
     */
    public CrawlResult crawlAndSaveArticles() {
        // 1. 从官网爬取本次通知文章。
        // List<Article> 表示“文章列表”，里面可以按顺序保存很多个 Article 对象。
        List<Article> crawledArticles = noticeCrawler.crawl();

        // 2. 从本地文件读取历史文章。
        // oldArticles 代表之前已经保存过的文章，用来和本次爬取结果做对比。
        List<Article> oldArticles = fileStorage.loadArticles();

        // 3. 把历史文章的 url 放入 Set，方便快速判断是否已经存在。
        // Set 的特点是“不允许重复”，并且 contains 查询速度通常比 List 更快。
        // 这里把 url 当作文章的唯一标识：如果两篇文章 url 一样，就认为它们是同一篇。
        Set<String> oldUrls = new HashSet<>();
        for (Article article : oldArticles) {
            // 有些异常数据可能没有 url，所以先判断不为 null、也不是空字符串。
            // 这样可以避免 NullPointerException，也避免把空链接加入 oldUrls。
            if (article.getUrl() != null && !article.getUrl().isEmpty()) {
                oldUrls.add(article.getUrl());
            }
        }

        // 4. 找出本次新增文章。
        // newArticles 专门保存“本次爬到了，但历史文件里没有”的文章。
        List<Article> newArticles = new ArrayList<>();
        for (Article article : crawledArticles) {
            String url = article.getUrl();

            // 判断条件含义：
            // - url != null：链接不是空对象；
            // - !url.isEmpty()：链接不是空字符串；
            // - !oldUrls.contains(url)：历史文章中还没有这个链接。
            if (url != null && !url.isEmpty() && !oldUrls.contains(url)) {
                newArticles.add(article);

                // 立刻把新文章的 url 加入 oldUrls。
                // 这样如果本次爬取结果里本身出现重复文章，也不会重复加入 newArticles。
                oldUrls.add(url);
            }
        }

        // 5. 合并新增文章和历史文章。新增文章放前面，查看历史时更容易看到最新内容。
        List<Article> allArticles = new ArrayList<>();

        // 先放新增文章，所以文件重新读取出来时，最新内容会排在前面。
        allArticles.addAll(newArticles);

        // 再放旧文章，保留以前已经保存过的历史记录。
        allArticles.addAll(oldArticles);

        // 6. 保存回 data/articles.dat。
        // saveArticles 会覆盖原来的文章文件，所以这里保存的是“新文章 + 旧文章”的完整列表。
        fileStorage.saveArticles(allArticles);

        // 把本次爬取结果包装成一个对象返回给 Main，Main 就可以负责显示结果。
        return new CrawlResult(crawledArticles.size(), newArticles.size(), newArticles);
    }

    /**
     * 保存一次爬取的结果，方便 Main 中显示。
     *
     * static class 表示这是 ArticleService 里面定义的一个“小类”。
     * 它只用来描述 crawlAndSaveArticles() 方法的返回结果，不负责复杂业务。
     */
    public static class CrawlResult {
        // 本次从官网爬到的文章总数，不代表新增数量。
        private int totalCount;

        // 本次新增文章数量，也就是以前本地文件里没有的文章数量。
        private int newCount;

        // 本次新增文章列表，Main 会遍历它并打印标题、日期、链接。
        private List<Article> newArticles;

        /**
         * 构造方法：创建 CrawlResult 对象时，把三个结果值一次性传进来。
         */
        public CrawlResult(int totalCount, int newCount, List<Article> newArticles) {
            this.totalCount = totalCount;
            this.newCount = newCount;
            this.newArticles = newArticles;
        }

        /**
         * 获取本次爬取到的文章总数。
         */
        public int getTotalCount() {
            return totalCount;
        }

        /**
         * 获取本次新增文章数量。
         */
        public int getNewCount() {
            return newCount;
        }

        /**
         * 获取本次新增文章列表。
         */
        public List<Article> getNewArticles() {
            return newArticles;
        }
    }
}
