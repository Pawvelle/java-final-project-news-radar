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
                    System.out.println("功能开发中：立即爬取最新资讯");
                    break;
                case "2":
                    System.out.println("功能开发中：关键词搜索");
                    break;
                case "3":
                    System.out.println("功能开发中：管理我的订阅关键词");
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
}
