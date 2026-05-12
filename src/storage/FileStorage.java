package storage;

import model.Article;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 负责把程序数据保存到本地文件，也负责从本地文件读取数据。
 *
 * 这个类相当于程序的“本地仓库”：
 * 1. 文章数据保存到 data/articles.dat
 * 2. 订阅关键词保存到 data/subscriptions.txt
 */
public class FileStorage {
    // 数据统一放在项目目录下的 data 文件夹中。
    private static final String DATA_DIR = "data";

    // File.separator 会根据操作系统自动选择路径分隔符，例如 macOS/Linux 是 "/"，Windows 是 "\"。
    private static final String ARTICLES_FILE = DATA_DIR + File.separator + "articles.dat";
    private static final String SUBSCRIPTIONS_FILE = DATA_DIR + File.separator + "subscriptions.txt";

    /**
     * 保存文章列表到 data/articles.dat。
     *
     * 这里使用 ObjectOutputStream，是因为 Article 已经实现了 Serializable，
     * Java 可以直接把 List<Article> 这样的对象写入文件。
     */
    public void saveArticles(List<Article> articles) {
        // 写文件之前先确保 data 文件夹存在，否则 FileOutputStream 可能会因为目录不存在而报错。
        createDataDirIfNeeded();

        // try 后面的小括号里创建的流会在代码执行完后自动关闭，不需要手动 close()。
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(ARTICLES_FILE))) {
            // writeObject 会把整个文章列表一次性写入 articles.dat 文件。
            outputStream.writeObject(articles);
        } catch (Exception e) {
            // 捕获异常，避免程序因为保存失败直接崩溃。
            System.out.println("保存文章失败，请稍后重试。错误信息：" + e.getMessage());
        }
    }

    /**
     * 从 data/articles.dat 读取文章列表。
     * 如果文件不存在，返回空列表。
     */
    // readObject() 返回的是 Object 类型，强制转换成 List<Article> 时编译器会有警告，这里用注解告诉编译器忽略该警告。
    @SuppressWarnings("unchecked")
    public List<Article> loadArticles() {
        File file = new File(ARTICLES_FILE);

        // 第一次运行程序时文件可能还没有创建，这种情况下直接返回空列表。
        if (!file.exists()) {
            return new ArrayList<>();
        }

        // 如果文件存在但内容为空，说明还没有真正保存过文章，也直接返回空列表。
        if (file.length() == 0) {
            System.out.println("文章数据文件为空，可能是第一次运行，将返回空列表。");
            return new ArrayList<>();
        }

        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
            // readObject 读出来的是 Object，所以需要强制转换回 List<Article>。
            return (List<Article>) inputStream.readObject();
        } catch (Exception e) {
            // 如果文件损坏、格式不对或读取失败，就返回空列表，保证后续代码还能继续运行。
            System.out.println("读取文章失败，将返回空列表。错误信息：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 保存订阅关键词到 data/subscriptions.txt，每个关键词一行。
     *
     * 订阅关键词是普通字符串，所以用文本文件保存，打开文件也能直接看懂。
     */
    public void saveSubscriptions(Set<String> keywords) {
        createDataDirIfNeeded();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SUBSCRIPTIONS_FILE))) {
            // Set 中不会有重复元素，所以这里写入的关键词天然是去重后的。
            for (String keyword : keywords) {
                // 写入一个关键词。
                writer.write(keyword);
                // 换行，让下一个关键词写到下一行。
                writer.newLine();
            }
        } catch (Exception e) {
            System.out.println("保存订阅关键词失败，请稍后重试。错误信息：" + e.getMessage());
        }
    }

    /**
     * 从 data/subscriptions.txt 读取订阅关键词。
     * 会去掉空行和关键词前后的空格。
     */
    public Set<String> loadSubscriptions() {
        // 使用 HashSet 保存关键词，可以自动去重。
        Set<String> keywords = new HashSet<>();
        File file = new File(SUBSCRIPTIONS_FILE);

        // 如果还没有保存过订阅关键词，就返回一个空集合。
        if (!file.exists()) {
            return keywords;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            // readLine() 每次读取一行；读到文件末尾时会返回 null，循环就结束。
            while ((line = reader.readLine()) != null) {
                // trim() 会去掉一行开头和结尾的空格，避免把 " Java " 当成包含空格的关键词。
                String keyword = line.trim();

                // 跳过空行，防止把空字符串也当成一个关键词。
                if (!keyword.isEmpty()) {
                    keywords.add(keyword);
                }
            }
        } catch (Exception e) {
            System.out.println("读取订阅关键词失败，将返回空集合。错误信息：" + e.getMessage());
        }

        return keywords;
    }

    /**
     * 如果 data 文件夹不存在，就自动创建。
     */
    private void createDataDirIfNeeded() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            // mkdirs() 可以创建多级目录；这里虽然只有 data 一层，用它也没问题。
            dataDir.mkdirs();
        }
    }
}
