package com.ilummc.mtd;

import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.control.TextArea;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Tooltip;

@SuppressWarnings("restriction")
public class MTDownload extends Application {
	static StringBuffer sb;
	static ProgressBar bar;
	static TextArea log;
	static Label barn, spdn;
	static Tooltip tip;
	static boolean start = false;

	public static void main(String[] args) {
		try {
			if (!new File(System.getProperty("user.home"), "/MultiThreadDownload").exists()) {
				new File(System.getProperty("user.home"), "/MultiThreadDownload").mkdir();
			}
			System.setOut(new PrintStream(new File(System.getProperty("user.home"), "/MultiThreadDownload/log.txt")));
			System.setErr(new PrintStream(new File(System.getProperty("user.home"), "/MultiThreadDownload/log.txt")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		launch(args);
	}

	@Override
	public void start(Stage state) throws Exception {
		sb = new StringBuffer();
		GridPane sp = new GridPane();
		sp.setPadding(new Insets(10, 10, 10, 10));
		sp.setVgap(5);
		sp.setHgap(5);

		// 下载链接
		HBox hb1 = new HBox();
		final TextField tf1 = new TextField();
		tf1.setPromptText("下载链接");
		tf1.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent e) {
				if (e.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
					if (nullOrEpt(tf1.getText())) {
						Pattern pattern = Pattern.compile(
								"https?\\:\\/\\/([a-zA-Z0-9\\-\\~]+\\.)+([a-zA-Z0-9\\-\\~]+\\/)*([a-zA-Z0-9\\-\\~\\?\\=\\&\\_\\.\\#]+)*");
						if (pattern.matcher(getSysClipboardText()).matches()) {
							tf1.setText(getSysClipboardText());
						}
					}
				}
			}

		});
		Pattern pattern = Pattern.compile(
				"https?\\:\\/\\/([a-zA-Z0-9\\-\\~]+\\.)+([a-zA-Z0-9\\-\\~]+\\/)*([a-zA-Z0-9\\-\\~\\?\\=\\&\\_\\.\\#]+)*");
		if (pattern.matcher(getSysClipboardText()).matches()) {
			tf1.setText(getSysClipboardText());
		}
		Label lb1 = new Label("下载链接 ");
		tf1.setPrefWidth(java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3);
		hb1.getChildren().addAll(lb1, tf1);
		GridPane.setConstraints(hb1, 0, 0);
		sp.getChildren().add(hb1);

		// 线程数
		HBox hb2 = new HBox();
		final TextField tf2 = new TextField() {
			@Override
			public void replaceText(int start, int end, String text) {
				if (!text.matches("[a-z\u4e00-\u9fa5\\s].*")) {
					super.replaceText(start, end, text);
				}
			}

			@Override
			public void replaceSelection(String text) {
				if (!text.matches("[a-z\u4e00-\u9fa5\\s].*")) {
					super.replaceSelection(text);
				}
			}
		};
		tf2.setPromptText("线程数量");
		tf2.setText(String.valueOf(Runtime.getRuntime().availableProcessors()));
		Label lb2 = new Label("线程数量 ");
		hb2.getChildren().addAll(lb2, tf2);
		GridPane.setConstraints(hb2, 0, 1);
		sp.getChildren().add(hb2);

		HBox h3 = new HBox();
		Button b = new Button("下载");
		b.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				if (!start) {
					if ((nullOrEpt(tf1.getText()) || nullOrEpt(tf2.getText())))
						return;
					File cache = new File(getBaseDir(), "cache");
					if (!cache.exists() || !cache.isDirectory()) {
						cache.mkdir();
					}
					log("clear");
					start = true;
					AsyncDownload.download(tf1.getText(), getBaseDir(), Integer.parseInt(tf2.getText()),
							new Callback() {

								@Override
								public void fail(HashMap<String, Object> map) {
									Platform.runLater(() -> {
										log.setText("文件 " + ((File) map.get("file")).getAbsolutePath() + "\n下载失败");
									});
									setTooltip("未开始下载");
									setSpeed("0B");
									setProgress(0.0D);
									start = false;
								}

								@Override
								public void success(HashMap<String, Object> map) {
									File file = (File) map.get("file");
									if (file.length() == (long) map.get("length")) {
										Platform.runLater(() -> {
											log.setText(
													"文件 " + ((File) map.get("file")).getAbsolutePath() + "\n下载完成！！");
										});
									}
									setTooltip("未开始下载");
									setSpeed("0B");
									setProgress(0.0D);
									start = false;
								}
							});
				}
			}
		});
		h3.getChildren().add(b);

		Button b2 = new Button("停止");
		b2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				if (start) {
					setProgress(0.0D);
					log("clear");
					setSpeed("0B");
					setTooltip("未开始下载");
					for (List<Thread> list : AsyncDownload.getTasks()) {
						for (Thread t : list) {
							t.interrupt();
						}
					}
					AsyncDownload.getTasks().clear();
					start = false;
				}
			}
		});
		h3.getChildren().add(b2);
		GridPane.setConstraints(h3, 0, 2);
		sp.getChildren().add(h3);

		bar = new ProgressBar();
		bar.setProgress(0.0D);
		bar.setPrefWidth(java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3);
		barn = new Label();
		barn.setText("0.0% ");
		spdn = new Label();
		spdn.setText("0B/s ");
		tip = new Tooltip();
		tip.setText("未开始下载");
		bar.setTooltip(tip);
		HBox hb3 = new HBox();
		hb3.getChildren().addAll(barn, spdn, bar);
		GridPane.setConstraints(hb3, 0, 3);
		sp.getChildren().add(hb3);

		Label lb4 = new Label("日志");
		GridPane.setConstraints(lb4, 0, 4);
		sp.getChildren().add(lb4);

		log = new TextArea();
		log.setPrefWidth(java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2);
		log.setPrefHeight(java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2);
		GridPane.setConstraints(log, 0, 6);
		sp.getChildren().add(log);

		Scene scene = new Scene(sp, java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2,
				java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2);

		state.setTitle("PCD专用多线程下载器 by754503921");
		state.setScene(scene);
		state.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent arg0) {
				for (List<Thread> list : AsyncDownload.getTasks()) {
					for (Thread t : list) {
						t.interrupt();
					}
				}
				Runtime.getRuntime().halt(0);
				System.exit(0);
			}
		});
		state.getIcons().add(new Image(MTDownload.class.getResourceAsStream("/res/PCD.png")));
		SplashScreen.getSplashScreen().close();
		state.show();
	}

	public static void setTooltip(String s) {
		Platform.runLater(() -> {
			tip.setText(s);
		});
	}

	public static void setSpeed(String s) {
		Platform.runLater(() -> {
			spdn.setText(s + "/s ");
		});
	}

	public static void setProgress(double d) {
		Platform.runLater(() -> {
			bar.setProgress(d);
			barn.setText(new DecimalFormat("##0.00").format(d * 100.0D) + "% ");
		});
	}

	public static void log(String s) {
		if (s.equals("clear")) {
			if (sb.length() > 0)
				sb.delete(0, sb.length() - 1);
			Platform.runLater(() -> {
				log.setText("");
			});
		} else {
			sb.insert(0, new SimpleDateFormat("[HH.mm.ss] ").format(new Date()) + s + "\n");
			Platform.runLater(() -> {
				log.setText(sb.toString());
			});
		}
	}

	static boolean nullOrEpt(String s) {
		return s == null || s.length() == 0;
	}

	public static File getBaseDir() {
		try {
			File file = new File(URLDecoder
					.decode(MTDownload.class.getProtectionDomain().getCodeSource().getLocation().toString(), "utf-8")
					.substring(6));
			return file.getParentFile();
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static String getSysClipboardText() {
		String ret = "";
		Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable clipTf = sysClip.getContents(null);
		if (clipTf != null) {
			if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				try {
					ret = (String) clipTf.getTransferData(DataFlavor.stringFlavor);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

}
