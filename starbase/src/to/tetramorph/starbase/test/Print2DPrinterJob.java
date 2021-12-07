/*
 * Print2DPrinterJob.java
 */
package to.tetramorph.starbase.test;
import java.awt.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
/**
 * 印刷メソッドの原理を理解するためのテスト。
 */
class Print2DPrinterJob implements Printable {
    
    public Print2DPrinterJob() {
        
                /* Construct the print request specification.
                 * The print data is a Printable object.
                 * the request additonally specifies a job name, 2 copies, and
                 * landscape orientation of the media.
                 */
        PrintRequestAttributeSet arqset = new HashPrintRequestAttributeSet();
        arqset.add( OrientationRequested.LANDSCAPE );
        arqset.add( new Copies(1) );
        arqset.add( new JobName("My job", null) );
        arqset.add( new MediaPrintableArea( 20f, 20f,170f, 257f,MediaPrintableArea.MM ));
        /* Create a print job */
        PrinterJob pj = PrinterJob.getPrinterJob();
        //使用されるプリンタを返す。プリンタ使用不可の環境ならnullが返る。
        PrintService ps = pj.getPrintService();
        System.out.println( "ps = " + ps.getName() );
        
        pj.setPrintable(this);
        System.out.println("set printable.");
        //デフォルトのプリンタを知る
        PrintService defServ = PrintServiceLookup.lookupDefaultPrintService();
        System.out.println( "defServ = " + defServ.getName());
        /* プリンタ一覧を取得 */
        PrintService[] services =
                PrinterJob.lookupPrintServices();
        for ( int i=0; i<services.length; i++ ) {
            System.out.println( i + " : " + services[i].getName() );
        }
        
        //ServiceUI.printDialog(null,800,200,services,defServ,null,arqset);
        
        
         PrintServiceAttributeSet pset = services[2].getAttributes();
         System.out.println("pset.size = " + pset.size() );
         Attribute [] att = pset.toArray();
         for ( Attribute a : att ) {
             System.out.println("att = " + a.getName() );
         }
        if ( services.length > 0 ) {
            System.out.println("selected printer " + services[2].getName());
            try {
                pj.setPrintService( services[2] );
                attecho(arqset);
                PageFormat format = pj.pageDialog( arqset ); // 用紙選択のダイアログが出現
                attecho(arqset);
                MediaPrintableArea ma = (MediaPrintableArea)arqset.get( MediaPrintableArea.class );
                if ( ma == null ) System.out.println("MediaPrintableArea is null.");
                else {
                    System.out.println("ma = " + ma.toString());
                }
//                if ( pj.printDialog(arqset) ) { //プリンタ選択のダイアログが出現
//                    PrintService ps = pj.getPrintService();
//                    for ( int i=0; i < services.length; i++ ) {
//                        if ( services[i].equals(ps) ) {
//                            System.out.println( i + "番目のプリンターが選択された");
//                        }
//                    }
//                    System.out.println("でも今は印刷しない");
//                    //pj.print(aset); //印刷
//                }
            } catch ( PrinterException pe ) {
                System.err.println( pe );
            }
        }
    }
    void attecho( PrintRequestAttributeSet aset ) {
        Attribute [] att = aset.toArray();
        for ( int i=0; i<att.length; i++ ) {
            System.out.println( i + " : " + att[i].getName() );
        }
        System.out.println("---");
    }
    public int print(Graphics g,PageFormat pf,int pageIndex) {
        
        if (pageIndex == 0) {
            Graphics2D g2d= (Graphics2D)g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());
            g2d.setColor(Color.black);
            g2d.drawString("example string", 250, 250);
            g2d.fillRect(0, 0, 200, 200);
            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }
    static void dump( double [] array) {
        for ( double d : array ) {
            System.out.print(d + ", " );
        }
        System.out.println();        
    }
    public static void main(String arg[]) {
        Print2DPrinterJob sp = new Print2DPrinterJob();
    }
}


