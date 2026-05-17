package service;

import model.Article;
import storage.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 订阅关键词业务类。
 *
 * 这个类位于 service 层，负责处理“订阅关键词”相关的业务规则：
 * 1. 查看当前保存的全部订阅关键词；
 * 2. 添加新的订阅关键词，并避免添加空关键词或重复关键词；
 * 3. 删除已有订阅关键词，并在关键词不存在时给出失败结果。
 *
 * 真正的文件读写操作交给 FileStorage 完成，这样 Main 只负责界面交互，
 * SubscriptionService 只负责业务判断，FileStorage 只负责数据存储。
 */
public class SubscriptionService {
    // 存储对象：负责读取和保存 data/subscriptions.txt 中的订阅关键词。
    private FileStorage fileStorage = new FileStorage();

    /**
     * 查看全部订阅关键词。
     *
     * FileStorage 读取出来的是 Set<String>，Set 的特点是不会保存重复元素，
     * 所以它很适合用来存储订阅关键词。
     *
     * 这里返回 List<String>，是为了让 Main 中的菜单展示代码更方便遍历。
     *
     * @return 当前保存的全部订阅关键词；如果还没有订阅关键词，则返回空列表。
     */
    public List<String> getAllKeywords() {
        // 从本地文件读取关键词集合，文件不存在时 FileStorage 会返回空集合。
        Set<String> keywords = fileStorage.loadSubscriptions();

        // 将 Set 转成 List 返回给调用者，避免外部代码直接依赖存储层使用的集合类型。
        return new ArrayList<>(keywords);
    }

    /**
     * 添加一个订阅关键词。
     *
     * 添加前会先清理关键词两端的空格，然后检查：
     * 1. 关键词是否为空；
     * 2. 关键词是否已经存在。
     *
     * 只有通过检查后，才会把关键词加入集合并保存回文件。
     *
     * @param keyword 用户输入的订阅关键词。
     * @return true 表示添加成功；false 表示关键词为空或已经存在。
     */
    public boolean addKeyword(String keyword) {
        // 统一清理输入，避免 null 或首尾空格影响后续判断。
        String cleanKeyword = cleanKeyword(keyword);

        // 清理后为空，说明用户没有输入有效关键词，不能添加。
        if (cleanKeyword.isEmpty()) {
            return false;
        }

        // 读取已有关键词，用 Set 保存可以天然避免重复。
        Set<String> keywords = fileStorage.loadSubscriptions();

        // 如果关键词已经存在，就不重复保存。
        if (keywords.contains(cleanKeyword)) {
            return false;
        }

        // 添加新关键词后，立即保存回本地文件，保证下次启动程序还能读取到。
        keywords.add(cleanKeyword);
        fileStorage.saveSubscriptions(keywords);
        return true;
    }

    /**
     * 删除一个订阅关键词。
     *
     * 删除前同样会清理关键词两端的空格，然后检查：
     * 1. 关键词是否为空；
     * 2. 当前订阅列表中是否存在这个关键词。
     *
     * 只有关键词存在时才会删除并保存。
     *
     * @param keyword 用户输入的订阅关键词。
     * @return true 表示删除成功；false 表示关键词为空或不存在。
     */
    public boolean deleteKeyword(String keyword) {
        // 统一清理输入，让删除时输入 " Java " 也能匹配已保存的 "Java"。
        String cleanKeyword = cleanKeyword(keyword);

        // 空关键词没有删除意义，直接返回失败。
        if (cleanKeyword.isEmpty()) {
            return false;
        }

        // 先读取当前保存的所有订阅关键词。
        Set<String> keywords = fileStorage.loadSubscriptions();

        // 如果集合中没有这个关键词，说明无法删除。
        if (!keywords.contains(cleanKeyword)) {
            return false;
        }

        // 从集合中移除关键词，并把更新后的集合保存回文件。
        keywords.remove(cleanKeyword);
        fileStorage.saveSubscriptions(keywords);
        return true;
    }

    /**
     * 从给定文章列表中找出命中任意订阅关键词的文章。
     *
     * @param articles 待检查的文章列表。
     * @return 命中订阅关键词的文章列表；如果没有订阅关键词或没有命中文章，则返回空列表。
     */
    public List<Article> findMatchedArticles(List<Article> articles) {
        // matchedArticles 用来保存所有命中订阅关键词的文章。
        List<Article> matchedArticles = new ArrayList<>();

        // 如果文章列表为空，说明当前没有文章需要检查，直接返回空结果。
        if (articles == null || articles.isEmpty()) {
            return matchedArticles;
        }

        // 读取当前保存的订阅关键词，这些关键词就是用户长期关注的内容。
        List<String> keywords = getAllKeywords();

        // 如果用户还没有添加订阅关键词，就无法判断哪些文章需要提醒。
        if (keywords.isEmpty()) {
            return matchedArticles;
        }

        // 遍历每一篇文章，只要它命中了任意一个订阅关键词，就加入匹配结果。
        for (Article article : articles) {
            // findMatchedKeywords(...) 会返回这篇文章命中的关键词列表。
            // 列表不为空，说明这篇文章和用户订阅内容有关。
            if (!findMatchedKeywords(article, keywords).isEmpty()) {
                matchedArticles.add(article);
            }
        }

        // 返回所有命中订阅关键词的文章，供 Main 或其他界面层展示。
        return matchedArticles;
    }

    /**
     * 找出某篇文章命中的全部订阅关键词。
     *
     * @param article 待检查的文章。
     * @return 当前文章命中的订阅关键词列表。
     */
    public List<String> findMatchedKeywords(Article article) {
        // 对外提供的简化方法：调用方只需要传入文章，不需要自己读取订阅关键词。
        // 当前保存的订阅关键词由 getAllKeywords() 统一读取。
        return findMatchedKeywords(article, getAllKeywords());
    }

    /**
     * 统一清理关键词，避免保存带空格的内容。
     *
     * 这个方法只在当前类内部使用，所以声明为 private。
     * 它可以保证添加和删除使用同一套清理规则，避免逻辑重复。
     *
     * @param keyword 原始关键词，可能为 null。
     * @return 清理后的关键词；如果传入 null，则返回空字符串。
     */
    private String cleanKeyword(String keyword) {
        // 防止调用 trim() 时因为 keyword 为 null 而抛出 NullPointerException。
        if (keyword == null) {
            return "";
        }

        // 去掉首尾空格，避免把 "通知" 和 " 通知 " 当成两个不同关键词。
        return keyword.trim();
    }

    /**
     * 根据指定关键词列表检查文章命中情况。
     */
    private List<String> findMatchedKeywords(Article article, List<String> keywords) {
        // matchedKeywords 用来保存这篇文章命中的关键词。
        List<String> matchedKeywords = new ArrayList<>();

        if (article == null || keywords == null || keywords.isEmpty()) {
            return matchedKeywords;
        }

        for (String keyword : keywords) {
            if (article.containsKeyword(keyword)) {
                matchedKeywords.add(keyword);
            }
        }

        return matchedKeywords;
    }
}
