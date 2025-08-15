public class test {
    // 这是一个测试类
    // 用于演示自定义注解的使用

    public static void main(String[] args) throws Exception {
        Class<?> clazz = String.class;
        String className = clazz.getName();
        System.out.println("类名: " + className);
        String simpleName = clazz.getSimpleName();
        System.out.println("简单类名: " + simpleName);
        Class<?> clazz1 = Class.forName("alsda");

    }
}