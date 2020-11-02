package com.congxiaoyao.autoclick;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class AppLauncher {

    public static boolean isWin() {
        String osName = System.getProperty("os.name");
        return  osName.toLowerCase().contains("win");
    }

    private static String[] jarFileNames = new String[]{
            "kotlin-stdlib-jdk7-1.3.72.jar",
            "kotlin-stdlib-common-1.3.72.jar",
            "kotlin-stdlib-jdk8-1.3.72.jar",
            "kotlin-stdlib-1.3.72.jar",
            "annotations-13.0.jar"
    };

    private static File[] localJarFiles = Arrays.stream(jarFileNames)
            .map(fileName -> new File(Cons.INSTANCE.getExternalLibsDir(), fileName))
            .toArray(File[]::new);

    private static String[] remoteFiles = Arrays.stream(jarFileNames)
            .map(fileName -> Cons.URL_REMOTE_LIBS + fileName)
            .toArray(String[]::new);

    private JLabel loadingLabel = null;
    private JProgressBar progressBar = null;
    private JFrame loadingFrame = null;

    public static <T> Stream<T> stream(T[] array) {
        return Arrays.stream(array, 0, array.length);
    }

    public void launch(){
        //从本地加载jar依赖
        boolean loadLocalSuccess = loadLocalJarFiles();
        //成功加载jar依赖
        if (loadLocalSuccess) {
            openMainWindow();
            return;
        }
        //网络获取jar依赖
        showLoadingFrame();
        boolean fetchSuccess = fetchRemoteJarFile();
        if (!fetchSuccess) {
            showErrAndExit();
            return;
        }
        //成功获取依赖，通过本地重新加载
        if (!loadLocalJarFiles()) {
            showErrAndExit();
            return;
        }
        //成功加载
        closeLoadingFrame();
        openMainWindow();
    }

    private void showErrAndExit() {
        JOptionPane.showMessageDialog(
                loadingFrame,
                "应用启动失败！",
                "错误",
                JOptionPane.WARNING_MESSAGE
        );
        System.exit(0);
    }

    private boolean fetchRemoteJarFile() {
        progressBar.setValue(0);
        loadingLabel.setText("准备中...");
        File dir = Cons.INSTANCE.getExternalLibsDir();
        if (!dir.exists()) System.out.println(dir.mkdirs());

        if (!dir.exists()) return false;

        boolean success = false;
        try {
            for (int i = 0; i < remoteFiles.length; i++) {
                String remoteFileUrl = remoteFiles[i];
                String localFilePath = localJarFiles[i].getAbsolutePath();
                URL url = new URL(remoteFileUrl);
                byte[] data = Objects.requireNonNull(responseOf(url));
                OutputStream out = new FileOutputStream(localFilePath);
                out.write(data);
                out.flush();
                loadingLabel.setText("正在加载...(" + (i + 1) + " / " + localJarFiles.length + ")");
                progressBar.setValue(i + 1);
                progressBar.repaint();
                loadingLabel.repaint();
            }
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    private void showLoadingFrame() {
        loadingFrame = new JFrame();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 400;
        int h = 150;
        loadingFrame.setBounds((screenSize.width - w) / 2, (screenSize.height - h) / 2, w, h);
        progressBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, localJarFiles.length);
        loadingLabel = new JLabel();
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.add(progressBar);
        panel.add(loadingLabel);
        progressBar.setPreferredSize(new Dimension(w - 20, 36));
        loadingFrame.add(panel);
        loadingFrame.setVisible(true);
        loadingFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void closeLoadingFrame() {
        loadingFrame.setVisible(false);
    }

    private void openMainWindow() {
        MainKt.main();
    }

    private boolean loadLocalJarFiles() {
        return stream(localJarFiles).allMatch(AppLauncher::loadJar);
    }

    public static boolean loadJar(File jarFile) {
        boolean success = false;
        Method method = null;
        try {
            if (!jarFile.exists()) return false;
            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            JOptionPane.showMessageDialog(
                    new JFrame(),
                    "SecurityException|NoSuchMethodException",
                    "错误",
                    JOptionPane.WARNING_MESSAGE
            );
            e1.printStackTrace();
        }
        if (method == null) return success;
        try {
            method.setAccessible(true);
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            URL url = jarFile.toURI().toURL();
            method.invoke(classLoader, url);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    new JFrame(),
                    e.getMessage(),
                    "错误",
                    JOptionPane.WARNING_MESSAGE
            );
        }
        return success;
    }

    public static byte[] responseOf(URL url) {
        try {
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            readStreamTo(stream, out);
            stream.close();
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void readStreamTo(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024 * 8];
        int bytes = in.read(buffer);
        while (bytes >= 0) {
            out.write(buffer, 0, bytes);
            bytes = in.read(buffer);
        }
    }

    public static void main(String[] args) {
        new AppLauncher().launch();
    }
}
