import model.Article;
import service.ArticleService;
import service.SubscriptionService;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    crawlLatestArticles();
                    break;
                case "2":
                    searchArticles(scanner);
                    break;
                case "3":
                    manageSubscriptions(scanner);
                    break;
                case "4":
                    System.out.println("功能开发中：查看统计报告");
                    break;
                case "5":
                    System.out.println("功能开发中：浏览历史归档");
                    break;
                case "0":
                    System.out.println("已退出。");
                    running = false;
                    break;
                default:
                    System.out.println("输入无效，请重新选择。");
                    break;
            }

            System.out.println();
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("========== 二师资讯雷达 ==========");
        System.out.println("1. 立即爬取最新资讯");
        System.out.println("2. 关键词搜索");
        System.out.println("3. 管理我的订阅关键词");
        System.out.println("4. 查看统计报告");
        System.out.println("5. 浏览历史归档");
        System.out.println("0. 退出");
        System.out.println("================================");
        System.out.print("请选择：");
    }

    private static void crawlLatestArticles() {
        System.out.println("程序爬取官网通知");

        ArticleService articleService = new ArticleService();
        ArticleService.CrawlResult result = articleService.crawlAndSaveArticles();

        System.out.println("本次爬取 " + result.getTotalCount() + " 条");
        System.out.println("新增 " + result.getNewCount() + " 条");

        checkSubscriptionMatches(result.getNewArticles());

        if (result.getNewArticles().isEmpty()) {
            System.out.println("暂无新增文章");
            return;
        }

        System.out.println("新增文章列表：");
        for (Article article : result.getNewArticles()) {
            System.out.println("标题：" + article.getTitle());
            System.out.println("日期：" + article.getPublishDate());
            System.out.println("链接：" + article.getUrl());
            System.out.println();
        }
    }

    private static void searchArticles(Scanner scanner) {
        System.out.print("请输入搜索关键词：");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("关键词不能为空，请重新输入。");
            return;
        }

        ArticleService articleService = new ArticleService();
        ArticleService.SearchResult result = articleService.searchArticlesByKeyword(keyword);

        if (result.getTotalCount() == 0) {
            System.out.println("暂无历史文章，请先选择菜单 1 爬取最新资讯。");
            return;
        }

        if (result.getMatchedArticles().isEmpty()) {
            System.out.println("没有找到包含“" + keyword + "”的文章，请尝试更换关键词。");
            return;
        }

        System.out.println("共找到 " + result.getMatchedArticles().size() + " 篇相关文章：");
        for (Article article : result.getMatchedArticles()) {
            System.out.println("标题：" + article.getTitle());
            System.out.println("日期：" + article.getPublishDate());
            System.out.println("来源：" + article.getSource());
            System.out.println("链接：" + article.getUrl());
            System.out.println();
        }
    }

    private static void checkSubscriptionMatches(List<Article> newArticles) {
        SubscriptionService subscriptionService = new SubscriptionService();
        List<String> keywords = subscriptionService.getAllKeywords();

        System.out.println();
        System.out.println("订阅关键词校对：");

        if (keywords.isEmpty()) {
            System.out.println("当前还没有订阅关键词，可在菜单 3 中添加。");
            return;
        }

        if (newArticles.isEmpty()) {
            System.out.println("本次没有新增文章，所以没有新的订阅命中。");
            return;
        }

        List<Article> matchedArticles = subscriptionService.findMatchedArticles(newArticles);
        if (matchedArticles.isEmpty()) {
            System.out.println("本次新增文章暂未命中你的订阅关键词。");
            return;
        }

        System.out.println("以下新增文章命中你的订阅关键词：");
        for (Article article : matchedArticles) {
            List<String> matchedKeywords = subscriptionService.findMatchedKeywords(article);

            System.out.println("命中关键词：" + String.join("、", matchedKeywords));
            System.out.println("标题：" + article.getTitle());
            System.out.println("日期：" + article.getPublishDate());
            System.out.println("链接：" + article.getUrl());
            System.out.println();
        }
    }

    private static void manageSubscriptions(Scanner scanner) {
        SubscriptionService subscriptionService = new SubscriptionService();
        boolean managing = true;

        while (managing) {
            printSubscriptionMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showSubscriptionKeywords(subscriptionService);
                    break;
                case "2":
                    addSubscriptionKeyword(scanner, subscriptionService);
                    break;
                case "3":
                    deleteSubscriptionKeyword(scanner, subscriptionService);
                    break;
                case "0":
                    managing = false;
                    break;
                default:
                    System.out.println("输入无效，请重新选择。");
                    break;
            }

            if (managing) {
                System.out.println();
            }
        }
    }

    private static void printSubscriptionMenu() {
        System.out.println("========== 管理我的订阅关键词 ==========");
        System.out.println("1. 查看订阅关键词");
        System.out.println("2. 添加订阅关键词");
        System.out.println("3. 删除订阅关键词");
        System.out.println("0. 返回主菜单");
        System.out.println("====================================");
        System.out.print("请选择：");
    }

    private static void showSubscriptionKeywords(SubscriptionService subscriptionService) {
        List<String> keywords = subscriptionService.getAllKeywords();

        if (keywords.isEmpty()) {
            System.out.println("当前还没有订阅关键词。");
            return;
        }

        System.out.println("当前订阅关键词：");
        for (String keyword : keywords) {
            System.out.println("- " + keyword);
        }
    }

    private static void addSubscriptionKeyword(Scanner scanner, SubscriptionService subscriptionService) {
        System.out.print("请输入要添加的订阅关键词：");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("关键词不能为空，添加失败。");
            return;
        }

        if (subscriptionService.addKeyword(keyword)) {
            System.out.println("已添加订阅关键词：“" + keyword + "”。");
        } else {
            System.out.println("订阅关键词“" + keyword + "”已存在，不需要重复添加。");
        }
    }

    private static void deleteSubscriptionKeyword(Scanner scanner, SubscriptionService subscriptionService) {
        System.out.print("请输入要删除的订阅关键词：");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("关键词不能为空，删除失败。");
            return;
        }

        if (subscriptionService.deleteKeyword(keyword)) {
            System.out.println("已删除订阅关键词：“" + keyword + "”。");
        } else {
            System.out.println("没有找到订阅关键词“" + keyword + "”，删除失败。");
        }
    }
}
