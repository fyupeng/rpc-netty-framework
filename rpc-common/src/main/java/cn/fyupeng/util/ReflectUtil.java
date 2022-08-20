package cn.fyupeng.util;

import com.fasterxml.jackson.databind.deser.impl.NullsAsEmptyProvider;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.DirectoryIteratorException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description:
 * @Package: cn.fyupeng.util
 * @Version: 1.0
 */
@Slf4j
public class ReflectUtil {

    public static String getStackTrace() {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        return stack[stack.length - 1].getClassName();
    }

    public static Set<Class<?>> getClasses(String packageName) {
        Set<Class<?>> classSet = new LinkedHashSet<>();
        boolean recursive = true;
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                // 获取 下一个元素
                URL url = dirs.nextElement();
                // 得到 协议名称
                String protocol = url.getProtocol();
                // 如果 以 文件 形式保存 在 服务器上
                if ("file".equals(protocol)) {
                    // 获取包的 物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classSet);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 以 / 开头
                            if (name.charAt(0) == '/') {
                                // 获取 后面字符串
                                name = name.substring(1);
                            }
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                if (idx != -1) {
                                    // 获取包名, 并 把 / 改为 .
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                //
                                if (idx != -1 || recursive) {
                                    // 如果 是 .class 文件，而且不是 目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的 .class 获取真正的 类名
                                        String className =
                                                name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            // 添加到 class 集合中
                                            classSet.add(Class.forName(packageName + "." + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classSet;
    }

    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath,
                                                         final boolean recursive, Set<Class<?>> classSet) {
        // 获取 此包的 目录，建立 一个 File
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirFiles = dir.listFiles(new FileFilter(){
            public boolean accept(File file) {
                return (recursive && file.isDirectory()
                || (file.getName().endsWith(".class")));
            }
        });
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classSet);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    classSet.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
