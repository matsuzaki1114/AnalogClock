


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.time.LocalTime;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

//
public final class MainPanel extends JPanel {
	  private MainPanel() {
	    super(new BorderLayout()); //親であるJPanelクラスのコンストラクタを起動。引数はLayoutManager型。BorderLayoutでパネルを生成
	    							//BorderLayout() コンポーネント間に間隔を設けずに、新しいボーダレイアウトを構築
	    add(new AnalogClock()); //指定したコンポーネントをコンテナの最後に追加するadd。内容はAnalogClockクラスで定義。
	    setPreferredSize(new Dimension(320, 240));//ウィンドウサイズの指定
	  }

	  public static void main(String[] args) {
	   // EventQueue.invokeLater(MainPanel::createAndShowGui);
	    MainPanel.createAndShowGui();
	  }

	  private static void createAndShowGui() {

	    JFrame frame = new JFrame("AnalogClock"); //ウィンドウはデフォルトで不可視
	    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//クローズ時、アプリケーションを終了。（デフォルトでは不可視にするだけ）
	    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	    frame.getContentPane().add(new MainPanel());//frameのContentPaneにコンポーネントを追加。内容はMainPanelクラスで定義。
	    frame.pack();//コンポーネントのサイズと位置を自動調整
	    frame.setLocationRelativeTo(null);//frameの中央を画面中央に。サイズを事前に指定する必要あり。
	    frame.setVisible(true); //ウィンドウを可視に設定
	  }
	}

	class AnalogClock extends JPanel {
	  private LocalTime time = LocalTime.now(); //timeに現在時刻
	  private final Timer timer = new Timer(100,new Action()); //timerを作成。引数1は初期遅延、2はActionEventを受け取るActionListener。100ミリ秒ごとにAction()を実行。start()により開始。stop()で終了
	  private HierarchyListener listener; //階層変更イベントを受け取る（ざっくり言えば、何かしら変化があった際にHierarchyEventが渡され、そいつがもつ様々なフィールドにIDやらフラグやらが格納される。今回使うのはSHOWING_CHANGEDなど）
	  
	class Action implements ActionListener{	//Timerで行う処理の内容。ActionListenrインターフェースの実装という形で記述
		public void actionPerformed(ActionEvent event) {
			time = LocalTime.now();	//現在時刻を更新し
			repaint();	//コンポーネントを再描画 SwingだとpaintComponent(Graphics g)を呼ぶ？はず
		}
	}

	  @Override public void updateUI() {
	    removeHierarchyListener(listener);//処理が始まるので一旦階層変更の監視を解除。
	    super.updateUI();
	    listener = e -> {
	      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
	        if (e.getComponent().isShowing()) {
	          timer.start(); //Timerを起動し、リスナーへのアクションイベントの送信を開始
	        } else {
	          //timer.stop(); //Timerを起動し、リスナーへのアクションイベントの送信を停止
	        }
	      }
	    };
	    addHierarchyListener(listener);//処理が終了。再度階層変更の監視を開始。
	  }

	  @Override protected void paintComponent(Graphics g) {
	    Graphics2D g2 = (Graphics2D) g.create();
	    //描画アルゴリズムの全ての推奨設定の値を指定されたhintsに置き換える。
	    //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    Rectangle rect = SwingUtilities.calculateInnerArea(this, null);	//指定した(This)コンポーネントのペイント領域の位置とサイズを返す
	    g2.setColor(Color.BLACK);
	    //rectの範囲を塗りつぶす。ここでのrectは描画領域全体
	    g2.fill(rect);
	    //時計の目盛り用。時計を円とみて、半径は「描画領域のうち、小さい方」の半分。余白を持たせるために、-10だけ引いておく。
	    double radius = Math.min(rect.width, rect.height) / 2d - 10d;
	    //現在、描画の基準点が左上になっているので、描画領域の中央座標分、x,y方向にtranslateする。＝基準点が中央になる。
	    g2.translate(rect.getCenterX(), rect.getCenterY());

	    // 目盛りの長さを定義。次の行で使用
	    double hourMarkerLen = radius / 6d - 10d;
	    //12時の位置の目盛りを定義。xは右、yは下で値が正なので、12時の目盛りはx:0→0、y:マイナス(半径) と マイナス(半径)+目盛りの長さ の座標を結ぶ線分　と定義。
	    Shape hourMarker = new Line2D.Double(0d, -radius, 0d, -radius + hourMarkerLen); //5の倍数の目盛りを描画
	    Shape minuteMarker = new Line2D.Double(0d, -radius, 0d, -radius+ hourMarkerLen / 2d ); //通常の目盛りは5の倍数の目盛りの半分の長さで描画
	    AffineTransform at = AffineTransform.getRotateInstance(0d); //xy座標変換のための行列をもつAffinTransformに、回転変換のための（変換）行列を渡す。
	    g2.setStroke(new BasicStroke(2f));	//目盛りの太さを指定
	    g2.setColor(Color.LIGHT_GRAY);
	    String[] timeArray = {"3","4","5","6","7","8","9","10","11","12","1","2"};
	    for (int i = 0,j=0; i < 60; i++) {
	      if (i % 5 == 0) {
	        g2.draw(at.createTransformedShape(hourMarker));//rotateした後のhourMarkerのShapeを得るために、atのcreateTransformedShapeを利用する　atには回転変換の行列が入っており、それをこのメソッドで、引数のhourMarkerに与えているイメージ（？）
	        
	        //数字盤の表示
	        int num = (int) (radius / 6); //良い感じの位置にずらす用の数字。キャストで生じるズレの修正
	        int x = (int) ((radius - num) * Math.cos(Math.PI / 30*i));
	        int y = (int) ((radius - num) * Math.sin(Math.PI / 30*i));
	        Font f = new Font("ＭＳ Ｐゴシック",Font.PLAIN,num);
	        g2.setFont(f);
	        g2.drawString(timeArray[j],x-num/5, y+num/6);
	        j++;
	        
	      } else {
	        g2.draw(at.createTransformedShape(minuteMarker));
	      }
	      at.rotate(Math.PI / 30d); //毎秒6度ずつ回転
	    }

	    // 時間ごとの回転角度を表す変数を用意
	    double secondRot = time.getSecond() * Math.PI / 30d; //秒針は1秒で6度
	    double minuteRot = time.getMinute() * Math.PI / 30d + secondRot / 60d; //分針は1分で1分で6度 + 秒針の回転の1/60
	    double hourRot = time.getHour() * Math.PI / 6d + minuteRot / 12d; //時針は1時間で30度 + 分針の回転の1/12

	    // 時針
	    double hourHandLen = radius / 2d;
	    Shape hourHand = new Line2D.Double(0d, 0d, 0d, -hourHandLen);
	    g2.setStroke(new BasicStroke(8f));
	    g2.setPaint(Color.GRAY);
	    g2.draw(AffineTransform.getRotateInstance(hourRot).createTransformedShape(hourHand));

	    //分針
	    double minuteHandLen = 5d * radius / 6d;
	    Shape minuteHand = new Line2D.Double(0d, 0d, 0d, -minuteHandLen);
	    g2.setStroke(new BasicStroke(4f));
	    g2.setPaint(Color.LIGHT_GRAY);
	    g2.draw(AffineTransform.getRotateInstance(minuteRot).createTransformedShape(minuteHand));

	    // 秒針
	    double r = radius / 6d;
	    double secondHandLen = radius - r;
	    Shape secondHand = new Line2D.Double(0d, r, 0d, -secondHandLen);
	    g2.setPaint(Color.RED);
	    g2.setStroke(new BasicStroke(1f));
	    g2.draw(AffineTransform.getRotateInstance(secondRot).createTransformedShape(secondHand));
	    g2.fill(new Ellipse2D.Double(-r / 4d, -r / 4d, r / 2d, r / 2d));

	    g2.dispose();
	  }
	}