package Admin;

import java.awt.Container;
import java.rmi.RemoteException;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import Commun.database.Pair;

import Commun.database;

class updateThread extends Thread
{
textAreaTest aa;
Integer i;
database db;
public updateThread(textAreaTest abc, database db)
   {
            aa = abc;
            this.db = db;
   }

@Override
   public void run()
   {
    while(true){
        try
            {
            HashMap<Integer,Pair<String,HashMap<Integer,Pair<String,Integer>>>> results = db.getResults(0);
            if(results != null){
                String display = "";
                for(Pair<String,HashMap<Integer,Pair<String,Integer>>> el : results.values()){
                    display="---"+display+el.left + "---\n";
                    for (Pair<String,Integer> li : el.right.values()){
                        display+="\t"+li.left+": " + li.right + " votes";
                    }
                }
                    aa.setText(display);
                    sleep(1000);
                }
            }
        catch (InterruptedException e)
            {
                //e.printStackTrace();
            } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        }
   }

}

public class textAreaTest extends javax.swing.JFrame
{
JTextArea area = new JTextArea();
updateThread thread;

public textAreaTest(database db)
    {
        thread = new updateThread(this, db);
        JPanel panel = new JPanel();
        panel.add(area);
        this.setSize(100, 100);
        Container c = this.getContentPane();
        c.add(area);
        this.pack();
        this.setVisible(true);
        thread.start();
    }

public void setText(String text)
    {
        area.setText(text);
    }
public void stop(){
    thread.stop();
}
}